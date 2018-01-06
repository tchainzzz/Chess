package chess.engine.player.ai;

import java.util.Observable;

import chess.engine.board.Board;
import chess.engine.board.Move;

public class GrandPatzer extends Observable implements MoveStrategy {

	/*
	 * This strategy attempts to emulate an amateur's thinking process in chess (a.k.a. mine) to determine
	 * logical moves dynamically from a list of candidate moves.
	 * 
	 * Emulating this process includes a special sort in which moves likely to be seen as useful (less quiet moves)
	 * are investigated first at a low depth of 2 - 4 via minimax.
	 * 
	 * The utility of moves are in this arbitrary order:
	 * Checkmate 
	 * Protects a hanging piece
	 * Check
	 * Attacks a piece
	 * Random pawn move (if it doesn't hang a piece)
	 * All other moves
	 */
	
	@Override
	public Move execute(Board board) {
		// TODO Auto-generated method stub
		return null;
	}

}
