import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
* Shows a delivery thread that acts like deliveries to the thrift store every so often. 
* Deliveries include a mix of random things spread out in different parts of the store.
 */

public class DeliveryThread implements Runnable {
    private final ThriftStore store;
    private final Random random = new Random(); // random intervals
    private int deliveryCounter = 0; // Track the number of ticks since the last delivery with this counter

    public DeliveryThread(ThriftStore store) {
        this.store = store; // Delivery Thread Constructor
    }

    @Override
    public void run() {
        try {
            // Always look out for delays and keep track of deliveries.
            while (!Thread.currentThread().isInterrupted()) {
                if (isTimeForNextDelivery()) { // Check to see if it's time to make a new supply
                    Map<String, Integer> delivery = simulateDeliveryWithRandomDistribution();
                    store.processDelivery(delivery); // Perform the delivery simulation
                    logDelivery(delivery); // Mark the shipment details for tracking purposes
                    resetDeliveryCounter(); // Once the delivery is confirmed, reset the counter
                } else {
                    waitForNextTick(); //If the delivery time has not yet arrived, please wait for the next tick
                }
            }
        } catch (InterruptedException e) {
            // Ensure the thread is properly interrupted in case of an interruption exception
            Thread.currentThread().interrupt();
        }
    }

    // Verifies that the present tick counter satisfies the delivery frequency criterion
    private boolean isTimeForNextDelivery() {
        return deliveryCounter >= store.getConfig().deliveryFrequencyTicks;
    }

    // Resets the delivery counter to zero
    private void resetDeliveryCounter() {
        deliveryCounter = 0;
    }

    // Activates the next tick by halting the thread and incrementing the counter
    private void waitForNextTick() throws InterruptedException {
        deliveryCounter++;
        Thread.sleep(ThriftStore.TICK_TIME_SIZE);
    }

    // Invents a scenario where different types of deliveries are distributed at random
    private Map<String, Integer> simulateDeliveryWithRandomDistribution() {
        Map<String, Integer> delivery = new HashMap<>();
        String[] categories = {"electronics", "clothing", "toys", "sporting goods", "furniture", "books"};
        int totalItems = 10; // The sum total of the things that will be distributed in this shipment

        // Split the total objects up into various groups at random
        while (totalItems > 0) {
            String category = categories[random.nextInt(categories.length)];
            int items = random.nextInt(Math.min(totalItems, 3)) + 1; //  Assemble one to three things to guarantee variety
            delivery.put(category, delivery.getOrDefault(category, 0) + items);
            totalItems -= items;
        }

        return delivery;
    }

    // Records information about a completed delivery
    private void logDelivery(Map<String, Integer> delivery) {
        System.out.println("Delivery processed: " + delivery);
    }
}