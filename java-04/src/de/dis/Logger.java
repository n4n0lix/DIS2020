package de.dis;

import java.io.*;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Logger {

  public Logger() {
    m_LastLSN = new AtomicInteger(-1);
  }

  public void LogWrite(int pTransactionId, int pPageId, String pData) throws IOException {
    // #1 Create new log entry
    Entry logEntry = new Entry();
    logEntry.LSN = m_LastLSN.incrementAndGet();
    logEntry.TransactionId = pTransactionId;
    logEntry.IsEoT = false;

    logEntry.PageId = pPageId;
    logEntry.Data = pData;

    // #2 Persist log entry
    PersistLogEntry(logEntry);
  }

  public void LogEoT(int pTransactionId) throws IOException {
    // #1 Create new log entry
    Entry logEntry = new Entry();
    logEntry.LSN = m_LastLSN.incrementAndGet();
    logEntry.TransactionId = pTransactionId;
    logEntry.IsEoT = true;

    logEntry.PageId = null;
    logEntry.Data = null;

    // #2 Persist log entry
    PersistLogEntry(logEntry);
  }

  public void DeleteLogs(int pTransactionId) {
    // Delete all files associated with the transaction
    for(var logFile : GetAllLogsForTransaction(pTransactionId))
      logFile.delete();
  }

  public PriorityQueue<Entry> GetAllLogEntriesSorted() {
    // Make sure the open log entries are provided in a sorted order
    PriorityQueue<Entry> openTransactions = new PriorityQueue<>(new EntryLSNComparator());

    // Read all existing log files
    for(var logFile : GetAllLogFiles()) {
      var entry = ReadLogEntry(logFile);
      openTransactions.add(entry);
    }

    return openTransactions;
  }

  private void PersistLogEntry(Entry pEntry) throws IOException {
    System.out.println("logging => " + pEntry.toString());

    String fileName = "" + pEntry.TransactionId + "_" + pEntry.LSN + ".log";
    File fileEntry = new File(fileName);
    fileEntry.createNewFile();

    try(FileWriter writer = new FileWriter(fileEntry)) {
      writer.write(pEntry.toString());
    }
  }

  private AtomicInteger m_LastLSN;

  private static String FILE_EXTENSION = ".log";

  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
  //                      Helper                       //
  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

  private static Entry ReadLogEntry(File pFile) {
    // #1 Read content of entry file
    String fileContent;
    try(FileReader fr = new FileReader(pFile);
        BufferedReader reader = new BufferedReader(fr)) {

      fileContent = reader.readLine();

    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }

    // #2 Deserialize file entry
    // #2.1 Strip string of unnecessary data
    fileContent = fileContent.replace("{","")
        .replace("}","")
        .replace(" ","")
        .replace("\n","")
        .replace("\t","");

    // #2.2 Deserialize string into entry
    Entry result = new Entry();

    for(var strProperty : fileContent.split(",")) {
      String propName = strProperty.substring(0, strProperty.indexOf(":"));
      String propValue = strProperty.substring(strProperty.indexOf(":")+1);

      switch(propName) {
        case "LSN":
          result.LSN = Integer.valueOf(propValue);
          break;
        case "TransactionId":
          result.TransactionId = Integer.valueOf(propValue);
          break;
        case "IsEoT":
          result.IsEoT = Boolean.valueOf(propValue);
          break;
        case "PageId":
          result.PageId = Integer.valueOf(propValue);
          break;
        case "Data":
          result.Data = propValue;
          break;
      }
    }

    return result;
  }

  private static File[] GetAllLogsForTransaction(int pTransactionId) {
    return new File(".")
        .listFiles(new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return name.startsWith(String.valueOf(pTransactionId)) && name.endsWith(FILE_EXTENSION);
          }
        });
  }

  private static File[] GetAllLogFiles() {
    return new File(".")
        .listFiles(new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return name.endsWith(FILE_EXTENSION);
          }
        });
  }

  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
  //                 Internal Classes                  //
  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

  /**
   * Log Entry holds information regarding an operation on a transaction.
   */
  public static class Entry {
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
      s.append("{ LSN :").append(LSN)
          .append(", TransactionId :").append(TransactionId)
          .append(", IsEoT :").append(IsEoT)
          .append(", PageId :").append(PageId)
          .append(", Data :").append(Data)
          .append(" }");
      return s.toString();
    }
  }

  public class EntryLSNComparator implements Comparator<Entry> {

    @Override
    public int compare(Entry a, Entry b) {
      return Integer.compare(a.LSN, b.LSN);
    }
  }
}
