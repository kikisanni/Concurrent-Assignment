import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DeliveryThread implements Runnable {
    private final ThriftStore store;
    private final Random random = new Random();
    private int deliveryCounter = 0; // Counter to track ticks since the last delivery

    public DeliveryThread(ThriftStore store) {
        this.store = store;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                // Check if it's time for the next delivery
                if (deliveryCounter >= store.getConfig().deliveryFrequencyTicks) {
                    Map<String, Integer> delivery = simulateDeliveryWithRandomDistribution();
                    store.processDelivery(delivery);
                    deliveryCounter = 0; // Reset the counter after a delivery
                } else {
                    // Increment the counter and wait for one tick
                    deliveryCounter++;
                    Thread.sleep(ThriftStore.TICK_TIME_SIZE);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    private long calculateDeliveryInterval() {
        // Define the average delivery interval in ticks
        final int averageInterval = store.getConfig().deliveryFrequencyTicks;
        // Ensure a minimum interval to prevent too frequent deliveries
        final int minInterval = averageInterval / 2; // For example, half the average as a minimum
        // Generate the interval based on an exponential distribution for randomness
        double lambda = 1.0 / averageInterval;
        long interval = (long) (-Math.log(1 - Math.random()) / lambda);
        // Ensure the interval is not below the minimum threshold
        return Math.max(interval, minInterval);
    }
      
    

    private Map<String, Integer> simulateDeliveryWithRandomDistribution() {
        // Initialize delivery map
        Map<String, Integer> delivery = new HashMap<>();
        // Categories
        String[] categories = {"electronics", "clothing", "toys", "sporting goods", "furniture", "books"};
        int itemsLeft = 10; // Total items to distribute
        while (itemsLeft > 0) {
            for (String category : categories) {
                if (itemsLeft == 0) break;
                // Distribute items randomly ensuring the total sum remains 10
                int items = random.nextInt(itemsLeft) + 1; // At least 1 item, up to the number of items left
                delivery.put(category, delivery.getOrDefault(category, 0) + items);
                itemsLeft -= items;
                if (itemsLeft <= 0) break;
            }
        }
        return delivery;
    }
}
