package de.dis.menus;

import de.dis.FormUtil;
import de.dis.Menu;
import de.dis.data.Apartment;
import de.dis.data.House;
import de.dis.data.Makler;

public class EstateMenu {

    private Makler makler;

    public EstateMenu(Makler makler) {
        this.makler = makler;
    }

    public void showEstateMenu() {
        final int NEW_HOUSE = 1;
        final int EDIT_HOUSE = 2;
        final int REMOVE_HOUSE = 3;
        final int NEW_APARTMENT = 4;
        final int EDIT_APARTMENT = 5;
        final int REMOVE_APARTMENT = 6;
        final int BACK = 0;

        Menu estateMenu = new Menu("Estate Menu");

        estateMenu.addEntry("Create New House", NEW_HOUSE);
        estateMenu.addEntry("Edit House", EDIT_HOUSE);
        estateMenu.addEntry("Delete House", REMOVE_HOUSE);

        estateMenu.addEntry("Create New Apartment", NEW_APARTMENT);
        estateMenu.addEntry("Edit Apartment", EDIT_APARTMENT);
        estateMenu.addEntry("Delete Apartment", REMOVE_APARTMENT);

        estateMenu.addEntry("Back to Main Menu", BACK);

        while (true) {
            int response = estateMenu.show();

            switch (response) {
                case NEW_HOUSE:
                    newHouse();
                    break;
                case EDIT_HOUSE:
                    editHouse();
                    break;
                case REMOVE_HOUSE:
                    removeHouse();
                    break;
                case NEW_APARTMENT:
                    newApartment();
                    break;
                case EDIT_APARTMENT:
                    editApartment();
                    break;
                case REMOVE_APARTMENT:
                    removeApartment();
                    break;
                case BACK:
                    return;
            }
        }
    }

    public void newHouse() {
        House house = new House();
        house.update(makler.getId());
        house.save();

        System.out.println(house.toString());
    }

    private void editHouse() {
        int id = FormUtil.readInt("House id");

        House house = House.load(id);
        if (house == null) {
            System.out.println("Cannot find house with ID : " + id);
        } else {
            System.out.println(house.toString());
            System.out.println("Update information: ");

            house.update(makler.getId());
            house.save();
            System.out.println(house.toString());
        }
    }

    private void removeHouse() {
        // TODO Auto-generated method stub
        int id = FormUtil.readInt("House Id");

        if (House.delete(id)) {
            System.out.println("House with the id " + id + " was deleted.");
        } else {
            System.out.println("Failed to delete house with the id " + id + ".");
        }
    }

    private void newApartment() {
        // TODO Auto-generated method stub
        Apartment apartment = new Apartment();

        apartment.update(makler.getId());
        apartment.save();

        System.out.println("Apartment with the id " + apartment.getId() + " was created.");

    }

    private void editApartment() {
        // TODO Auto-generated method stub
        int id = FormUtil.readInt("Apartment Id");

        Apartment apartment = Apartment.load(id);

        if (apartment == null) {
            System.out.println("Cannot find apartment with the id " + id);
        } else {
            System.out.println(apartment.toString());

            System.out.println("Enter the new values:");

            apartment.update(makler.getId());
            apartment.save();

            System.out.println("Apartment id " + apartment.getId() + " is changed.");
        }

    }

    private void removeApartment() {
        // TODO Auto-generated method stub
        int id = FormUtil.readInt("Apartment Id");

        if (Apartment.delete(id)) {
            System.out.println("Apartment id " + id + " is deleted.");
        } else {
            System.out.println("Failed to delete apartment id " + id + ".");
        }
    }

}
