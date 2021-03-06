package de.dis.menus;

import de.dis.FormUtil;
import de.dis.Menu;
import de.dis.data.EstateAgent;

public class EstateAgentMenu {

    /**
     * Zeigt die Maklerverwaltung
     */
    public static void showMenu() {
        //Men�optionen
        final int NEW_MAKLER = 1;
        final int EDIT_MAKLER = 2;
        final int REMOVE_MAKLER = 3;
        final int BACK = 0;

        //Maklerverwaltungsmen�
        Menu maklerMenu = new Menu("Estate Agent Management");
        maklerMenu.addEntry("New Estate Agent", NEW_MAKLER);
        maklerMenu.addEntry("Edit Estate Agent", EDIT_MAKLER);
        maklerMenu.addEntry("Delete Estate Agent", REMOVE_MAKLER);
        maklerMenu.addEntry("Back to Main Menu", BACK);

        //Verarbeite Eingabe
        while(true) {
            int response = maklerMenu.show();

            switch(response) {
                case NEW_MAKLER:
                    newEstateAgent();
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

        EstateAgent m = EstateAgent.load(userFeedBack);

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

        if (EstateAgent.delete(userFeedBack)) {
            System.out.println("Estate agent is successfully removed");
        } else {
            System.err.println("Nothing to delete.");
        }
    }

    /**
     * Legt einen neuen Makler an, nachdem der Benutzer 			Create a new broker after the user entered the corresponding data.
     * die entprechenden Daten eingegeben hat.
     */
    public static void newEstateAgent() {
        EstateAgent m = new EstateAgent();

        m.setName(FormUtil.readString("Name"));
        m.setAddress(FormUtil.readString("Adress"));
        m.setLogin(FormUtil.readString("Login"));
        m.setPassword(FormUtil.readString("Password"));
        m.save();

        System.out.println("Estate Agent with ID "+m.getId()+" has been created.");
    }


}
