package de.dis;

import java.io.*;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Logger {
  public Logger() {
    // #1 Init
    m_LastLSN = new AtomicInteger(-1);
  }

  public void Start() throws IOException {
    File logFile = new File(LOG_FILE);
    logFile.createNewFile();

    m_LogFileWriter = new FileWriter(logFile);
  }

  public void Close() throws IOException {
    m_LogFileWriter.close();
    m_LogFileWriter = null;
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

  public PriorityQueue<Entry> GetAllLogEntries() throws IOException {
    if (m_LogFileWriter != null)
      throw new IOException("Cannot read and write at the same time to logfile "+LOG_FILE);

    // #1 Make sure log file exists
    File logFile = new File(LOG_FILE);
    logFile.createNewFile();

    // #2 Read all entries
    PriorityQueue<Entry> result = new PriorityQueue<>(new EntryLSNComparator());
    try(FileReader fr = new FileReader(logFile);
        BufferedReader reader = new BufferedReader(fr)) {

      var entry = Entry.FromString(reader.readLine());
      if (entry != null)
        result.add(entry);
    }

    return result;
  }

  private synchronized void PersistLogEntry(Entry pEntry) throws IOException {
    if (m_LogFileWriter == null)
      throw new IOException("Cannot write to logfile "+LOG_FILE+" until Logger is started");

    System.out.println("logging => " + pEntry.toString());
    m_LogFileWriter.write(pEntry.toString() + "\n");
    m_LogFileWriter.flush();
  }

  private              FileWriter    m_LogFileWriter;
  private        final AtomicInteger m_LastLSN;

  private static final String        LOG_FILE = "persistence.log";

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
      s.append("LSN : ").append(LSN)
          .append(", TransactionId : ").append(TransactionId)
          .append(", IsEoT : ").append(IsEoT)
          .append(", PageId : ").append(PageId)
          .append(", Data : ").append(Data);
      return s.toString();
    }

    public static Entry FromString(String pStr) {
      // #1 Check for invalid strings and cleanup the string
      if (pStr == null || pStr.isEmpty())
        return null;
      
      // #2.2 Deserialize string into entry
      Entry result = new Entry();

      for(var strProperty : pStr.split(",")) {
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
  }

  public class EntryLSNComparator implements Comparator<Entry> {

    @Override
    public int compare(Entry a, Entry b) {
      return Integer.compare(a.LSN, b.LSN);
    }
  }
}
