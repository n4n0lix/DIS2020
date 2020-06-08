package de.dis;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    public static int NUM_TRANSACTIONS = 10;
    public static int NUM_MIN_WRITE_OPS = 10;
    public static int NUM_MAX_WRITE_OPS = 20;

    public static void main(String[] args) {
        // pm -> Persistence Manager
        // #1 Client
        Client.New(PersistenceManager.Get()).Execute(pm -> {
            try {

                for(int i = 0; i < NUM_TRANSACTIONS; i++)
                    RandomTransaction(pm,10,19);

            } catch (Exception e) {
                System.err.println("Something went wrong: " + e.getMessage());
            }
        });

        // #2 Client
        Client.New(PersistenceManager.Get()).Execute(pm -> {
            try {

                for(int i = 0; i < NUM_TRANSACTIONS; i++)
                    RandomTransaction(pm,20,29);

            } catch (Exception e) {
                System.err.println("Something went wrong: " + e.getMessage());
            }
        });

        // #2 Client
        Client.New(PersistenceManager.Get()).Execute(pm -> {
            try {

                for(int i = 0; i < NUM_TRANSACTIONS; i++)
                    RandomTransaction(pm,30,39);

            } catch (Exception e) {
                System.err.println("Something went wrong: " + e.getMessage());
            }
        });

        // Terminate when no clients are active anymore
        Client.AwaitFinish();
    }

    private static void RandomTransaction(PersistenceManager persistenceManager, int pMinPageId, int pMaxPageId) throws IOException {
        int numWriteOps = ThreadLocalRandom.current().nextInt(NUM_MIN_WRITE_OPS,NUM_MAX_WRITE_OPS);

        // Start transaction
        int id = persistenceManager.BeginTransaction();

        // Do x random write operations
        for(int i = 0; i < numWriteOps; i++) {
            int pageId = ThreadLocalRandom.current().nextInt(pMinPageId,pMaxPageId);
            persistenceManager.Write(id, pageId, "data_v"+numWriteOps);
        }

        // Commit
        persistenceManager.Commit(id);
    }
}
