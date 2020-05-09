package de.dis.menus;

import de.dis.Menu;
import de.dis.data.Person;
import de.dis.data.PurchaseContract;
import de.dis.data.TenancyContract;

public class ContractMenu {
    public static void show() {
        final int CREATE_PERSON = 0;
        final int CREATE_PURCHASE_CONTRACT = 1;
        final int CREATE_TENANCY_CONTRACT = 2;
        final int PRINT_ALL_CONTRACTS = 3;
        final int BACK = 4;

        Menu menu = new Menu("Contract Management");
        menu.addEntry("Create person", CREATE_PERSON);
        menu.addEntry("Create purchase contract", CREATE_PURCHASE_CONTRACT);
        menu.addEntry("Create tenancy contract", CREATE_TENANCY_CONTRACT);
        menu.addEntry("Print all contracts", PRINT_ALL_CONTRACTS);
        menu.addEntry("Back to main menu", BACK);

        while(true) {
            int response = menu.show();

            switch(response) {
                case CREATE_PERSON:
                    createPerson();
                    break;
                case CREATE_PURCHASE_CONTRACT:
                    createPurchaseContract();
                    break;
                case CREATE_TENANCY_CONTRACT:
                    createTenancyContract();
                    break;
                case PRINT_ALL_CONTRACTS:
                    printAllContracts();
                    break;
                case BACK:
                    return;
            }
        }
    }

    private static void createPerson() {
        Person person = new Person();

        person.update();
        person.save();

        System.out.println("Person id " + person.getId() + " is created.");
    }

    private static void createPurchaseContract() {
        PurchaseContract purchase = new PurchaseContract();

        purchase.update();
        purchase.save();

        System.out.println("Purchase contract id " + purchase.getId() + " is created.");
    }

    private static void createTenancyContract() {
        TenancyContract tenancy = new TenancyContract();

        tenancy.update();
        tenancy.save();

        System.out.println("Tenancy contract id " + tenancy.getId() + " is created.");
    }

    private static void printAllContracts() {
        System.out.println("Purchase contract:");
        PurchaseContract.print();
        System.out.println("Tenancy contract:");
        TenancyContract.print();
    }
}
