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
                    System.out.printf("<Tick %d> [Thread %d] Scheduled delivery of items: %s in %d ticks%n", store.getCurrentTick(), Thread.currentThread().getId(), deliveredItems, calculateDeliveryInterval());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private long calculateDeliveryInterval() {
        // Use the deliveryFrequencyTicks from the store's config
        return (long) (Math.random() * 2 * store.getConfig().deliveryFrequencyTicks);
    }
}
