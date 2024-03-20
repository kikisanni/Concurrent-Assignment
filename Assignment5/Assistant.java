import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Assistant implements Runnable {
    private final ThriftStore store;
    private final int id;
    private final Random random = new Random();
    private int ticksSinceLastBreak;
    private int totalWorkTime; // Accumulate total work time here

    public Assistant(ThriftStore store, int id) {
        this.store = store;
        this.id = id;
        this.totalWorkTime = 0; // Initialize to zero
        this.ticksSinceLastBreak = 0; // Initialize ticks since last break
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                synchronized (store.getDeliveryLock()) {
                    while (store.deliveryBoxIsEmpty()) {
                        waitOnDeliveries();
                        // Ensure to check if the assistant needs to take a break after waiting.
                        if (needsBreak()) {
                            takeBreak();
                        }
                    }
                    if (needsBreak()) {
                        takeBreak();
                    } else {
                        Map<String, Integer> itemsToStock = new HashMap<>(store.takeItemsFromDelivery());
                        processDelivery(itemsToStock);
                    }
                }

                int workDuration = simulateWork();
                totalWorkTime += workDuration;
                ticksSinceLastBreak += workDuration;

                // Record total work time periodically or at certain checkpoints
                // For simplicity, doing it here but should be adjusted according to actual logic
                store.recordAssistantWorkTime(totalWorkTime);
                // Outside of synchronized block to allow delivery processing to be interrupted by breaks
                // Perform regular duties that don't require locking on the delivery box.
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        finally {
            // Ensure the total work time is recorded when the thread is interrupted
            store.recordAssistantWorkTime(totalWorkTime);
        }
    }

    private void recordTotalWorkTime() {
        // Correct invocation of recording work time in ThriftStore
        store.recordAssistantWorkTime(this.totalWorkTime);
    }

    private boolean needsBreak() {
        int interval = 200 + random.nextInt(101); // Interval between 200 and 300 ticks
        return ticksSinceLastBreak >= interval;
    }


    private void takeBreak() throws InterruptedException {
        int breakDuration = 150 + random.nextInt(51); // Break duration between 150 and 200 ticks
        System.out.printf("<Tick %d> [Thread %d] [Assistant %d] Taking a break for %d ticks.\n",
                store.getCurrentTick(), Thread.currentThread().getId(), id, breakDuration);

        Thread.sleep(breakDuration * ThriftStore.TICK_TIME_SIZE);
        ticksSinceLastBreak = 0; // Reset the tick count since last break

        System.out.printf("<Tick %d> [Thread %d] [Assistant %d] Back from break.\n",
                store.getCurrentTick(), Thread.currentThread().getId(), id);
        
        // Record break time in ThriftStore
        store.recordAssistantBreakTime(breakDuration);
    }

    private int simulateWork() {
        // Simulate work duration randomly for example purposes
        return random.nextInt(50) + 1; // Random work duration between 1 and 50 ticks
    }

    private void waitOnDeliveries() throws InterruptedException {
        // Assistant waits for deliveries to arrive
        int waitTicks = random.nextInt(50) + 50;
        System.out.printf("<Tick %d> [Thread %d] [Assistant %d] Waiting for deliveries for %d ticks.\n",
                store.getCurrentTick(), Thread.currentThread().getId(), id, waitTicks);
        Thread.sleep(waitTicks * ThriftStore.TICK_TIME_SIZE);
        ticksSinceLastBreak += waitTicks;
    }

    private void processDelivery(Map<String, Integer> itemsToStock) throws InterruptedException {
        for (Map.Entry<String, Integer> entry : itemsToStock.entrySet()) {
            String section = entry.getKey();
            int itemCount = entry.getValue();
            int walkToTicks = 10 + itemCount;
            System.out.printf("<Tick %d> [Thread %d] [Assistant %d] Walking to %s with %d items, taking %d ticks.\n", 
                              store.getCurrentTick(), Thread.currentThread().getId(), id, section, itemCount, walkToTicks);
            store.incrementTickCountBy(walkToTicks);
            Thread.sleep(walkToTicks * ThriftStore.TICK_TIME_SIZE);

            int stockingTicks = itemCount;
            System.out.printf("<Tick %d> [Thread %d] [Assistant %d] Stocking %s section with %d items, taking %d ticks.\n", 
                              store.getCurrentTick(), Thread.currentThread().getId(), id, section, itemCount, stockingTicks);
            store.incrementTickCountBy(stockingTicks);
            Thread.sleep(stockingTicks * ThriftStore.TICK_TIME_SIZE);

            int returnTicks = 10;
            System.out.printf("<Tick %d> [Thread %d] [Assistant %d] Returning from %s section, taking %d ticks.\n", 
                              store.getCurrentTick(), Thread.currentThread().getId(), id, section, returnTicks);
            store.incrementTickCountBy(returnTicks);
            Thread.sleep(returnTicks * ThriftStore.TICK_TIME_SIZE);
        }
    }
}
