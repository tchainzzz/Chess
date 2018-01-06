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

public class Knight extends Piece {

	/*
	 * Square-coordinate offsets given a board is stored in a 120-square mailbox
	 * array.
	 */
	private final static int[] CANDIDATE_MOVE_COORDINATES = { -21, -19, -12, -8, 8, 12, 19, 21 };

	/*
	 * Constructor: position and color. Position is in terms of 120-square array
	 * location.
	 */

	public Knight(final int piecePosition, final Side color) {
		super(PieceType.KNIGHT, piecePosition, color, false);
	}

	public Knight(final int piecePosition, final Side color, final boolean hasMoved) {
		super(PieceType.KNIGHT, piecePosition, color, hasMoved);
	}

	/*
	 * Calculate legal moves
	 */

	@Override
	public Collection<Move> calculateLegalMoves(Board board) {

		int candidateDestination; // mailbox 21-98
		final List<Move> legal = new ArrayList<>();

		for (final int current : CANDIDATE_MOVE_COORDINATES) { // check each
																// candidate
																// move
			candidateDestination = BoardUtils.board64[this.piecePosition] + current;
			if (BoardUtils.inBoard(candidateDestination)) {
				final Tile candidateDestinationTile = board.getTile(BoardUtils.mailbox[candidateDestination]); // 64
																												// coordinate
				if (!candidateDestinationTile.tileIsOccupied()) {
					legal.add(new Move.QuietMove(board, this, candidateDestination));
				} else { // if it's occupied, is it a capture or is it illegal?
					final Piece pieceAtDestination = candidateDestinationTile.getPiece();
					final Side color = pieceAtDestination.getSide();
					if (this.color != color) { // capture
						legal.add(new Move.CaptureMove(board, this, candidateDestination, pieceAtDestination));
					}
				}
			}
		}
		return ImmutableList.copyOf(legal);
	}

	@Override
	public String toString() {
		return PieceType.KNIGHT.toString();
	}

	@Override
	public Knight pieceFromMove(Move move) {
		int destination = BoardUtils.mailbox[move.getDestination()];
		int side = move.getMovedPiece().getSide().isWhite() ? 0 : 1;
		return (Knight) Piece.ALL_POSSIBLE_PIECES[destination][side][Piece.KNIGHT_INDEX];
	}

}
