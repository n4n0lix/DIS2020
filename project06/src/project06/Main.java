package project06;

import project06.warehouse.Sales;
import project06.warehouse.Product;
import project06.warehouse.Store;

public class Main {

  public static void main(String[] args) {
    // #1 Import SQL into warehouse
    System.out.println("=== SQL Import ===");
    System.out.println("importing products ...");
    var products = Product.Import();
    int storedProducts = Product.StoreInWarehouse(products);
    System.out.println("✓ (" + storedProducts + "/" + products.size() + " products)");

    System.out.println("importing stores ...");
    var stores = Store.Import();
    int storedStores = Store.StoreInWarehouse(stores);
    System.out.println("✓ (" + storedStores + "/" + stores.size() + " stores)");

    // #2 Import CSV into warehouse
    System.out.println("\n== CSV Import ===");
    System.out.println("importing sales data ... ");
    var sales = Sales.ImportFromFile("sales.csv");
    System.out.println("✓ (" + sales.size() + " entries)");

    // #4 Make available
  }

}
