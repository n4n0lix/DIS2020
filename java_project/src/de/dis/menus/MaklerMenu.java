package de.dis.menus;

import java.sql.Connection;
import java.sql.PreparedStatement;

import de.dis.FormUtil;
import de.dis.Menu;
import de.dis.data.DBConnectionManager;
import de.dis.data.Makler;

public class MaklerMenu {

    /**
     * Zeigt die Maklerverwaltung
     */
    public static void showMaklerMenu() {
        //Men�optionen
        final int NEW_MAKLER = 1;
        final int EDIT_MAKLER = 2;
        final int REMOVE_MAKLER = 3;
        final int BACK = 0;

        //Maklerverwaltungsmen�
        Menu maklerMenu = new Menu("Makler-Verwaltung");
        maklerMenu.addEntry("New Makler", NEW_MAKLER);
        maklerMenu.addEntry("Edit Makler", EDIT_MAKLER);
        maklerMenu.addEntry("Delete Makler", REMOVE_MAKLER);
        maklerMenu.addEntry("Back to Main Menu", BACK);

        //Verarbeite Eingabe
        while(true) {
            int response = maklerMenu.show();

            switch(response) {
                case NEW_MAKLER:
                    newMakler();
                    break;
                case EDIT_MAKLER:
                    editMakler();
                    break;
                case REMOVE_MAKLER:
                    removeMakler();
                    break;
                case BACK:
                    return;
            }
        }
    }

    public static void editMakler() {
        int userFeedBack = FormUtil.readInt("Estate agent id");

        Makler m = Makler.load(userFeedBack);

        if (m == null) {
            System.err.println("No estate agent has this id.");
        } else {
            System.out.println(m.toString());
            System.out.println("Update old information: ");

            m.setName(FormUtil.readString("Name"));
            m.setAddress(FormUtil.readString("Address"));
            m.setLogin(FormUtil.readString("Login"));
            m.setPassword(FormUtil.readString("Password"));

            m.save();
            System.out.println(m.toString());
            System.out.println("Update is successful");
        }
    }

    public static void removeMakler() {
        int userFeedBack = FormUtil.readInt("Estate agent id");

        if (Makler.delete(userFeedBack)) {
            System.out.println("Makler is successfully removed");
        } else {
            System.err.println("Nothing to delete.");
        }
    }

    /**
     * Legt einen neuen Makler an, nachdem der Benutzer 			Create a new broker after the user entered the corresponding data.
     * die entprechenden Daten eingegeben hat.
     */
    public static void newMakler() {
        Makler m = new Makler();

        m.setName(FormUtil.readString("Name"));
        m.setAddress(FormUtil.readString("Adresse"));
        m.setLogin(FormUtil.readString("Login"));
        m.setPassword(FormUtil.readString("Passwort"));
        m.save();

        System.out.println("Makler mit der ID "+m.getId()+" wurde erzeugt.");
    }


}
