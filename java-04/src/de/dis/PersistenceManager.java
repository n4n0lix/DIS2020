package de.dis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A persistence manager that accepts and performs transactions.
 */
public class PersistenceManager {

  /**
   * Begins a new transaction and returns the transactions id. Use this id for writing and commiting.
   * @return The transaction id
   */
  public int BeginTransaction() {
    // #1 Create new transaction
    int id =  NewTransactionId();
    m_Buffers.put(id, new TransactionBuffer((id)));

    // #2 Try to shrink buffers
    int shrinkThreshold = 5;
    if (m_Buffers.size() > shrinkThreshold) {
      m_Buffers.entrySet().removeIf(entry -> entry.getValue().IsCommitted());
    }

    // #3 Artificial delay
    Delay();

    return id;
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
    m_Logger.LogWrite(pTransactionId, pPageId, pData);

    // #2 Get transaction buffer
    TransactionBuffer buffer = m_Buffers.get(pTransactionId);

    if (buffer == null)
      throw new IllegalArgumentException("No transaction-buffer found for id: " + pTransactionId +". You have to " +
          "begin a new transaction with BeginTransaction() before writing.");

    // #3 Write buffer changes
    buffer.Write(pPageId, pData);

    // #4 Artificial delay
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
    m_Logger.LogEoT(pTransactionId);

    // #2 Get transaction buffer
    TransactionBuffer buffer = m_Buffers.get(pTransactionId);

    if (buffer == null)
      throw new IllegalArgumentException("No transaction-buffer found for id: " + pTransactionId +". You have to " +
          "begin a new transaction with BeginTransaction() before writing.");

    // #3 Persist transaction
    for (var bufferEntry : buffer.getReadOnlyBuffer().entrySet()) {
      int pageId = bufferEntry.getKey();
      String data = bufferEntry.getValue();
      Persist(pageId, data);
    }

    // #4 Delete logs upon successful persist
    m_Logger.DeleteLogs(pTransactionId);
  }

  private void Persist(int pPageId, String pData) throws IOException {
    System.out.println("persisting => [" + pPageId + "] : " + pData);

    // #1 Get handle of page file
    File pageFile = new File(String.valueOf(pPageId));
    pageFile.createNewFile(); // This only creates a new file IF the file didn't exists yet

    // #2 Persist data
    try (FileWriter writer = new FileWriter(pageFile)) {
      writer.write(pData);
    }

    // #3 Artificial delay
    Delay();
  }

  /**
   * Attempts a redo recovery.
   * @throws IOException If writing or committing failed. This is a critical failure.
   */
  private void RedoRecovery() throws IOException {
    System.out.println("performing redo recovery ...");

    // #1 Replay open transactions
    // This is used to map old transaction ids to new transaction ids
    Map<Integer,Integer> transactionIdMapping = new Hashtable<>();
    for(Logger.Entry anEntry : m_Logger.GetAllLogEntriesSorted()) {

      // #1.1 Begin a new transaction if none exists yet
      int newId;
      // No new transaction exists for this old transaction yet
      if (transactionIdMapping.containsKey(anEntry.TransactionId)) {
        newId = BeginTransaction();
        transactionIdMapping.put(anEntry.TransactionId, newId);
      }
      // We already began a transaction
      else {
        newId = transactionIdMapping.get(anEntry.TransactionId);
      }

      // #1.2 Replay the operation
      if (!anEntry.IsEoT) {
        Write(newId, anEntry.PageId, anEntry.Data);
      }
      else {
        Commit(newId);
        System.out.println("restored transaction ["+anEntry.TransactionId+"] successfully!");
      }

      // #2 Discard invalid logs
    }
  }

  private int NewTransactionId() {
    return m_LastTransactionId.incrementAndGet();
  }


  // Logging & Recovery
  private Logger m_Logger;

  // Transactions
  private AtomicInteger m_LastTransactionId;
  private ConcurrentHashMap<Integer, TransactionBuffer> m_Buffers;

  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
  //                      Helper                      //
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
    // Transactions
    m_LastTransactionId = new AtomicInteger(0);
    m_Buffers = new ConcurrentHashMap<>();

    // Logging
    m_Logger = new Logger();

    // #2 Trigger redo recovery
    try {
      RedoRecovery();
    } catch (Exception e) {
      System.err.println("recovery failed with exception: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }
}
