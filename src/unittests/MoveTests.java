package unittests;

import static org.junit.Assert.*;

import org.junit.Test;

import chess.engine.board.Board;
import chess.engine.board.BoardUtils;
import chess.engine.board.Move;

public class MoveTests {

	@Test
	public void testEquals() {
		Board test = Board.createStandardBoard();
		Move m1 = new Move.QuietMove(test, test.getTile(BoardUtils.mailbox[85]).getPiece(), 65);
		Move m2 = new Move.QuietMove(test, test.getTile(BoardUtils.mailbox[85]).getPiece(), 65);
		assertTrue(m1.equals(m2));
	}

}
