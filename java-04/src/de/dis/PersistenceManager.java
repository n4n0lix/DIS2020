package de.dis;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A persistence manager that performs transactions, logging and recovery.
 */
public class PersistenceManager {

  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
  //              Transactions                 //
  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

  //<editor-fold desc="Transactions">
  private int NewTransactionId() {
    return m_LastTransactionId.incrementAndGet();
  }

  /**
   * Begins a new transaction and returns the transactions id. Use this id for writing and commiting.
   * @return The transaction id
   */
  public int BeginTransaction() {
    // #1 Artificial delay
    Delay();

    // #2 Create new transaction
    return NewTransactionId();
  }

  /**
   * Writes pData on page with id pPageId
   * @param pTransactionId the id of a valid transaction
   * @param pPageId the id of a page
   * @param pData the data to write into the page
   * @throws IllegalArgumentException if pTransactionId is not a valid transaction id
   * @throws IOException if logging failed. This is a critical failure
   */
  public void Write(int pTransactionId, int pPageId, String pData) throws IllegalArgumentException, IOException {
    // #1 Logging
    int lsn = LogWrite(pTransactionId, pPageId, pData);

    // #2 Construct write operation
    var newWriteOp = new PageWrite();
    newWriteOp.PageId = pPageId;
    newWriteOp.LSN = lsn;
    newWriteOp.TransactionId = pTransactionId;
    newWriteOp.Data = pData;

    // #3 Write into buffer
    m_Buffer.put(pPageId, newWriteOp);

    // #4 Try to shrink buffer and persist changes
    if (m_Buffer.size() > 5) {
      synchronized (m_Buffer) {
        int initSize = m_Buffer.size();

        List<PageWrite> writeOps = new ArrayList<>(m_Buffer.values());
        for(var writeOp : writeOps)
          if (m_CommittedTransactions.contains(writeOp.TransactionId)) {
            Persist(writeOp);
            m_Buffer.remove(writeOp.PageId);
          }

        if (initSize != m_Buffer.size())
          System.out.println("Persisted " + (initSize - m_Buffer.size()) + " changes ...");
      }
    }

    // #5 Artificial delay
    Delay();
  }

  /**
   * Commit the transaction.
   * @param pTransactionId the id of a valid transaction
   * @throws IllegalArgumentException if pTransactionId is not a valid transaction id
   * @throws IOException if logging or persisting failed. Both are considered a critical failure
   */
  public void Commit(int pTransactionId) throws IllegalArgumentException, IOException {
    // #1 Logging
    LogEoT(pTransactionId);

    // #2 Get transaction buffer
    m_CommittedTransactions.add(pTransactionId);

    // #3 Artificial delay
    Delay();
  }

  private void Persist(PageWrite pWriteOp) throws IOException {
    // #1 Get handle of page file
    File pageFile = new File(String.valueOf(pWriteOp.PageId));
    pageFile.createNewFile(); // This only creates a new file IF the file didn't exists yet

    // #2 Persist data
    try (FileWriter writer = new FileWriter(pageFile)) {
      writer.write(String.valueOf(pWriteOp.LSN) + "," + pWriteOp.Data);
    }

    // #3 Artificial delay
    Delay();
  }
  //</editor-fold>

  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
  //                 Logging                   //
  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

  //<editor-fold desc="Logging">
  private void BackupAndCreateNewLogFile() throws IOException {
    // Rename old `persistence.log` to `backup.log`
    if (m_LogFile.exists()) {
      m_LogFile.renameTo(m_BackupLogFile);
    }

    m_LogFile.createNewFile(); // Create new `persistence.log`
  }

  /**
   * Writes a user-data change to the page by the given page-id.
   * @param pTransactionId The transaction id
   * @param pPageId The page to write to
   * @param pData The user data
   * @return The LSN
   * @throws IOException
   */
  public int LogWrite(int pTransactionId, int pPageId, String pData) throws IOException {
    // #1 Create new log entry
    LogEntry logEntry = new LogEntry();
    logEntry.LSN = m_LastLSN.incrementAndGet();
    logEntry.TransactionId = pTransactionId;
    logEntry.IsEoT = false;

    logEntry.PageId = pPageId;
    logEntry.Data = pData;

    // #2 Persist log entry
    PersistLogEntry(logEntry);

    // #3 Return LSN
    return logEntry.LSN;
  }

  public void LogEoT(int pTransactionId) throws IOException {
    // #1 Create new log entry
    LogEntry logEntry = new LogEntry();
    logEntry.LSN = m_LastLSN.incrementAndGet();
    logEntry.TransactionId = pTransactionId;
    logEntry.IsEoT = true;

    logEntry.PageId = null;
    logEntry.Data = null;

    // #2 Persist log entry
    PersistLogEntry(logEntry);
  }

  private Collection<LogEntry> GetAllLogEntries() throws IOException {
    // #1 Make sure log file exists
    File logFile = new File(LOG_FILE);
    logFile.createNewFile();

    // #2 Read all entries
    ArrayList<LogEntry> result = new ArrayList<LogEntry>();
    try(FileReader fr = new FileReader(logFile);
        BufferedReader reader = new BufferedReader(fr)) {

      var entry = LogEntry.FromString(reader.readLine());
      if (entry != null)
        result.add(entry);
    }

    return result;
  }

  private synchronized void PersistLogEntry(LogEntry pLogEntry) throws IOException {
    try(FileWriter writer = new FileWriter(m_LogFile, true)) {
      writer.write(pLogEntry.toString() + "\n");
      writer.flush();
    }
  }
  //</editor-fold>

  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
  //                 Recovery                  //
  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

  //<editor-fold desc="Recovery">
  private void RecoverPage(int pPageId, Collection<LogEntry> pLogs) throws IOException {
    if (pLogs.isEmpty())
      return;

    PriorityQueue<LogEntry> sortedLogs = new PriorityQueue<>(Comparator.comparingInt(o -> o.LSN));
    sortedLogs.addAll(pLogs);

    // #1 Analysis-Phase
    // #1.1 Read current page LSN
    int pageLSN = -1;
    File pageFile = new File(String.valueOf(pPageId));

    if (pageFile.exists()) {
      try (FileReader fr = new FileReader(pageFile);
           BufferedReader reader = new BufferedReader(fr)) {

        String[] data = reader.readLine().split(",");
        pageLSN = Integer.parseInt(data[0]);

      } catch (Exception e) {
        System.err.println("couldn't determine page status, assuming to be empty");
        pageLSN = -1;
      }
    }

    // #1.2 Determine winner transaction. We assume the logs are sorted so that the last log for any
    //      transaction is either its EOT or it doesn't has a logged EOT. The winner transaction here
    //      is the transaction that modified the page value and committed last.
    Set<Integer> participants = new HashSet<>();
    // TODO: Save all winner transactions and replay them in order
    int winnerTransId = -1;
    int winnerEOTLSN = -1;

    for(var logEntry : sortedLogs) {
      boolean isRelevantPageId = logEntry.PageId == pPageId;
      boolean isRelevantLSN    = logEntry.LSN > pageLSN;

      if (!isRelevantPageId) continue;
      if (!isRelevantLSN) continue;

      // Collect participants
      if (!logEntry.IsEoT) {
        participants.add(logEntry.TransactionId);
        continue;
      }

      // Check if this is the new winner EOT
      boolean isRelevantEoT = participants.contains(logEntry.TransactionId);

      if (!isRelevantEoT) continue;

      // TODO: We are comparing Write-LSN with EOT-LSN here. it should be Write-LSN vs Write-LSN
      // Update new winner transaction
      if (winnerEOTLSN < logEntry.LSN) {
        winnerTransId = logEntry.TransactionId;
        winnerEOTLSN = logEntry.LSN;
      }
    }

    // #2 Redo-Phase
    //    Replay the winner transaction for page
    if (winnerTransId == -1)
      return;

    for(var logEntry : sortedLogs) {
      boolean isWinnerReplayable =
          logEntry.TransactionId == winnerTransId
          && logEntry.PageId == pPageId
          && !logEntry.IsEoT;

      if (isWinnerReplayable) {
        PageWrite write = PageWrite.FromLogEntry(logEntry);
        Persist(write);
      }
    }
  }

  /**
   * Attempts a redo recovery.
   * @throws IOException If writing or committing failed. This is a critical failure.
   */
  private void Recovery() throws IOException {
    System.out.println("starting recovery...");

    // #1 Find all pages in logs
    var logs = GetAllLogEntries();
    Set<Integer> recoveredPages = new HashSet<>();

    for(var logEntry : logs) {
      if (recoveredPages.contains(logEntry.PageId))
        continue;

      RecoverPage(logEntry.PageId, logs);
      recoveredPages.add(logEntry.PageId);
    }

    System.out.println("recovery finished");
  }
  //</editor-fold>

  // Logging & Recovery
  private        final AtomicInteger m_LastLSN;
  private        final File          m_BackupLogFile;
  private        final File          m_LogFile;

  private static final String        LOG_FILE = "persistence.log";
  private static final String        BACKUP_FILE = "backup.log";

  // Transactions
  private final AtomicInteger m_LastTransactionId;
  private final Map<Integer, PageWrite> m_Buffer; // Key: PageId
  private final Set<Integer> m_CommittedTransactions; // Key: TransactionId

  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
  //                      Helper                       //
  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

  private void Wait(int pMSec) {
    try {
      Thread.sleep(pMSec);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void Delay() {
    int minDelay = 10;
    int maxDelay = 100;
    Wait(ThreadLocalRandom.current().nextInt(minDelay,maxDelay));
  }

  public static class PageWrite {
    public int PageId;
    public int LSN;
    public int TransactionId;
    public String Data;

    public static PageWrite FromLogEntry(LogEntry pLogEntry) {
      PageWrite pageWrite = new PageWrite();
      pageWrite.LSN = pLogEntry.LSN;
      pageWrite.Data = pLogEntry.Data;
      pageWrite.PageId = pLogEntry.PageId;
      pageWrite.TransactionId = pLogEntry.TransactionId;
      return pageWrite;
    }
  }

  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
  //                     Singleton                     //
  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

  public static PersistenceManager Get() {
    if (INSTANCE == null)
      INSTANCE = new PersistenceManager();

    return INSTANCE;
  }

  private static PersistenceManager INSTANCE;

  private PersistenceManager() {
    // #1 Initialize
    m_LastTransactionId = new AtomicInteger(0);
    m_Buffer = new ConcurrentHashMap<>();
    m_LastLSN = new AtomicInteger(-1);
    m_CommittedTransactions = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());

    // #2 Recovery & Logging
    m_BackupLogFile = new File(BACKUP_FILE);
    m_LogFile = new File(LOG_FILE);

    try {
      BackupAndCreateNewLogFile();
      Recovery();
    } catch (Exception e) {
      System.err.println("recovery failed with exception: " + e.getMessage());
      System.exit(1);
    }

    System.out.println("started persistence-manager");
  }
}
