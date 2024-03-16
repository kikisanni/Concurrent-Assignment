import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Assistant implements Runnable {
    private final ThriftStore store;
    private final int id;
    private final Random random = new Random();
    private int totalWaitedTicks = 0; // To keep track of total waited ticks for this assistant

    public Assistant(ThriftStore store, int id) {
        this.store = store;
        this.id = id;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                synchronized (store.getDeliveryLock()) {
                    while (store.deliveryBoxIsEmpty()) {
                        int waitTicks = random.nextInt(50) + 50; // Random wait time between 50 to 100 ticks
                        store.getDeliveryLock().wait(waitTicks * ThriftStore.TICK_TIME_SIZE);
                        totalWaitedTicks += waitTicks;
                        System.out.printf("<Tick %d> [Thread %d] Assistant %d is waiting for new deliveries. Total waited ticks: %d\n",
                                store.getCurrentTick(), Thread.currentThread().getId(), id, totalWaitedTicks);
                    }

                    Map<String, Integer> itemsToStock = new HashMap<>(store.takeItemsFromDelivery());
                    System.out.printf("<Tick %d> [Thread %d] Assistant %d collected items for stocking: %s\n",
                            store.getCurrentTick(), Thread.currentThread().getId(), id, itemsToStock.toString());

                    itemsToStock.forEach((section, itemCount) -> {
                        if (itemCount > 0) {
                            try {
                                store.startStockingSection(section);
                                System.out.printf("<Tick %d> [Thread %d] Assistant %d starts walking to stock %s section with %d items.\n",
                                        store.getCurrentTick(), Thread.currentThread().getId(), id, section, itemCount);
                                Thread.sleep((10 + itemCount) * ThriftStore.TICK_TIME_SIZE); // Simulate walking time
                                System.out.printf("<Tick %d> [Thread %d] Assistant %d begins stocking %s section.\n",
                                        store.getCurrentTick(), Thread.currentThread().getId(), id, section);
                                Thread.sleep(itemCount * ThriftStore.TICK_TIME_SIZE); // Simulate stocking time
                                store.finishStockingSection(section);
                                System.out.printf("<Tick %d> [Thread %d] Assistant %d finished stocking %s section.\n",
                                        store.getCurrentTick(), Thread.currentThread().getId(), id, section);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    });
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.printf("<Tick %d> [Thread %d] Assistant %d interrupted.\n",
                    store.getCurrentTick(), Thread.currentThread().getId(), id);
        }
    }
}
