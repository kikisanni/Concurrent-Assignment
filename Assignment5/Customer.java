import java.util.Random;

/**
 * This thread represents a customer interacting with the store, that is, purchasing items, leaving the store after waiting too long and so on.
 * Customers choose what they want to buy at random sections, and if the item isn't accessible right away, they have to wait a while.
 */
public class Customer implements Runnable {
    private final ThriftStore store; // Reference to the thrift store
    private final int id; // Unique identifier for the customer
    private final Random random = new Random(); // Using a random generator, simulate consumer behaviour
    private final int MAX_WAIT_TICKS; // Maximum ticks a customer will wait for an item
    private int totalWaitTime = 0; // Total wait time accumulated by the customer

    /**
     * Constructs a Customer instance.
     *
     * @param store             The thrift store where the customer shops.
     * @param id                The unique identifier for this customer.
     * @param patienceMultiplier A multiplier influencing the customer's tolerance for waiting times.
     */
    public Customer(ThriftStore store, int id, double patienceMultiplier) {
        this.store = store;
        this.id = id;
        this.MAX_WAIT_TICKS = (int)(100 * patienceMultiplier); // Calculate max wait ticks based on the patience multiplier
    }

    /**
     * The main run loop for the customer thread, manages the customer's shopping process.
     */
    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                String sectionToBuyFrom = selectRandomSection(); // Select a random section to buy from
                boolean purchased = false; // Track to see if the purchase was successful
                int waitedTicksForThisPurchase = 0; // Ticks waited for the current purchase attempt

                while (!purchased) {
                    if (store.sectionIsBeingStocked(sectionToBuyFrom) || !store.sectionHasItems(sectionToBuyFrom)) {
                        // Check if the customer needs to leave due to excessive waiting
                        if (waitedTicksForThisPurchase >= MAX_WAIT_TICKS) {
                            logAndGUIUpdate(String.format("<Tick %d> [Thread %d] Customer %d leaves after waiting too long in %s section.",
                                store.getCurrentTick(), Thread.currentThread().getId(), id, sectionToBuyFrom));
                            return; // Leave the store
                        }
                        waitedTicksForThisPurchase++; // Increment wait time
                        Thread.sleep(ThriftStore.TICK_TIME_SIZE); // Simulate waiting
                    } else {
                        // Make a purchase and update wait time and log
                        store.buyItemFromSection(sectionToBuyFrom);
                        purchased = true; // Flag it as true if purchase was successful
                        totalWaitTime += waitedTicksForThisPurchase; // Update total wait time by the waited ticks for the current purchase
                        logAndGUIUpdate(String.format("<Tick %d> [Thread %d] Customer %d successfully purchased from %s section after waiting for %d ticks.",
                                store.getCurrentTick(), Thread.currentThread().getId(), id, sectionToBuyFrom, waitedTicksForThisPurchase));
                    }
                }
                store.recordCustomerWaitTime(totalWaitTime); // store the customer's total wait time
                simulateShoppingDelay(); // Simulate delay between shopping actions of customers
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            store.recordCustomerWaitTime(totalWaitTime); // records total wait time upon thread interruption
        }
    }

    /**
     * Selects a random section from the thriftstore to purchase from.
     *
     * @return The name of the selected section.
     */
    private String selectRandomSection() {
        String[] sections = store.getSectionNames(); //Get the section names from the store
        return sections[random.nextInt(sections.length)]; // Return a randomly selected section
    }

    /**
     * Creates the impression of a delay, signifying the amount of time a buyer needs to peruse and choose their course of action.
     *
     * @throws InterruptedException if the thread is interrupted while sleeping.
     */
    private void simulateShoppingDelay() throws InterruptedException {
        int delay = random.nextInt(10, 50); // Delay for ten to fifty ticks is simulated.
        Thread.sleep(delay * ThriftStore.TICK_TIME_SIZE); // Apply the simulated delay
    }

    /**
     * Updates the GUI with the data and logs a message to the terminal.
     *
     * @param message The message to be logged and displayed on the GUI.
     */
    private void logAndGUIUpdate(String message) {
        System.out.println(message); // Log the message to the terminal
        store.getGui().updateCustomerInfo(message); // Update the GUI with the customer data
    }
}
