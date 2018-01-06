package chess.engine.player;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import com.google.common.collect.ImmutableList;

import chess.engine.Side;
import chess.engine.board.Board;
import chess.engine.board.BoardUtils;
import chess.engine.board.Move;
import chess.engine.board.Tile;
import chess.engine.pieces.Piece;
import chess.engine.pieces.Rook;
import chess.engine.pieces.Piece.PieceType;

/*
 * WhitePlayer is a subclass of the abstract Player class. 
 */

public class WhitePlayer extends Player {

	public WhitePlayer(final Board board, final Collection<Move> whiteLegalMoves,
			final Collection<Move> blackLegalMoves) {
		super(board, whiteLegalMoves, blackLegalMoves);
	}

	@Override
	public Collection<Piece> getActivePieces() {
		return this.board.getWhitePieces();
	}

	@Override
	public Side getSide() {
		return Side.WHITE;
	}

	@Override
	public Player getOpponent() {
		return this.board.blackPlayer();
	}

	@Override
	protected Collection<Move> calculateCastles(final Collection<Move> legalMoves,
			final Collection<Move> opponentMoves) {
		final List<Move> kingCastles = new ArrayList<>();
		// castle conditions:
		// 1. not in check
		// 2. K + R have not moved //TODO: fix hasMoved!!!
		// Note: playerKing.getPosition() == 60
		if (!this.playerKing.hasMoved() && !this.inCheck() && this.playerKing.getPosition() == 60) {
			// O-O; 61 and 62 are true 0 - 64 coordinates that correspond to f1
			// and g1
			if (!this.board.getTile(61).tileIsOccupied() && !this.board.getTile(62).tileIsOccupied()) {
				final Tile rookTile = this.board.getTile(63); // 63 = h1
				if (rookTile.tileIsOccupied() && !rookTile.getPiece().hasMoved()
						&& rookTile.getPiece().getType() == PieceType.ROOK) {
					if (this.attacksOnTile(61, opponentMoves).isEmpty()
							&& this.attacksOnTile(62, opponentMoves).isEmpty()) { // 3.
																					// can't
																					// castle
																					// through
																					// or
																					// into
																					// check
						kingCastles.add(new Move.KingsideCastleMove(this.board, this.playerKing, BoardUtils.board64[62],
								(Rook) rookTile.getPiece(), rookTile.getCoordinate(), BoardUtils.board64[61]));
					}
				}
			}
			// O-O-O
			if (!this.board.getTile(59).tileIsOccupied() && !this.board.getTile(58).tileIsOccupied()
					&& !this.board.getTile(57).tileIsOccupied()) {
				final Tile rookTile = this.board.getTile(56); // 63 = h1
				if (rookTile.tileIsOccupied() && !rookTile.getPiece().hasMoved()
						&& rookTile.getPiece().getType() == PieceType.ROOK) {
					if (this.attacksOnTile(59, opponentMoves).isEmpty()
							&& this.attacksOnTile(58, opponentMoves).isEmpty()) { // 3.
																					// can't
																					// castle
																					// through
																					// or
																					// into
																					// check
						kingCastles
								.add(new Move.QueensideCastleMove(this.board, this.playerKing, BoardUtils.board64[58],
										(Rook) rookTile.getPiece(), rookTile.getCoordinate(), BoardUtils.board64[59]));
					}
				}
			}

		}

		return ImmutableList.copyOf(kingCastles);
	}
}
