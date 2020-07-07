package project06.warehouse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Product {
  public int    Id;
  public String Name;
  public String Group;
  public String Family;
  public String Category;

  public static List<Product> Import() {
    // #1 Create connection
    Connection dbConn = null;
    try {
      dbConn = DriverManager.getConnection("jdbc:postgresql://localhost/postgres", "root", "pw");
      dbConn.setSchema("stores_and_products");
    } catch (SQLException e1) {
      e1.printStackTrace();
    }

    // #2 Load data
    List<Product> result = new ArrayList<>();
    try {
      String selectSQL = ImportQuery;
      PreparedStatement stmt = dbConn.prepareStatement(ImportQuery);
      ResultSet rs = stmt.executeQuery();

      while(rs.next()) {
        Product product = new Product();
        product.Name = rs.getString("product");
        product.Group  = rs.getString("product_group");
        product.Family  = rs.getString("product_family");
        product.Category  = rs.getString("product_category");

        result.add(product);
      }

      rs.close();
      stmt.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return result;
  }

  public static int StoreInWarehouse(List<Product> pProducts) {
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
      for(var product : pProducts) {
        stmt.setString(1, product.Name);
        stmt.setString(2, product.Group);
        stmt.setString(3, product.Family);
        stmt.setString(4, product.Category);
        stmt.addBatch();
      }

      // #2.2 Execute and count writes
      int[] result = stmt.executeBatch();
      for(var r : result)
        written += r;

      // #2.3 Assign ids
      // If no exception has been thrown until here we wrote every line successful and therefore
      // num generated keys == num products
      try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
        for(var product : pProducts) {
          generatedKeys.next();
          product.Id = generatedKeys.getInt(1);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return written;
  }

  private static String ImportQuery = "select \n" +
      "\ta.\"name\" as \"product\", \n" +
      "\tpg.\"name\" as \"product_group\", \n" +
      "\tpf.\"name\" as \"product_family\",\n" +
      "\tpc.\"name\"  as \"product_category\"\n" +
      "from \n" +
      "\tstores_and_products.article a, \n" +
      "\tstores_and_products.productgroup pg, \n" +
      "\tstores_and_products.productfamily pf, \n" +
      "\tstores_and_products.productcategory pc\n" +
      "where\n" +
      "\ta.productgroupid = pg.productgroupid \n" +
      "\tand pg.productfamilyid = pf.productfamilyid \n" +
      "\tand pc.productcategoryid = pf.productcategoryid\n" +
      ";";

  private static String InsertQuery = "insert into warehouse.products (\"name\" ,product_group, product_family, product_category)\n" +
      "values (?,?,?,?);";
}
