package de.dis.menus;

import de.dis.FormUtil;
import de.dis.Menu;
import de.dis.data.Makler;

public class MainMenu {

    private final static String PW = "12345";
    /**
     * Zeigt das Hauptmen�
     */
    public static void showMainMenu() {
        //Men�optionen
        final int MENU_MAKLER = 1;
        final int MENU_ESTATE = 2;
        final int MENU_CONTRACT = 3;
        final int QUIT = 0;

        //Erzeuge Men�
        Menu mainMenu = new Menu("Main Menu");
        mainMenu.addEntry("Estate Agent Management", MENU_MAKLER);
        mainMenu.addEntry("Estate Management", MENU_ESTATE);
        mainMenu.addEntry("Contract Management", MENU_CONTRACT);
        mainMenu.addEntry("Quit", QUIT);

        //Verarbeite Eingabe												// Process input
        while(true) {
            int response = mainMenu.show();

            switch(response) {
                case MENU_MAKLER:
                    System.out.println("Please enter admin password for Makler:");
                    String password = FormUtil.readString("Password");
                    if (password.equals(PW)) {
                        MaklerMenu.showMaklerMenu();
                    } else {
                        System.err.println("Wrong password.");
                    }
                    break;
                case MENU_ESTATE:
                    System.out.println("Please login to Estate Management services:");
                    String login = FormUtil.readString("Login");
                    String password1 = FormUtil.readString("Password");
                    Makler makler = Makler.login(login, password1);

                    if (makler != null) {
                        new EstateMenu(makler).showEstateMenu();
                    } else {
                        System.out.println("Wrong login info");
                    }
                    break;
                case MENU_CONTRACT:
                    ContractMenu.show();
                    break;
                case QUIT:
                    return;
            }
        }
    }
}
