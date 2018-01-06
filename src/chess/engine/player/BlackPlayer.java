package chess.engine.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

import chess.engine.Side;
import chess.engine.board.Board;
import chess.engine.board.BoardUtils;
import chess.engine.board.Move;
import chess.engine.board.Tile;
import chess.engine.pieces.Piece;
import chess.engine.pieces.Rook;
import chess.engine.pieces.Piece.PieceType;

public class BlackPlayer extends Player {

	public BlackPlayer(final Board board, final Collection<Move> whiteLegalMoves,
			final Collection<Move> blackLegalMoves) {
		super(board, blackLegalMoves, whiteLegalMoves);
		// board, your side, other side
	}

	@Override
	public Collection<Piece> getActivePieces() {
		return this.board.getBlackPieces();
	}

	@Override
	public Side getSide() {
		return Side.BLACK;
	}

	@Override
	public Player getOpponent() {
		return this.board.whitePlayer();
	}

	@Override
	protected Collection<Move> calculateCastles(final Collection<Move> legalMoves,
			final Collection<Move> opponentMoves) {
		final List<Move> kingCastles = new ArrayList<>();
		// castle conditions:
		// 1. not in check
		// 2. K + R have not moved
		// Note: playerKing.getPosition() == 4
		if (!this.playerKing.hasMoved() && !this.inCheck()) {
			// O-O; 5 and 6 are true 0 - 64 coordinates that correspond to f8
			// and g8
			if (!this.board.getTile(5).tileIsOccupied() && !this.board.getTile(6).tileIsOccupied()) {
				final Tile rookTile = this.board.getTile(7); // 7 = h8
				if (rookTile.tileIsOccupied() && !rookTile.getPiece().hasMoved()
						&& rookTile.getPiece().getType() == PieceType.ROOK) {
					if (this.attacksOnTile(5, opponentMoves).isEmpty()
							&& this.attacksOnTile(6, opponentMoves).isEmpty()) { // 3.
																					// can't
																					// castle
																					// through
																					// or
																					// into
																					// check
						kingCastles.add(new Move.KingsideCastleMove(this.board, this.playerKing, BoardUtils.board64[6],
								(Rook) rookTile.getPiece(), BoardUtils.board64[rookTile.getCoordinate()],
								BoardUtils.board64[5])); // TODO: add actual
															// castle move);
															// //TODO: add
															// actual castle
															// move
					}
				}
			}
			// O-O-O
			if (!this.board.getTile(1).tileIsOccupied() && !this.board.getTile(2).tileIsOccupied()
					&& !this.board.getTile(3).tileIsOccupied()) {
				final Tile rookTile = this.board.getTile(0); // 0 = a8
				if (rookTile.tileIsOccupied() && !rookTile.getPiece().hasMoved()
						&& rookTile.getPiece().getType() == PieceType.ROOK) {
					if (this.attacksOnTile(2, opponentMoves).isEmpty()
							&& this.attacksOnTile(3, opponentMoves).isEmpty()) { // 3.
																					// can't
																					// castle
																					// through
																					// or
																					// into
																					// check
						kingCastles.add(new Move.QueensideCastleMove(this.board, this.playerKing, BoardUtils.board64[2],
								(Rook) rookTile.getPiece(), BoardUtils.board64[rookTile.getCoordinate()],
								BoardUtils.board64[3])); // TODO: add actual
															// castle move;
															// //TODO: add
															// actual castle
															// move
					}
				}
			}

		}

		return ImmutableList.copyOf(kingCastles);
	}

}
