package project06;

import project06.warehouse.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

  public enum LocationLevel {
    COUNTRY,
    REGION,
    CITY,
    STORE
  }

  public enum ProductLevel {
    PRODUCT_CATEGORY,
    PRODUCT_FAMILY,
    PRODUCT_GROUP,
    PRODUCT
  }

  public enum TimeLevel {
    YEAR,
    QUARTER,
    MONTH,
    DAY
  }

  public static void main(String[] args) {
    boolean doEtl = true;

    if (doEtl)
      DoETL();

    Query(LocationLevel.STORE, TimeLevel.DAY, ProductLevel.PRODUCT);
  }

  private static void DoETL() {
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
  }


  private static void Query(LocationLevel pGeo, TimeLevel pTime, ProductLevel pProductLevel) {
    // #1 Pick selectors
    String selGeo = "";
    switch (pGeo) {
      case STORE -> selGeo = Sel_LocStore;
      case CITY -> selGeo = Sel_LocCity;
      case REGION -> selGeo = Sel_LocRegion;
      case COUNTRY -> selGeo = Sel_LocCountry;
    }

    String selTime = "";
    switch (pTime) {
      case DAY -> selTime = Sel_TimeDay;
      case MONTH -> selTime = Sel_TimeMonth;
      case QUARTER -> selTime = Sel_TimeQuarter;
      case YEAR -> selTime = Sel_TimeYear;
    }

    String selProductLevel = "";
    switch (pProductLevel) {
      case PRODUCT -> selProductLevel = Sel_ProductName;
      case PRODUCT_GROUP -> selProductLevel = Sel_ProductGroup;
      case PRODUCT_FAMILY -> selProductLevel = Sel_ProductFamily;
      case PRODUCT_CATEGORY -> selProductLevel = Sel_ProductCategory;
    }

    // #2 Prepare Query
    Connection dbConn = null;
    try {
      dbConn = DriverManager.getConnection("jdbc:postgresql://localhost/postgres", "root", "pw");
      dbConn.setSchema("stores_and_products");
    } catch (SQLException e1) {
      e1.printStackTrace();
    }

    String sql = QueryQuery.replace("{t_product}", selProductLevel)
                           .replace("{t_loc}", selGeo)
                           .replace("{t_time}", selTime);

    System.out.println(sql);

    try (var stmt = dbConn.prepareStatement(sql)) {
      var result = stmt.executeQuery();

      System.out.println(FixedWidth("LOCATION",20) + " | " + FixedWidth("TIME",20) + " | " + FixedWidth("PRODUCT",20) + " | " + FixedWidth("SALES",20));
      System.out.println("------------------------------------------------------------------------------------------");
      while(result.next()) {
        String geo = result.getString("lctn");
        if (geo == null)
          continue;
        // We skip geo total

        String product = result.getString("prdct");
        if (product == null)
          product = "TOTAL";

        String time = result.getString("tme");
        if (time == null)
          continue;
          // We skip time total

        var str = new StringBuilder()
            .append(FixedWidth(geo,20)).append(" | ")
            .append(FixedWidth(time,20)).append(" | ")
            .append(FixedWidth(product,20)).append(" | ")
            .append(FixedWidth(result.getString("sls"),20)).append(" | ");
        System.out.println(str.toString());
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static String FixedWidth(String str, int width) {
    if (str == null)
      return "";

    if (str.length() > width) {
      str = str.substring(0,width);
    }
    else if (str.length() < width) {
      while(str.length() < width)
        str = str + " ";
    }

    return str;
  }

  private static String Sel_ProductName     = "warehouse.products.\"name\"";
  private static String Sel_ProductGroup    = "warehouse.products.product_group";
  private static String Sel_ProductFamily   = "warehouse.products.product_family";
  private static String Sel_ProductCategory = "warehouse.products.product_category";

  private static String Sel_TimeDay      = "warehouse.date.\"day\"";
  private static String Sel_TimeMonth    = "warehouse.date.\"month\"";
  private static String Sel_TimeQuarter  = "warehouse.date.quarter";
  private static String Sel_TimeYear     = "warehouse.date.\"year\"";

  private static String Sel_LocStore   = "warehouse.stores.\"name\"";
  private static String Sel_LocCity    = "warehouse.stores.city";
  private static String Sel_LocRegion  = "warehouse.stores.region";
  private static String Sel_LocCountry = "warehouse.stores.country";

  private static String Sel_SoldUnity = "warehouse.facts.sales";
  private static String Sel_Revenue   = "warehouse.facts.revenue";

  private static String QueryQuery = "select \n" +
      "\t{t_product} as prdct, {t_loc} as lctn, {t_time} as tme, SUM(warehouse.facts.sales) as sls \n" +
      "from warehouse.facts, warehouse.products, warehouse.\"date\", warehouse.stores  \n" +
      "where " +
      "warehouse.facts.product_id = warehouse.products.id " +
      "and warehouse.facts.store_id = warehouse.stores.id " +
      "and warehouse.facts.date_id = warehouse.\"date\".id \n" +
      "GROUP BY\n" +
      "   CUBE(lctn, tme, prdct)\n" +
      "ORDER by\n" +
      "   lctn, tme, prdct;";

}
