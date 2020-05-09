package de.dis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Kleine Helferklasse für Menüs
 * Little helper class for menus
 * Zuvor müssen mit addEntry Menüoptionen hinzugefügt werden.
 * You must first add menu options with addEntry.
 * Mit der Methode show() wird das Menü angezeigt und die mit der Option
 * The show () method displays the menu and returns the constant specified by the option.
 * angegebene Konstante zurückgeliefert.
 *
 * Beispiel:																example
 * Menu m = new Menu("Hauptmenü");											Menu m = new Menu("main menu");
 * m.addEntry("Hart arbeiten", 0);											m.addEntry("Hard work", 0);
 * m.addEntry("Ausruhen", 1);												m.addEntry("relax", 1);
 * m.addEntry("Nach Hause gehen", 2);										m.addEntry("Go home", 2);
 * int wahl = m.show();														int choice = m.show();
 *
 * Angezeigt wird dann das Menü:
 * The menu will be displayed:
 * Hauptmenü:
 * main menu
 * [1] Hart arbeiten
 * [2] Ausruhen
 * [3] Nach Hause gehen
 * --
 */
public class Menu {
	private String title;
	private ArrayList<String> labels = new ArrayList<String>();
	private ArrayList<Integer> returnValues = new ArrayList<Integer>();

	/**
	 * Konstruktor.
	 * @param title Titel des Menüs z.B. "Hauptmenü"
	 */
	public Menu(String title) {
		super();
		this.title = title;
	}

	/**
	 * Fügt einen Menüeintrag zum Menü hinzu
	 * Adds a menu item to the menu
	 * @param label Name des Eintrags
	 * label Name of the entry
	 * @param returnValue Konstante, die bei Wahl dieses Eintrags zurückgegeben wird
	 * returnValue Constant returned when this entry is selected
	 */
	public void addEntry(String label, int returnValue) {
		this.labels.add(label);
		this.returnValues.add(new Integer(returnValue));
	}

	/**
	 * Zeigt das Menü an
	 * Displays the menu
	 * @return Die Konstante des ausgewählten Menüeintrags
	 * The constant of the selected menu item
	 */
	public int show()  {
		int selection = -1;

		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

		while(selection == -1) {
			System.out.println(title+":");

			for(int i = 0; i < labels.size(); ++i) {
				System.out.println("["+(i+1)+"] "+labels.get(i));
			}

			System.out.print("-- ");
			try {
				selection = Integer.parseInt(stdin.readLine());
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if(selection < 1 || selection > returnValues.size()) {
				System.err.println("Ungültige Eingabe!");				// invalid input
				selection = -1;
			}
		}

		return returnValues.get(selection-1);
	}
}
