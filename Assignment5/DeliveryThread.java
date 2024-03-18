import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DeliveryThread implements Runnable {
    private final ThriftStore store;
    private final Random random = new Random();

    public DeliveryThread(ThriftStore store) {
        this.store = store;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Thread.sleep(calculateDeliveryInterval());
                Map<String, Integer> delivery = simulateDeliveryWithRandomDistribution();
                store.processDelivery(delivery);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private long calculateDeliveryInterval() {
        // Simulates an average of every 100 ticks
        return (long) (Math.random() * 2 * store.getConfig().deliveryFrequencyTicks);
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
