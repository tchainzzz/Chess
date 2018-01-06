package unittests;

import static org.junit.Assert.*;

import org.junit.Test;

import chess.engine.board.Board;
import chess.engine.board.BoardUtils;
import chess.engine.pieces.Piece;

public class BoardUtilsTests {

	@Test
	public void inBoard() {
		assertFalse(BoardUtils.inBoard(120));
		assertFalse(BoardUtils.inBoard(119));
		assertFalse(BoardUtils.inBoard(0));
		assertTrue(BoardUtils.inBoard(21));
		assertTrue(BoardUtils.inBoard(98));
		
		Board board = Board.createStandardBoard();
		for (int i = 8; i < 16; i++) {
			Piece p = board.getTile(i).getPiece();
			assertTrue(BoardUtils.canTwoSquare(p));
		}
		for (int i = 48; i < 56; i++) {
			Piece p = board.getTile(i).getPiece();
			assertTrue(BoardUtils.canTwoSquare(p));
			
		}
	}

}
