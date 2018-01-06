package chess.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;

import chess.engine.Side;
import chess.engine.pieces.Piece.PieceType;

public class PromotionFrame extends JDialog implements ActionListener {

	public static final Dimension PROMOTION_FRAME_SIZE = new Dimension(200, 200);
	private static final String RES_FOLDER = "res/pieces/";
//	private static final PromotionFrame WHITE_INSTANCE = new PromotionFrame(Side.WHITE);
//	private static final PromotionFrame BLACK_INSTANCE = new PromotionFrame(Side.BLACK);

	private PieceType promoted;
	private JButton knight;
	private JButton bishop;
	private JButton rook;
	private JButton queen;

	PromotionFrame(Side color) {

		this.setTitle("Promotion");
		this.setSize(PROMOTION_FRAME_SIZE);
		this.setLayout(new FlowLayout());
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setModal(true);

		knight = new JButton();
		bishop = new JButton();
		rook = new JButton();
		queen = new JButton();
		try {
			BufferedImage knightIcon = ImageIO.read(new File(RES_FOLDER + color.toString().substring(0, 1) + "N.gif"));
			BufferedImage bishopIcon = ImageIO.read(new File(RES_FOLDER + color.toString().substring(0, 1) + "B.gif"));
			BufferedImage rookIcon = ImageIO.read(new File(RES_FOLDER + color.toString().substring(0, 1) + "R.gif"));
			BufferedImage queenIcon = ImageIO.read(new File(RES_FOLDER + color.toString().substring(0, 1) + "Q.gif"));
			knight.setIcon(new ImageIcon(knightIcon));
			bishop.setIcon(new ImageIcon(bishopIcon));
			rook.setIcon(new ImageIcon(rookIcon));
			queen.setIcon(new ImageIcon(queenIcon));
		} catch (IOException e) {
			throw new RuntimeException("Promotion menu icons not found!");
		}

		knight.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("knighted");
				promoted = PieceType.KNIGHT;
	
			}
		});
		bishop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				promoted = PieceType.BISHOP;
				setVisible(false);

			}
		});
		rook.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				promoted = PieceType.ROOK;
				setVisible(false);
			}
		});
		queen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println(promoted);
				promoted = PieceType.QUEEN;
				setVisible(false);
			}
		});
		this.add(knight);
		this.add(bishop);
		this.add(rook);
		this.add(queen);
		this.setVisible(true);

	}


//	public static PromotionFrame getInstance(Side color) {
//		if (color == Side.WHITE) {
//			return WHITE_INSTANCE;
//		} else {
//			return BLACK_INSTANCE;
//		}
//	}

	public PieceType selectedPiece() {
		return this.promoted;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println("dispose");
		dispose();
	}

}
