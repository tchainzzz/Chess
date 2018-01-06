package chess.engine.player.ai;

import chess.engine.board.Board;
import chess.engine.board.Move;

/*
 * Move search strategy interface.
 */

public interface MoveStrategy {

	Move execute(Board board);

}
