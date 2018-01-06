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

public class Pawn extends Piece {

	private final static int[] CANDIDATE_MOVE_COORDINATES = { 10, 9, 11 };

	public Pawn(final int piecePosition, final Side color) {
		super(PieceType.PAWN, piecePosition, color, false);
	}

	public Pawn(final int piecePosition, final Side color, final boolean hasMoved) {
		super(PieceType.PAWN, piecePosition, color, hasMoved);
	}

	@Override
	public Collection<Move> calculateLegalMoves(final Board board) {
		final List<Move> legal = new ArrayList<>();

		for (final int offset : CANDIDATE_MOVE_COORDINATES) {
			final int sideOffset = offset * this.getSide().getDirection();
			final int candidateDestination = BoardUtils.board64[this.piecePosition] + sideOffset;
			if (!BoardUtils.inBoard(candidateDestination)) {
				continue;
			}
			final Tile candidateDestinationTile = board.getTile(BoardUtils.mailbox[candidateDestination]);
			if (offset == 10 && !candidateDestinationTile.tileIsOccupied()) {
				if (this.getSide().isPromotionSquare(BoardUtils.mailbox[candidateDestination])) {
					legal.add(new Move.PawnPromotion(new Move.PawnMove(board, this, candidateDestination),
							new Queen(BoardUtils.mailbox[candidateDestination], this.color)));
					legal.add(new Move.PawnPromotion(new Move.PawnMove(board, this, candidateDestination),
							new Rook(BoardUtils.mailbox[candidateDestination], this.color)));
					legal.add(new Move.PawnPromotion(new Move.PawnMove(board, this, candidateDestination),
							new Bishop(BoardUtils.mailbox[candidateDestination], this.color)));
					legal.add(new Move.PawnPromotion(new Move.PawnMove(board, this, candidateDestination),
							new Knight(BoardUtils.mailbox[candidateDestination], this.color)));
				} else {
					legal.add(new Move.PawnMove(board, this, candidateDestination));
				}
				if (BoardUtils.canTwoSquare(this)) {
					// you can only 2 square if you can 1 square already
					// BoardUtils.canTwoSquare only tests the potential for such
					// a move (correct position + first move)
					// no risk of out of bounds — two square will not allow it
					if (!board.getTile(BoardUtils.mailbox[candidateDestination + sideOffset]).tileIsOccupied()) {
						legal.add(new Move.PawnJump(board, this, candidateDestination + sideOffset));
					}
				}
			} else { // captures (offset = 9 or 11), including enpassant
				if (offset != 10) {
					if (candidateDestinationTile.tileIsOccupied()) {
						final Piece pieceAtDestination = candidateDestinationTile.getPiece();
						final Side color = pieceAtDestination.getSide();
						if (this.color != color) { // capture
							if (this.getSide().isPromotionSquare(BoardUtils.mailbox[candidateDestination])) {
								legal.add(new Move.PawnPromotion(
										new Move.PawnCapture(board, this, candidateDestination, pieceAtDestination),
										new Queen(BoardUtils.mailbox[candidateDestination], this.color)));
								legal.add(new Move.PawnPromotion(
										new Move.PawnCapture(board, this, candidateDestination, pieceAtDestination),
										new Rook(BoardUtils.mailbox[candidateDestination], this.color)));
								legal.add(new Move.PawnPromotion(
										new Move.PawnCapture(board, this, candidateDestination, pieceAtDestination),
										new Bishop(BoardUtils.mailbox[candidateDestination], this.color)));
								legal.add(new Move.PawnPromotion(
										new Move.PawnCapture(board, this, candidateDestination, pieceAtDestination),
										new Knight(BoardUtils.mailbox[candidateDestination], this.color)));
							} else {
								legal.add(new Move.PawnCapture(board, this, candidateDestination, pieceAtDestination));
							}
						}
					} else if (board.getEnPassantPawn() != null) {
						if ((offset == 9 && board.getEnPassantPawn().getPosition() == 
								this.piecePosition - this.getSide().getDirection()) ||
								offset == 11 && board.getEnPassantPawn().getPosition() == 
								this.piecePosition + this.getSide().getDirection()) {
							final Piece captured = board.getEnPassantPawn();
							if (captured.getSide() != this.color) {
								legal.add(new Move.EnPassant(board, this, candidateDestination, captured));
							}
						} 
					}
				}
			}
		}

		return ImmutableList.copyOf(legal);
	}

	@Override
	public String toString() {
		return PieceType.PAWN.toString();
	}

	@Override
	public Pawn pieceFromMove(Move move) {
		int destination = BoardUtils.mailbox[move.getDestination()];
		int side = move.getMovedPiece().getSide().isWhite() ? 0 : 1;
		return (Pawn) Piece.ALL_POSSIBLE_PIECES[destination][side][Piece.PAWN_INDEX];
	}

	public Piece getPromotionPiece() {
		// TODO: underpromotions
		int side = this.color.isWhite() ? 0 : 1;
		return Piece.ALL_POSSIBLE_PIECES[this.piecePosition][side][Piece.QUEEN_INDEX];
	}
}
