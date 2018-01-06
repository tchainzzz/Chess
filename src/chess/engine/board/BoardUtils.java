package chess.engine.board;

import java.util.*;

import com.google.common.collect.ImmutableMap;

import chess.engine.pieces.*;

/*
 * Stores useful constants and static methods.
 */

public class BoardUtils {

	public static final int NUM_TILES = 64;
	public static final int TILES_PER_ROW = 8;
	public static final int WHITE_PAWN_RANK = 6;
	public static final int BLACK_PAWN_RANK = 1;
	public static final int SPECIAL_RANK_OFFSET = 2; // for algebraic notation
														// calculation
	public static final int[] mailbox = { 
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 0/* 21 */, 1, 2, 3, 4, 5, 6, 7, -1, 
			-1, 8, 9, 10, 11, 12, 13, 14, 15, -1, 
			-1, 16, 17, 18, 19, 20, 21, 22, 23, -1, 
			-1, 24, 25, 26, 27, 28, 29, 30, 31, -1, 
			-1, 32, 33, 34, 35, 36, 37, 38, 39, -1, 
			-1, 40, 41, 42, 43, 44, 45, 46, 47, -1,
			-1, 48, 49, 50, 51, 52, 53, 54, 55, -1, 
			-1, 56, 57, 58, 59, 60, 61, 62, 63, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1 };
	// mailbox[board64coordinate] = 8x8 true position
	// mailbox[board64coordinate] == -1 ? outOfBounds : inBounds

	public static final int[] board64 = { 
			21, 22, 23, 24, 25, 26, 27, 28, 
			31, 32, 33, 34, 35, 36, 37, 38, 
			41, 42, 43, 44, 45, 46, 47, 48, 
			51, 52, 53, 54, 55, 56, 57, 58, 
			61, 62, 63, 64, 65, 66, 67, 68, 
			71, 72, 73, 74, 75, 76, 77, 78, 
			81, 82, 83, 84, 85, 86, 87, 88, 
			91, 92, 93, 94, 95, 96, 97, 98 };

	public static final String[] algebraic = { 
			"a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8", 
			"a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7", 
			"a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6", 
			"a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5",
			"a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4", 
			"a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3",
			"a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2", 
			"a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1", };
	public final static Map<String, Integer> ALGEBRAIC_TO_MOVE_COORDINATE = initializeAlgebraicToMove();

	private BoardUtils() {
		throw new RuntimeException("You cannot instantiate this class.");
	}

	private static Map<String, Integer> initializeAlgebraicToMove() {
		final Map<String, Integer> map = new HashMap<>();
		for (int i = 0; i < NUM_TILES; i++) {
			map.put(algebraic[i], board64[i]);
		}
		return ImmutableMap.copyOf(map);
	}

	public static boolean inBoard(final int coordinate) {
		// int coordinate is passed in as board64 index
		if (coordinate < mailbox.length) { // if in bounds of mailbox
			return mailbox[coordinate] != -1;
		}
		return false;
	}

	public static boolean canTwoSquare(final Piece piece) {
		if (piece instanceof Pawn) {
			if (!piece.hasMoved()) {
				int trueRank = piece.getPosition() / TILES_PER_ROW;
				// if (black && 7th rank) || (white && 2nd rank)
				if ((piece.getSide().isBlack() && trueRank == BLACK_PAWN_RANK)
						|| (piece.getSide().isWhite() && trueRank == WHITE_PAWN_RANK))
					return true;

			}
		}
		return false;
	}

	public static int getAt(String square) {
		return ALGEBRAIC_TO_MOVE_COORDINATE.get(square);
	}
}
