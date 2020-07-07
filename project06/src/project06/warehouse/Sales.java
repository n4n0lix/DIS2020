package project06.warehouse;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Sales {


  private static String DateInsertQuery = "insert into warehouse.date (\"date\") values (?)";

    public String ShopName;
    public String ArticleName;
    public int Sold;
    public double Revenue;
    public LocalDate Date;

    public LocalDate GetDate() {
      return Date;
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder("Entry{");
      sb.append("ShopName='").append(ShopName).append('\'');
      sb.append(", ArticleName='").append(ArticleName).append('\'');
      sb.append(", Sold=").append(Sold);
      sb.append(", Revenue=").append(Revenue);
      sb.append(", Date=").append(Date);
      sb.append('}');
      return sb.toString();
    }


  public static List<Sales> ImportFromFile(String pFileName) {
    List<Sales> result = new ArrayList<>();
    Path path = Paths.get(pFileName);

    if (!new File(pFileName).exists()) {
      System.err.println("file " + pFileName + " not found");
      return result;
    }

    try {
      List<String> lines = Files.readAllLines(path, StandardCharsets.ISO_8859_1);
      lines.remove(0); // Remove header line
      int lineCounter = 0;
      for(String line : lines) {
        lineCounter++;

        // #1 Prepare for reading
        var dateParser = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        var splits = line.split(";");

        // #2 Read entry
        if (splits[1].equals("furt")) {
          int i = 0;
        }

        try {
          Sales e = new Sales();
          e.Date = LocalDate.parse(splits[0], dateParser);
          e.ShopName = splits[1];
          e.ArticleName = splits[2];
          e.Sold = Integer.parseInt(splits[3]);
          e.Revenue = Double.parseDouble(splits[4].replace(",", "."));

          result.add(e);
        } catch (Exception e) {
          System.err.println("invalid format detected in line:" +lineCounter+": `" + line + "` (this line will be ignored)");
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    return result;
  }

  public static int StoreDatesInWarehouse(List<Sales> pEntries) {
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
      String selectSQL = DateInsertQuery;
      PreparedStatement stmt = dbConn.prepareStatement(DateInsertQuery);

      for(var entry : pEntries) {
        stmt.setObject(1, entry.Date);
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
}
