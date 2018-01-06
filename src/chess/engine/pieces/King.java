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

public class King extends Piece {

	private final static int[] CANDIDATE_MOVE_COORDINATES = { -10, -1, 1, 10, -11, -9, 9, 11 };
	private final boolean isCastled;
	private boolean canKingsideCastle;
	private boolean canQueensideCastle;

	public King(final int piecePosition, final Side color) {
		super(PieceType.KING, piecePosition, color, false);
		this.isCastled = false;
	}

	public King(final int piecePosition, final Side color, final boolean hasMoved, final boolean isCastled) {
		super(PieceType.KING, piecePosition, color, hasMoved);
		this.isCastled = isCastled;
	}

	public boolean isCastled() {
		return this.isCastled;
	}

	@Override
	public Collection<Move> calculateLegalMoves(Board board) {
		final List<Move> legal = new ArrayList<>();

		for (final int current : CANDIDATE_MOVE_COORDINATES) { // check each
																// candidate ray

			final int candidateDestination = BoardUtils.board64[this.piecePosition] + current;
			if (BoardUtils.inBoard(candidateDestination)) {
				final Tile candidateDestinationTile = board.getTile(BoardUtils.mailbox[candidateDestination]);
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
		return PieceType.KING.toString();
	}

	@Override
	public King pieceFromMove(Move move) {
		int destination = BoardUtils.mailbox[move.getDestination()];
		int side = move.getMovedPiece().getSide().isWhite() ? 0 : 1;
		return (King) Piece.ALL_POSSIBLE_PIECES[destination][side][Piece.KING_INDEX];
	}

}
