package chess.engine.player;

import chess.engine.board.Board;
import chess.engine.board.Move;

/*
 * This class captures a "move transition," which contains the destination board after a move. When a move
 * is made, an instance of MoveTransition is created to tells us about the status of the move. This class
 * allows us to easily test, make, and undo potential moves without creating a bunch of unnecessary board states.
 */

public class MoveTransition {

	private final Board transitionBoard;
	private final Move move;
	private final MoveStatus moveStatus; // can do move

	public MoveTransition(final Board transitionBoard, final Move move, final MoveStatus moveStatus) {
		this.transitionBoard = transitionBoard;
		this.move = move;
		this.moveStatus = moveStatus;
	}

	public MoveStatus getMoveStatus() {
		return this.moveStatus;
	}

	public Board getBoard() {
		return this.transitionBoard;
	}

}
