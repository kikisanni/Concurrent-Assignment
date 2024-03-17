import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Assistant implements Runnable {
    private final ThriftStore store;
    private final int id;
    private final Random random = new Random();
    private int totalWaitedTicks = 0;
    private int ticksSinceLastBreak = 0;
    private int totalWorkedTicks = 0;

    public Assistant(ThriftStore store, int id) {
        this.store = store;
        this.id = id;
    }

    public int getTotalWorkedTicks() {
        return totalWorkedTicks;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                if (ticksSinceLastBreak >= getRandomBreakInterval()) {
                    int breakDuration = store.getConfig().breakDurationTicks; // Assume this is 150 ticks
                    System.out.printf("<Tick %d> [Thread %d] Assistant %d is taking a break for %d ticks.%n",
                                      store.getCurrentTick(), Thread.currentThread().getId(), id, breakDuration);
                    Thread.sleep(breakDuration * ThriftStore.TICK_TIME_SIZE);
                    ticksSinceLastBreak = 0; // Reset ticks since last break
                    System.out.printf("<Tick %d> [Thread %d] Assistant %d has finished their break.%n",
                                      store.getCurrentTick(), Thread.currentThread().getId(), id);
                    continue;
                } 

                synchronized (store.getDeliveryLock()) {
                    while (store.deliveryBoxIsEmpty()) {
                        int waitTicks = random.nextInt(50) + 50; // Random wait time between 50 to 100 ticks
                        store.getDeliveryLock().wait(waitTicks * ThriftStore.TICK_TIME_SIZE);
                        totalWaitedTicks += waitTicks;
                        ticksSinceLastBreak += waitTicks; // Update ticks since last break
                        System.out.printf("<Tick %d> [Thread %d] Assistant %d is waiting for new deliveries. Total waited ticks: %d%n",
                                store.getCurrentTick(), Thread.currentThread().getId(), id, totalWaitedTicks);
                    }
    
                    Map<String, Integer> itemsToStock = new HashMap<>(store.takeItemsFromDelivery());
                    System.out.printf("<Tick %d> [Thread %d] Assistant %d collected items for stocking: %s%n",
                            store.getCurrentTick(), Thread.currentThread().getId(), id, itemsToStock.toString());
    

                    itemsToStock.forEach((section, itemCount) -> {
                        if (itemCount > 0) {
                            try {
                                store.startStockingSection(section);
                                System.out.printf("<Tick %d> [Thread %d] Assistant %d starts walking to stock %s section with %d items.\n",
                                        store.getCurrentTick(), Thread.currentThread().getId(), id, section, itemCount);
                                int walkAndStockTicks = 10 + itemCount;
                                Thread.sleep(walkAndStockTicks * ThriftStore.TICK_TIME_SIZE); // Simulate walking time
                                ticksSinceLastBreak += walkAndStockTicks; // Update ticks since last break
                                
                                // Simulate the actual stocking process
                                System.out.printf("<Tick %d> [Thread %d] Assistant %d begins stocking %s section.\n",
                                        store.getCurrentTick(), Thread.currentThread().getId(), id, section);
                                Thread.sleep(itemCount * ThriftStore.TICK_TIME_SIZE); // Simulate stocking time
                                
                                // Here is where you add the total worked ticks
                                totalWorkedTicks += walkAndStockTicks + itemCount; // Add this line
                    
                                ticksSinceLastBreak += itemCount; // Update ticks since last break again
                                store.finishStockingSection(section);
                                System.out.printf("<Tick %d> [Thread %d] Assistant %d finished stocking %s section.\n",
                                        store.getCurrentTick(), Thread.currentThread().getId(), id, section);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    });
                }
                ticksSinceLastBreak++; 
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.printf("<Tick %d> [Thread %d] Assistant %d interrupted.%n",
                    store.getCurrentTick(), Thread.currentThread().getId(), id);
        }
    }
    private int getRandomBreakInterval() {
        return random.nextInt(store.getConfig().maxBreakInterval - store.getConfig().minBreakInterval + 1) + store.getConfig().minBreakInterval;
    }
}