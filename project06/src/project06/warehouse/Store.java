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
    try(PreparedStatement stmt = dbConn.prepareStatement(InsertQuery, Statement.RETURN_GENERATED_KEYS)) {
      for(var store : pStores) {
        stmt.setString(1, store.Name);
        stmt.setString(2, store.City);
        stmt.setString(3, store.Region);
        stmt.setString(4, store.Country);
        stmt.addBatch();
        System.out.println("save store - `" + store.Name + "`");
      }

      // #2.2 Execute and count writes
      int[] result = stmt.executeBatch();
      for(var r : result)
        written += r;

      // #2.3 Assign ids
      // If no exception has been thrown until here we wrote every line successful and therefore
      // num generated keys == num products
      try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
        for(var store : pStores) {
          generatedKeys.next();
          store.Id = generatedKeys.getInt(1);
        }
      }
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
