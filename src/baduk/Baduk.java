/*
 * Copyright (c) 2023 Eugene Hong
 *
 * This software is distributed under license. Use of this software
 * implies agreement with all terms and conditions of the accompanying
 * software license.
 * Please refer to LICENSE
 * */

package baduk;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;

public class Baduk {
	private static Cell[][] cells = new Cell[19][19];
	public static final String version = "v1.0";
	static JFrame frame;
	static boolean blackTurn = true;
	static boolean verbose = true;
	static boolean gameOver = true;
	static LinkedList<History> history = new LinkedList<History>();
	static JMenuItem restart;

	public static void main(String[] args) {
		init();
		startNewGame();
		frame.setVisible(true);
	}

	public static void init() {

		int w = 947;
		int h = 919;
		frame = new JFrame("Baduk " + version);
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
		m.add(restart);
		mb.add(m);
		frame.setJMenuBar(mb);
	}

	public static void startNewGame() {
		for (int i = 0; i < 19; i++)
			for (int j = 0; j < 19; j++)
				cells[i][j].init();
		blackTurn = true;
		gameOver = false;
		history = new LinkedList<History>();
	}

	@SuppressWarnings("unused")
	public static void check() {
		//현재 상태를 history로 저장
		for (int i = 0; i < 19; i++)
			for (int j = 0; j < 19; j++) {
				//TODO: scan algorithm
				if (true) { // 자충수면 에러 띄우고
					//cell.init();
					return;
				}
			}
	}

	public static void rewindHistory() {
//blackTurn 도 바꾸기
	}

	public static void log(String a) {
		if (verbose)
			System.out.println(a);
	}
}

class Cell {
	public static final Color BROWN = new Color(185, 122, 87).darker();
	JButton btn = new JButton();
	int color = 0; // 0 : non, 1 = black, 2
	boolean isEmpty = true;
	int x;
	int y;

	public Cell(int i, int j) {
		x = j + 1;
		y = i + 1;
		init();
		btn.setForeground(Color.BLACK);
		btn.addActionListener(new Clicked(this));
		btn.addMouseListener(new ShowCoordinates(this));
		btn.addKeyListener(new RollBack());
	}

	public void init() {
		setColor(0);
	}

	public void setColor(int c) {
		if (c == 0) {
			if ((x == 4 && y == 4) || (x == 4 && y == 16) || (x == 16 && y == 4) || (x == 16 && y == 16)
					|| (x == 4 && y == 10) || (x == 10 && y == 4) || (x == 10 && y == 16) || (x == 16 && y == 10)
					|| (x == 10 && y == 10))
				btn.setText("●"); // ○
		} else {
			btn.setText("");
		}
		btn.setBackground(c == 0 ? BROWN : (c == 1 ? Color.BLACK : Color.WHITE));
		isEmpty = c == 0;
		color = c;
	}
}

class History {
}

class Clicked implements ActionListener {
	private Cell cell;

	public Clicked(Cell c) {
		cell = c;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Baduk.log("(" + cell.x + "," + cell.y + ")");
		if (Baduk.gameOver)
			return;
		if (!cell.isEmpty)
			return;
		cell.setColor(Baduk.blackTurn ? 1 : 2);
		Baduk.blackTurn = !Baduk.blackTurn;
		Baduk.check();
	}
}

class ShowCoordinates extends MouseAdapter {
	private Cell cell;

	public ShowCoordinates(Cell c) {
		cell = c;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (Baduk.verbose)
			Baduk.frame.setTitle("Baduk " + Baduk.version + " - (" + cell.x + "," + cell.y + ")");
	}
}

class RollBack extends KeyAdapter {
	public RollBack() {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (Baduk.gameOver || Baduk.history.isEmpty())
			return;
		if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
			Baduk.rewindHistory();
		}
	}
}

class StartNewGame implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
		Baduk.startNewGame();
	}
}