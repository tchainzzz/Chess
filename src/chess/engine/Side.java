package chess.engine;

import chess.engine.player.BlackPlayer;
import chess.engine.player.Player;
import chess.engine.player.WhitePlayer;

public enum Side {
	WHITE {
		@Override
		public int getDirection() {
			return -1;
		}

		@Override
		public boolean isWhite() {
			return true;
		}

		@Override
		public boolean isBlack() {
			return false;
		}

		@Override
		public Player choosePlayer(WhitePlayer whiteplayer, BlackPlayer blackplayer) {
			return whiteplayer;
		}

		@Override
		public boolean isPromotionSquare(int position) {
			return position / 8 == 0; // on 8th rank?
		}

		@Override
		public String toString() {
			return "White";
		}

		@Override
		public int getEnum() {
			return 0;
		}

	},
	BLACK {
		@Override
		public int getDirection() {
			return 1;
		}

		@Override
		public boolean isWhite() {
			return false;
		}

		@Override
		public boolean isBlack() {
			return true;
		}

		@Override
		public Player choosePlayer(WhitePlayer whiteplayer, BlackPlayer blackplayer) {
			return blackplayer;
		}

		@Override
		public boolean isPromotionSquare(int position) {
			return position / 8 == 7; // on 1st rank?
		}

		@Override
		public String toString() {
			return "Black";
		}

		@Override
		public int getEnum() {
			return 1;
		}
	};

	public abstract int getDirection();

	public abstract boolean isWhite();

	public abstract boolean isBlack();

	public abstract boolean isPromotionSquare(int position);

	public abstract Player choosePlayer(WhitePlayer whiteplayer, BlackPlayer blackplayer);

	public abstract int getEnum();
}
