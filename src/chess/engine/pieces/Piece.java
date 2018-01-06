package chess.engine.pieces;

import java.util.Collection;

import chess.engine.Side;
import chess.engine.board.Board;
import chess.engine.board.BoardUtils;
import chess.engine.board.Move;

public abstract class Piece {

	protected final PieceType type;
	protected final int piecePosition; // IMPORTANT: stored in terms of board64
										// coordinate, not mailbox!
	protected final Side color;
	protected boolean hasMoved;
	private final int cachedHashCode;

	public static final Piece[][][] ALL_POSSIBLE_PIECES = initializeAllPossiblePieces();
	public static final int PAWN_INDEX = 0;
	public static final int KNIGHT_INDEX = 1;
	public static final int BISHOP_INDEX = 2;
	public static final int ROOK_INDEX = 3;
	public static final int QUEEN_INDEX = 4;
	public static final int KING_INDEX = 5;

	/*
	 * This is the constructor for the piece object.
	 * 
	 * @param piecePosition: position stored as an int in terms of board index
	 * (0 ~ 63 -> vals. 21 ~ 98); see class BoardUtils
	 * 
	 * @param color: piece color
	 */
	Piece(final PieceType type, final int piecePosition, final Side color, final boolean hasMoved) {
		this.piecePosition = piecePosition;
		this.color = color;
		this.type = type;
		this.hasMoved = hasMoved;
		this.cachedHashCode = computeHashCode();
	}

	private int computeHashCode() {
		int result = this.type.hashCode(); // 32-bit integer representing piece
											// type
		result = 31 * result + this.color.hashCode();
		result = 31 * result + this.piecePosition;
		result = 31 * result + (hasMoved ? 1 : 0);
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		// referential equality; &this == &obj — default implementation
		if (this == obj)
			return true;
		if (!(obj instanceof Piece))
			return false;

		final Piece other = (Piece) obj;
		return this.piecePosition == other.getPosition() && this.type == other.getType()
				&& this.color == other.getSide();
		// if all fields match, objects are equivalent
	}

	/*
	 * Since the Piece object is immutable, we can just compute and cache a
	 * hashcode
	 */

	@Override
	public int hashCode() {
		return this.cachedHashCode;
	}

	public Side getSide() {
		return this.color;
	}

	public boolean hasMoved() {
		return this.hasMoved;
	}

	public int getPosition() {
		return this.piecePosition;
	}

	public PieceType getType() {
		return this.type;
	}

	public int getValue() {
		return this.type.getValue();
	}

	public void hasNowMoved() {
		this.hasMoved = true;

	}

	/*
	 * To get a piece: [tileID (0 ~ 63)][color (0 ~ 1)][pieceType (0 ~ 5)]
	 * 
	 * @color: 0 = Side.WHITE; 1 = Side.BLACK
	 * 
	 * @pieceType: refer to enum PieceType order below
	 */

	private static Piece[][][] initializeAllPossiblePieces() {
		Piece[][][] arr = new Piece[BoardUtils.NUM_TILES][2][6];
		for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
			for (int j = 0; j <= 1; j++) {
				Side currentSide = j == 0 ? Side.WHITE : Side.BLACK;
				arr[i][j][0] = new Pawn(i, currentSide);
				arr[i][j][1] = new Knight(i, currentSide);
				arr[i][j][2] = new Bishop(i, currentSide);
				arr[i][j][3] = new Rook(i, currentSide);
				arr[i][j][4] = new Queen(i, currentSide);
				arr[i][j][5] = new King(i, currentSide);
			}
		}
		return arr;
	}

	/*
	 * Based on a Board, calculate a list of legal moves.
	 */

	public abstract Collection<Move> calculateLegalMoves(final Board board);

	// TODO: pre-compute and memoize all 768 (12 x 64) possible pieces given the
	// board
	public abstract Piece pieceFromMove(Move move);

	public enum PieceType {

		PAWN("P", 100) {
			@Override
			public int getEnum() {
				return 0;
			}
		},
		KNIGHT("N", 320) {
			@Override
			public int getEnum() {
				return 1;
			}
		},
		BISHOP("B", 330) {
			@Override
			public int getEnum() {
				return 2;
			}
		},
		ROOK("R", 500) {
			@Override
			public int getEnum() {
				return 3;
			}
		},
		QUEEN("Q", 900) {
			@Override
			public int getEnum() {
				return 4;
			}
		},
		KING("K", 20000) {
			@Override
			public int getEnum() {
				return 5;
			}
		};

		private String pieceName;
		private int val;

		PieceType(final String pieceName, final int val) {
			this.pieceName = pieceName;
			this.val = val;
		}

		public int getValue() {
			return this.val;
		}

		@Override
		public String toString() {
			return this.pieceName;
		}

		public abstract int getEnum();
	}

}
