package unittests;

import static org.junit.Assert.*;

import org.junit.Test;

import chess.engine.Side;
import chess.engine.ZobristHash;
import chess.engine.board.Board;
import chess.engine.board.BoardUtils;
import chess.engine.board.Move;
import chess.engine.player.MoveTransition;
import chess.engine.player.Player;

public class TileAttacks {

	@Test
	public void test() {
		Board board = Board.createStandardBoard();
		ZobristHash.fillArray();
		long hash = ZobristHash.getZobristHash(board);
		
		for (int i = 0; i < 64; i++) 
			assertTrue(board.currentPlayer().attacksOnTile(i, board.currentPlayer().getLegalMoves()).isEmpty());
		//no attacks on tile in initial position
		
		//1. e4 e5 2. Nf3 Nf6 3. Nc3 Be7 4. Be2 d5 5. Bb5+
		final MoveTransition move1a = board.currentPlayer()
				.makeMove(Move.MoveFactory.createMove(board, BoardUtils.getAt("e2"), BoardUtils.getAt("e4")));
		final MoveTransition move1b = move1a.getBoard().currentPlayer()
				.makeMove(Move.MoveFactory.createMove(move1a.getBoard(), BoardUtils.getAt("e7"), BoardUtils.getAt("e5")));
		final MoveTransition move2a = move1b.getBoard().currentPlayer()
				.makeMove(Move.MoveFactory.createMove(move1b.getBoard(), BoardUtils.getAt("g1"), BoardUtils.getAt("f3")));
		final MoveTransition move2b = move2a.getBoard().currentPlayer()
				.makeMove(Move.MoveFactory.createMove(move2a.getBoard(), BoardUtils.getAt("g8"), BoardUtils.getAt("f6")));
		final MoveTransition move3a = move2b.getBoard().currentPlayer()
				.makeMove(Move.MoveFactory.createMove(move2b.getBoard(), BoardUtils.getAt("b1"), BoardUtils.getAt("c3")));
		final MoveTransition move3b = move3a.getBoard().currentPlayer()
				.makeMove(Move.MoveFactory.createMove(move3a.getBoard(), BoardUtils.getAt("f8"), BoardUtils.getAt("e7")));
		final MoveTransition move4a = move3b.getBoard().currentPlayer()
				.makeMove(Move.MoveFactory.createMove(move3b.getBoard(), BoardUtils.getAt("f1"), BoardUtils.getAt("e2")));
		final MoveTransition move4b = move4a.getBoard().currentPlayer()
				.makeMove(Move.MoveFactory.createMove(move4a.getBoard(), BoardUtils.getAt("d7"), BoardUtils.getAt("d5")));
		final Move check = Move.MoveFactory.createMove(move4b.getBoard(), BoardUtils.getAt("e2"), BoardUtils.getAt("b5"));
		final MoveTransition move5a = move4b.getBoard().currentPlayer()
				.makeMove(check);
		Player player = move5a.getBoard().currentPlayer();
		assertTrue(player.getSide() == Side.BLACK);
		assertTrue(player == move5a.getBoard().currentPlayer());
		assertTrue(move5a.getMoveStatus().isDone());
		assertTrue(player.inCheck());
		
		assertTrue(check.toString()
				.equals("Bb5+"));
		assertFalse(player.attacksOnTile(player.getKing().getPosition(), player.getOpponent().getLegalMoves()).isEmpty());

	}

}
