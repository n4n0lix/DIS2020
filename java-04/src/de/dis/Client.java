package de.dis;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Client {

  private PersistenceManager m_PersistenceManager;

  public void Execute(Consumer<PersistenceManager> pTransaction) {
    // #1 Execute transaction in a new thread
    THREAD_POOL.submit(() -> {
      pTransaction.accept(m_PersistenceManager);
    });
  }

  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
  //                   Initialization                  //
  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

  private static ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

  public static void Shutdown() {
    THREAD_POOL.shutdown();
  }

  public static Client New(PersistenceManager pPersistenceManager) {
    // 1# Check for valid initialization
    if (pPersistenceManager == null) return null;

    // #2 Create instance
    return new Client(pPersistenceManager);
  }

  private Client(PersistenceManager pPersistenceManager) {
    m_PersistenceManager = pPersistenceManager;
  }
}
