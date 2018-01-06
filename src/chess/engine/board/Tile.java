/* Trenton Chang - 06/25/2017
 * 
 */

package chess.engine.board;

import java.util.HashMap;
import java.util.Map;
import com.google.common.collect.ImmutableMap;

import chess.engine.pieces.Piece;

/*
 * tile = square on chessboard
 */

public abstract class Tile {

	// fields
	protected final int tileCoordinate; // coordinate of tile

	private static final Map<Integer, EmptyTile> EMPTY_TILES_CACHE = createAllPossibleEmptyTiles();

	/*
	 * Creates a cache of all possible empty tiles so that each tile may be
	 * referenced easily
	 */
	private static Map<Integer, EmptyTile> createAllPossibleEmptyTiles() {

		final Map<Integer, EmptyTile> emptyTileMap = new HashMap<>();

		for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
			emptyTileMap.put(i, new EmptyTile(i));
		}

		return ImmutableMap.copyOf(emptyTileMap);

	}

	/*
	 * Creates a tile, occupied or not based on piece and position
	 */
	public static Tile createTile(final int coordinate, final Piece piece) {
		return piece != null ? new OccupiedTile(coordinate, piece) : EMPTY_TILES_CACHE.get(coordinate);
	}

	/*
	 * Constructor for tile
	 * 
	 * @param tileCoordinate: the coordinate of the tile; the position of the
	 * tile on the board
	 */

	private Tile(int tileCoordinate) {
		this.tileCoordinate = tileCoordinate;
	}

	/*
	 * Returns whether the tile is occupied or not
	 */
	public abstract boolean tileIsOccupied();

	/*
	 * Returns the piece on the tile
	 */
	public abstract Piece getPiece();

	public int getCoordinate() {
		return this.tileCoordinate;
	}

	public String toAlgebraic() {
		int fauxCoordinate = BoardUtils.board64[this.tileCoordinate];
		return "" + (BoardUtils.TILES_PER_ROW - (fauxCoordinate / 10) + 'a' - 1 + BoardUtils.SPECIAL_RANK_OFFSET)
				+ (fauxCoordinate % 10);
	}

	/*
	 * Class for an empty tile
	 */

	public static final class EmptyTile extends Tile {

		private EmptyTile(final int coordinate) {
			super(coordinate);
		}

		@Override
		public String toString() {
			return "" + BoardUtils.board64[this.tileCoordinate];
		}

		@Override
		public boolean tileIsOccupied() {
			return false;
		}

		@Override
		public Piece getPiece() {
			return null;
		}

	}

	public static final class OccupiedTile extends Tile {

		private final Piece piece;

		private OccupiedTile(final int coordinate, Piece piece) {
			super(coordinate);
			this.piece = piece;
		}

		@Override
		public String toString() {
			return getPiece().getSide().isBlack() ? getPiece().toString().toLowerCase() : getPiece().toString(); // default
																													// piece.toString()
																													// returns
																													// upper
																													// case

		}

		@Override
		public boolean tileIsOccupied() {
			return true;
		}

		@Override
		public Piece getPiece() {
			return this.piece;
		}

	}

}
