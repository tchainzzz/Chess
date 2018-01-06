package chess.engine.player.ai;

import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import chess.engine.board.Board;
import chess.engine.board.Move;
import chess.gui.Table;
import chess.gui.Table.PlayerType;

public class AIThinker extends SwingWorker<Move, String> {
	
	public static final int SEARCH_DEPTH = 6;

//	private boolean instanceExists;

	public AIThinker() {
//		instanceExists = true;
	}

	/*
	 * Invokes move strategy algorithm on a new thread.
	 */

	@Override
	protected Move doInBackground() throws Exception {
//		if (!instanceExists) 
			final MoveStrategy algorithm = new MiniMax(SEARCH_DEPTH);
			final Move bestMove = algorithm.execute(Table.getInstance().getBoard());
			System.out.println("Selected move " + bestMove);
			return bestMove;
//		}
//		return Move.NULL_MOVE;
	}

	//TODO: fix find king when in check
	//TODO: castling out of check-o
	
	@Override
	public void done() {
		try {
//			if (instanceExists) {
				final Move bestMove = get();
				final Board newBoard = Table.getInstance().getBoard().currentPlayer().makeMove(bestMove).getBoard();
				Table.getInstance().updateComputerMove(bestMove);
				Table.getInstance()
				.updateGameBoard(newBoard);
				Table.getInstance().appendBoard(newBoard);
				Table.getInstance().getMoveLog().addMove(bestMove);
				Table.getInstance().getGameHistoryPanel().redo(Table.getInstance().getVisitedBoards(),
						Table.getInstance().getMoveLog());
				Table.getInstance().getTakenPiecesPanel().redo(Table.getInstance().getMoveLog());
				Table.getInstance().getBoardPanel().drawBoard(Table.getInstance().getBoard());
				Table.getInstance().moveMadeUpdate(PlayerType.COMPUTER);
//			}
//			instanceExists = false;
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

	}

}
