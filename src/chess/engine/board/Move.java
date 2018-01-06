package chess.engine.board;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import chess.engine.Side;
import chess.engine.ZobristHash;
import chess.engine.board.Board.Builder;
import chess.engine.pieces.King;
import chess.engine.pieces.Pawn;
import chess.engine.pieces.Piece;
import chess.engine.pieces.Piece.PieceType;
import chess.engine.pieces.Rook;
import chess.engine.player.BlackPlayer;
import chess.engine.player.MoveTransition;
import chess.engine.player.Player;
import chess.engine.player.WhitePlayer;
import chess.gui.Table;

/*
 * This class Move stores the information of a move on the chessboard, by keeping track of the current board state,
 * the piece(s) to be (re)moved, and where those pieces go.
 */

// Possible future TODO: pre-generate all 4972 (8 x 8 x 73) moves 
// 8 x 8 squares; 73 -> 56 "queen moves" ( = up to 7 squares moved * 8 directions) + 8 knight moves + 9 pawn moves 
// ( = 3 [straight + 2 captures] * 3 underpromotions (N, B, R)

public abstract class Move {
	protected final Board board; // incoming/current board
	protected final Piece movedPiece;
	protected final int destination; // 21-98 mailbox coordinates
	protected boolean isCheck;

	public static final Move NULL_MOVE = new NullMove(); // constant — returned
	// as a default move
	// below in
	// MoveFactory

	/*
	 * CONSTRUCTOR: This instantiates the move class.
	 * 
	 * @param board: current board state
	 * 
	 * @param movedPiece: piece to move
	 * 
	 * @param destination: location to move piece. Due to the structure of legal
	 * move calculation, "destination" is a coordinate in board64[], meaning
	 * only values within 21 - 98 are valid.
	 */

	Move(final Board board, final Piece movedPiece, final int destination) {
		this.board = board;
		this.movedPiece = movedPiece;
		this.destination = destination;
		this.isCheck = false;
	}

	/*
	 * Copy constructor
	 */

	Move(final Move move) {
		this.board = move.board;
		this.movedPiece = move.movedPiece;
		this.destination = move.destination;
		this.isCheck = move.isCheck;
	}

	/*
	 * Hash code and equals implementation.
	 */

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.destination;
		result = prime * result + this.movedPiece.hashCode();
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Move))
			return false;
		final Move other = (Move) obj;
		return this.getStart() == other.getStart() && this.getDestination() == other.getDestination()
				&& this.getMovedPiece().equals(other.getMovedPiece());
	}

	/*
	 * This takes in a 21 - 98 offset-calculation pseudo-coordinate.
	 * 
	 * @param coordinate: square to be converted into algebraic notation
	 */

	public String toAlgebraic(int coordinate) {
		// example: passing in 21 returns "a8"
		return BoardUtils.algebraic[BoardUtils.mailbox[coordinate]];

	}

	public String getFile(int coordinate) {
		// example: passing in 21 returns "a"
		return toAlgebraic(coordinate).substring(0, 1);
	}

	public String getRank(int coordinate) {
		// example: passing in 21 returns "8"
		return toAlgebraic(coordinate).substring(1);
	}

	/*
	 * Coordinate and piece accessors. Returns a 21-98 psuedo-coordinate.
	 */

	public int getStart() {
		return BoardUtils.board64[this.getMovedPiece().getPosition()];
		// conversion required, since getPosition is tied to Tile class, which
		// takes uses values 0-63.
	}

	public int getDestination() {
		return this.destination;
	}

	public Piece getMovedPiece() {
		return this.movedPiece;
	}

	/*
	 * Base class move-type booleans and accessors.
	 */

	public boolean isAttack() {
		return false;
	}

//	public void isCheck() {
//		isCheck = true;
//	}
//
//	public boolean isCheckmate() {
//		return isCheck; //TODO
//	}

	public boolean isCastlingMove() {
		return false;
	}

	public Piece getAttackedPiece() {
		return null;
	}

//	public String checkHash() {
//		if (!isCheck) {
//			return "";
//		} else {
//			if (isCheckmate()) { //TODO
//				return "#";
//			}
//			return "+";
//		}
//
//	}

	/*
	 * This method returns a new Board with a move (this) made. This does not
	 * edit the pre-existing this.board field. We will polymorphically implement
	 * this in the concrete subclasses of move.
	 */

	public Board execute() {
		final Builder builder = new Builder();

		// put all pieces on new board
		for (final Piece piece : board.getWhitePieces()) {
			if (!this.movedPiece.equals(piece)) {
				builder.setPiece(piece);
			}
		}
		for (final Piece piece : board.getBlackPieces()) {
			if (!this.movedPiece.equals(piece)) {
				builder.setPiece(piece);
			}
		}

		// put the moved piece in the correct position. This works for both
		// captures and quiet moves, since for captures,
		// the captured piece is here overwritten.
		builder.setPiece(this.movedPiece.pieceFromMove(this));
		builder.setHashValue(ZobristHash.updateZobristHash(board.getHash(), this));
		builder.setSide(this.board.currentPlayer().getOpponent().getSide());
		return builder.build();
	}

	public Board unmake() {
		return board;
	}

	/*
	 * Concrete subclass of Move. A QuietMove is any move that does not have a
	 * capture.
	 */

	public static class QuietMove extends Move {
		public QuietMove(final Board board, final Piece movedPiece, final int destination) {
			super(board, movedPiece, destination);
		}

		/*
		 * Returns a string representing the move in standard algebraic
		 * notation.
		 */

		@Override
		public String toString() {
			return this.movedPiece.toString() + toAlgebraic(this.destination);
			// doesn't add single-character piece qualifier for pawns
		}

		/*
		 * Returns a string representing the move in the format "P## to ##".
		 */

		public String toReferenceString() {
			return this.movedPiece.toString() + BoardUtils.board64[movedPiece.getPosition()] + " to "
					+ this.destination;
		}

	}

	/*
	 * Concrete subclass of Move. A CaptureMove is any move that captures
	 * another piece. Thus, we add the field Piece captured to the subclass.
	 */

	public static class CaptureMove extends Move {
		final Piece captured;

		public CaptureMove(final Board board, final Piece movedPiece, final int destination, final Piece captured) {
			super(board, movedPiece, destination);
			this.captured = captured;
		}

		/*
		 * Hashcode and equals implementation.
		 */

		@Override
		public int hashCode() {
			return this.getAttackedPiece().hashCode() + super.hashCode();
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof CaptureMove))
				return false;
			final CaptureMove other = (CaptureMove) obj;
			return super.equals(other) && getAttackedPiece().equals(other.getAttackedPiece());
		}

		/*
		 * Returns a string in format "P## to ## captures C##"
		 */

		public String toReferenceString() {
			return this.movedPiece.toString() + BoardUtils.board64[movedPiece.getPosition()] + " to " + this.destination
					+ " captures " + this.captured.toString() + BoardUtils.board64[this.captured.getPosition()];
		}

		/*
		 * Returns a string in standard algebraic notation.
		 */

		@Override
		public String toString() {
			return this.movedPiece.toString() + "x" + toAlgebraic(this.destination);
		}

		/*
		 * Overridden method - all captures are attacking moves.
		 */

		@Override
		public boolean isAttack() {
			return true;
		}

		/*
		 * Accessor for attacked piece.
		 */

		@Override
		public Piece getAttackedPiece() {
			return captured;
		}
	}

	/*
	 * Because pawns are very weird pieces in chess, being neither leaper or
	 * rider pieces, there are multiple subclasses dealing with pawn moves in
	 * particular. They do not capture in the same direction they move, for one,
	 * and they can perform an en passant and be promoted. These subclasses
	 * handle special pawn moves.
	 */

	/*
	 * This concrete subclass represents a quiet pawn move.
	 */
	public static class PawnMove extends QuietMove {
		public PawnMove(final Board board, final Piece movedPiece, final int destination) {
			super(board, movedPiece, destination);
		}

		@Override
		public boolean equals(final Object obj) {
			return this == obj || obj instanceof PawnMove && super.equals(obj);
		}

		/*
		 * Overridden – quiet pawn moves are represented only by their
		 * destination.
		 */

		@Override
		public String toString() {
			return toAlgebraic(this.destination);
		}
	}

	/*
	 * This concrete subclass represents a pawn capture.
	 */

	public static class PawnCapture extends CaptureMove {
		public PawnCapture(final Board board, final Piece movedPiece, final int destination, final Piece captured) {
			super(board, movedPiece, destination, captured);
		}

		@Override
		public boolean equals(final Object obj) {
			return this == obj || obj instanceof PawnCapture && super.equals(obj);
		}

		/*
		 * Overridden - pawn captures in algebraic notation are of the form
		 * FILExSQUARE.
		 */

		@Override
		public String toString() {
			return getFile(BoardUtils.board64[this.movedPiece.getPosition()]) + "x" + toAlgebraic(this.destination);
		}
	}

	/*
	 * This concrete subclass extends PawnCapture, representing an EnPassant.
	 */

	public static final class EnPassant extends PawnCapture {
		public EnPassant(final Board board, final Piece movedPiece, final int destination, final Piece captured) {
			super(board, movedPiece, destination, captured);
		}

		@Override
		public boolean equals(final Object obj) {
			return this == obj || obj instanceof EnPassant && super.equals(obj);
		}

		@Override
		public Board execute() {
			final Builder builder = new Builder();
			for (final Piece piece : this.board.currentPlayer().getActivePieces()) {
				if (!this.movedPiece.equals(piece)) {
					builder.setPiece(piece);
				}
			}
			for (final Piece piece : this.board.currentPlayer().getOpponent().getActivePieces()) {
				if (!piece.equals(captured)) {
					builder.setPiece(piece);
				}
			}

			builder.setPiece(this.movedPiece.pieceFromMove(this));
			builder.setHashValue(ZobristHash.updateZobristHash(this.board.getHash(), this));
			builder.setSide(this.board.currentPlayer().getOpponent().getSide());
			return builder.build();
		}

		@Override
		public String toString() {
			return super.toString() + "e.p.";
		}
	}

	public static class PawnPromotion extends Move {

		final Move decoratedMove;
		final Pawn promoted;
		final Piece promoteTo;

		public PawnPromotion(final Move decoratedMove, final Piece promoteTo) {
			super(decoratedMove);
			this.decoratedMove = decoratedMove;
			this.promoted = (Pawn) decoratedMove.getMovedPiece();
			this.promoteTo = promoteTo;
		}

		@Override

		public int hashCode() {
			int result = decoratedMove.hashCode() + (31 * promoted.hashCode());
			return 31 * promoteTo.hashCode();
		}

		@Override
		public boolean equals(final Object obj) {
			return this == obj || obj instanceof PawnPromotion && (super.equals(obj));
		}

		/*
		 * This executes the decorated move, and then implements the special
		 * behavior for a promotion.
		 */

		@Override
		public Board execute() {

			final Board board = this.decoratedMove.execute();
			final Board.Builder builder = new Builder();
			for (final Piece piece : board.currentPlayer().getActivePieces()) {
				if (!this.promoted.equals(piece)) {
					builder.setPiece(piece);
				}
			}
			for (final Piece piece : board.currentPlayer().getOpponent().getActivePieces()) {
				builder.setPiece(piece);
			}
			builder.setPiece(this.promoted.getPromotionPiece().pieceFromMove(this));
			builder.setHashValue(ZobristHash.updateZobristHash(this.board.getHash(), this));
			builder.setSide(board.currentPlayer().getSide());
			return builder.build();
		}

		@Override
		public boolean isAttack() {
			return this.decoratedMove.isAttack();
		}

		@Override
		public Piece getAttackedPiece() {
			return this.decoratedMove.getAttackedPiece();
		}

		public Piece getPromotionPiece() {
			return this.promoteTo;
		}

		@Override
		public String toString() {
			return this.decoratedMove.toString() + "=" + promoteTo.getType().toString();
		}

	}

	/*
	 * This concrete subclass PawnJump represents the special first move of the
	 * pawn, in which it can go two squares at once.
	 */

	public static final class PawnJump extends PawnMove {
		public PawnJump(final Board board, final Piece movedPiece, final int destination) {
			super(board, movedPiece, destination);
		}

		/*
		 * The execute method for a pawn jump is overridden, since we now have
		 * to set an en passant pawn. This just means that when a pawn "jumps"
		 * it is vulnerable to an en passant capture on the next turn.
		 */

		@Override
		public Board execute() {
			final Builder builder = new Builder();

			// set all unmoved pieces
			for (final Piece piece : this.board.currentPlayer().getActivePieces()) {
				if (!this.movedPiece.equals(piece)) {
					builder.setPiece(piece);
				}
			}
			for (final Piece piece : this.board.currentPlayer().getOpponent().getActivePieces()) {
				builder.setPiece(piece);
			}

			// infer pawn object from this move
			final Pawn moved = (Pawn) this.movedPiece.pieceFromMove(this);
			builder.setEnPassantPawn(moved);
			builder.setPiece(moved);
			builder.setHashValue(ZobristHash.updateZobristHash(this.board.getHash(), this));
			builder.setSide(this.board.currentPlayer().getOpponent().getSide());
			return builder.build();
		}
	}

	/*
	 * Castling is also a special move. This abstract class CastleMove will have
	 * two concrete subclasses for O-O (kingside castle) and O-O-O (queenside
	 * castle).
	 */

	public static abstract class CastleMove extends Move {
		protected final Rook castleRook;
		protected final int castleRookStart;
		protected final int castleRookDestination;

		/*
		 * CONSTRUCTOR: Same as the Move class, but we also need to tell the
		 * program about the rook to be moved — essentially store implicitly a
		 * second move within this class.
		 */

		public CastleMove(final Board board, final King movedPiece, final int destination, final Rook castleRook,
				final int castleRookStart, final int castleRookDestination) {
			super(board, movedPiece, destination);
			this.castleRook = castleRook;
			this.castleRookStart = castleRookStart;
			this.castleRookDestination = castleRookDestination;
		}

		/*
		 * Accessor for rook to be moved.
		 */

		public Rook getCastleRook() {
			return this.castleRook;
		}

		/*
		 * Overrides — these moves are castling moves.
		 */

		@Override
		public boolean isCastlingMove() {
			return true;
		}

		/*
		 * Overrides execute: we need to set the locations of two pieces.
		 */

		@Override
		public Board execute() {
			final Builder builder = new Builder();
			for (final Piece piece : this.board.currentPlayer().getActivePieces()) {
				if (!this.movedPiece.equals(piece) && !this.castleRook.equals(piece)) {
					builder.setPiece(piece);
				}
			}
			for (final Piece piece : this.board.currentPlayer().getOpponent().getActivePieces()) {
				builder.setPiece(piece);
			}
			builder.setPiece(this.movedPiece.pieceFromMove(this));
			// recall builder takes in a 0 - 64 coordinate, so we must convert
			// it here
			builder.setPiece(new Rook(BoardUtils.mailbox[this.castleRookDestination], this.castleRook.getSide(), true));
			builder.setSide(this.board.currentPlayer().getOpponent().getSide());
			return builder.build();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + this.castleRook.hashCode();
			result = prime * result + this.castleRookDestination;
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof CastleMove))
				return false;
			final CastleMove other = (CastleMove) obj;
			return super.equals(other) && this.castleRook.equals(other.getCastleRook());
		}
	}

	/*
	 * This concrete subclass inherits all of CastleMove, and mostly exists
	 * because the algebraic notation of this move is irregular.
	 */

	public static final class KingsideCastleMove extends CastleMove {
		public KingsideCastleMove(final Board board, final King movedPiece, final int destination,
				final Rook castleRook, final int castleRookStart, final int castleRookDestination) {
			super(board, movedPiece, destination, castleRook, castleRookStart, castleRookDestination);
		}

		@Override
		public String toString() {
			return "O-O";
		}
	}

	/*
	 * This concrete subclass inherits all of CastleMove, and mostly exists
	 * because the algebraic notation of this move is irregular.
	 */

	public static final class QueensideCastleMove extends CastleMove {
		public QueensideCastleMove(final Board board, final King movedPiece, final int destination,
				final Rook castleRook, final int castleRookStart, final int castleRookDestination) {
			super(board, movedPiece, destination, castleRook, castleRookStart, castleRookDestination);
		}

		@Override
		public String toString() {
			return "O-O-O";
		}
	}

	/*
	 * This concrete subclass is the default empty move, and stores no board, no
	 * piece, and a destination of -1.
	 */

	public static final class NullMove extends Move {
		public NullMove() {
			super(null, null, -1);
		}

		/*
		 * Used only for the null-move heuristic — this is only called in the AI thinking algorithm.
		 */

		@Override
		public Board execute() {
			throw new RuntimeException("Can't execute null move");
			//return new Board(this.board, this.board.currentPlayer().getOpponent().getSide());
		}

		/*
		 * Field accessors
		 */

		@Override
		public int getStart() {
			return -1;
		}

		@Override
		public int getDestination() {
			return -1;
		}

		/*
		 * Hashcode implementation.
		 */

		@Override
		public int hashCode() {
			return 1;
		}
		
		@Override
		public String toString() {
			return "NULL_MOVE";
		}
	}

	/*
	 * Creates a move from a list of moves.
	 */

	public static final class MoveFactory {
		private MoveFactory() {
			throw new RuntimeException("Not instantiable.");
		}

		/*
		 * Returns a move given certain parameters.
		 */

		public static Move createMove(final Board board, final int currentCoordinate, final int destinationCoordinate) {
			for (final Move move : board.currentPlayer().getLegalMoves()) {
				if (move.getStart() == currentCoordinate && move.getDestination() == destinationCoordinate) {
					return move;
				}
			}
			return NULL_MOVE;
		}

		public static Move createMove(final Board board, final Move candidate) {
			for (final Move move : board.currentPlayer().getLegalMoves()) {
				if (candidate.equals(move)) return move;
			}
			return NULL_MOVE;
		}

		public static Move getNullMove() {
			return NULL_MOVE;
		}
		
	}

}
