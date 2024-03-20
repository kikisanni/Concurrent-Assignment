import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Customer implements Runnable {
    private final ThriftStore store;
    private final int id;
    private final Random random = new Random();
    private final int MAX_WAIT_TICKS; // Define a timeout based on customer patience
    private static int totalWaitedTicksForAllCustomers = 0; // Static counter for all customer waited ticks
    private int totalWaitTime = 0; // Tracks this customer's total wait time

    public Customer(ThriftStore store, int id, double patienceMultiplier) {
        this.store = store;
        this.id = id;
        this.MAX_WAIT_TICKS = (int)(100 * patienceMultiplier); // Adjust maximum wait ticks based on patience multiplier
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                String sectionToBuyFrom = selectRandomSection();
                boolean purchased = false;
                int waitedTicksForThisPurchase = 0;

                while (!purchased) {
                    if (store.sectionIsBeingStocked(sectionToBuyFrom) || !store.sectionHasItems(sectionToBuyFrom)) {
                        if (shouldLeaveDueToWait(waitedTicksForThisPurchase)) {
                            logAndGUIUpdate(String.format("<Tick %d> [Thread %d] Customer %d leaves after waiting too long in %s section.",
                                store.getCurrentTick(), Thread.currentThread().getId(), id, sectionToBuyFrom));
                            return; // Customer leaves due to excessive wait
                        }
                        // Simulate waiting for one tick
                        Thread.sleep(ThriftStore.TICK_TIME_SIZE);
                        waitedTicksForThisPurchase++;
                    } else {
                        // Attempt to purchase from the section
                        if (store.buyItemFromSection(sectionToBuyFrom)) {
                            purchased = true;
                            logAndGUIUpdate(String.format("<Tick %d> [Thread %d] Customer %d successfully purchased from %s section after waiting for %d ticks.",
                                store.getCurrentTick(), Thread.currentThread().getId(), id, sectionToBuyFrom, waitedTicksForThisPurchase));
                        }
                    }
                }
                totalWaitTime += waitedTicksForThisPurchase;
                // Simulate additional delay representing browsing/shopping time before potentially attempting another purchase
                simulateShoppingDelay();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            recordTotalWaitTime(); // Record the customer's total wait time upon thread interruption
        }
    }

    // Helper methods
    private String selectRandomSection() {
        String[] sections = store.getSectionNames();
        return sections[random.nextInt(sections.length)];
    }

    private boolean shouldLeaveDueToWait(int waitedTicks) {
        return waitedTicks > MAX_WAIT_TICKS;
    }

    private void simulateShoppingDelay() throws InterruptedException {
        int delay = random.nextInt(10, 50); // Simulate a delay between 10 and 50 ticks
        Thread.sleep(delay * ThriftStore.TICK_TIME_SIZE);
    }

    private void recordTotalWaitTime() {
        totalWaitedTicksForAllCustomers += totalWaitTime; // Accumulate the total wait time for all customers
        store.recordCustomerWaitTime(totalWaitTime); // Record this customer's wait time in the store statistics
    }

    private void logAndGUIUpdate(String message) {
        System.out.println(message);
        store.getGui().updateCustomerInformation(message); // Update the GUI with the customer's actions
    }
}
