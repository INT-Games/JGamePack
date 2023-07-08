/*
 * Copyright (c) 2023 Eugene Hong
 *
 * This software is distributed under license. Use of this software
 * implies agreement with all terms and conditions of the accompanying
 * software license.
 * Please refer to LICENSE
 * */

package tetris;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Random;
import java.util.Arrays;

// graphic으로 그려서 pointStr 나타낼까?
public class Tetris {
	public static final String version = "v1.1";
	static final int hLen = 18;
	static final int wLen = 10;
	static boolean verbose = false;
	static JFrame frame;
	static boolean gameOver = false;
	static boolean paused = false;
	private static long waited = 0;
	static Block nowBlock = null;
	static long loopTime;
	static int point = 0;
	static String[] pointStr = new String[] { "", " (Single!)", " (Double!)", " (Triple!)", " (Tetris!)" };
	static int pointStrWait = 0;
	static Cell[][] cells = new Cell[hLen][wLen];
	static JMenuItem restart;
	static JMenuItem pause;
	static JMenuItem showLeaderBoard;

	public static void main(String[] args) {
		HighScore.readLeaderBoard();
		init();
		frame.setVisible(true);
		startNewGame();
	}

	public static void init() {
		int w = 386;
		int h = 694;// 61
		frame = new JFrame("Tetris" + version);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new GridLayout(hLen, wLen));
		frame.setSize(w, h);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setBounds(d.width / 2 - w / 2, d.height / 2 - h / 2, w, h);
		frame.addKeyListener(new KeyIn());
		for (int i = 0; i < hLen; i++)
			for (int j = 0; j < wLen; j++)
				cells[i][j] = new Cell(i, j);
		for (int i = 0; i < hLen; i++)
			for (int j = 0; j < wLen; j++)
				frame.add(cells[i][j].btn);
		JMenuBar mb = new JMenuBar();
		JMenu m = new JMenu("Game");
		restart = new JMenuItem("Start New Game");
		restart.setMnemonic(KeyEvent.VK_S);
		restart.addActionListener(new StartNewGame());
		pause = new JMenuItem("Pause");
		pause.setMnemonic(KeyEvent.VK_P);
		pause.addActionListener(new Pause());
		showLeaderBoard = new JMenuItem("LeaderBoard");
		showLeaderBoard.setMnemonic(KeyEvent.VK_L);
		showLeaderBoard.addActionListener(new LeaderBoard());
		m.add(restart);
		m.add(pause);
		m.add(showLeaderBoard);
		mb.add(m);
		frame.setJMenuBar(mb);
	}

	public static void startNewGame() {
		gameOver = false;
		paused = false;
		waited = 0;
		point = 0;
		pointStrWait = 0;
		frame.setTitle("Tetris " + version + " - " + point + "pts");
		nowBlock = nextBlock();
		for (int i = 0; i < hLen; i++)
			for (int j = 0; j < wLen; j++)
				cells[i][j].clear();
		loopTime = System.currentTimeMillis();
		loop();
	}

	public static void gameOver() {
		gameOver = true;
		frame.setTitle(frame.getTitle() + " (GAME OVER)");
		JOptionPane.showMessageDialog(null, "Game Over! \nPoints: " + point, "Game Over!",
				JOptionPane.INFORMATION_MESSAGE);

		if (point >= HighScore.get(HighScore.LENGTH - 1).point) {
			String name = JOptionPane.showInputDialog(null, "Enter Your Name", "New High Score!",
					JOptionPane.QUESTION_MESSAGE);
			if (name == null)
				return;
			HighScore.put(name, point);
			HighScore.writeLeaderBoard();
		}
	}

	public static void loop() {
		if (gameOver)
			return;
		loopOnce();
		SwingUtilities.invokeLater(new Loop());
	}

	public static void loopOnce() {
		long interval = System.currentTimeMillis() - loopTime;
		int speed = point > 400 ? (point > 700 ? (point / 100 - 2) * 100 : 500) : 0;
		if (interval >= 1000 - speed && !paused) {
			log("loop IN");
			if (++pointStrWait > 2)
				frame.setTitle("Tetris " + version + " - " + point + "pts");
			Position[] pos = nowBlock.getPos();
			boolean needNewBlock = false;
			nowBlock.blockOff();
			for (int i = 0; i < pos.length; i++)
				if (isCollid(pos[i].plus(new Position(0, 1))))
					needNewBlock = true;

			if (needNewBlock) {
				boolean hitCelling = isCollid(pos);
				for (int i = 0; i < pos.length; i++)
					if (isCollidWall(pos[i].plus(new Position(0, -1))))
						hitCelling = true;

				nowBlock.blockOn();
				if (hitCelling) {
					log("gameover: hit the ceiling!");
					paint();
					gameOver();
					return;
				}
				scan();
				nowBlock = nextBlock();
				pos = nowBlock.getPos();
				if (isCollid(pos)) {
					log("gameover: clipped!");
					nowBlock.blockOn();
					paint();
					gameOver();
					return;
				}
				nowBlock.blockOn();
			} else {
				nowBlock.down();
			}

			log("needNewBlock : " + needNewBlock);

			nowBlock.blockOn();
			paint();
			loopTime = System.currentTimeMillis();
			log("loop OUT");
		}
	}

	public static void log(boolean a) {
		log(String.valueOf(a));
	}

	public static void log(String a) {
		if (verbose)
			System.out.println(a);
	}

	public static Block nextBlock() {
		return Block.getBlock(new Random().nextInt(7));
	}

	public static Cell getCell(Position pos) {
		return getCell(pos.x, pos.y);
	}

	public static Cell getCell(int x, int y) { //TODO : 여기 다시 보기
//log("x : " + x + "y: " + y + null: " + ! (0 <= x && x < wLen && 0 <= y && y < hLen));
		//System.out.println(x + " / " + y); //TODO : debug
		return 0 <= x && x < wLen && 0 <= y && y < hLen ? cells[y][x] : null;
	}

	public static void scan() {
		int inARow = 0;
		for (int i = hLen - 1; i > -1; i--) {
			boolean result = true;
			for (int j = 0; j < wLen; j++)
				if (cells[i][j].isEmpty)
					result = false;
			if (result) {
				inARow++;
				log("row " + (hLen - i) + "is lined! (" + inARow + " in a row)");
				for (int ii = i; ii > -1; ii--)
					for (int j = 0; j < wLen; j++) {
						cells[ii][j].clear();
						if (ii != 0)
							cells[ii][j].setColor(cells[ii - 1][j].getColor());
					}
				paint();
				i++;
			}
		}

		if (inARow > 0) {
			point += inARow * 10 + (inARow - 1) * 10;
			pointStrWait = 0;
		}
		frame.setTitle("Tetris " + version + " - " + point + "pts" + pointStr[inARow]);
	}

	public static void paint() {
		for (int i = 0; i < hLen; i++)
			for (int j = 0; j < wLen; j++)
				cells[i][j].paint();
	}

	public static boolean isCollidWall(Position pos) {
		return getCell(pos) == null;
	}

	public static boolean isCollid(Position pos) {
		Cell cell = getCell(pos);
		return cell == null || !cell.isEmpty;
	}

	public static boolean isCollid(Position[] pos) {
		boolean ret = false;
		for (int i = 0; i < pos.length; i++)
			if (isCollid(pos[i]))
				ret = true;
		return ret;
	}

	public static void pause() {
		if (paused) {
			frame.setTitle(frame.getTitle().replace(" (PAUSED)", ""));
			pause.setText("Pause");
			loopTime = System.currentTimeMillis() - waited;
		} else {
			frame.setTitle(frame.getTitle() + " (PAUSED) ");
			pause.setText("Restart");
			waited = System.currentTimeMillis() - loopTime;
		}
		paused = !paused;
	}
}

class Cell {
	JButton btn = new JButton();
	boolean isEmpty = true;
	Color color;
	int i;
	int j;

	public Cell(int i, int j) {
		clear();
		btn.setEnabled(false);
		this.i = i;
		this.j = j;
	}

	public void clear() {
		isEmpty = true;
		btn.setBackground((color = Color.WHITE));
	}

	public void setColor(Color c) {
		isEmpty = (c == Color.WHITE);
		color = c;
	}

	public Color getColor() {
		return color;
	}

	public void paint() {
		btn.setBackground(color);
	}
}

class Block {
//public static Block [] blocks = new Block [1 (A, B, C, D, E, F, G);
	private Color color;
	Position rootPosition; // Position of root
	private Position[] add; // change of root coordinates added when rotated. length should be 4
	private Position[] arr; // coordinates representing default shape of blocks
	private int rotate = 0; // 0, 1, 2, 3

	private Block(Position[] add, Position[] arr, Color color) {
		if (add.length != 4)
			throw new IllegalArgumentException("lenght of \"add\" should be 4");
		this.add = add;
		this.arr = arr;
		this.color = color;
		this.rootPosition = new Position(Tetris.wLen / 2, getRootPos());
	}

	public void blockOn() {
		Position[] pos = getPos();
		for (int i = 0; i < pos.length; i++)
			Tetris.getCell(pos[i]).setColor(getColor());
	}

	public void blockOff() {
		Position[] pos = getPos();
		for (int i = 0; i < pos.length; i++)
			Tetris.getCell(pos[i]).clear(); 
	}

	public void down() {
		rootPosition.add(new Position(0, 1));
	}

	public void right() {
		rootPosition.add(new Position(1, 0));
	}

	public void left() {
		rootPosition.add(new Position(-1, 0));
	}

	public void rotate() {
		rotate++;
		if (rotate > 3)
			rotate = 0;
	}

	public void rotateRev() {
		rotate--;
		if (rotate < 0)
			rotate = 3;
	}

	public Position[] getPos() {
		Position[] ret = new Position[arr.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = rootPosition.plus(add[rotate]);
		}
		switch (rotate) {
		case 0:
			for (int i = 0; i < ret.length; i++) {
				ret[i].x += arr[i].x;
				ret[i].y += arr[i].y;
			}
			break;

		case 1:
			for (int i = 0; i < ret.length; i++) {
				ret[i].x += arr[i].y;
				ret[i].y -= arr[i].x;
			}
			break;

		case 2:
			for (int i = 0; i < ret.length; i++) {
				ret[i].x -= arr[i].x;
				ret[i].y -= arr[i].y;
			}
			break;

		case 3:
			for (int i = 0; i < ret.length; i++) {
				ret[i].x -= arr[i].y;
				ret[i].y += arr[i].x;
			}
			break;

		default:
			System.out.println("Invalide rotate value : " + rotate);
			return null;
		}
		return ret;
	}

	public int getRootPos() {
		int min = Integer.MAX_VALUE;
		for (Position p : arr) {
			min = min > p.y ? p.y : min;
		}
		return -min;
	}

	public Color getColor() {
		return color;
	}

	/**
	 * OO 
	 * 0O
	 * 
	 * O1OO
	 * 
	 *  O  O     O
	 * O2O O3O O4O
	 * 
	 * OO   OO
	 *  50 06
	 */
	public static Block getBlock(int i) {
		Tetris.log("New Block : " + i);
		switch (i) {
		case 0:
			return new Block(
					new Position[] { new Position(0, 0), new Position(1, 0), new Position(1, -1), new Position(0, -1) },
					new Position[] { new Position(0, 0), new Position(1, 0), new Position(1, -1), new Position(0, -1) },
					Color.RED);
		case 1:
			return new Block(
					new Position[] { new Position(0, 0), new Position(0, 0), new Position(1, 0), new Position(1, -1) },
					new Position[] { new Position(0, 0), new Position(-1, 0), new Position(1, 0), new Position(2, 0) },
					Color.GREEN);
		case 2:
			return new Block(
					new Position[] { new Position(0, 0), new Position(0, 0), new Position(0, 0), new Position(0, 0) },
					new Position[] { new Position(0, 0), new Position(-1, 0), new Position(1, 0), new Position(0, -1) },
					Color.ORANGE);
		case 3:
			return new Block(
					new Position[] { new Position(0, 0), new Position(0, 0), new Position(0, 0), new Position(0, 0) },
					new Position[] { new Position(0, 0), new Position(-1, 0), new Position(1, 0), new Position(1, -1) },
					Color.PINK);
		case 4:
			return new Block(
					new Position[] { new Position(0, 0), new Position(0, 0), new Position(0, 0), new Position(0, 0) },
					new Position[] { new Position(0, 0), new Position(-1, 0), new Position(1, 0),
							new Position(-1, -1) },
					Color.MAGENTA);
		case 5:
			return new Block(
					new Position[] { new Position(0, 0), new Position(0, 0), new Position(0, -1), new Position(0, 0) },
					new Position[] { new Position(0, 0), new Position(1, 0), new Position(0, -1),
							new Position(-1, -1) },
					Color.CYAN);
		case 6:
			return new Block(
					new Position[] { new Position(0, 0), new Position(0, 0), new Position(0, -1), new Position(0, 0) },
					new Position[] { new Position(0, 0), new Position(-1, 0), new Position(1, -1),
							new Position(0, -1) },
					Color.BLUE);
		default:
			System.out.println("Invalide Block number: " + i);
			return null;
		}
	}
}

class Position {
	int x, y;

	public Position() {
		this(0, 0);
	}

	public Position(int a, int b) {
		x = a;
		y = b;
	}

	public void add(Position p) {
		x += p.x;
		y += p.y;
	}

	public Position plus(Position p) {
		return new Position(x + p.x, y + p.y);
	}
}

class Loop implements Runnable {
	@Override
	public void run() {
		Tetris.loop();
	}
}

class KeyIn extends KeyAdapter {
	@Override
	public void keyPressed(KeyEvent e) {
		// if (e.getKeyCode () ==KeyEvent.VK_T)
		// Tetris.frame.setTitle(Tetris.frame.getHeight()+"
		// "+Tetris.frame.getWidth()+"/"+Tetris cells[0][0].btn.getHeight()+"
		// "+Tetris.cells[0][0].btn.getWidth());
		if (Tetris.gameOver || (Tetris.paused && e.getKeyCode() != KeyEvent.VK_ESCAPE))
			return;

		Tetris.log("keyStroke IN!");
		Position[] pos = Tetris.nowBlock.getPos();
		Tetris.nowBlock.blockOff();
		boolean iscolid = false;
		switch (e.getKeyCode()) {
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_A:
			for (int i = 0; i < pos.length; i++)
				if (Tetris.isCollid(pos[i].plus(new Position(-1, 0))))
					iscolid = true;
			if (!iscolid)
				Tetris.nowBlock.left();
			break;
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_D:
			for (int i = 0; i < pos.length; i++)
				if (Tetris.isCollid(pos[i].plus(new Position(1, 0))))
					iscolid = true;
			if (!iscolid)
				Tetris.nowBlock.right();
			break;
		case KeyEvent.VK_DOWN:
		case KeyEvent.VK_S:
			for (int i = 0; i < pos.length; i++)
				if (Tetris.isCollid(pos[i].plus(new Position(0, 1))))
					iscolid = true;
			if (!iscolid)
				Tetris.nowBlock.down();
			break;
		case KeyEvent.VK_Z:
		case KeyEvent.VK_Q:
			Tetris.nowBlock.rotate();
			pos = Tetris.nowBlock.getPos();
			for (int i = 0; i < pos.length; i++)
				if (Tetris.isCollid(pos[i]))
					iscolid = true;
			if (iscolid)
				Tetris.nowBlock.rotateRev();
			break;
		case KeyEvent.VK_X:
		case KeyEvent.VK_E:
			Tetris.nowBlock.rotateRev();
			pos = Tetris.nowBlock.getPos();
			for (int i = 0; i < pos.length; i++)
				if (Tetris.isCollid(pos[i]))
					iscolid = true;
			if (iscolid)
				Tetris.nowBlock.rotate();
			break;
		case KeyEvent.VK_SPACE:
			while (!iscolid) {
				for (int i = 0; i < pos.length; i++)
					if (Tetris.isCollid(pos[i].plus(new Position(0, 1))))
						iscolid = true;
				if (!iscolid) {
					Tetris.nowBlock.down();
					pos = Tetris.nowBlock.getPos();
				}
			}
			Tetris.loopTime -= 1000;
			Tetris.loopOnce();
			break;
		case KeyEvent.VK_ESCAPE:
			Tetris.pause();
			break;
		}
		Tetris.nowBlock.blockOn();
		Tetris.paint();
		Tetris.log("keyStroke OUT!");
	}
}

class StartNewGame implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
		Tetris.startNewGame();
	}
}

class Pause implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
		Tetris.pause();
	}
}

class LeaderBoard implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
		JOptionPane.showMessageDialog(null, HighScore.getString(), "Leader Board", JOptionPane.PLAIN_MESSAGE);
	}
}

class HighScore {
	public static final int LENGTH = 21;
	private static Score[] leaderBoard = new Score[LENGTH]; // 21st tail is for storing new high score before sorting
															// the array (hence avoid doing insert logic)
	private static final String encodeCharset = "UTF-16";
	private static final String outputCharset = "UTF-8";
	private static String delimiter = "\n";
	private static String key = null;
	static final String leaderBoardPath = "TetrisHighScore.bin"; // TODO: let user choose

	public static void put(String name, int point) {
//Put object at tail
		leaderBoard[LENGTH - 1] = new Score(name, point);
	}

	public static Score get(int i) {
		return leaderBoard[i];
	}

	public static void readLeaderBoard() {
		try {
			BufferedReader br = new BufferedReader(
					new InputStreamReader(new FileInputStream(leaderBoardPath), outputCharset));
			key = br.readLine(); // TODO: base64
			for (int i = 0; i < LENGTH; i++) {
				String str = HighScore.decode(br.readLine().toCharArray(), i);
				leaderBoard[i] = new Score(str.substring(str.indexOf(delimiter) + 1),
						Integer.parseInt(str.substring(0, str.indexOf(delimiter))));
			}
			br.close(); // Possible leak ignored..
		} catch (UnsupportedEncodingException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Unable to use charset " + encodeCharset + "!",
					JOptionPane.ERROR_MESSAGE);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getClass() + " : " + e.getMessage(), "Cannot Read LeaderBoard!",
					JOptionPane.WARNING_MESSAGE);
			setDefaultArray();
		}
	}

	public static void writeLeaderBoard() {
		Arrays.sort(leaderBoard);
		new File(leaderBoardPath).delete();
		try {
			PrintWriter pw = new PrintWriter(new File(leaderBoardPath), outputCharset);
			pw.println((key = String.valueOf(System.currentTimeMillis())));
			for (int i = 0; i < LENGTH; i++) {
				byte[] buf = (leaderBoard[i].point + delimiter + leaderBoard[i].name).getBytes(encodeCharset);
				pw.println(encode(buf, i));
			}
			pw.close(); // Possible leak ignored..
		} catch (UnsupportedEncodingException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Unable to use charset " + encodeCharset + "!",
					JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Cannot Write LeaderBoard!", JOptionPane.ERROR_MESSAGE);
		}
	}

	public static String encode(byte[] buf, int cnt) throws UnsupportedEncodingException {
		char[] arr = new char[buf.length];
		for (int i = 0; i < buf.length; i++)
			arr[i] = (char) (buf[i] + getRand(cnt));
		return new String(arr);
	}

	public static String decode(char[] arr, int cnt) throws UnsupportedEncodingException {
		byte[] buf = new byte[arr.length];
		for (int i = 0; i < arr.length; i++)
			buf[i] = (byte) (arr[i] - getRand(cnt));
		return new String(buf, 0, buf.length, encodeCharset);
	}

	private static int getRand(int cnt) throws UnsupportedEncodingException {
		float num = new Random(Long.parseLong(key) * (long) cnt).nextFloat();
//num = num < 0 ? -num : num; num = num % 100000000; //cutout number for better random number
		return (int) (44162 + ((55075 - 44162 + 1) * num));
	}

	public static String getString() {

		StringBuilder sb = new StringBuilder();
		int longSc = 3;
		int cnt = 0;
		for (int i = 0; i < 20; i++)
			longSc = Math.max(longSc, String.valueOf(leaderBoard[i].point).length());
		for (int i = 0; i < 20; i++)
			sb.append(String.format("%02d", ++cnt)).append(".  ")
					.append(String.format("%0" + longSc + "dpts", leaderBoard[i].point)).append("-")
					.append(leaderBoard[i].name).append("\n");
		return sb.toString();
	}

	private static void setDefaultArray() {
		Arrays.fill(leaderBoard, new Score("???", 0));
	}

	static class Score implements Comparable<Score> {
		String name;
		int point;

		public Score(String s, int p) {
			name = s;
			point = p;
		}

		@Override
		public int compareTo(Score h) {
			if (point < h.point)
				return 1;
			else if (point > h.point)
				return -1;
			else {
				if (this.name.equals("???"))
					return 1;
				else if (h.name.equals("???"))
					return -1;
				else
					return 0;
			}
		}
	}
}
