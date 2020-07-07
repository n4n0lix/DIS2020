package project06.warehouse;

import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WHDate {

  private static String InsertQuery = "insert into warehouse.date (\"day\",\"month\",quarter, \"year\" ) values (?,?,?,?)";

  public int        Id;
  public int        Day;
  public int        Month;
  public int        Quarter;
  public int        Year;

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
        stmt.setInt(1, date.getDayOfMonth());
        stmt.setInt(2, date.getMonthValue());
        stmt.setInt(3, (date.getMonthValue()/3)+1);
        stmt.setInt(4, date.getYear());
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
          whdate.Day = date.getDayOfMonth();
          whdate.Month = date.getMonthValue();
          whdate.Quarter = (date.getMonthValue()/3)+1;
          whdate.Year = date.getYear();
          result.add(whdate);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return result;
  }

}
