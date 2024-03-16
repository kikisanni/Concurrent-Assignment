import java.util.Random;

public class Customer implements Runnable {
    private final ThriftStore store;
    private final int id;
    private final Random random = new Random();
    private  final int MAX_WAIT_TICKS; // Define a timeout
    private static int totalWaitedTicksForAllCustomers = 0;

    public Customer(ThriftStore store, int id, double patienceMultiplier) {
        this.store = store;
        this.id = id;
        this.MAX_WAIT_TICKS = (int)(100 * patienceMultiplier); // Adjust maximum wait ticks based on multiplier
    }

    public static int getTotalWaitedTicksForAllCustomers() {
        return totalWaitedTicksForAllCustomers;
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
                        if (waitedTicksForThisPurchase >= MAX_WAIT_TICKS) {
                            if (random.nextBoolean()) { // 50% chance
                                System.out.printf("<Tick %d> [Thread %d] Customer %d is attempting to buy from section %s.%n",
                                        store.getCurrentTick(), Thread.currentThread().getId(), id, sectionToBuyFrom);
                                return; // Customer leaves
                            } else {
                                sectionToBuyFrom = selectRandomSection(); // Choose a different section
                                waitedTicksForThisPurchase = 0; // Reset wait time
                                System.out.printf("<Tick %d> [Thread %d] Customer %d decides to try a different section after waiting too long.%n",
                                        store.getCurrentTick(), Thread.currentThread().getId(), id);
                                continue;
                            }
                        }

                        Thread.sleep(ThriftStore.TICK_TIME_SIZE);
                        waitedTicksForThisPurchase++;
                    } else {
                        store.buyItemFromSection(sectionToBuyFrom);
                        purchased = true;
                        synchronized (Customer.class) {
                            totalWaitedTicksForAllCustomers += waitedTicksForThisPurchase;
                        }
                        logPurchase(waitedTicksForThisPurchase, sectionToBuyFrom);
                    }
                }

                simulateShoppingDelay();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.printf("<%d> Customer %d has been interrupted.%n", store.getCurrentTick(), id);
        }
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
            System.out.printf("<Tick %d> [Thread %d] Customer %d bought item from section %s without the need to wait.%n",
                    store.getCurrentTick(), Thread.currentThread().getId(), id, section);
        }
    }
}
