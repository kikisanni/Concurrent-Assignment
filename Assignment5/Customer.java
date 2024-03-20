import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Customer implements Runnable {
    private final ThriftStore store;
    private final int id;
    private final Random random = new Random();
    private  final int MAX_WAIT_TICKS; // Define a timeout
    private static int totalWaitedTicksForAllCustomers = 0;
    private int totalWaitTime = 0; // Tracks the total wait time of this customer

    public Customer(ThriftStore store, int id, double patienceMultiplier) {
        this.store = store;
        this.id = id;
        this.MAX_WAIT_TICKS = (int)(100 * patienceMultiplier); // Adjust maximum wait ticks based on multiplier
    }

    public static int getTotalWaitedTicksForAllCustomers() {
        return totalWaitedTicksForAllCustomers;
    }

    private void recordTotalWaitTime() {
        // Correct invocation of recording wait time in ThriftStore
        store.recordCustomerWaitTime(this.totalWaitTime);
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
                            System.out.printf("<Tick %d> [Thread %d] Customer %d leaves after waiting too long.%n",
                                              store.getCurrentTick(), Thread.currentThread().getId(), id);
                            return; // Customer leaves
                        } else if (waitedTicksForThisPurchase >= MAX_WAIT_TICKS) {
                            sectionToBuyFrom = selectAlternativeSection(sectionToBuyFrom); // Select alternative section
                            waitedTicksForThisPurchase = 0; // Reset wait time
                        } else {
                            Thread.sleep(ThriftStore.TICK_TIME_SIZE);
                            waitedTicksForThisPurchase++;
                        }
                    } else  {
                        // Adjust purchase attempt based on dynamic conditions
                        if (attemptPurchaseBasedOnDynamicConditions(sectionToBuyFrom, waitedTicksForThisPurchase)) {
                            store.buyItemFromSection(sectionToBuyFrom);
                            purchased = true;
                            logPurchase(waitedTicksForThisPurchase, sectionToBuyFrom);
                        }
                    }
                    recordTotalWaitTime();
                }
                simulateShoppingDelay();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }



    private boolean attemptPurchaseBasedOnDynamicConditions(String sectionToBuyFrom, int waitedTicks) {
        // Logic to dynamically adjust the probability of attempting a purchase
        double baseProbability = 0.5; // Start with a base probability
        double stockLevelAdjustmentFactor = store.sectionIsLowOnStock(sectionToBuyFrom) ? -0.2 : 0.2;
        double waitTimeAdjustmentFactor = Math.min(0.5, waitedTicks / 100.0);

        double adjustedProbability = baseProbability + stockLevelAdjustmentFactor + waitTimeAdjustmentFactor;
        return random.nextDouble() < adjustedProbability;
    }

    private String selectRandomSection() {
        String[] sections = store.getSectionNames();
        return sections[random.nextInt(sections.length)];
    }

    private void simulateShoppingDelay() throws InterruptedException {
        int delay = random.nextInt(10, 50) * ThriftStore.TICK_TIME_SIZE;
        Thread.sleep(delay);
    }

    private void logPurchase(int waitedTicks, String section) {
        if (waitedTicks > 0) {
            System.out.printf("<Tick %d> [Thread %d] Customer %d bought item from section %s after waiting %d ticks.%n",
                    store.getCurrentTick(), Thread.currentThread().getId(), id, section, waitedTicks);
        } else {
            System.out.printf("<Tick %d> [Thread %d] Customer %d bought item from section %s.%n",
                    store.getCurrentTick(), Thread.currentThread().getId(), id, section);
        }
    }
    private boolean shouldLeaveDueToWait(int waitedTicks) {
        int dynamicWaitThreshold = MAX_WAIT_TICKS; // Base wait threshold
        if(store.isStoreBusy()) {
            dynamicWaitThreshold *= 0.75; // Less patience if store is busy
        } else {
            dynamicWaitThreshold *= 1.25; // More patience if store is less busy
        }
        return waitedTicks > dynamicWaitThreshold;
    }
    private String selectAlternativeSection(String currentSection) {
    String[] sections = store.getSectionNames();
    // Filter out the current section and any sections low on stock
    List<String> availableSections = Arrays.stream(sections)
                                           .filter(s -> !s.equals(currentSection) && store.sectionHasItems(s))
                                           .collect(Collectors.toList());
    if (availableSections.isEmpty()) {
        return currentSection; // Stick with the current section if no better options
    }
    // Randomly select from available sections
    return availableSections.get(random.nextInt(availableSections.size()));
    }
}
