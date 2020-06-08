package de.dis;

/**
 * Log Entry holds information regarding an operation on a transaction.
 */
public class LogEntry {
  // Mandatory members
  public  int           LSN;
  public  int           TransactionId;
  public  boolean       IsEoT;

  // Optional members
  public  Integer       PageId;
  public  String        Data;

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    s.append("LSN : ").append(LSN)
     .append(", TransactionId : ").append(TransactionId)
     .append(", IsEoT : ").append(IsEoT);

    if (!IsEoT) {
      s.append(", PageId : ").append(PageId)
       .append(", Data : ").append(Data);
    }

    return s.toString();
  }

  public static LogEntry FromString(String pStr) {
    // #1 Check for invalid strings and cleanup the string
    if (pStr == null || pStr.isEmpty())
      return null;

    // #2.2 Deserialize string into entry
    LogEntry result = new LogEntry();

    for(var strProperty : pStr.split(",")) {
      String propName = strProperty.substring(0, strProperty.indexOf(":")).trim();
      String propValue = strProperty.substring(strProperty.indexOf(":")+1).trim();

      switch(propName) {
        case "LSN":
          result.LSN = Integer.parseInt(propValue);
          break;
        case "TransactionId":
          result.TransactionId = Integer.parseInt(propValue);
          break;
        case "IsEoT":
          result.IsEoT = Boolean.parseBoolean(propValue);
          break;
        case "PageId":
          result.PageId = Integer.parseInt(propValue);
          break;
        case "Data":
          result.Data = propValue;
          break;
      }
    }

    return result;
  }
}