package chess.engine.board;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import chess.engine.Side;
import chess.engine.ZobristHash;
import chess.engine.pieces.*;
import chess.engine.pieces.Piece.PieceType;
import chess.engine.player.BlackPlayer;
import chess.engine.player.Player;
import chess.engine.player.WhitePlayer;

/*
 * This class represents the main chessboard. It has a list of tiles, which themselves store corresponding pieces,
 * collections of white and black pieces, and player objects.
 */

public class Board {

	// CONSTANTS:
	// actual initial
	public static final String INITIAL_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

	// 1. e4
	// public static final String INITIAL_FEN =
	// "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1";

	// 1. e4 c5 2. Nf3 d6 3. d4
	// public static final String INITIAL_FEN =
	// "rnbqkbnr/pp2pppp/3p4/2p5/3PP3/5N2/PPP2PPP/RNBQKB1R b KQkq - 0 3";

	// FIELDS:
	private final List<Tile> gameBoard; // all tiles on 8x8 board
	private final Collection<Piece> whitePieces; // all white pieces in gameBoard
	private final Collection<Piece> blackPieces; // all black pieces in gameBoard
	private final Pawn enPassantPawn;
	private long currentHash;

	// player objects
	private final WhitePlayer whiteplayer;
	private final BlackPlayer blackplayer;
	private final Player currentPlayer;

	/*
	 * CONSTRUCTOR
	 * 
	 * We pass in a builder-pattern class (see class definition below) to help
	 * us dynamically build a board. This constructor also initializes the
	 * player fields and piece lists, in addition to getting the psuedo-legal
	 * starting moves.
	 * 
	 * @param builder: Builder object to build the board representation (list of
	 * Tile objects)
	 */
	private Board(final Builder builder) {
		this.gameBoard = createGameBoard(builder);
		this.whitePieces = calculateActivePieces(this.gameBoard, Side.WHITE);
		this.blackPieces = calculateActivePieces(this.gameBoard, Side.BLACK);
		this.enPassantPawn = builder.enPassantPawn;
		this.currentHash = builder.hash;

		//pseudo-legal collection
		final Collection<Move> whiteLegalMoves = calculateLegalMoves(this.whitePieces);
		final Collection<Move> blackLegalMoves = calculateLegalMoves(this.blackPieces);

		this.whiteplayer = new WhitePlayer(this, whiteLegalMoves, blackLegalMoves);
		this.blackplayer = new BlackPlayer(this, whiteLegalMoves, blackLegalMoves);
		this.currentPlayer = builder.sideToMove.choosePlayer(this.whiteplayer, this.blackplayer);
		// parameters help determine legal castle moves
	}

	/*
	 * This method prints out a representation of the board.
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
			final String tiletext = this.gameBoard.get(i).toString();
			// tiletext derived from Piece object at square i
			builder.append(String.format("%3s", tiletext));
			if ((i + 1) % BoardUtils.TILES_PER_ROW == 0) { 
				builder.append("\n");
			}
		}
		return builder.toString();
	}

	/*
	 * Calculate legal moves given a list of pieces.
	 * 
	 * @param pieceList: A collection of pieces upon which to calculate all
	 * legal moves of all pieces.
	 */
	private Collection<Move> calculateLegalMoves(Collection<Piece> pieceList) {
		final List<Move> legal = new ArrayList<>();
		for (final Piece p : pieceList) {
			legal.addAll(p.calculateLegalMoves(this));
		}
		return ImmutableList.copyOf(legal);
	}

	/*
	 * Get all pieces on the board of a certain color.
	 * 
	 * @param gameBoard: board representation in which to find pieces
	 * @param color: color of pieces to add to the collection
	 */
	private static Collection<Piece> calculateActivePieces(List<Tile> gameBoard, Side color) {
		final List<Piece> active = new ArrayList<>();
		for (final Tile tile : gameBoard) {
			if (tile.tileIsOccupied()) { 
				final Piece piece = tile.getPiece();
				if (piece.getSide() == color)
					active.add(piece);
			}
		}
		return ImmutableList.copyOf(active);
	}

	/*
	 * Returns a tile given a 0 - 64 candidate destination.
	 * 
	 * @param candidateDestination: an int specifying which tile to return
	 */
	public Tile getTile(int candidateDestination) {
		return this.gameBoard.get(candidateDestination);
	}

	/*
	 * Accessors for the piecelists.
	 * 
	 * @param side: returns a pieceList for a given side.
	 */

	public Collection<Piece> getPieceList(Side side) {
		return side == Side.WHITE ? this.whitePieces : this.blackPieces;
	}

	public Collection<Piece> getBlackPieces() {
		return this.blackPieces;
	}

	public Collection<Piece> getWhitePieces() {
		return this.whitePieces;
	}

	public Collection<Piece> getAllPieces() {
		List<Piece> all = new ArrayList<>();
		all.addAll(this.whitePieces);
		all.addAll(this.blackPieces);
		return all;
	}

	/*
	 * Returns all legal moves on board.
	 */

	public Iterable<Move> getAllLegalMoves() {
		return Iterables.unmodifiableIterable(
				Iterables.concat(this.whiteplayer.getLegalMoves(), this.blackplayer.getLegalMoves()));
	}

	/*
	 * Accessors for player objects.
	 */

	public Player whitePlayer() {
		return this.whiteplayer;
	}

	public Player blackPlayer() {
		return this.blackplayer;
	}

	public Player currentPlayer() {
		return this.currentPlayer;
	}

	/*
	 * Accessor for the en passant pawn.
	 */

	public Pawn getEnPassantPawn() {
		return this.enPassantPawn;
	}

	/*
	 * Creates game board given a FEN position.
	 */

	private static List<Tile> createGameBoard(final Builder builder) {
		final Tile[] tiles = new Tile[BoardUtils.NUM_TILES]; 
		for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
			tiles[i] = Tile.createTile(i, builder.boardConfig.get(i));
			// board config is <Integer, Piece>; OccupiedTile constructor is (location, piece)
		} // tiles are numbered 0 - 63
		return ImmutableList.copyOf(tiles);
	}
	
	public void setInitialHashValue() {
		this.currentHash = ZobristHash.getZobristHash(this);
	}
	
	public long getHash() {
		return this.currentHash;
	}

	/*
	 * Creates the initial chess position.
	 * 
	 * @param fen: Forsyth-Edwards Notation chess position.
	 */

	public static Board createStandardBoard() {
		return parseFEN(INITIAL_FEN);
	}

	/*
	 * Creates a given chess position based on FEN notation.
	 * 
	 * @param fen: Forsyth-Edwards Notation chess position.
	 */
	public static Board parseFEN(String fen) {
		String[] all = fen.split(" ");

		// -- FEN notation breakdown --
		// all[0]: the fen with "/"
		// all[1]: side to move
		// all[2]: castling flags
		// all[3]: en passant square, if applicable
		// all[4]: halfmoves since last capture/pawn advance
		// all[5]: absolute fullmove number

		String position = all[0];
		position = position.replace("/", "");
		// position: fen with "/" removed

		// calls builders
		final Builder builder = new Builder();
		int iterator = 0; // start from square 0
		while (iterator < BoardUtils.NUM_TILES) {
			for (int i = 0; i < position.length(); i++) {
				char c = position.charAt(i);
				// black pieces:
				if (c == 'r') {
					// this construction simultaneously initializes the position
					// field in the Piece classes
					// and adds that position to the Builder class' boardConfig
					// HashMap of piece positions
					builder.setPiece(Piece.ALL_POSSIBLE_PIECES[iterator][Side.BLACK.getEnum()][Piece.ROOK_INDEX]);
				} else if (c == 'n') {
					builder.setPiece(Piece.ALL_POSSIBLE_PIECES[iterator][Side.BLACK.getEnum()][Piece.KNIGHT_INDEX]);
				} else if (c == 'b') {
					builder.setPiece(Piece.ALL_POSSIBLE_PIECES[iterator][Side.BLACK.getEnum()][Piece.BISHOP_INDEX]);
				} else if (c == 'q') {
					builder.setPiece(Piece.ALL_POSSIBLE_PIECES[iterator][Side.BLACK.getEnum()][Piece.QUEEN_INDEX]);
				} else if (c == 'k') {
					builder.setPiece(Piece.ALL_POSSIBLE_PIECES[iterator][Side.BLACK.getEnum()][Piece.KING_INDEX]);
				} else if (c == 'p') {
					builder.setPiece(Piece.ALL_POSSIBLE_PIECES[iterator][Side.BLACK.getEnum()][Piece.PAWN_INDEX]);
				}

				// white pieces:
				else if (c == 'R') {
					builder.setPiece(Piece.ALL_POSSIBLE_PIECES[iterator][Side.WHITE.getEnum()][Piece.ROOK_INDEX]);
				} else if (c == 'N') {
					builder.setPiece(Piece.ALL_POSSIBLE_PIECES[iterator][Side.WHITE.getEnum()][Piece.KNIGHT_INDEX]);
				} else if (c == 'B') {
					builder.setPiece(Piece.ALL_POSSIBLE_PIECES[iterator][Side.WHITE.getEnum()][Piece.BISHOP_INDEX]);
				} else if (c == 'Q') {
					builder.setPiece(Piece.ALL_POSSIBLE_PIECES[iterator][Side.WHITE.getEnum()][Piece.QUEEN_INDEX]);
				} else if (c == 'K') {
					builder.setPiece(Piece.ALL_POSSIBLE_PIECES[iterator][Side.WHITE.getEnum()][Piece.KING_INDEX]);
				} else if (c == 'P') {
					builder.setPiece(Piece.ALL_POSSIBLE_PIECES[iterator][Side.WHITE.getEnum()][Piece.PAWN_INDEX]);
				}

				// blank:
				else if (c - '0' >= 1 && c - '0' <= 8) {
					iterator += (c - '0' - 1);
				} else {
					throw new RuntimeException("Invalid FEN character!");
				}
				iterator++;
			}
		}

		builder.setSide(all[1].equals("w") ? Side.WHITE : Side.BLACK); 
		return builder.build();
	}

	/*
	 * The class Builder is tied to all instances of Board. It has a map storing
	 * the 0 - 64 coordinate of each Piece, and the current side to move.
	 */

	public static class Builder {

		Map<Integer, Piece> boardConfig; // Map<coordinate (0 - 64), piece>
		Side sideToMove;
		Pawn enPassantPawn;
		long hash;

		/*
		 * CONSTRUCTOR Initializes an empty HashMap.
		 */
		public Builder() {
			this.boardConfig = new HashMap<>();
		}

		/*
		 * Puts a piece and its coordinate into boardConfig.
		 * 
		 * @param piece: current instance of Piece
		 */
		public Builder setPiece(final Piece piece) {
			this.boardConfig.put(piece.getPosition(), piece);
			return this;
		}

		/*
		 * Sets the current side to move.
		 * 
		 * @param toMove: current side to move
		 */
		public Builder setSide(final Side toMove) {
			this.sideToMove = toMove;
			return this;
		}
		
		public Builder setHashValue(final long hash) {
			this.hash = hash;
			return this;
		}

		/*
		 * Returns resultant Board after Builder methods are called.
		 */
		public Board build() {
			return new Board(this); // creates immutable board based on builder
		}

		public void setEnPassantPawn(Pawn enPassantPawn) {
			this.enPassantPawn = enPassantPawn;
		}

	}

}
