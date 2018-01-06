package chess.engine.player.ai;

import java.util.Comparator;

import com.google.common.collect.ComparisonChain;

import chess.engine.ZobristHash;
import chess.engine.board.Board;
import chess.engine.board.Move;
import chess.gui.Table;

class MoveSorter {
	public static Comparator<Move> CACHED_SORT = new Comparator<Move>() {
		@Override
		//<board resulting from move, score?
		public int compare(final Move move1, final Move move2) {
			 final Board board1 = move1.execute();
			 final Board board2 = move2.execute();
			 //get cached scores
		     int key1 = (int) ((board1.getHash() % ZobristHash.DEFAULT_TABLE_SIZE) + ZobristHash.DEFAULT_TABLE_SIZE)
						% ZobristHash.DEFAULT_TABLE_SIZE;
		     int key2 = (int) ((board2.getHash() % ZobristHash.DEFAULT_TABLE_SIZE) + ZobristHash.DEFAULT_TABLE_SIZE)
						% ZobristHash.DEFAULT_TABLE_SIZE;
			 int score1 = Table.SEEN_POSITIONS.containsKey(key1) ? Table.SEEN_POSITIONS.get(key1) : Integer.MIN_VALUE;
			 int score2 = Table.SEEN_POSITIONS.containsKey(key2) ? Table.SEEN_POSITIONS.get(key2) : Integer.MIN_VALUE;
			 return ComparisonChain.start().compare(score2, score1).result();
		}
	};
}
