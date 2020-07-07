package project06.warehouse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalesFact {

  private static String InsertQuery = "insert into warehouse.facts (product_id, store_id, date_id, sales, revenue) values (?,?,?,?,?)";

  // Foreign keys
  public int Id;
  public int ProductId;
  public int StoreId;
  public int DateId;

  // Facts
  public int    Sales;
  public double Revenue;

  public static List<SalesFact> Construct(List<Sales> pSales, List<Product> pProducts, List<Store> pStore, List<WHDate> pDates) {
    var result = new ArrayList<SalesFact>();

    for(var sale : pSales) {
      var fact = new SalesFact();

      // #1 Find product
      var product = pProducts.stream()
          .filter(p -> p.Name.toLowerCase().trim().equals(sale.ArticleName.toLowerCase().trim()))
          .findFirst();
      if (product.isEmpty()) {
        System.err.println("no product found for name ´" + sale.ArticleName.toLowerCase().trim() + "` (skipping entry)");
        continue;
      }

      // #2 Find store
      var store = pStore.stream()
          .filter(s -> s.Name.toLowerCase().trim().equals(sale.ShopName.toLowerCase().trim()))
          .findFirst();
      if (store.isEmpty()) {
        System.err.println("no store found for name ´" + sale.ShopName.toLowerCase().trim() + "` (skipping entry)");
        continue;
      }

      // #3 Find date
      var date = pDates.stream()
          .filter(d -> d.Date.equals(sale.Date))
          .findFirst();
      if (date.isEmpty()) {
        System.err.println("no date found for ´" + sale.Date + "` (skipping entry)");
        continue;
      }

      fact.ProductId = product.get().Id;
      fact.StoreId = store.get().Id;
      fact.DateId = date.get().Id;
      fact.Sales = sale.Sold;
      fact.Revenue = sale.Revenue;
      result.add(fact);
    }

    return result;
  }

  public static int StoreInWarehouse(List<SalesFact> pFacts) {
    // #1 Create connection
    Connection dbConn = null;
    int written = 0;

    try {
      dbConn = DriverManager.getConnection("jdbc:postgresql://localhost/postgres", "root", "pw");
      dbConn.setSchema("warehouse");
    } catch (SQLException e1) {
      e1.printStackTrace();
    }

    // #2 Load data
    try(PreparedStatement stmt = dbConn.prepareStatement(InsertQuery, Statement.RETURN_GENERATED_KEYS)) {
      // #2.1 Prepare batch
      for(var fact : pFacts) {
        stmt.setInt(1, fact.ProductId);
        stmt.setInt(2, fact.StoreId);
        stmt.setInt(3, fact.DateId);
        stmt.setInt(4, fact.Sales);
        stmt.setDouble(5, fact.Revenue);
        stmt.addBatch();
      }

      // #2.2 Execute and count writes
      for(var r : stmt.executeBatch())
        written += r;

      // #2.3 Assign ids
      // If no exception has been thrown until here we wrote every line successful and therefore
      // num generated keys == num products
      try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
        for(var fact : pFacts) {
          generatedKeys.next();
          fact.Id = generatedKeys.getInt(1);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return written;
  }
}
