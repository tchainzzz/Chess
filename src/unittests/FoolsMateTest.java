package unittests;

import static org.junit.Assert.*;

import org.junit.Test;

import chess.engine.ZobristHash;
import chess.engine.board.Board;
import chess.engine.board.BoardUtils;
import chess.engine.board.Move;
import chess.engine.player.MoveTransition;
import chess.engine.player.ai.MiniMax;
import chess.engine.player.ai.MoveStrategy;
import chess.gui.Table;

public class FoolsMateTest {

	@Test/*(timeout = 100000)*/
	public void testAI() {
		final Board board = Board.createStandardBoard();
		ZobristHash.fillArray();
		long hash = ZobristHash.getZobristHash(board);
		board.setInitialHashValue();
		

		final MoveStrategy moveStrategy = new MiniMax(8);
		final Move move = moveStrategy.execute(board);
		System.out.println("Selected move " + move);
	}

	@Test
	public void testFoolsMate() {
		//these four lines of code are as of now idiomatic. I am trying to find a way to simplify this.
		final Board board = Board.createStandardBoard();
		ZobristHash.fillArray();
		long hash = ZobristHash.getZobristHash(board);
		board.setInitialHashValue();
		
		final MoveTransition move1a = board.currentPlayer()
				.makeMove(Move.MoveFactory.createMove(board, BoardUtils.getAt("f2"), BoardUtils.getAt("f3")));

		assertTrue(move1a.getMoveStatus().isDone());

		final MoveTransition move1b = move1a.getBoard().currentPlayer().makeMove(
				Move.MoveFactory.createMove(move1a.getBoard(), BoardUtils.getAt("e7"), BoardUtils.getAt("e5")));
		assertTrue(move1b.getMoveStatus().isDone());

		final MoveTransition move2a = move1b.getBoard().currentPlayer().makeMove(
				Move.MoveFactory.createMove(move1b.getBoard(), BoardUtils.getAt("g2"), BoardUtils.getAt("g4")));
		assertTrue(move2a.getMoveStatus().isDone());

		final MoveStrategy strategy = new MiniMax(4);
		final Move aiMove = strategy.execute(move2a.getBoard());

		final Move best = Move.MoveFactory.createMove(move2a.getBoard(), BoardUtils.getAt("d8"),
				BoardUtils.getAt("h4"));
		final MoveTransition move2b = move2a.getBoard().currentPlayer().makeMove(best);
		assertTrue(move2b.getMoveStatus().isDone());

		final MoveTransition illegal = move2b.getBoard().currentPlayer().makeMove(
				Move.MoveFactory.createMove(move2b.getBoard(), BoardUtils.getAt("a2"), BoardUtils.getAt("a4")));
		assertFalse(illegal.getMoveStatus().isDone());

		System.out.println("Selected move " + aiMove);
		assertEquals(best, aiMove);
	}

}
