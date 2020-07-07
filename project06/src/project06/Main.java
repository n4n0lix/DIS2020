package project06;

import project06.warehouse.*;

import java.util.List;
import java.util.stream.Collectors;

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
    // Reading csv
    var sales = Sales.ImportFromFile("sales.csv");
    System.out.println("✓ (" + sales.size() + " entries)");

    // Importing Dates
    var saleDates = sales.stream()
                         .map(e -> e.Date)
                         .collect(Collectors.toSet());
    var dates = WHDate.StoreInWarehouse(saleDates);
    System.out.println("✓ (" + dates.size() + " dates)");

    // #3 Construct facts
    System.out.println("writing facts ... ");
    List<SalesFact> facts = SalesFact.Construct(sales, products, stores, dates);
    int factsWritten = SalesFact.StoreInWarehouse(facts);
    System.out.println("✓ (" + factsWritten + " facts)");

    // #4 Make available

  }

}
