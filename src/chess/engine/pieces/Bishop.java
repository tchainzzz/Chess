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

public class Bishop extends Piece {
	private final static int[] CANDIDATE_MOVE_COORDINATES = { -11, -9, 9, 11 };

	public Bishop(final int piecePosition, final Side color) {
		super(PieceType.BISHOP, piecePosition, color, false);
	}

	public Bishop(final int piecePosition, final Side color, final boolean hasMoved) {
		super(PieceType.BISHOP, piecePosition, color, hasMoved);
	}

	@Override
	public Collection<Move> calculateLegalMoves(Board board) {
		int candidateDestination = BoardUtils.board64[this.piecePosition];
		final List<Move> legal = new ArrayList<>();

		for (final int current : CANDIDATE_MOVE_COORDINATES) { // check each
																// candidate ray
			// repeatedly add -11, then -9, etc.
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
		return PieceType.BISHOP.toString();
	}

	@Override
	public Bishop pieceFromMove(Move move) {
		int destination = BoardUtils.mailbox[move.getDestination()];
		int side = move.getMovedPiece().getSide().isWhite() ? 0 : 1;
		return (Bishop) Piece.ALL_POSSIBLE_PIECES[destination][side][Piece.BISHOP_INDEX];
	}

}
