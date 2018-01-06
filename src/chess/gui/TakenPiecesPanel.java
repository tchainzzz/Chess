package chess.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import com.google.common.primitives.Ints;

import chess.engine.Side;
import chess.engine.board.Move;
import chess.engine.pieces.Piece;
import chess.gui.Table.MoveLog;

/*
 * This class is a graphical panel that represents which pieces have been taken.
 */

public class TakenPiecesPanel extends JPanel {

	private final JPanel north;
	private final JPanel south;

	private static final EtchedBorder PANEL_BORDER = new EtchedBorder(EtchedBorder.RAISED);
	private static final Color PANEL_COLOR = Color.decode("#fdf5e6");
	private static final Dimension TAKEN_PIECES_DIMENSION = new Dimension(160, 160);

	public TakenPiecesPanel() {
		super(new BorderLayout());
		setBackground(PANEL_COLOR);
		setBorder(PANEL_BORDER);
		this.north = new JPanel(new GridLayout(8, 2)); // magic numbers TODO:
														// change so it adapts
														// to bigger piece lists
		this.south = new JPanel(new GridLayout(8, 2));
		this.north.setBackground(PANEL_COLOR);
		this.south.setBackground(PANEL_COLOR);
		add(this.north, BorderLayout.NORTH);
		add(this.south, BorderLayout.SOUTH);
		setPreferredSize(TAKEN_PIECES_DIMENSION);

	}

	public void redo(final MoveLog movelog) {
		south.removeAll();
		north.removeAll();

		final List<Piece> capturedWhite = new ArrayList<>();
		final List<Piece> capturedBlack = new ArrayList<>();

		for (final Move move : movelog.getMoves()) {
			if (move.isAttack()) {
				final Piece taken = move.getAttackedPiece();
				if (taken.getSide() == Side.WHITE) {
					capturedWhite.add(taken);
				} else if (taken.getSide() == Side.BLACK) {
					capturedBlack.add(taken);
				} else {
					throw new RuntimeException("Piece is neither black nor white!");
				}
			}
		}

		// comparator created so the taken pieces panel can be sorted in order
		// of weakest to stronger pieces
		Collections.sort(capturedWhite, new Comparator<Piece>() {

			@Override
			public int compare(Piece o1, Piece o2) {
				return Ints.compare(o1.getValue(), o2.getValue());
			}

		});

		Collections.sort(capturedBlack, new Comparator<Piece>() {

			@Override
			public int compare(Piece o1, Piece o2) {
				return Ints.compare(o1.getValue(), o2.getValue());
			}

		});

		// draw pieces
		for (final Piece p : capturedWhite) {
			try {
				final BufferedImage image = ImageIO.read(new File(
						(Table.RES_FOLDER) + p.getSide().toString().substring(0, 1) + "" + p.toString() + ".gif"));

				final ImageIcon icon = new ImageIcon(image);
				final JLabel imageLabel = new JLabel(icon);
				this.north.add(imageLabel);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		for (final Piece p : capturedBlack) {
			try {
				final BufferedImage image = ImageIO.read(new File(
						(Table.RES_FOLDER) + p.getSide().toString().substring(0, 1) + "" + p.toString() + ".gif"));
				final ImageIcon icon = new ImageIcon(image);
				final JLabel imageLabel = new JLabel(icon);
				this.south.add(imageLabel);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		validate();
	}

	void clearPanel() {
		this.north.removeAll();
		this.south.removeAll();
		validate();
		repaint();
	}

}
