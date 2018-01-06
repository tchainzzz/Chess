package chess.engine.player.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;

import com.google.common.collect.ImmutableList;

import chess.engine.Side;
import chess.engine.board.Board;
import chess.engine.board.Move;
import chess.engine.board.Move.MoveFactory;
import chess.engine.player.MoveTransition;
import chess.engine.player.Player;
import chess.gui.Table;

public class MiniMax extends Observable implements MoveStrategy {

	private final BoardEvaluator boardEvaluator;
	private long boardsEvaluated;
	private int evaldepth;
	private int nodesCached;
	private static Move[][] principalVariation;
	
	private static final int WINDOW = 100;

	public MiniMax(final int depth) {
		this.boardEvaluator = new StandardBoardEvaluator();
		this.evaldepth = depth;
		this.boardsEvaluated = 0;
		this.nodesCached = 0;
		this.principalVariation = triangularArray(this.evaldepth);

	}

	private static Move[][] triangularArray(int depth) {
		Move[][] temp = new Move[depth][];
		for (int i = 0; i < depth; i++) {
			temp[i] = new Move[i + 1];
		}
		return temp;
	}

	@Override
	public String toString() {
		return "Minimax";
	}

	public long getBoardsEvaluted() {
		return this.boardsEvaluated;
	}

	public void incrementNodesCached() {
		this.nodesCached++;
	}

	public int getNodesCached() {
		return this.nodesCached;
	}

	/*
	 * Wrapper function for recursive minimax search.
	 * TODO: aspiration windows, quiesence search via iterative deepening, PV search
	 * possible future TODO: null move pruning via zugzwang heuristic, futility pruning (max possible eval change)
	 */

	@Override
	public Move execute(Board board) {
		final long startTime = System.currentTimeMillis();
		final Player currentPlayer = board.currentPlayer();
		final Side currentSide = board.currentPlayer().getSide();
		Move best = MoveFactory.getNullMove();
		int alpha = Integer.MIN_VALUE;
		int beta = Integer.MAX_VALUE;
		System.out.println("BOTVinnik is thinking using MINIMAX with depth = " + evaldepth);


		int counter = 1;
		//TODO: iterative deepening to populate tables
		Collection<Move> sorted = sort(currentPlayer.getLegalMoves(), board);
		
		int numMoves = sorted.size();
		for (final Move move : sorted) {
			final MoveTransition moveTransition = currentPlayer.makeMove(move);	
			this.boardsEvaluated++;
			System.out.println("\t(" + this.toString() + ") Analyzing move: " + move + " (" + counter + "/" + numMoves + ")");
			int current = currentSide.isWhite() ? min(moveTransition.getBoard(), this.evaldepth - 1, alpha, beta)
					: max(moveTransition.getBoard(), this.evaldepth - 1, alpha, beta);
			if (currentSide.isWhite() && current > alpha) {
				alpha = current;
				best = move;
				if (moveTransition.getBoard().blackPlayer().inCheckmate())
					break;
			} else if (currentSide.isBlack() && current < beta) {
				beta = current;
				best = move;
				if (moveTransition.getBoard().whitePlayer().inCheckmate())
					break;
			}

			if (counter > numMoves) throw new RuntimeException("# of analyzed moves exceeds # of legal moves!");
			counter++;
			setChanged();
			notifyObservers();

		}

		
		final long executionTime = System.currentTimeMillis() - startTime;
		System.out.println("Time taken to find best move: " + executionTime + "ms");
		System.out.println("# of nodes evaluated: " + this.getBoardsEvaluted());
		System.out.println("# of cached positions retreived: " + this.getNodesCached());
		System.out.println("# of collisions: " + Table.getCollisions());
		System.out.println("Final move score: " + (double) (currentSide.isWhite() ? alpha / 100.0 : beta / 100.0));
		System.out.println("Final PV: " + Arrays.toString(principalVariation));
		return best;
	}
	

	private static String score(final Player current, final int highest, final int lowest) {
		if (current.getSide().isWhite()) {
			return "[score: " + highest + "]";
		} else if (current.getSide().isBlack()) {
			return "[score: " + lowest + "]";
		}
		throw new RuntimeException("Invalid color!");
	}

	/*
	 * Co-recursive minimax implementation.
	 * 
	 * @param alpha: best move evaluation must be better than alpha - maximum
	 * lower bound
	 * @param beta: best move evaluation must be worse than beta - minimum upper
	 * bound
	 */

	public int min(final Board board, final int depth, int alpha, int beta) {
		this.boardsEvaluated++;
		if (depth == 0 || gameOver(board)) {	
			long key = board.getHash();
			if (Table.SEEN_POSITIONS.containsKey(key)) {
				incrementNodesCached();
				return Table.SEEN_POSITIONS.get(key);
			} else {
				return this.boardEvaluator.evaluate(board, 1);
			}

		}
		int lowest = beta; //This is Integer.MAX_VALUE in the first call.
		Collection<Move> sorted = sort(board.currentPlayer().getLegalMoves(), board);

		for (final Move move : sorted) {
			final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
			//lowest = Math.min(lowest, max(moveTransition.getBoard(), depth - 1, alpha, lowest)); 
			int score = max(moveTransition.getBoard(), depth - 1, alpha, lowest);
			if (score < lowest) {
				lowest = score;
			}
			if (alpha >= lowest) { //fail-low: less than the maximum lower bound — player can avoid this	
				return alpha; 
			}
		}
		return lowest;
		//move "backed-up" : found a good tangible move
	}




	public int max(final Board board, final int depth, int alpha, int beta) {
		this.boardsEvaluated++;
		if (depth == 0 || gameOver(board)) {		
			long key = board.getHash();
			if (Table.SEEN_POSITIONS.containsKey(key)) {
				incrementNodesCached();
				return Table.SEEN_POSITIONS.get(key);
			} else {
				return this.boardEvaluator.evaluate(board, depth);
			}
		}

		int highest = alpha; //This is Integer.MIN_VALUE in the first call.
		Collection<Move> sorted = sort(board.currentPlayer().getLegalMoves(), board);

		for (final Move move : sorted) {
			final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
			//highest = Math.max(highest, min(moveTransition.getBoard(), depth - 1, highest, beta)); 
			int score = min(moveTransition.getBoard(), depth - 1, highest, beta);
			if (score > highest) {
				highest = score;
			}
			if (beta <= highest) {//fail high: passed the minimum upper bound — opponent can avoid this
				return beta; 
				
			}
		}
		//move "backed-up" : found a good tangible move
		return highest;
	}

	//TODO: killer heuristic, history heuristic, countermove, captures (SEE) and potential captures (MVV/LVA)
	/*
	 * new sort order TODO: 
	 * 1. PV
	 * 2. captures & promotions 
	 * 2.1 (MVV+LVA) via Static Exchange Eval (SEE) if (MVV/LVA < 0)
	 * 2.2 if (SEE < 0) goto 6
	 * 3. Killer moves (beta-cutoff @ this depth)
	 * 4. non-captures — sort by (refutationTable.contains()?)
	 * 5. all other non-captures (derive from move history + countermove?)
	 * 6. losing captures
	 */
	private Collection<Move> sort(final Collection<Move> legalMoves, final Board board) {
		int score;
		List<Move> sortedMoves = new ArrayList<>();
		HashMap<Move, Integer> moveScoreTable = new HashMap<>();
		for (Move m : legalMoves) {
			final MoveTransition transition = board.currentPlayer().makeMove(m);
			if (transition.getMoveStatus().isDone()) {
				if (Table.SEEN_POSITIONS.containsKey(transition.getBoard().getHash())) {
					score = Table.SEEN_POSITIONS.get(transition.getBoard().getHash());
				} else {
					score = this.boardEvaluator.evaluate(board, 1);					
				}				
				moveScoreTable.put(m, score);
				if (sortedMoves.isEmpty()) {
					sortedMoves.add(m);
				} else { //perform insertion sort by score
					if (score >= moveScoreTable.get(sortedMoves.get(sortedMoves.size() - 1))) {
						sortedMoves.add(m);
					} else {
						for (int i = 0; i < sortedMoves.size(); i++) {				
							if (score < moveScoreTable.get(sortedMoves.get(i))) {
								sortedMoves.add(i, m); //add before index i
								break;
							}

						}
					}
				}
			}

		}
		if (board.currentPlayer().getSide().isWhite()) Collections.reverse(sortedMoves);
		return ImmutableList.copyOf(sortedMoves);
	}


	boolean gameOver(final Board board) {
		return board.currentPlayer().inCheckmate() || board.currentPlayer().inStalemate();

	}

}
