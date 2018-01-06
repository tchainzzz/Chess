package unittests;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;

import chess.engine.Side;
import chess.engine.board.Board;
import chess.engine.board.BoardUtils;
import chess.engine.board.Move;
import chess.engine.board.Tile;
import chess.engine.pieces.King;
import chess.engine.pieces.Piece;
import chess.engine.player.Player;

public class InitialBoardTest {

	Board board;

	@Before
	public void setUp() throws Exception {
		board = Board.createStandardBoard();
	}

	@Test
	public void testInitial() {
		Player white = board.currentPlayer();
		Player black = board.currentPlayer().getOpponent();

		// initial position - WHITE
		assertEquals(20, white.getLegalMoves().size());
		assertEquals(Side.WHITE, white.getSide());
		assertEquals(board.whitePlayer(), white);
		assertEquals(board.blackPlayer(), white.getOpponent());
		assertFalse(white.inCheck());
		assertFalse(white.inCheckmate());
		assertFalse(white.isCastled());
		assertFalse(white.inStalemate());
		assertEquals(16, board.getWhitePieces().size());
		// king test
		King whiteKing = white.getKing();
		assertEquals(60, whiteKing.getPosition());
		assertEquals(95, BoardUtils.board64[whiteKing.getPosition()]);
		assertFalse(whiteKing.hasMoved());
		assertEquals(Side.WHITE, whiteKing.getSide());

		// initial position - BLACK
		assertEquals(20, black.getLegalMoves().size());
		assertEquals(Side.BLACK, black.getSide());
		assertEquals(board.blackPlayer(), black);
		assertEquals(board.whitePlayer(), black.getOpponent());
		assertFalse(black.inCheck());
		assertFalse(black.inCheckmate());
		assertFalse(black.inStalemate());
		assertFalse(black.isCastled());
		assertEquals(16, board.getBlackPieces().size());

		// king test
		King blackKing = black.getKing();
		assertEquals(4, blackKing.getPosition());
		assertEquals(25, BoardUtils.board64[blackKing.getPosition()]);
		assertFalse(blackKing.hasMoved());
		assertEquals(Side.BLACK, blackKing.getSide());

		final Iterable<Piece> allPieces = board.getAllPieces();
		assertEquals(32, Iterables.size(allPieces));
		final Iterable<Move> allMoves = board.getAllLegalMoves();
		assertEquals(40, Iterables.size(allMoves));
		for (final Move move : allMoves) {
			assertFalse(move.isAttack());
			assertFalse(move.isCastlingMove());
		}

		for (final Piece piece : allPieces) {
			int size = piece.calculateLegalMoves(board).size();
			switch (piece.getType()) {
			case PAWN:
				assertEquals(2, size);
				break;
			case KNIGHT:
				assertEquals(2, size);
				break;
			case BISHOP:
				assertEquals(0, size);
				break;
			case ROOK:
				assertEquals(0, size);
				break;
			case QUEEN:
				assertEquals(0, size);
				break;
			case KING:
				assertEquals(0, size);
				break;
			default:
				fail("This is not a piece!");
				break;

			}
		}

		// empty tile tests
		for (int position = 16; position < 48; position++) {
			Tile current = board.getTile(position);
			assertFalse(current.tileIsOccupied());
			assertEquals(null, current.getPiece());
			assertEquals(position, current.getCoordinate());
		}
	}

}
