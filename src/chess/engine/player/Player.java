package chess.engine.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import chess.engine.Side;
import chess.engine.ZobristHash;
import chess.engine.board.Board;
import chess.engine.board.BoardUtils;
import chess.engine.board.Move;
import chess.engine.board.Move.CaptureMove;
import chess.engine.pieces.King;
import chess.engine.pieces.Piece;
import chess.engine.pieces.Piece.PieceType;
import chess.engine.player.ai.StandardBoardEvaluator;
import chess.gui.Table;

public abstract class Player {

	protected final Board board;
	protected final King playerKing;
	protected final Collection<Move> legalMoves; // derived from psuedo-legal move generation
	
	
	private final boolean inCheck;
	private final boolean canKingsideCastle;
	private final boolean canQueensideCastle;
	private final int mobility;
	private final int pieces;

	private static final StandardBoardEvaluator EVALUATOR = new StandardBoardEvaluator();
	
	Player(final Board board, final Collection<Move> legalMoves, final Collection<Move> opponentMoves) {
		this.board = board;
		this.playerKing = findKing();
		this.inCheck = !this.attacksOnTile(this.playerKing.getPosition(), opponentMoves).isEmpty();
		Collection<Move> castles = calculateCastles(legalMoves, opponentMoves);
		this.legalMoves = ImmutableList.copyOf(Iterables.concat(legalMoves, castles));
		
		this.canKingsideCastle = castles.size() >= 1;
		this.canQueensideCastle = castles.size() == 2;
		this.mobility = this.legalMoves.size();
		this.pieces = this.getActivePieces().size();
		// calculateAttacksOnTile takes in board64 position
	}

	/*
	 * This returns a list of all attacking opponent moves on a given tile. This
	 * is static because it is used in instantiation of boolean inCheck.
	 * 
	 * @param piecePosition: the piece for which we find all threats
	 * 
	 * @param opponentMoves: list of opponent's moves
	 */

	public Collection<Move> attacksOnTile(int piecePosition, Collection<Move> attackingMoveSet) {
		// piecePosition passed in as 0-63 is converted to 21 - 98 psuedo-coordinate
		// because move stores the pseudo-coordinate as well
		final List<Move> attacks = new ArrayList<>();
		for (final Move move : attackingMoveSet) {
			if (BoardUtils.board64[piecePosition] == move.getDestination()) {
				// check to make sure this isn't a quiet pawn move
				attacks.add(move);
			}
		}
		return ImmutableList.copyOf(attacks);
	}

	private King findKing() {
		boolean firstKing = true;
		King candidate = null;
		for (final Piece piece : getActivePieces()) {
			if (piece.getType() == PieceType.KING) {
				if (firstKing) {
					candidate = (King) piece;
					firstKing = false;
				} else {
					throw new RuntimeException("Player must have exactly one king!");
				}
			}
		}

		if (candidate != null) {
			return candidate;
		}
		throw new RuntimeException("Player must have a king!");
	}

	public King getKing() {
		return this.playerKing;
	}

	/*
	 * Accessors for move
	 */

	public Collection<Move> getLegalMoves() {
		return this.legalMoves;
	}
	
	public int mobility() {
		return this.mobility;
	}
	
	public int numPieces() {
		return this.pieces;
	}

	public boolean isLegal(final Move move) {
		return this.legalMoves.contains(move);
	}

	public boolean inCheck() {
		return this.inCheck;
	}

	/*
	 * Returns whether or not player is in checkmate. This is not done in the
	 * constructor, because to check whether or not a player has escape moves,
	 * board states are made in the MoveTransition tester class, which in turn
	 * initialize the player class, creating infinite recursive calls.
	 */

	public boolean inCheckmate() {
		return this.inCheck && !hasEscapeMoves();
	}

	public boolean hasEscapeMoves() {
		for (final Move move : this.legalMoves) {
			final MoveTransition transition = makeMove(move);
			if (transition.getMoveStatus() == MoveStatus.DONE) { 
				return true;
			}
		}
		return false;
	}

	public boolean inStalemate() {
		return !this.inCheck && !hasEscapeMoves();
	}

	public boolean isCastled() {
		return this.playerKing.isCastled();
	}

	public boolean canKingsideCastle() {
		return this.canKingsideCastle;
	}

	public boolean canQueensideCastle() {
		return this.canQueensideCastle;
	}

	/*
	 * This method creates a MoveTransition object to test a move.
	 */

	public MoveTransition makeMove(final Move move) {
		if (!isLegal(move)) {
			return new MoveTransition(this.board, move, MoveStatus.ILLEGAL_MOVE);
		}
		if (move.getDestination() /* 21 - 98 */ == BoardUtils.board64[this.getOpponent().getKing().getPosition()]) {
			return new MoveTransition(this.board, move, MoveStatus.KING_CAPTURE);
		}
		// if move is legal
		final Board transition = move.execute();

		// calculates attacks on king tile. Note that
		// currentPlayer().getOpponent() is used because execute()
		// switches the currentPlayer to the other Side.
		// In other words, will we be in check after move.execute()?
		final Collection<Move> kingAttacks = this.attacksOnTile(
				transition.currentPlayer().getOpponent().getKing().getPosition(),
				transition.currentPlayer().getLegalMoves());
		if (!kingAttacks.isEmpty()) { // if king would be under attack
			return new MoveTransition(this.board, move, MoveStatus.LEAVES_PLAYER_IN_CHECK);
		}
		//calculate hash
		long newHash = transition.getHash(); //hash is updated in move.execute();
		int score = EVALUATOR.evaluate(transition, 1); //maybe don't call SEE everytime a node is reached?
		//populate table
		if (!Table.SEEN_POSITIONS.containsKey(newHash)) {
			Table.SEEN_POSITIONS.put(newHash, score); 
		} else { //table.containsKey(newHash)
			if (score != Table.SEEN_POSITIONS.get(newHash)) {
				Table.SEEN_POSITIONS.put(newHash, score);
				Table.incrementCollisions();
			}
		}
		return new MoveTransition(transition, move, MoveStatus.DONE);
	}

	public abstract Collection<Piece> getActivePieces();

	public abstract Side getSide();

	public abstract Player getOpponent();

	/*
	 * Player generates its own castle-moves.
	 */
	protected abstract Collection<Move> calculateCastles(Collection<Move> legalMoves, Collection<Move> opponentMoves);

}
