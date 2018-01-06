package unittests;

import static org.junit.Assert.*;

import org.junit.Test;

import chess.engine.Side;
import chess.engine.ZobristHash;
import chess.engine.board.Board;
import chess.engine.board.BoardUtils;
import chess.engine.board.Move;
import chess.engine.player.MoveTransition;
import chess.gui.Table;

public class ZobristHashTest {

	public static final int TEST_WHITE = 0;
	public static final int TEST_BLACK = 1;

	public static final int PAWN = 0;
	public static final int KNIGHT = 1;
	public static final int BISHOP = 2;
	public static final int ROOK = 3;
	public static final int QUEEN = 4;
	public static final int KING = 5;

	/*
	 * Make/undo regular move.
	 */

	@Test
	public void plainTest() {
		Board board = Board.createStandardBoard();
		ZobristHash.fillArray();
		long hash = ZobristHash.getZobristHash(board);
		assertEquals(board.currentPlayer().getSide(), Side.WHITE);

		Board test1 = Board.parseFEN("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1");
		assertEquals(test1.currentPlayer().getSide(), Side.BLACK);
		long hashTest1 = ZobristHash.getZobristHash(test1);

		// manual
		// [tile][side][piece]
		long manualHash = hash;
		manualHash ^= ZobristHash.zobristArray[BoardUtils.mailbox[85]][0][0]; 
		manualHash ^= ZobristHash.zobristArray[BoardUtils.mailbox[65]][0][0]; 
		manualHash ^= ZobristHash.zobristBlackMove;
		assertEquals(hashTest1, manualHash);

		// use method
		Move e2e4 = new Move.PawnMove(board, board.getTile(BoardUtils.mailbox[85]).getPiece(), 65);
		assertEquals(e2e4.getStart(), 85);
		assertEquals(e2e4.getDestination(), 65);

		long hashUpdated = ZobristHash.updateZobristHash(hash, e2e4);
		assertEquals(hashTest1, hashUpdated);

		// test undo
		Move e2e4undo = new Move.PawnMove(test1, test1.getTile(BoardUtils.mailbox[65]).getPiece(), 85);
		long hashUndoTest = ZobristHash.updateZobristHash(hashUpdated, e2e4undo);
		assertEquals(hashUndoTest, hash);
		
	}

	@Test
	public void captureTest() {
		Board scandinavian = Board.parseFEN("rnbqkbnr/ppp1pppp/8/3p4/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2");
		ZobristHash.fillArray();
		long hash = ZobristHash.getZobristHash(scandinavian);
		scandinavian.setInitialHashValue();
		System.out.println(scandinavian.getHash());
		
		Board copy =  Board.parseFEN("rnbqkbnr/ppp1pppp/8/3p4/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2");
		copy.setInitialHashValue();
		assertEquals(copy.getHash(), scandinavian.getHash());
		assertFalse(copy.getHash() == 0);
		assertFalse(scandinavian.getHash() == 0);
		assertEquals(scandinavian.currentPlayer().getSide(), Side.WHITE);

		// after exd5 capture
		Board scandiMain = Board.parseFEN("rnbqkbnr/ppp1pppp/8/3P4/8/8/PPPP1PPP/RNBQKBNR b KQkq - 0 2");
		long captureHash = ZobristHash.getZobristHash(scandiMain);
		assertEquals(scandiMain.currentPlayer().getSide(), Side.BLACK);

		// manual hash for board scandiMain
		long manualHash = hash;
		manualHash ^= ZobristHash.zobristArray[BoardUtils.mailbox[65]][0][0]; 
		manualHash ^= ZobristHash.zobristArray[BoardUtils.mailbox[54]][0][0]; 
		manualHash ^= ZobristHash.zobristArray[BoardUtils.mailbox[54]][1][0];
		manualHash ^= ZobristHash.zobristBlackMove;
		assertEquals(captureHash, manualHash);

		// computed hash for board scandiMain
		MoveTransition transition = scandinavian.currentPlayer().makeMove(Move.MoveFactory.createMove(scandinavian, 
				BoardUtils.getAt("e4"), BoardUtils.getAt("d5")));
		assertTrue(transition.getMoveStatus().isDone());
		assertTrue(transition.getBoard().toString().equals(scandiMain.toString()));
		long hashUpdated = transition.getBoard().getHash();
		assertEquals(ZobristHash.getZobristHash(transition.getBoard()), hashUpdated);
		assertEquals(captureHash, hashUpdated);

	}
	
	@Test
	public void mainImplementation() {
		Board board = Board.createStandardBoard();
		ZobristHash.fillArray();
		long hash = ZobristHash.getZobristHash(board);
		board.setInitialHashValue();
		
		final MoveTransition move1a = board.currentPlayer()
				.makeMove(Move.MoveFactory.createMove(board, BoardUtils.getAt("e2"), BoardUtils.getAt("e4")));

		assertTrue(move1a.getMoveStatus().isDone());
		
		Move e2e4 = new Move.PawnMove(board, board.getTile(BoardUtils.mailbox[85]).getPiece(), 65);
		
		assertTrue(e2e4.equals(Move.MoveFactory.createMove(board, BoardUtils.getAt("e2"), BoardUtils.getAt("e4"))));	
		assertEquals(move1a.getBoard().getHash(), ZobristHash.updateZobristHash(hash, e2e4));
		board = move1a.getBoard();
		Move e7e6 = Move.MoveFactory.createMove(board, 
				BoardUtils.getAt("e7"), BoardUtils.getAt("e6"));
		final MoveTransition move1b = board.currentPlayer().makeMove(e7e6);
		assertTrue(move1b.getMoveStatus().isDone());
		assertEquals(move1b.getBoard().getHash(), ZobristHash.updateZobristHash(
				board.getHash(), e7e6));
	}
	//TODO: handle all special moves' Zobrist recalculation
	//TODO: handle move-switch sides

}
