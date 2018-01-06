package chess.engine.player.ai;

import chess.engine.Side;
import chess.engine.ZobristHash;
import chess.engine.board.Board;
import chess.engine.board.BoardUtils;
import chess.engine.board.Move;
import chess.engine.pieces.Piece;
import chess.engine.pieces.Piece.PieceType;
import chess.engine.player.Player;
import chess.gui.Table;
import chess.engine.player.ai.MiniMax;

public final class StandardBoardEvaluator implements BoardEvaluator {

	private static final int CHECK_BONUS = 10;
	private static final int CHECKMATE_BONUS = 20000;
	private static final int DEPTH_BONUS = 100;
	private static final int CASTLE_BONUS = 60;
	private static final int DEVELOPMENT_BONUS = 10;

	//reverse for black: same file, reversed rank order
	private static final int[] PAWN_POSITION_SCORES =
		  { 0,  0,  0,  0,  0,  0,  0,  0,
			50, 50, 50, 50, 50, 50, 50, 50,
			10, 10, 20, 30, 30, 20, 10, 10,
			5,  5, 10, 25, 25, 10,  5,  5,
			0,  0,  0, 20, 20,  0,  0,  0,
			5, -5,-10,  0,  0,-10, -5,  5,
			5, 10, 10,-20,-20, 10, 10,  5,
			0,  0,  0,  0,  0,  0,  0,  0 };
	
	private static final int[] KNIGHT_POSITION_SCORES =
		  { -50,-40,-30,-30,-30,-30,-40,-50,
			-40,-20,  0,  0,  0,  0,-20,-40,
			-30,  0, 10, 15, 15, 10,  0,-30,
			-30,  5, 15, 20, 20, 15,  5,-30,
			-30,  0, 15, 20, 20, 15,  0,-30,
			-30,  5, 10, 15, 15, 10,  5,-30,
			-40,-20,  0,  5,  5,  0,-20,-40,
			-50,-40,-30,-30,-30,-30,-40,-50};
	
	private static final int[] BISHOP_POSITION_SCORES =
		  { -20,-10,-10,-10,-10,-10,-10,-20,
		    -10,  0,  0,  0,  0,  0,  0,-10,
		    -10,  0,  5, 10, 10,  5,  0,-10,
		    -10,  5,  5, 10, 10,  5,  5,-10,
		    -10,  0, 10, 10, 10, 10,  0,-10,
		    -10, 10, 10, 10, 10, 10, 10,-10,
		    -10,  5,  0,  0,  0,  0,  5,-10,
		    -20,-10,-10,-10,-10,-10,-10,-20};

	private static final int[] ROOK_POSITION_SCORES =
		 { 0,  0,  0,  0,  0,  0,  0,  0,
		   5, 10, 10, 10, 10, 10, 10,  5,
		  -5,  0,  0,  0,  0,  0,  0, -5,
		  -5,  0,  0,  0,  0,  0,  0, -5,
		  -5,  0,  0,  0,  0,  0,  0, -5,
		  -5,  0,  0,  0,  0,  0,  0, -5,
		  -5,  0,  0,  0,  0,  0,  0, -5,
		   0,  0,  0,  5,  5,  0,  0,  0 };

	private static final int[] QUEEN_POSITION_SCORES =
		 { -20,-10,-10, -5, -5,-10,-10,-20,
		   -10,  0,  0,  0,  0,  0,  0,-10,
		   -10,  0,  5,  5,  5,  5,  0,-10,
		   -5,  0,  5,  5,  5,  5,  0, -5,
		    0,  0,  5,  5,  5,  5,  0, -5,
		   -10,  5,  5,  5,  5,  5,  0,-10,
		   -10,  0,  5,  0,  0,  0,  0,-10,
		   -20,-10,-10, -5, -5,-10,-10,-20};
	
	private static final int[] KING_MIDGAME_SCORES =
		 { -30,-40,-40,-50,-50,-40,-40,-30,
		   -30,-40,-40,-50,-50,-40,-40,-30,
		   -30,-40,-40,-50,-50,-40,-40,-30,
		   -30,-40,-40,-50,-50,-40,-40,-30,
		   -20,-30,-30,-40,-40,-30,-30,-20,
		   -10,-20,-20,-20,-20,-20,-20,-10,
		    20, 20,  0,  0,  0,  0, 20, 20,
		    20, 30, 10,  0,  0, 10, 30, 20};
	
	private static final int[] KING_ENDGAME_SCORES =
		{ -50,-40,-30,-20,-20,-30,-40,-50,
			-30,-20,-10,  0,  0,-10,-20,-30,
			-30,-10, 20, 30, 30, 20,-10,-30,
			-30,-10, 30, 40, 40, 30,-10,-30,
			-30,-10, 30, 40, 40, 30,-10,-30,
			-30,-10, 20, 30, 30, 20,-10,-30,
			-30,-30,  0,  0,  0,  0,-30,-30,
			-50,-30,-30,-30,-30,-30,-30,-50};

	// 35, 36, 43, 44 are the coordinates of the center squares
	// 26, 27, 28, 29, 34, 37, 42, 45, 50, 51, 52, 53 are less important center
	// squares
	private static final int[] TRUECENTER_POSITIONS = { 35, 36, 43, 44 };
	private static final int[] CENTER_POSITIONS = { 26, 27, 28, 29, 34, 37, 42, 45, 50, 51, 52, 53 };
	
	public int quickEvaluate(Board board) {
		return pieceValue(board.whitePlayer())
				+ centerControl(board.whitePlayer(), board)
				- pieceValue(board.blackPlayer())
				- centerControl(board.blackPlayer(), board);
	}
	
	@Override
	public int evaluate(final Board board, final int depth) {
		return scorePlayer(board, board.whitePlayer(), depth) - scorePlayer(board, board.blackPlayer(), depth);
	}

	private int scorePlayer(final Board board, final Player player, final int depth) {
		return pieceValue(player)
				+ mobility(player)
				+ centerControl(player, board)
				+ attacks(player)
				+ check(player) 
				+ checkmate(player, depth) 
				+ castled(player)	
				+ queenMovePenalty()
				/*+ development(player, board)
				+ pairRewards(player)*/;
	}

	private static int queenMovePenalty() {
		return Table.getInstance().getNumMoves() <= 12 ? -50 : 0;
	}

	private int pairRewards(final Player player) {
		int bishopCount = 0;
		int knightCount = 0;
		for (final Piece piece : player.getActivePieces()) {
			if (piece.getType() == PieceType.KNIGHT)
				knightCount++;
			if (piece.getType() == PieceType.BISHOP)
				bishopCount++;
		}
		return bishopCount * 25 - knightCount * 25;
	}

	private int attacks(Player player) {
		int total = 0;
		for (final Move move : player.getLegalMoves()) {
			if (move.isAttack()) total++;
		}
		return total * 2;
	}

	private static int pieceValue(Player player) {
		int totalValue = 0;
		for (final Piece piece : player.getActivePieces()) {
			totalValue += piece.getValue();
		}
		return totalValue;
	}

	private static int mobility(Player player) {
		return 2 * (player.mobility() - player.getOpponent().mobility());
	}

	private static int check(Player player) {
		return player.getOpponent().inCheck() ? CHECK_BONUS : 0;
	}

	private static int checkmate(Player player, int depth) {
		return player.getOpponent().inCheckmate() ? CHECKMATE_BONUS * depthBonus(depth) : 0;
	}

	private static int depthBonus(int depth) {
		return depth == 0 ? 1 : DEPTH_BONUS * 100;
	}

	private static int castled(Player player) {
		if (Table.getInstance().getNumMoves() <= 6) return 0;
		return player.isCastled() ? CASTLE_BONUS : 0;
	}

	//TODO: reimplement using static position-score arrays
	private static int centerControl(final Player player, final Board board) {
		int controlScore = 0;
		for (Piece p : player.getActivePieces()) {
			int index = convertIndex(p.getPosition(), player.getSide());
			controlScore += positionScore(p, index, board);
		}
		return 2 * controlScore;
	}
	
	private static int positionScore(Piece p, int index, Board board) {
		switch (p.getType()) {
		case PAWN:
			return PAWN_POSITION_SCORES[index];
		case KNIGHT:
			return KNIGHT_POSITION_SCORES[index];
		case BISHOP:
			return BISHOP_POSITION_SCORES[index];
		case ROOK:
			return ROOK_POSITION_SCORES[index];
		case QUEEN:
			return queenMovePenalty() != 0 ? 0 : QUEEN_POSITION_SCORES[index];
		case KING:
			return inEndgame(board) ? KING_ENDGAME_SCORES[index] : KING_MIDGAME_SCORES[index];
		default:
			throw new RuntimeException("Piece type not recognized!");
		}
	}

	private static int convertIndex(int index, Side side) {
		return side.isWhite() ? index : 8 * (7  - index / 8) + index % 8;
	}

	private static int development(final Player player, final Board board) {
		// search tiles 0-7 for black, tiles 56-63 for white
		int tileOffset = player.getSide().isWhite() ? 56 : 0;
		int bonus = 0;
		for (int tile = tileOffset; tile < tileOffset + BoardUtils.TILES_PER_ROW; tileOffset++) {
			if (!board.getTile(tile).tileIsOccupied())
				bonus += DEVELOPMENT_BONUS;
		}
		return bonus;
	}

	private static boolean inEndgame(final Board board) {
		boolean queenFlag = false;
		boolean pieceFlag = false;
		for (Piece p : board.getWhitePieces()) {
			if (p.getType() == PieceType.QUEEN) {
				queenFlag = true;
			} else if (p.getType() != PieceType.PAWN && p.getType() != PieceType.KING) {
				pieceFlag = true;
			} 
			
			if (queenFlag && pieceFlag) {
				if (p.getType() != PieceType.PAWN && p.getType() != PieceType.KING) {
					return false;
				}
			}
		}
		queenFlag = false;
		pieceFlag = false;
		for (Piece p : board.getBlackPieces()) {
			if (p.getType() == PieceType.QUEEN) {
				queenFlag = true;
			} else if (p.getType() != PieceType.PAWN && p.getType() != PieceType.KING) {
				pieceFlag = true;
			} 
			
			if (queenFlag && pieceFlag) {
				if (p.getType() != PieceType.PAWN && p.getType() != PieceType.KING) {
					return false;
				}
			}
		}
		return true;
	}

}
