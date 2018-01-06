package chess.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import chess.engine.board.Board;
import chess.engine.board.Move;
import chess.gui.Table.MoveLog;

public class MoveHistory extends JPanel {

	private final DataModel model;
	private final JScrollPane scrollPane;
	private static final Dimension PANEL_DIMENSION = new Dimension(200, 400);

	MoveHistory() {
		this.setLayout(new BorderLayout());
		this.model = new DataModel();
		final JTable table = new JTable(model);
		table.setRowHeight(15);
		this.scrollPane = new JScrollPane(table);
		scrollPane.setColumnHeaderView(table.getTableHeader()); // this part
																// doesn't
																// scroll
		scrollPane.setPreferredSize(PANEL_DIMENSION);
		this.add(scrollPane, BorderLayout.CENTER); // rel. to main obj
		this.setVisible(true);
	}

	/*
	 * This method reconstructs the entire move history panel.
	 */

	public void redo(final List<Board> positions, final MoveLog movelog) {
		int currentRow = 0;
		int counter = 1; //TODO: make this more exception-safe
		this.model.clear();
		for (final Move move : movelog.getMoves()) {
			final String moveText = move.toString();
			if (move.getMovedPiece().getSide().isWhite()) {
				this.model.setValueAt(moveText + calculateCheckAndCheckMateHash(positions.get(counter)), currentRow, 0);
				counter++;
			} else if (move.getMovedPiece().getSide().isBlack()) {
				this.model.setValueAt(moveText + calculateCheckAndCheckMateHash(positions.get(counter)), currentRow, 1);
				currentRow++;
				counter++;
			}
		}

		final JScrollBar vertical = scrollPane.getVerticalScrollBar();
		vertical.setValue(vertical.getMaximum()); // auto-advance bar to last move
	}

	void clearModel() {
		this.model.clear();
	}

	void removeLast() {
		this.model.takeback();
	}

	private static class DataModel extends DefaultTableModel {

		private final List<Row> values;
		private static final String[] HEADERS = { "White", "Black" };

		DataModel() {
			this.values = new ArrayList<>();
		}

		public void clear() {
			this.values.clear();
			setRowCount(0);
		}

		private void takeback() {
			if (getValueAt(getRowCount() - 1, 1) == null) {
				this.setValueAt(null, getRowCount(), 0);
				setRowCount(getRowCount() - 1);
			} else { // both are filled
				this.setValueAt(null, getRowCount(), 1);
			}
		}

		/*
		 * Overrides the default count from javax.swing
		 */
		@Override
		public int getRowCount() {
			if (this.values == null)
				return 0;
			return this.values.size();
		}

		@Override
		public int getColumnCount() {
			return HEADERS.length;
		}

		/*
		 * Override - called when swing tries to render a cell
		 */
		@Override
		public Object getValueAt(final int row, final int column) {
			final Row currentRow = this.values.get(row);
			if (column == 0) {
				return currentRow.getWhiteMove();
			} else if (column == 1) {
				return currentRow.getBlackMove();
			}
			return null;
		}

		@Override
		public void setValueAt(final Object obj, final int row, final int column) {
			final Row currentRow;
			if (this.getRowCount() <= row) { // if we pass in a row parameter
												// larger than our current data
												// fields
				currentRow = new Row();
				this.values.add(currentRow);
			} else {
				currentRow = this.values.get(row);
			}
			if (column == 0) {
				currentRow.setWhiteMove((String) obj);
				fireTableRowsInserted(row, row);
			} else if (column == 1) {
				currentRow.setBlackMove((String) obj);
				fireTableCellUpdated(row, column); // notify observers/listeners
			}
		}

		@Override
		public Class<?> getColumnClass(final int column) {
			return Move.class;
		}

		@Override
		public String getColumnName(final int column) {
			return HEADERS[column];
		}

	}
	
    private static String calculateCheckAndCheckMateHash(final Board board) {
        if(board.currentPlayer().inCheckmate()) {
            return "#";
        } else if(board.currentPlayer().inCheck()) {
            return "+";
        }
        return "";
    }

	private static class Row {
		// Row represented as two strings, white and black's move
		private int moveNumber;
		private String whiteMove;
		private String blackMove;

		Row() {

		}

		// TODO: implement this in JTable and MoveHistory
		public int getMoveNumber() {
			return this.moveNumber;
		}

		public void setMoveNumber(int n) {
			this.moveNumber = n;
		}

		public String getWhiteMove() {
			return this.whiteMove;
		}

		public void setWhiteMove(final String move) {
			this.whiteMove = move;
		}

		public String getBlackMove() {
			return this.blackMove;
		}

		public void setBlackMove(final String move) {
			this.blackMove = move;
		}

		@Override
		public String toString() {
			return "Row: " + whiteMove + ", " + blackMove;
		}

	}
}
