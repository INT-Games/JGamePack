/*
 * Copyright (c) 2023 Eugene Hong
 *
 * This software is distributed under license. Use of this software
 * implies agreement with all terms and conditions of the accompanying
 * software license.
 * Please refer to LICENSE
 * */

package numBaseBall;

import java.util.*;

public class NumBaseBall {
	private static final String version = "1.0";
	private static final int N = 4;
	private static final String p2Prefix = "\t\t\t\t\t\t\t\t\t";
	private static int[] p1 = new int[N];
	private static int[] p2 = new int[N];
	private static Scanner sc = new Scanner(System.in);

	public static void main(String[] args) {
		System.out.println("NumBaseBall " + version + " with " + N + " numbers\n");
		getAnswer(1, p1);
		getAnswer(2, p2);
		System.out.println();
		System.out.println("Game Start!\n");
		boolean plturn = true;
		while (true) {
			if (!plturn)
				System.out.print(p2Prefix);
			int[] g = getGuessInput("[Player " + (plturn ? "1" : "2") + "] Enter your guess: ");
			if (g == null) {
				System.out.println("Player " + (plturn ? "1" : "2") + " gave up!!");
				System.out.print("\n\nPlayer 1's number: ");
				for (int i = 0; i < N; i++)
					System.out.print(p1[i] + " ");
				System.out.print("\nPlayer 2's number: ");
				for (int i = 0; i < N; i++)
					System.out.print(p2[i] + " ");
				System.out.println();
				return;
			}
			int strike = 0, ball = 0;
			for (int i = 0; i < N; i++) {
				if (g[i] == (!plturn ? p1 : p2)[i]) {
					strike++;
				} else {
					for (int j = 0; j < N; j++) {
						if (g[i] == (!plturn ? p1 : p2)[j]) {
							ball++;
						}
					}
				}
			}
			if (!plturn)
				System.out.print(p2Prefix);
			if (strike == N) {
				System.out.println("[Player " + (plturn ? "1" : "2") + "] WINS!");
				System.out.print("\n\nPlayer 1's number : ");
				for (int i = 0; i < N; i++)
					System.out.print(p1[i] + " ");
				System.out.print("\nPlayer 2's number : ");
				for (int i = 0; i < N; i++)
					System.out.print(p2[i] + " ");
				return;
			} else if (strike == 0 && ball == 0)
				System.out.println("result : OUT!");
			else
				System.out.println("result : " + strike + " strike, " + ball + " ball!");
			plturn = !plturn;
		}
	}

	private static void getAnswer(int p, int[] arr) {
		while (true) {
			System.out.print(" [Player " + p + "] Enter your numbers: ");
			String s = new String(System.console().readPassword());
			//String s = new Scanner(System.in).nextLine();
			try {
				for (int i = 0; i < N; i++) {
					int n = Integer.parseInt(s.substring(i, i + 1));
					for (int j = 0; j < i; j++)
						if (n == arr[j])
							throw new Exception("no same number!");
					arr[i] = n;
				}
				return;
			} catch (NumberFormatException e) {
				System.out.println(
						"Invalid input! - " + e.getMessage() + " - Only a number between 0 and 9 acceptable!\n");
			} catch (Exception e) {
				System.out.println("Invalid input! - ' " + e.getMessage() + "\n");
			}
		}
	}

	private static int[] getGuessInput(String prompt) {
		int[] ret = new int[N];
		while (true) {
			System.out.print(prompt);
			String s = sc.nextLine();
			if ("give up".equals(s))
				return null;
			try {
				for (int i = 0; i < N; i++) {
					int n = Integer.parseInt(s.substring(i, i + 1));
					for (int j = 0; j < i; j++)
						if (n == ret[j])
							throw new Exception("no same number!");
					ret[i] = n;
				}
				return ret;
			} catch (NumberFormatException e) {
				System.out.println(
						"Invalid input! - " + e.getMessage() + " - Only a number between 0 and 9 acceptable!\n");
			} catch (Exception e) {
				System.out.println("Invalid input! - " + e.getMessage() + "\n");
			}
		}
	}
}
