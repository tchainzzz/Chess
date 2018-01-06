package chess.engine.pieces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

import chess.engine.Side;
import chess.engine.board.Board;
import chess.engine.board.BoardUtils;
import chess.engine.board.Move;
import chess.engine.board.Tile;
import chess.engine.pieces.Piece.PieceType;

public class Rook extends Piece {
	private final static int[] CANDIDATE_MOVE_COORDINATES = { -10, -1, 1, 10 };

	public Rook(final int piecePosition, final Side color) {
		super(PieceType.ROOK, piecePosition, color, false);
	}

	public Rook(final int piecePosition, final Side color, final boolean hasMoved) {
		super(PieceType.ROOK, piecePosition, color, hasMoved);
	}

	@Override
	public Collection<Move> calculateLegalMoves(Board board) {
		int candidateDestination = BoardUtils.board64[this.piecePosition];
		final List<Move> legal = new ArrayList<>();

		for (final int current : CANDIDATE_MOVE_COORDINATES) { // check each
																// candidate ray
			// repeatedly add -10, then -1, etc.
			boolean blocked = false;
			while (!blocked) {
				candidateDestination += current;
				if (BoardUtils.inBoard(candidateDestination)) {
					final Tile candidateDestinationTile = board.getTile(BoardUtils.mailbox[candidateDestination]);
					if (!candidateDestinationTile.tileIsOccupied()) {
						legal.add(new Move.QuietMove(board, this, candidateDestination));
					} else { // if it's occupied, is it a capture or is it
								// illegal?
						blocked = true;
						final Piece pieceAtDestination = candidateDestinationTile.getPiece();
						final Side color = pieceAtDestination.getSide();
						if (this.color != color) { // capture
							legal.add(new Move.CaptureMove(board, this, candidateDestination, pieceAtDestination));
						}
					}
				} else {
					blocked = true; // off-board = blocked
				}
			}
			candidateDestination = BoardUtils.board64[this.piecePosition]; // reset
																			// to
																			// start
																			// after
																			// finishing
																			// while
																			// loop
		}
		return ImmutableList.copyOf(legal);
	}

	@Override
	public String toString() {
		return PieceType.ROOK.toString();
	}

	@Override
	public Rook pieceFromMove(Move move) {
		int destination = BoardUtils.mailbox[move.getDestination()];
		int side = move.getMovedPiece().getSide().isWhite() ? 0 : 1;
		return (Rook) Piece.ALL_POSSIBLE_PIECES[destination][side][Piece.ROOK_INDEX];
	}
}
