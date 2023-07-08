/*
 * Copyright (c) 2023 Eugene Hong
 *
 * This software is distributed under license. Use of this software
 * implies agreement with all terms and conditions of the accompanying
 * software license.
 * Please refer to LICENSE
 * */

package nemoNemo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class NemoNemo {
	private static Cell[][] cells;
	private static JLabel[][] labels;
	private static boolean[][] anwser;
	public static final String version = "v1.0";
	static JFrame frame;
	static boolean verbose = true;
	public static boolean isSolveMode;
	public static boolean isColoringMode;
	public static boolean edited;
	static int row = 10;
	static int col = 10;
	static JMenuBar mb;
	static JMenu playGame;
	static JMenu makeGame;
	static JMenuItem solve;
	static JMenuItem make;
	static JMenuItem saveAs;
	static JMenuItem load;
	static final String savePath = ".\\problems\\";
	static String nowDoing = null;

	public static void main(String[] args) {
		new File(savePath).mkdir();
		init();
		startNewMake(null); // TODO: startNewSolve 로 제일 최근에 했던 문제 풀기
		frame.setVisible(true);
	}

	public static void init() {
		frame = new JFrame("NemoNemo Logic " + version);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//frame.addKeyListener (new KeyIn());
		mb = new JMenuBar();
		playGame = new JMenu("Play Game");
		makeGame = new JMenu("Make Game");
		solve = new JMenuItem("Solve New Problem");
		solve.setMnemonic(KeyEvent.VK_N);
		solve.addActionListener(new StartNewSolve());
		make = new JMenuItem("Make New Problem");
		make.setMnemonic(KeyEvent.VK_M);
		make.addActionListener(new StartNewMake());
		saveAs = new JMenuItem("Save Problem as...");
		saveAs.setMnemonic(KeyEvent.VK_S);
		saveAs.addActionListener(new SaveAs());
		load = new JMenuItem("Load Problem to edit");
		load.setMnemonic(KeyEvent.VK_L);
		load.addActionListener(new Load());
		playGame.add(solve);
		makeGame.add(make);
		makeGame.add(saveAs);
		makeGame.add(load);
		mb.add(playGame);
		mb.add(makeGame);
		frame.setJMenuBar(mb);
		isColoringMode = true;
		resetTitle();
	}

	public static void resetTitle() {
		frame.setTitle((edited ? "*" : "") + (nowDoing == null ? "" : (nowDoing + " - ")) + "NemoNemo Logic " + version
				+ " [" + (isColoringMode ? "Coloring" : "Deleting") + "]");
	}

	public static void clear() {
		int w = 489 * (row / 10);
		int h = 504 * (col / 10);
		frame.getContentPane().removeAll();

		frame.setContentPane(new JPanel());
		// frame.setSize (w, h);
		frame.repaint();
		frame.setLayout(new GridLayout(row + 1, col + 1));
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setBounds(d.width / 2 - w / 2, d.height / 2 - h / 2, w + 1, h + 1);
		frame.setBounds(d.width / 2 - w / 2, d.height / 2 - h / 2, w, h);
		cells = new Cell[col][row];
		labels = new JLabel[2][row];
		for (int i = 0; i < col; i++)
			for (int j = 0; j < row; j++)
				cells[i][j] = new Cell(i, j);
		for (int j = 0; j < row; j++) {
			labels[0][j] = new JLabel("0", SwingConstants.CENTER);
			labels[0][j].setVerticalAlignment(SwingConstants.BOTTOM);
		}
		for (int j = 0; j < row; j++)
			labels[1][j] = new JLabel("0", SwingConstants.RIGHT);

		frame.add(new JLabel(isSolveMode ? "[Solve]" : "[Make]"));
		for (int j = 1; j < row + 1; j++)
			frame.add(labels[0][j - 1]);

		for (int i = 1; i < col + 1; i++) {
			frame.add(labels[1][i - 1]);
			for (int j = 1; j < row + 1; j++)
				frame.add(cells[i - 1][j - 1].btn);
		}

		for (int i = 0; i < col; i++)
			for (int j = 0; j < row; j++)
				cells[i][j].init();
		// frame.repaint ();
	}

	public static void startNewSolve(boolean[][] arr) {
		if (arr == null)
			return;
		anwser = arr;
		isSolveMode = true;

		clear();

		String t = "";
		for (int j = 0; j < row; j++) { // TODO: duplicate.... html tag
			t = "";
			int result = 0;
			for (int i = 0; i < col; i++)
				if (anwser[i][j])
					result++;
				else {
					t += result > 0 ? (result + "<br>") : "";
					result = 0;
				} // add to List?

			t += result > 0 ? result : "";
			result = 0;
			labels[0][j].setText("<html>" + ("".equals(t) ? ("" + 0) : t) + "</html>");
		}
		for (int i = 0; i < col; i++) {
			t = "";
			int result = 0;
			for (int j = 0; j < row; j++)
				if (anwser[i][j])
					result++;
				else {
					t += result > 0 ? (result + " ") : "";
					result = 0;
				}

			t += result > 0 ? result : "";
			result = 0;
			labels[1][i].setText("".equals(t) ? ("" + 0) : t);
		}
	}

	public static void startNewMake(boolean[][] preset) { // TODO: ask how many grid?
		isSolveMode = false;
		clear();
		if (preset == null)
			nowDoing = null;
		resetTitle();
		if (preset != null) {
			for (int i = 0; i < col; i++)
				for (int j = 0; j < row; j++)
					cells[i][j].setColor(preset[i][j] ? 1 : 0);
			scan();
		}
	}

	public static void scan() {
		if (isSolveMode) {
			boolean success = true;
			for (int i = 0; i < col; i++)
				for (int j = 0; j < row; j++)
					if (cells[i][j].isColored != anwser[i][j])
						success = false;
			if (success)
				JOptionPane.showMessageDialog(null, "WINS!!");
		} else {
			String t = "";
			for (int j = 0; j < row; j++) { // TODO: duplicate...
				t = "";
				int result = 0;
				for (int i = 0; i < col; i++)
					if (cells[i][j].isColored)
						result++;
					else {
						t += result > 0 ? (result + "<br>") : "";
						result = 0;
					}
				t += result > 0 ? result : "";
				result = 0;
				labels[0][j].setText("<html>" + ("".equals(t) ? ("" + 0) : t) + "</html>");
			}
			for (int i = 0; i < col; i++) {
				t = "";
				int result = 0;
				for (int j = 0; j < row; j++)
					if (cells[i][j].isColored)
						result++;
					else {
						t += result > 0 ? (result + " ") : "";
						result = 0;
					}
				t += result > 0 ? result : "";
				result = 0;
				labels[1][i].setText("".equals(t) ? ("" + 0) : t);
			}
		}
		resetTitle();
	}

	public static void saveAs() {
		String name = JOptionPane.showInputDialog(null, "Name of your Problem?", "Set a name for the problem :",
				JOptionPane.QUESTION_MESSAGE);
		if (name == null || "".equals(name))
			;
		File saveTo = new File(savePath + name);
		try {
			saveTo.createNewFile();
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(saveTo));
			os.writeObject(encode());
			os.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getClass() + ": " + e.getMessage(), "I/O Failed!",
					JOptionPane.ERROR_MESSAGE);
		}
		edited = false;
		nowDoing = name;
		resetTitle();
	}

	private static boolean[][] encode() {

		boolean[][] arr = new boolean[col][row];
		for (int i = 0; i < col; i++)
			for (int j = 0; j < row; j++)
				arr[i][j] = cells[i][j].isColored;
		return arr;
	}

	public static boolean[][] load() {
		JFileChooser jfc = new JFileChooser(savePath);
		jfc.setDialogTitle("Open Problem");
		jfc.showOpenDialog(null);
		File loadFrom = jfc.getSelectedFile();
		if (loadFrom == null)
			return null;
		boolean[][] arr = null;
		try {
			ObjectInputStream is = new ObjectInputStream(new FileInputStream(loadFrom));
			Object o = is.readObject();
			if (o instanceof boolean[][])
				arr = (boolean[][]) o;
			else {
				is.close();
				throw new IOException(o.getClass() + " is not a valid type!!"); // TODO : try-catch-resource
			}
			is.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getClass() + ": " + e.getMessage(), "I/O Failed!",
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
		nowDoing = loadFrom.getName();
		edited = false;
		resetTitle();
		return arr;
	}

	public static void log(String a) {
		if (verbose)
			System.out.println(a);
	}
}

class Cell implements ActionListener {
	JButton btn = new JButton();
	int mode = 0; // 0 - white, 1 black, 2 = X
	boolean isColored;

	public Cell(int i, int j) {
		init();
		btn.addActionListener(this);
		btn.addKeyListener(new KeyIn());
	}

	public void init() {
		mode = 0;
		isColored = false;
		btn.setBackground(Color.WHITE);
		btn.setText("");
	}

	public void setColor(int newMode) {
		mode = newMode;
		isColored = mode == 1;
		btn.setBackground(mode == 1 ? Color.BLACK : Color.WHITE);
		btn.setText(mode == 2 ? "X" : "");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (NemoNemo.isColoringMode) {
			if (mode == 0) {
				if (!NemoNemo.isSolveMode)
					NemoNemo.edited = true;
				setColor(1);
			} else
				return;
		} else {
			if (NemoNemo.isSolveMode && mode == 0) {
				setColor(2);
			} else {
				if (!NemoNemo.isSolveMode && mode == 0)
					NemoNemo.edited = true;
				setColor(0);
			}
		}
		NemoNemo.scan();
	}
}

class KeyIn extends KeyAdapter {
	public KeyIn() {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
			NemoNemo.isColoringMode = !NemoNemo.isColoringMode;
			NemoNemo.resetTitle();
		}
	}
}

class StartNewMake implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
		NemoNemo.startNewMake(null);
	}
}

class StartNewSolve implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
		NemoNemo.startNewSolve(NemoNemo.load());
	}
}

class SaveAs implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
		NemoNemo.saveAs();
	}
}

class Load implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
		NemoNemo.startNewMake(NemoNemo.load());
	}
}
