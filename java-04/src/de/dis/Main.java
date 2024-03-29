package de.dis;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

public class Main {


    public static boolean RECOVERY_ONLY = false;
    public static int SHRINK_THRESHOLD = 5;

    public static int NUM_TRANSACTIONS = 10;
    public static int NUM_MIN_WRITE_OPS = 10;
    public static int NUM_MAX_WRITE_OPS = 20;

    public static void main(String[] args) {
        // #1 Recovery only mode
        if (RECOVERY_ONLY) {
            PersistenceManager.Get(); // This triggers recovery implicit
            return;
        }

        // #2 Test mode
        StartNewClient(10,19);
        StartNewClient(20,29);
        StartNewClient(30,39);
        StartNewClient(40,49);
        StartNewClient(50,59);

        // Terminate when no active clients
        Client.AwaitFinish();
    }

    private static void StartNewClient(int pMinPageId, int pMaxPageId) {
        Client.New(PersistenceManager.Get()).Execute(pm -> {
            try {
                for(int i = 0; i < NUM_TRANSACTIONS; i++)
                    RandomTransaction(pm,pMinPageId,pMaxPageId);
            } catch (Exception e) {
                System.err.println("Something went wrong: " + e.getMessage());
            }
        });
    }

    private static void RandomTransaction(PersistenceManager persistenceManager, int pMinPageId, int pMaxPageId) throws IOException {
        int numWriteOps = ThreadLocalRandom.current().nextInt(NUM_MIN_WRITE_OPS,NUM_MAX_WRITE_OPS);

        // Start transaction
        int id = persistenceManager.BeginTransaction();

        // Do x random write operations
        for(int i = 0; i < numWriteOps; i++) {
            int pageId = ThreadLocalRandom.current().nextInt(pMinPageId,pMaxPageId);
            persistenceManager.Write(id, pageId, ""+ThreadLocalRandom.current().nextInt(0,9999));
        }

        // Commit
        persistenceManager.Commit(id);
    }
}
