package project06.warehouse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Store {
  public int Id;
  public String Name;
  public String City;
  public String Region;
  public String Country;

  public static List<Store> Import() {
    // #1 Create connection
    Connection dbConn = null;
    try {
      dbConn = DriverManager.getConnection("jdbc:postgresql://localhost/postgres", "root", "pw");
      dbConn.setSchema("stores_and_products");
    } catch (SQLException e1) {
      e1.printStackTrace();
    }

    // #2 Load data
    List<Store> result = new ArrayList<>();
    try {
      String selectSQL = ImportQuery;
      PreparedStatement stmt = dbConn.prepareStatement(ImportQuery);
      ResultSet rs = stmt.executeQuery();

      while(rs.next()) {
        Store store = new Store();
        store.Name = rs.getString("store");
        store.City  = rs.getString("city");
        store.Region  = rs.getString("region");
        store.Country  = rs.getString("country");

        result.add(store);
      }

      rs.close();
      stmt.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return result;
  }

  public static int StoreInWarehouse(List<Store> pStores) {
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
    try {
      String selectSQL = ImportQuery;
      PreparedStatement stmt = DB.Connection().prepareStatement(InsertQuery);

      for(var product : pStores) {
        stmt.setString(1, product.Name);
        stmt.setString(2, product.City);
        stmt.setString(3, product.Region);
        stmt.setString(4, product.Country);
        stmt.addBatch();
      }

      int[] result = stmt.executeBatch();
      for(var r : result)
        written += r;

      stmt.close();

    } catch (Exception e) {
      e.printStackTrace();
    }

    return written;
  }

  private static String ImportQuery = "select \n" +
      "\ts.\"name\" as \"store\", \n" +
      "\tc.\"name\" as \"city\", \n" +
      "\tr.\"name\" as \"region\",\n" +
      "\tcr.\"name\"  as \"country\"\n" +
      "from \n" +
      "\tstores_and_products.shop s, \n" +
      "\tstores_and_products.city c, \n" +
      "\tstores_and_products.region r , \n" +
      "\tstores_and_products.country cr \n" +
      "where\n" +
      "\ts.cityid = c.cityid \n" +
      "\tand c.regionid = r.regionid \n" +
      "\tand r.countryid = cr.countryid \n" +
      ";";

  private static String InsertQuery = "insert into warehouse.stores (\"name\", city, region, country)\n" +
      "values (?,?,?,?)";
}
