package de.dis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Date;
import java.sql.Time;

/**
 * Kleine Helferklasse zum Einlesen von Formulardaten Small helper class for
 * reading form data
 */
public class FormUtil {
	/**
	 * Liest einen String vom standard input ein Reads a string from the standard
	 * input
	 *
	 * @param label
	 *            Zeile, die vor der Eingabe gezeigt wird label line that is shown
	 *            before entering
	 * @return eingelesene Zeile read line
	 */
	public static String readString(String label) {
		String ret = null;
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

		try {
			System.out.print(label + ": ");
			ret = stdin.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ret;
	}

	/**
	 * Liest einen Integer vom standard input ein
	 *
	 * @param label
	 *            Zeile, die vor der Eingabe gezeigt wird
	 * @return eingelesener Integer
	 */
	public static int readInt(String label) {
		int ret = 0;
		boolean finished = false;

		while (!finished) {
			String line = readString(label);

			try {
				ret = Integer.parseInt(line);
				finished = true;
			} catch (NumberFormatException e) {
				System.err.println("Input is invalid Integer");
			}
		}

		return ret;
	}

	public static double readDouble(String label) {
		double ret = 0;
		boolean finished = false;

		while (!finished) {
			String line = readString(label);

			try {
				ret = Double.parseDouble(line);
				finished = true;
			} catch (NumberFormatException e) {
				System.err.println("Input is invalid Double");
			}
		}

		return ret;
	}

	public static boolean readBoolean(String label) {
		boolean ret = false;
		boolean finished = false;

		while (!finished) {
			String line = readString(label);

			try {
				ret = Boolean.parseBoolean(line);
				finished = true;
			} catch (NumberFormatException e) {
				System.err.println("Input is invalid String");
			}
		}

		return ret;
	}

	public static Date readDate(String label) {
		Date ret = null;
		boolean finished = false;

		while (!finished) {
			String line = readString(label);

			try {
				ret = Date.valueOf(line);
				finished = true;
			} catch (NumberFormatException e) {
				System.err.println("Input is invalid Date");
			}
		}

		return ret;
	}

	public static Time readTime(String label) {
		Time ret = null;
		boolean finished = false;

		while (!finished) {
			String line = readString(label);

			try {
				ret = Time.valueOf(line);
				finished = true;
			} catch (NumberFormatException e) {
				System.err.println("Input is invalid Date");
			}
		}

		return ret;
	}
}
