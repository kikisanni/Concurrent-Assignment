import java.util.Map;
import java.util.Random;

/**
 * Stands in for a thrift store assistant who handles deliveries and restocks shelves. 
 * In order to replicate real-world work situations, assistants also take breaks according to their working hours.
 */

public class Assistant implements Runnable {
    private final ThriftStore store;
    private final int id;
    private final Random random = new Random(); // random intervals
    private int ticksSinceLastBreak; 
    private int totalWorkTime; // Accumulate total work time here

    // Assistant constructor
    public Assistant(ThriftStore store, int id) {
        this.store = store;
        this.id = id;
        this.totalWorkTime = 0; // Initialize to zero
        this.ticksSinceLastBreak = 0; // Initialize ticks since last break to zero
    }
    /**
     * The main logic for the assistant's lifecycle, handling deliveries, stocking, and taking breaks.
     */
    @Override
    public void run() {
        try {
            //
            while (!Thread.currentThread().isInterrupted()) {
                // Wait for deliveries if necessary or take a break
                if (store.deliveryBoxIsEmpty()) {
                    waitOnDeliveries(); //wait, there  are no deliveries yet
                }
                //if the assistant needs a break
                if (needsBreak()) {
                    assistantTakeBreak(); //take break!
                
                } else {
                    Map<String, Integer> itemsToStock = store.takeItemsFromDelivery();
                    processDelivery(itemsToStock);
                }
                // Simulate the time required to process the delivery or wait.
                int workDuration = simulateDurationOfWork();
                totalWorkTime += workDuration;
                ticksSinceLastBreak += workDuration;

                // Record total work time in ThriftStore
                store.recordAssistantWorkTime(totalWorkTime);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // Ensure the total work time is recorded when the thread is interrupted
            store.recordAssistantWorkTime(totalWorkTime);
        }
    }

    //Determines whether the assistant requires a break depending on the configured intervals and work length.
    private boolean needsBreak() {
        Config config = store.getConfig();
        int breakInterval = config.minBreakInterval + 
                            random.nextInt(config.maxBreakInterval - store.getConfig().minBreakInterval + 1);
        return ticksSinceLastBreak >= breakInterval;
    }

    // Handles the logic behind the helper taking a break. Pauses the thread to mimic a break in time.
    private void assistantTakeBreak() throws InterruptedException {
        Config config = store.getConfig();

        int breakDuration = config.breakDurationTicks;
        logAndUpdateGUI(String.format("<Tick %d> [Thread %d] [Assistant %d] Taking a break for %d ticks.", store.getCurrentTick(), Thread.currentThread().getId(), id, breakDuration));

        Thread.sleep(breakDuration * ThriftStore.TICK_TIME_SIZE);
        ticksSinceLastBreak = 0; // Reset the counter after the break.
        logAndUpdateGUI(String.format("<Tick %d> [Thread %d] [Assistant %d] Back from break.", store.getCurrentTick(), Thread.currentThread().getId(), id));

        store.recordAssistantBreakTime(breakDuration); // Record the break time for reporting purposes.
    }

    //Generates a random interval to simulate the duration of work. This replicates the time required to process deliveries or stock things.
    private int simulateDurationOfWork() {
        return random.nextInt(50) + 1; // Random work duration between 1 and 50 ticks
    }

    //When the delivery box is empty, it waits for new deliveries to come. It simulates waiting by halting the thread.
    private void waitOnDeliveries() throws InterruptedException {
        //Sleeping for an unknown amount of time simulates the wait for deliveries.
        int waitTicks = random.nextInt(50) + 50;
        logAndUpdateGUI(String.format("<Tick %d> [Thread %d] [Assistant %d] Waiting for deliveries for %d ticks.", store.getCurrentTick(), Thread.currentThread().getId(), id, waitTicks));
        Thread.sleep(waitTicks * ThriftStore.TICK_TIME_SIZE);
        ticksSinceLastBreak += waitTicks; //Calculate the wait time for the next break interval.
    }

    private void processDelivery(Map<String, Integer> itemsToStock) throws InterruptedException {
        itemsToStock.forEach((section, itemCount) -> {
            try {
                int walkToTicks = 10 + itemCount;
                logAndUpdateGUI(String.format("<Tick %d> [Thread %d] [Assistant %d] Walking to %s with %d items, taking %d ticks.", store.getCurrentTick(), Thread.currentThread().getId(), id, section, itemCount, walkToTicks));
                Thread.sleep(walkToTicks * ThriftStore.TICK_TIME_SIZE);

                int stockingTicks = itemCount;
                logAndUpdateGUI(String.format("<Tick %d> [Thread %d] [Assistant %d] Stocking %s section with %d items, taking %d ticks.", store.getCurrentTick(), Thread.currentThread().getId(), id, section, itemCount, stockingTicks));
                Thread.sleep(stockingTicks * ThriftStore.TICK_TIME_SIZE);

                int returnTicks = 10;
                logAndUpdateGUI(String.format("<Tick %d> [Thread %d] [Assistant %d] Returning from %s section, taking %d ticks.", store.getCurrentTick(), Thread.currentThread().getId(), id, section, returnTicks));
                Thread.sleep(returnTicks * ThriftStore.TICK_TIME_SIZE);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    //reflects the actions of the assistant in the store's graphical user interface.
    private void logAndUpdateGUI(String message) {
        System.out.println(message);
        store.getGui().updateAssistantInfo(message);
    }
}
