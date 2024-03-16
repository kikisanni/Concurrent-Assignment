import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

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
                Map<String, Integer> delivery = store.simulateDelivery();
                String deliveredItems = delivery.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining(", "));
                System.out.printf("[Thread %d] Deposit of items: %s%n", Thread.currentThread().getId(), deliveredItems);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private long calculateDeliveryInterval() {
        // Simulate delivery every 100 ticks on average
        return (long) (Math.random() * 2 * ThriftStore.TICK_TIME_SIZE * 100);
    }
}
