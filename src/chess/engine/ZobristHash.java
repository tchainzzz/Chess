package chess.engine;

import java.security.SecureRandom;

import chess.engine.board.Board;
import chess.engine.board.BoardUtils;
import chess.engine.board.Move;
import chess.engine.board.Move.EnPassant;
import chess.engine.pieces.Pawn;
import chess.engine.pieces.Piece;

public class ZobristHash {

	private static final int NUM_COLORS = 2;
	private static final int NUM_PIECES = 6;

	public static long zobristArray[][][] = new long[BoardUtils.NUM_TILES][NUM_COLORS][NUM_PIECES];
	public static long zobristEPFile[] = new long[8];
	public static long zobristCastle[] = new long[4]; // KQkq
	public static long zobristBlackMove;	
	
	public static int DEFAULT_TABLE_SIZE = 1000000;
	
	private ZobristHash() {
		throw new RuntimeException("Cannot be instantiated");
	}
	
	public static long random64() {
		SecureRandom rand = new SecureRandom();
		return rand.nextLong();
	}

	public static void fillArray() {
		for (int tile = 0; tile < BoardUtils.NUM_TILES; tile++) {
			for (int color = 0; color < NUM_COLORS; color++) {
				for (int pieceType = 0; pieceType < NUM_PIECES; pieceType++) {
					zobristArray[tile][color][pieceType] = random64();
				}
			}
		}
		for (int file = 0; file < BoardUtils.TILES_PER_ROW; file++) {
			zobristEPFile[file] = random64();
		}
		for (int castleMoves = 0; castleMoves < 4; castleMoves++) {
			zobristCastle[castleMoves] = random64();
		}
		zobristBlackMove = random64();
	}

	/*
	 * SHOULD ONLY BE CALLED ONCE AT STARTUP!
	 */

	public static long getZobristHash(final Board board) {
		long returnKey = 0;
		// XOR in board state
		for (int tile = 0; tile < BoardUtils.NUM_TILES; tile++) {
			final Piece current = board.getTile(tile).getPiece();

			if (current != null) {
				final int side = current.getSide().getEnum();
				returnKey ^= zobristArray[tile][side][current.getType().getEnum()];
			}
		}
		// XOR in en passant
		Pawn ep = board.getEnPassantPawn();
		if (ep != null) {
			int file = ep.getPosition() % BoardUtils.TILES_PER_ROW; // file 0 -
																	// 7
			returnKey ^= zobristEPFile[file];
		}
		// XOR in castling rights
		if (board.whitePlayer().canKingsideCastle())
			returnKey ^= zobristCastle[0];
		if (board.whitePlayer().canQueensideCastle())
			returnKey ^= zobristCastle[1];
		if (board.blackPlayer().canKingsideCastle())
			returnKey ^= zobristCastle[2];
		if (board.blackPlayer().canQueensideCastle())
			returnKey ^= zobristCastle[3];

		// XOR in blackToMove
		 if (board.currentPlayer().getSide().isBlack()) returnKey ^= zobristBlackMove;
		return returnKey;
	}

	public static long updateZobristHash(long currentHash, final Move move) {
		// XOR out piece
		currentHash ^= zobristArray[BoardUtils.mailbox[move.getStart()]][move.getMovedPiece().getSide().getEnum()][move
				.getMovedPiece().getType().getEnum()];
		// XOR in piece moved
		currentHash ^= zobristArray[BoardUtils.mailbox[move.getDestination()]][move.getMovedPiece().getSide()
				.getEnum()][move.getMovedPiece().getType().getEnum()];
		// XOR out captured piece -> delegate?
		Piece taken = move.getAttackedPiece();
		if (taken != null) {
			currentHash ^= zobristArray[taken.getPosition()][taken.getSide().getEnum()][taken.getType().getEnum()];
		}
		if (move instanceof EnPassant) {
			Pawn ep = (Pawn) move.getAttackedPiece();
			currentHash ^= zobristArray[ep.getPosition()][ep.getSide().getEnum()][ep.getType().getEnum()];
		}
		// if castle, XOR castling rights -> delegate?
		// if (move instanceof CastleMove) {
		// CastleMove castle = (CastleMove) move;
		// TODO
		// }
		// XOR in side to move
		currentHash ^= zobristBlackMove;
		return currentHash; 
	}

}
