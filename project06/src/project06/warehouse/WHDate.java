package project06.warehouse;

import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WHDate {

  private static String InsertQuery = "insert into warehouse.date (\"date\") values (?)";

  public int        Id;
  public LocalDate  Date;

  public static List<WHDate> StoreInWarehouse(Collection<LocalDate> pDates) {
    // #1 Create connection
    Connection dbConn = null;
    List<WHDate> result = new ArrayList<>();

    try {
      dbConn = DriverManager.getConnection("jdbc:postgresql://localhost/postgres", "root", "pw");
      dbConn.setSchema("warehouse");
    } catch (SQLException e1) {
      e1.printStackTrace();
    }

    // #2 Load data
    try(PreparedStatement stmt = dbConn.prepareStatement(InsertQuery, Statement.RETURN_GENERATED_KEYS)) {
      for(var date : pDates) {
        stmt.setObject(1, date);
        stmt.addBatch();
      }

      // #2.2 Execute and count writes
      stmt.executeBatch();

      // #2.3 Assign ids
      // If no exception has been thrown until here we wrote every line successful and therefore
      // num generated keys == num products
      try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
        for(var date : pDates) {
          generatedKeys.next();
          WHDate whdate = new WHDate();
          whdate.Id = generatedKeys.getInt(1);
          whdate.Date = date;
          result.add(whdate);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return result;
  }

}
