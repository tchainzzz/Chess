package chess.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import chess.engine.ZobristHash;
import chess.engine.board.Board;
import chess.engine.board.BoardUtils;
import chess.engine.board.Move;
import chess.engine.board.Move.PawnPromotion;
import chess.engine.board.Tile;
import chess.engine.pieces.Piece;
import chess.engine.pieces.Piece.PieceType;
import chess.engine.player.MoveStatus;
import chess.engine.player.MoveTransition;
import chess.engine.player.Player;
import chess.engine.player.ai.AIThinker;

public class Table extends Observable {

	private final JFrame gameFrame;
	private final MoveHistory history;
	private final TakenPiecesPanel taken;
	private final BoardPanel boardPanel;
	private final MoveLog movelog;
	private final SetupMenu setupMenu;
	private Board chessboard;
	private Move lastMove; //TODO: implement
	private Move computerMove; //TODO: implement
	private List<Board> previousPositions;
	private long currentHash;
	private int numMoves;
	private static int collisions;

	private Tile startTile;
	private Tile endTile;
	private Piece humanMovedPiece;
	private BoardDirection boardDirection;

	private boolean highlightLegalMoves = true;

	public static final Color LIGHT_COLOR = Color.decode("#f4f4d2");
	public static final Color DARK_COLOR = Color.decode("#296a24");

	public static final String RES_FOLDER = "res/pieces/";

	private final Dimension OUTER_FRAME_DIMENSION = new Dimension(920, 720);
	private final Dimension BOARD_PANEL_DIMENSION = new Dimension(400, 400);
	private final Dimension TILE_PANEL_DIMENSION = new Dimension(10, 10);
	private final int TILE_SIZE = 60;
	private final int HIGHLIGHT_OFFSET = 18;

	public static HashMap<Long, Integer> SEEN_POSITIONS = new HashMap<>();

	private static final Table INSTANCE = new Table();

	private Table() {
		this.gameFrame = new JFrame("BOTVinnik 0.0");
		this.gameFrame.setLayout(new BorderLayout());
		this.gameFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		final JMenuBar menuBar = createMenuBar();
		this.gameFrame.setJMenuBar(menuBar);
		this.gameFrame.setSize(OUTER_FRAME_DIMENSION);
		this.gameFrame.setResizable(false);

		this.chessboard = Board.createStandardBoard();
		this.chessboard.setInitialHashValue();

		this.history = new MoveHistory();
		this.movelog = new MoveLog();
		this.taken = new TakenPiecesPanel();
		this.setupMenu = new SetupMenu(this.gameFrame, true);
		this.lastMove = null;
		this.numMoves = 0;

		this.boardPanel = new BoardPanel();
		this.boardDirection = BoardDirection.NORMAL;
		this.gameFrame.add(this.taken, BorderLayout.WEST);
		this.gameFrame.add(this.boardPanel, BorderLayout.CENTER);
		this.gameFrame.add(this.history, BorderLayout.EAST);

		this.addObserver(new AIWatcher());
		this.previousPositions = new ArrayList<>();
		this.previousPositions.add(chessboard);
		this.currentHash = ZobristHash.getZobristHash(this.chessboard);

		this.gameFrame.setVisible(true);

	}

	public static Table getInstance() {
		return INSTANCE;
	}

	public void show() {
		Table.getInstance().getMoveLog().clear();
		Table.getInstance().getGameHistoryPanel().redo(previousPositions, Table.getInstance().getMoveLog());
		Table.getInstance().getTakenPiecesPanel().redo(Table.getInstance().getMoveLog());
		Table.getInstance().getBoardPanel().drawBoard(Table.getInstance().getBoard());
	}

	private JMenuBar createMenuBar() {
		final JMenuBar menuBar = new JMenuBar();
		menuBar.add(createFileMenu());
		menuBar.add(createGameMenu());
		menuBar.add(createPreferencesMenu());
		return menuBar;
	}

	private JMenu createFileMenu() {
		final JMenu fileMenu = new JMenu("File");
		final JMenuItem openPGN = new JMenuItem("Load PGN File...");
		openPGN.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Opening PGN file...");
			}
		});
		fileMenu.add(openPGN);

		final JMenuItem reset = new JMenuItem("Reset");
		reset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				chessboard = Board.createStandardBoard();
				history.clearModel();
				taken.clearPanel();
				boardPanel.drawBoard(chessboard);
			}
		});
		fileMenu.add(reset);

		fileMenu.addSeparator();

		final JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		fileMenu.add(exitItem);

		return fileMenu;
	}

	private JMenu createGameMenu() {
		final JMenu gameMenu = new JMenu("Game");

		final JMenuItem setup = new JMenuItem("Options");
		setup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Table.getInstance().getSetupMenu().promptUser();
				Table.getInstance().setupUpdate(Table.getInstance().getSetupMenu());

			}
		});
		gameMenu.add(setup);

		final JMenuItem takeback = new JMenuItem("Takeback move");
		takeback.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (lastMove != null) {
					// chessboard = lastMove.unmake();
					// history.removeLast();
					// if (lastMove.getAttackedPiece() != null) {
					// deal with taken pieces panel
					// }
					// boardPanel.drawBoard(chessboard);
				}
				// just return last board state
			}
		});
		gameMenu.add(takeback);

		return gameMenu;
	}

	private JMenu createPreferencesMenu() {
		final JMenu preferences = new JMenu("Preferences");
		final JMenuItem flip = new JMenuItem("Flip Board");
		flip.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				boardDirection = boardDirection.opposite();
				boardPanel.drawBoard(chessboard);
			}
		});
		preferences.add(flip);

		preferences.addSeparator();

		final JCheckBoxMenuItem legalHighlight = new JCheckBoxMenuItem("Highlight Legal Moves", false);
		legalHighlight.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				highlightLegalMoves = legalHighlight.isSelected();
			}

		});
		preferences.add(legalHighlight);

		return preferences;
	}

	private SetupMenu getSetupMenu() {
		return this.setupMenu;
	}

	public Board getBoard() {
		return this.chessboard;
	}
	
	public List<Board> getVisitedBoards() {
		return ImmutableList.copyOf(this.previousPositions);
	}
	
	public void appendBoard(Board newBoard) {
		this.previousPositions.add(newBoard);
	}

	/*
	 * Notifies AI player to make their move.
	 */

	private void setupUpdate(final SetupMenu menu) {
		setChanged(); // setup called
		notifyObservers();
	}

	public void updateComputerMove(final Move bestMove) {
		this.computerMove = bestMove;
	}

	public void updateGameBoard(final Board board) {
		this.chessboard = board;
	}

	public MoveLog getMoveLog() {
		return this.movelog;
	}

	public MoveHistory getGameHistoryPanel() {
		return this.history;
	}

	public TakenPiecesPanel getTakenPiecesPanel() {
		return this.taken;
	}

	public BoardPanel getBoardPanel() {
		return this.boardPanel;
	}
	
	public int getNumMoves() {
		return this.numMoves;
	}

	public void moveMadeUpdate(final PlayerType type) {
		setChanged();
		numMoves++;
		notifyObservers(type);

	}

	private static class AIWatcher implements Observer {

		/*
		 * If current player is an AI player, make move.
		 */

		@Override
		public void update(Observable o, Object arg) {
			Player current = Table.getInstance().getBoard().currentPlayer();
			if (Table.getInstance().getSetupMenu().isAI(current) && !current.inCheckmate() && !current.inStalemate()) {

				System.out.println("AI thinking...");
				// create AI thread and execute AI calculations
				AIThinker ai = new AIThinker();
				ai.execute();
			}
			if (current.inCheckmate()) {
				System.out.println("Game over - " + current.toString() + " wins!");
			}
			if (current.inStalemate()) {
				System.out.println("Game over - " + current.toString() + " is in stalemate.");

			}


		}

	}

	public class BoardPanel extends JPanel {
		final List<TilePanel> boardTiles;

		BoardPanel() {
			super(new GridLayout(BoardUtils.TILES_PER_ROW, BoardUtils.TILES_PER_ROW));
			this.boardTiles = new ArrayList<>();
			for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
				final TilePanel tilePanel = new TilePanel(this, i);
				this.boardTiles.add(tilePanel);
				add(tilePanel); // lays it out like a chessboard 8x8 by default
			}
			setPreferredSize(BOARD_PANEL_DIMENSION);
			validate();
		}

		public void drawBoard(final Board board) {
			removeAll();
			for (final TilePanel tilePanel : boardDirection.traverse(boardTiles)) {
				tilePanel.drawTile(board);
				add(tilePanel);
			}
			validate();
			repaint();
		}
	}


	/*
	 * This class represents all moves that have been made on the game board.
	 */

	public static class MoveLog {
		private final List<Move> moves;

		MoveLog() {
			this.moves = new ArrayList<>();
		}

		public List<Move> getMoves() {
			return this.moves;
		}

		public void addMove(final Move move) {
			this.moves.add(move);
		}

		public int size() {
			return this.moves.size();
		}

		public void clear() {
			this.moves.clear();
		}

		public Move removeMove(final int index) {
			return this.moves.remove(index);
		}

		public boolean removeMove(final Move move) {
			return this.moves.remove(move);
		}

	}

	public enum PlayerType {
		HUMAN, COMPUTER;
	}

	private class TilePanel extends JPanel {
		private final int tileID; // 0 - 63

		TilePanel(final BoardPanel board, final int tileID) {
			super(new GridBagLayout());
			this.tileID = tileID;
			setPreferredSize(TILE_PANEL_DIMENSION);
			assignTileColor();
			assignPieceIconAndHighlight(chessboard);

			// handle mouseclicks
			// startTile == null ? isFirstClick : isSecondClick;
			addMouseListener(new MouseListener() {

				@Override
				public void mouseClicked(MouseEvent e) {
					if (!setupMenu.isAI(chessboard.currentPlayer())) {
						if (SwingUtilities.isRightMouseButton(e)) {
							startTile = null;
							endTile = null;
							humanMovedPiece = null;

						} else if (SwingUtilities.isLeftMouseButton(e)) {
							if (startTile == null) { // haven't picked a start tile
								// first click
								startTile = chessboard.getTile(tileID);
								humanMovedPiece = startTile.getPiece();
								if (humanMovedPiece == null) { // clicked on empty
									// tile
									startTile = null; // don't select
								}
							} else {
								// second click
								endTile = chessboard.getTile(tileID);

								final Move move = Move.MoveFactory.createMove(chessboard,
										BoardUtils.board64[startTile.getCoordinate()],
										BoardUtils.board64[endTile.getCoordinate()]); 
								// TODO: deal with multiple promotion options
	

								final MoveTransition transition = chessboard.currentPlayer().makeMove(move); // try move
								if (transition.getMoveStatus().isDone()) { // accept move?
									chessboard = transition.getBoard();
									previousPositions.add(chessboard);
									movelog.addMove(move);
									lastMove = move;
									currentHash = ZobristHash.updateZobristHash(currentHash, move);
									move.getMovedPiece().hasNowMoved();
									
									numMoves++;
									System.out.println(move);
									System.out.println(chessboard);
									System.out.println("Zobrist hash code: " + currentHash);
								}

								startTile = null;
								humanMovedPiece = null;

								if (endTile.getPiece() != null) {
									if (endTile.getPiece().getSide() == chessboard.currentPlayer().getSide()) {
										startTile = chessboard.getTile(tileID);
										humanMovedPiece = startTile.getPiece();
										if (humanMovedPiece == null) {
											startTile = null;
										}
									}
								}
								endTile = null;
							}
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									
									history.redo(previousPositions, movelog);
									taken.redo(movelog);

									if (setupMenu.isAI(chessboard.currentPlayer())) {
										Table.getInstance().moveMadeUpdate(PlayerType.HUMAN);
									}

									boardPanel.drawBoard(chessboard);
								}
							});
						}
					}
				}

				@Override
				public void mousePressed(MouseEvent e) {

				}

				@Override
				public void mouseReleased(MouseEvent e) {

				}

				@Override
				public void mouseEntered(MouseEvent e) {

				}

				@Override
				public void mouseExited(MouseEvent e) {

				}

			});
		}

		public void getDebugStream() {
			System.out.println(chessboard.currentPlayer().getSide().toString() + ": "
					+ chessboard.currentPlayer().getLegalMoves());
			System.out.println(chessboard.currentPlayer().getOpponent().getSide().toString() + ": "
					+ chessboard.currentPlayer().getOpponent().getLegalMoves());

		}

		public void drawTile(final Board board) {
			assignTileColor();
			assignPieceIconAndHighlight(board);
			validate();
			repaint();
		}

		/*
		 * Puts piece image on a single tile and highlights legal moves for
		 * clicked pieces. These were originally written in two methods, but to
		 * allow clean image overlay they have been combined.
		 */

		private void assignPieceIconAndHighlight(final Board board) {
			this.removeAll();

			BufferedImage composite = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
			Graphics2D tile = composite.createGraphics();

			if (board.getTile(this.tileID).tileIsOccupied()) {
				try {
					final BufferedImage piece = ImageIO.read(new File(
							RES_FOLDER + board.getTile(this.tileID).getPiece().getSide().toString().substring(0, 1)
							+ board.getTile(this.tileID).getPiece().toString() + ".gif"));
					// e.g. white bishop: "WB.gif"

					tile.drawImage(piece, 0, 0, null);

				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (highlightLegalMoves && humanMovedPiece != null) {
				List<Move> legalMoves = (List<Move>) pieceLegalMoves(board);
				for (final Move move : legalMoves) {
					if (BoardUtils.mailbox[move.getDestination()] == this.tileID && board.currentPlayer().makeMove(move)
							.getMoveStatus() != MoveStatus.LEAVES_PLAYER_IN_CHECK) {
						try {
							BufferedImage highlight = ImageIO.read(new File("res/dot.png"));
							tile.drawImage(highlight, HIGHLIGHT_OFFSET, HIGHLIGHT_OFFSET, null);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

			}
			tile.dispose();
			this.add(new JLabel(new ImageIcon(composite)));

		}

		private Collection<Move> pieceLegalMoves(final Board board) {
			List<Move> pieceMoves = new ArrayList<>();
			for (final Move m : board.currentPlayer().getLegalMoves()) {
				if (humanMovedPiece.equals(m.getMovedPiece())) {
					pieceMoves.add(m);
				}
			}
			return ImmutableList.copyOf(pieceMoves);
		}

		/*
		 * Method that assigns colors to tiles in alternating fashion.
		 */

		private void assignTileColor() {
			if (rowIsOdd(tileID)) { // rows 1, 3, 5, 7
				setBackground(this.tileID % 2 == 0 ? LIGHT_COLOR : DARK_COLOR);
			} else { // 2, 4, 6, 8
				setBackground(this.tileID % 2 != 0 ? LIGHT_COLOR : DARK_COLOR);
			}
		}

		private boolean rowIsOdd(int tileID) {// tileID is 0 - 63
			return ((tileID / 8) + 1) % 2 == 1;
		}

	}

	public enum BoardDirection {
		NORMAL { // black on top
			@Override
			public List<TilePanel> traverse(final List<TilePanel> board) {
				return board;
			}

			@Override
			public BoardDirection opposite() {
				return FLIPPED;
			}
		},
		FLIPPED {
			@Override
			public List<TilePanel> traverse(final List<TilePanel> board) {
				return Lists.reverse(board);
			}

			@Override
			public BoardDirection opposite() {
				return NORMAL;
			}
		};

		public abstract BoardDirection opposite();

		public abstract List<TilePanel> traverse(final List<TilePanel> board);
	}

	public static void incrementCollisions() {
		collisions++;
	}

	public static int getCollisions() {
		return collisions;
	}



}
