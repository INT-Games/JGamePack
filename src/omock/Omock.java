/*
 * Copyright (c) 2023 Eugene Hong
 *
 * This software is distributed under license. Use of this software
 * implies agreement with all terms and conditions of the accompanying
 * software license.
 * Please refer to LICENSE
 * */

package omock;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;

public class Omock {
	private static Cell[][] cells = new Cell[19][19];
	public static final String version = "v1.2";
	static JFrame frame;
	static boolean blackTurn = true;
	static boolean verbose = true;
	static boolean gameOver = false;
	static LinkedList<Cell> history = new LinkedList<Cell>();
	static boolean DISABLE33RULE = false;
	static JMenuItem restart;
	static JMenuItem samsam;

	public static void main(String[] args) {
		init();
		startNewGame();
		frame.setVisible(true);
	}

	public static void init() {
		int w = 947;
		int h = 919;
		frame = new JFrame("Omock " + version);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new GridLayout(19, 19));
		frame.setSize(w, h);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setBounds(d.width / 2 - w / 2, d.height / 2 - h / 2, w, h);

		for (int i = 0; i < 19; i++)
			for (int j = 0; j < 19; j++)
				cells[i][j] = new Cell(i, j);
		for (int i = 0; i < 19; i++)
			for (int j = 0; j < 19; j++)
				frame.add(cells[i][j].btn);
		JMenuBar mb = new JMenuBar();
		JMenu m = new JMenu("Game");
		restart = new JMenuItem("Start New Game");
		restart.setMnemonic(KeyEvent.VK_S);
		restart.addActionListener(new StartNewGame());
		samsam = new JMenuItem("Disable 33 rule");
		samsam.setMnemonic(KeyEvent.VK_3);
		samsam.addActionListener(new Disable33());
		m.add(restart);
		m.add(samsam);
		mb.add(m);
		frame.setJMenuBar(mb);
	}

	public static void startNewGame() {
		for (int i = 0; i < 19; i++)
			for (int j = 0; j < 19; j++)
				cells[i][j].init();
		blackTurn = true;
		gameOver = false;
		history = new LinkedList<Cell>();
	}

	public static void check() {
		for (int i = 0; i < 19; i++)
			for (int j = 0; j < 19; j++) {
				if (checkAlign(cells[i][j], 5) > 0) {
					JOptionPane.showMessageDialog(null, (blackTurn ? "White" : "Black") + " WINS!!");
					gameOver = true;
					return;
				}
			}
	}

	public static boolean is33() {
		if (DISABLE33RULE)
			return false;
		for (int i = 0; i < 19; i++)
			for (int j = 0; j < 19; j++) {
				if (checkAlign(cells[i][j], 3) > 1) {
					JOptionPane.showMessageDialog(null, "No samsam!!", "No 3-3!!", JOptionPane.ERROR_MESSAGE);
					return true;
				}
			}
		return false;
	}

	public static boolean is6Mock() {
		for (int i = 0; i < 19; i++)
			for (int j = 0; j < 19; j++) {
				if (testRightDown(cells[i][j]) > 5 || testRightUp(cells[i][j]) > 5 || testDown(cells[i][j]) > 5
						|| testRight(cells[i][j]) > 5) {
					JOptionPane.showMessageDialog(null, "No Yukmock!!", "No 6mock!!", JOptionPane.ERROR_MESSAGE);
					return true;
				}
			}
		return false;
	}

	/**
	 * @param k minimum lenght of an alignement
	 * @return How many lines aligned
	 */

	private static int checkAlign(Cell c, int k) {
		if (c.isEmpty)
			return -1;
		int result = 0;
		int cnt;

		if ((cnt = testRightDown(c)) >= k) {
			log(" (" + c.x + "," + c.y + "): " + cnt + " alignment to right-down");
			result++;
		}

		if ((cnt = testRightUp(c)) >= k) {
			log(" (" + c.x + "," + c.y + ") :" + cnt + " alignment to right-up");
			result++;
		}

		if ((cnt = testDown(c)) >= k) {
			log(" (" + c.x + "," + c.y + ") : " + cnt + " alignment to down");
			result++;
		}

		if ((cnt = testRight(c)) >= k) {
			log(" (" + c.x + "," + c.y + "): " + cnt + " alignment to right");
			result++;
		}

		return result;
	}

	/** @return how long is the alignment */
	private static int testRightDown(Cell c) {
		int i = c.y - 1;
		int j = c.x - 1;
		int cnt = 1;
		for (int dx = 1, dy = 1; dx < 19 && dx + j < 19 && dy < 19 && dy + i < 19; dx++, dy++) { // right down
			if (cells[i + dy][j + dx].isEmpty || cells[i + dy][j + dx].color != c.color) {
				break;
			} else {
				cnt++;
			}
		}

		for (int dx = 1, dy = 1; dx < 19 && j - dx > -1 && dy < 19 && i - dy > -1; dx++, dy++) { // left up
			if (cells[i - dy][j - dx].isEmpty || cells[i - dy][j - dx].color != c.color) {
				break;
			} else {
				cnt++;
			}
		}
		return cnt;
	}

	/** @return how long is the alignment */
	private static int testRightUp(Cell c) {
		int i = c.y - 1;
		int j = c.x - 1;
		int cnt = 1;
		for (int dx = 1, dy = 1; dx < 19 && dx + j < 19 && dy < 19 && i - dy > -1; dx++, dy++) {
//right up
			if (cells[i - dy][j + dx].isEmpty || cells[i - dy][j + dx].color != c.color) {
				break;
			} else {
				cnt++;
			}
		}

		for (int dx = 1, dy = 1; dx < 19 && j - dx > -1 && dy < 19 && dy + i < 19; dx++, dy++) {
//left down
			if (cells[i + dy][j - dx].isEmpty || cells[i + dy][j - dx].color != c.color) {
				break;
			} else {
				cnt++;
			}
		}
		return cnt;
	}

	/** @return how long is the alignment */
	private static int testDown(Cell c) {
		int i = c.y - 1;
		int j = c.x - 1;
		int cnt = 1;
		for (int n = 1; n < 19 && n + i < 19; n++) {
			if (cells[i + n][j].isEmpty || cells[i + n][j].color != c.color) {// down
				break;
			} else {
				cnt++;
			}
		}
		for (int n = 1; n < 19 && i - n > -1; n++) {
			if (cells[i - n][j].isEmpty || cells[i - n][j].color != c.color) {// up
				break;
			} else {
				cnt++;
			}
		}
		return cnt;
	}

	/** @return how long is the alignment */
	private static int testRight(Cell c) {
		int i = c.y - 1;
		int j = c.x - 1;
		int cnt = 1;

		for (int n = 1; n < 19 && n + j < 19; n++) {
			if (cells[i][j + n].isEmpty || cells[i][j + n].color != c.color) {// right break;
				break;
			} else {
				cnt++;
			}
		}
		for (int n = 1; n < 19 && j - n > -1; n++) {
			if (cells[i][j - n].isEmpty || cells[i][j - n].color != c.color) {// left
				break;
			} else {
				cnt++;
			}
		}
		return cnt;
	}

	public static void log(String a) {
		if (verbose)
			System.out.println(a);
	}

	static class Cell {
		public static final Color BROWN = new Color(185, 122, 87);
		JButton btn = new JButton();
		int color = 0; // 0: non, 1 = black, 2 = white
		boolean isEmpty = true;
		int x;
		int y;

		public Cell(int i, int j) {
			init();
			btn.addActionListener(new Clicked(this));
			btn.addMouseListener(new MouseOver(this));
			btn.addKeyListener(new RollBack());
			x = j + 1;
			y = i + 1;

			if ((x == 4 && y == 4) || (x == 4 && y == 16) || (x == 16 && y == 4) || (x == 16 && y == 16)
					|| (x == 4 && y == 10) || (x == 10 && y == 4) || (x == 10 && y == 16) || (x == 16 && y == 10)
					|| (x == 10 && y == 10))
				btn.setText("0");
		}

		public void init() {
			color = 0;
			btn.setBackground(Omock.Cell.BROWN);
			isEmpty = true;
		}
	}
}

class Clicked implements ActionListener {
	private Omock.Cell cell;

	public Clicked(Omock.Cell c) {
		cell = c;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println(" (" + cell.x + "," + cell.y + ")");
		if (Omock.gameOver)
			return;
		if (!cell.isEmpty)
			return;
		cell.btn.setBackground(Omock.blackTurn ? Color.BLACK : Color.WHITE);
		cell.isEmpty = false;
		cell.color = Omock.blackTurn ? 1 : 2;
		if (Omock.blackTurn && (Omock.is33() || Omock.is6Mock())) {
			cell.init();
			return;
		}
		Omock.history.add(cell);
		Omock.blackTurn = !Omock.blackTurn;
		Omock.check();
	}
}

class MouseOver extends MouseAdapter {
	private Omock.Cell cell;

	public MouseOver(Omock.Cell c) {
		cell = c;
		cell.btn.setBackground(new Color(185, 122, 87));
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		Omock.frame.setTitle("Omock " + Omock.version + " - (" + cell.x + ", " + cell.y + ")");
	}
}

class RollBack extends KeyAdapter {
	public RollBack() {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (Omock.gameOver || Omock.history.isEmpty())
			return;
		if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
			Omock.Cell cell = Omock.history.removeLast();
			cell.btn.setBackground(Omock.Cell.BROWN);
			cell.isEmpty = true;
			cell.color = 0;
			Omock.blackTurn = !Omock.blackTurn;
		}
	}
}

class StartNewGame implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
		Omock.startNewGame();
	}
}

class Disable33 implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
		Omock.samsam.setText(Omock.DISABLE33RULE ? "Disable 33 rule" : "Enable 33 rule");
		Omock.DISABLE33RULE = !Omock.DISABLE33RULE;
	}
}
