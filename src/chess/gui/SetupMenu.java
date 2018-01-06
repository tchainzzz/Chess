package chess.gui;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import chess.engine.Side;
import chess.engine.player.Player;
import chess.gui.Table.PlayerType;

public class SetupMenu extends JDialog {

	private PlayerType whiteType;
	private PlayerType blackType;
	private JSpinner depthSpinner;

	private static final String HUMAN_TEXT = "Human";
	private static final String COMPUTER_TEXT = "Computer";

	SetupMenu(final JFrame frame, final boolean modal) {
		super(frame, modal);
		final JPanel panel = new JPanel(new GridLayout(0, 1));
		final JRadioButton whiteHumanButton = new JRadioButton(HUMAN_TEXT);
		final JRadioButton whiteComputerButton = new JRadioButton(COMPUTER_TEXT);
		final JRadioButton blackHumanButton = new JRadioButton(HUMAN_TEXT);
		final JRadioButton blackComputerButton = new JRadioButton(COMPUTER_TEXT);
		whiteHumanButton.setActionCommand(HUMAN_TEXT);

		final ButtonGroup whiteGroup = new ButtonGroup();
		whiteGroup.add(whiteHumanButton);
		whiteGroup.add(whiteComputerButton);
		whiteHumanButton.setSelected(true);

		final ButtonGroup blackGroup = new ButtonGroup();
		blackGroup.add(blackHumanButton);
		blackGroup.add(blackComputerButton);
		blackComputerButton.setSelected(true);

		getContentPane().add(panel);
		panel.add(new JLabel("White"));
		panel.add(whiteHumanButton);
		panel.add(whiteComputerButton);
		panel.add(new JLabel("Black"));
		panel.add(blackHumanButton);
		panel.add(blackComputerButton);

		panel.add(new JLabel("Computer"));
		this.depthSpinner = addLabeledSpinner(panel, "Search Depth",
				new SpinnerNumberModel(6, 0, Integer.MAX_VALUE, 1));

		final JButton cancel = new JButton("Cancel");
		final JButton ok = new JButton("OK");
		
		whiteType = PlayerType.HUMAN;
		blackType = PlayerType.COMPUTER;

		ok.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				whiteType = whiteComputerButton.isSelected() ? PlayerType.COMPUTER : PlayerType.HUMAN;
				blackType = blackComputerButton.isSelected() ? PlayerType.COMPUTER : PlayerType.HUMAN;
				SetupMenu.this.setVisible(false);
			}

		});

		cancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Canceled");
				SetupMenu.this.setVisible(false);
			}

		});

		panel.add(cancel);
		panel.add(ok);

		setLocationRelativeTo(frame);
		pack();
		setVisible(false);
	}

	void promptUser() {
		setVisible(true);
		repaint();
	}

	boolean isAI(final Player player) {
		return (player.getSide() == Side.WHITE) ? whiteType == PlayerType.COMPUTER : blackType == PlayerType.COMPUTER;
	}

	private JSpinner addLabeledSpinner(final Container c, final String label, final SpinnerModel spinnerModel) {
		final JLabel l = new JLabel(label);
		c.add(l);
		final JSpinner spinner = new JSpinner(spinnerModel);
		l.setLabelFor(spinner);
		c.add(spinner);
		return spinner;
	}

}
