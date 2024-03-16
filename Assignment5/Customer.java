import java.util.Random;

public class Customer implements Runnable {
    private final ThriftStore store;
    private final int id;
    private final Random random = new Random();
    private static int totalWaitedTicksForAllCustomers = 0; // Static to track across all instances

    public Customer(ThriftStore store, int id) {
        this.store = store;
        this.id = id;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                String sectionToBuyFrom = selectRandomSection();
                boolean purchased = false;
                int waitedTicksForThisPurchase = 0;

                while (!purchased) {
                    // Wait if the section is being restocked
                    if (store.sectionIsBeingStocked(sectionToBuyFrom)) {
                        int startTick = store.getCurrentTick();
                        while (store.sectionIsBeingStocked(sectionToBuyFrom)) {
                            Thread.sleep(ThriftStore.TICK_TIME_SIZE);
                            waitedTicksForThisPurchase++;
                        }
                        int endTick = store.getCurrentTick();
                        System.out.printf("<Tick %d> [Thread %d] Customer %d waited %d ticks for section %s to be restocked.%n", endTick, Thread.currentThread().getId(), id, waitedTicksForThisPurchase, sectionToBuyFrom);
                    }

                    if (store.sectionHasItems(sectionToBuyFrom)) {
                        store.buyItemFromSection(sectionToBuyFrom);
                        purchased = true;
                        synchronized (Customer.class) {
                            totalWaitedTicksForAllCustomers += waitedTicksForThisPurchase;
                        }
                        if (waitedTicksForThisPurchase > 0) {
                            System.out.printf("<Tick %d> [Thread %d] Customer %d bought item from section %s after waiting %d ticks.%n", store.getCurrentTick(), Thread.currentThread().getId(), id, sectionToBuyFrom, waitedTicksForThisPurchase);
                        } else {
                            System.out.printf("<Tick %d> [Thread %d] Customer %d bought item from section %s without the need to wait.%n", store.getCurrentTick(), Thread.currentThread().getId(), id, sectionToBuyFrom);
                        }
                    } else {
                        // If the section is out of stock, wait one tick
                        Thread.sleep(ThriftStore.TICK_TIME_SIZE);
                        waitedTicksForThisPurchase++;
                    }
                }

                // Simulate time taken for browsing before the next purchase
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
        // Simulate a random delay between shopping actions
        int delay = random.nextInt(10, 50) * ThriftStore.TICK_TIME_SIZE;
        Thread.sleep(delay);
    }
}
