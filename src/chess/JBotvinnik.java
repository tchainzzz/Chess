package chess;

import chess.engine.ZobristHash;
import chess.engine.board.Board;
import chess.engine.player.ai.MiniMax;
import chess.engine.player.ai.MoveStrategy;
import chess.gui.Table;

/*
 * This is the GUI of the chess engine.
 */

public class JBotvinnik {

	public static void main(String[] args) {

		ZobristHash.fillArray();
		Board board = Board.createStandardBoard();
		
		
		System.out.println(board);

		Table.getInstance().show();

	}

}
