import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Assistant implements Runnable {
    private final ThriftStore store;
    private final int id;
    private final Random random = new Random();

    public Assistant(ThriftStore store, int id) {
        this.store = store;
        this.id = id;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                if (needsBreak()) {
                    takeBreak();
                    continue;
                }

                synchronized (store.getDeliveryLock()) {
                    while (store.deliveryBoxIsEmpty()) {
                        waitOnDeliveries();
                    }
                    Map<String, Integer> itemsToStock = new HashMap<>(store.takeItemsFromDelivery());
                    processDelivery(itemsToStock);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean needsBreak() {
        // Determine if a break is needed
        return false; // Simplified for brevity
    }

    private void takeBreak() throws InterruptedException {
        // Calculate the break duration in ticks
        int breakDuration = store.getConfig().breakDurationTicks;
        
        // Log the start of the break
        System.out.printf("<Tick %d> [Thread %d] [Assistant %d] Taking a break for %d ticks.\n",
                          store.getCurrentTick(), Thread.currentThread().getId(), id, breakDuration);
        
        // Simulate the break duration
        Thread.sleep(breakDuration * ThriftStore.TICK_TIME_SIZE);
        
        // Log when the assistant is back from the break
        System.out.printf("<Tick %d> [Thread %d] [Assistant %d] Back from break.\n",
                          store.getCurrentTick(), Thread.currentThread().getId(), id);
        
        // Update the total break ticks in the ThriftStore
        store.addBreakTicks(breakDuration);
    }
    
    

    private void waitOnDeliveries() throws InterruptedException {
        int waitTicks = random.nextInt(50) + 50;
        System.out.printf("<Tick %d> [Thread %d] [Assistant %d] Waiting for deliveries for %d ticks.\n", 
                          store.getCurrentTick(), Thread.currentThread().getId(), id, waitTicks);
        store.incrementTickCountBy(waitTicks);
        Thread.sleep(waitTicks * ThriftStore.TICK_TIME_SIZE);
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
