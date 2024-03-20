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
                // Wait for deliveries if necessary
                if (store.deliveryBoxIsEmpty()) {
                    waitOnDeliveries();
                }

                if (needsBreak()) {
                    takeBreak();
                } else {
                    Map<String, Integer> itemsToStock = store.takeItemsFromDelivery();
                    processDelivery(itemsToStock);
                }

                int workDuration = simulateWork();
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

    private boolean needsBreak() {
        int breakInterval = store.getConfig().minBreakInterval + 
                            random.nextInt(store.getConfig().maxBreakInterval - store.getConfig().minBreakInterval + 1);
        return ticksSinceLastBreak >= breakInterval;
    }

    private void takeBreak() throws InterruptedException {
        int breakDuration = store.getConfig().breakDurationTicks;
        logAndUpdateGUI(String.format("<Tick %d> [Assistant %d] Taking a break for %d ticks.", store.getCurrentTick(), id, breakDuration));
        Thread.sleep(breakDuration * ThriftStore.TICK_TIME_SIZE);
        ticksSinceLastBreak = 0; // Reset the tick count since last break
        logAndUpdateGUI(String.format("<Tick %d> [Assistant %d] Back from break.", store.getCurrentTick(), id));
        store.recordAssistantBreakTime(breakDuration);
    }

    private int simulateWork() {
        return random.nextInt(50) + 1; // Random work duration between 1 and 50 ticks
    }

    private void waitOnDeliveries() throws InterruptedException {
        int waitTicks = random.nextInt(50) + 50;
        logAndUpdateGUI(String.format("<Tick %d> [Assistant %d] Waiting for deliveries for %d ticks.", store.getCurrentTick(), id, waitTicks));
        Thread.sleep(waitTicks * ThriftStore.TICK_TIME_SIZE);
        ticksSinceLastBreak += waitTicks;
    }

    private void processDelivery(Map<String, Integer> itemsToStock) throws InterruptedException {
        itemsToStock.forEach((section, itemCount) -> {
            try {
                int walkToTicks = 10 + itemCount;
                logAndUpdateGUI(String.format("<Tick %d> [Assistant %d] Walking to %s with %d items, taking %d ticks.", store.getCurrentTick(), id, section, itemCount, walkToTicks));
                Thread.sleep(walkToTicks * ThriftStore.TICK_TIME_SIZE);

                int stockingTicks = itemCount;
                logAndUpdateGUI(String.format("<Tick %d> [Assistant %d] Stocking %s section with %d items, taking %d ticks.", store.getCurrentTick(), id, section, itemCount, stockingTicks));
                Thread.sleep(stockingTicks * ThriftStore.TICK_TIME_SIZE);

                int returnTicks = 10;
                logAndUpdateGUI(String.format("<Tick %d> [Assistant %d] Returning from %s section, taking %d ticks.", store.getCurrentTick(), id, section, returnTicks));
                Thread.sleep(returnTicks * ThriftStore.TICK_TIME_SIZE);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private void logAndUpdateGUI(String message) {
        System.out.println(message);
        store.getGui().updateAssistantInfo(message);
    }
}



