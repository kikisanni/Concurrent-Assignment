import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ThriftStore {
    private static final int LOW_STOCK_THRESHOLD = 2;
    public static final int INITIAL_SECTION_ITEMS = 5;
    public static final int TICK_TIME_SIZE = 50; // 50 ms = 1 tick
    private final Map<String, Section> sections = new ConcurrentHashMap<>();
    private final AtomicInteger tickCount = new AtomicInteger();
    private final Random randgen = new Random();
    private final Object deliveryLock = new Object();
    private Map<String, Integer> itemsForDelivery = new HashMap<>();
    public AtomicInteger nextAssistantId = new AtomicInteger(1);
    public AtomicInteger nextCustomerId = new AtomicInteger(1);

    public ThriftStore() {
        initializeSections();
        initialDelivery();
    }

    private void initializeSections() {
        sections.put("electronics", new Section("electronics", INITIAL_SECTION_ITEMS));
        sections.put("clothing", new Section("clothing", INITIAL_SECTION_ITEMS));
        sections.put("furniture", new Section("furniture", INITIAL_SECTION_ITEMS));
        sections.put("toys", new Section("toys", INITIAL_SECTION_ITEMS));
        sections.put("sporting goods", new Section("sporting goods", INITIAL_SECTION_ITEMS));
        sections.put("books", new Section("books", INITIAL_SECTION_ITEMS));
    }

    public Map<String, Integer> takeItemsFromDelivery() {
        synchronized (deliveryLock) {
            Map<String, Integer> itemsToStock = new HashMap<>(itemsForDelivery);
            itemsForDelivery.clear();
            return itemsToStock;
        }
    }

    public boolean sectionsNeedRestocking() {
        return sections.values().stream().anyMatch(section -> section.isLowOnStock());
    }

    public boolean deliveryBoxIsEmpty() {
        return itemsForDelivery.isEmpty();
    }

    public boolean sectionIsLowOnStock(String sectionName) {
        Section section = sections.get(sectionName);
        return section != null && section.isLowOnStock();
    }

    public boolean sectionHasItems(String sectionName) {
        Section section = sections.get(sectionName);
        return section != null && section.getItemCount() > 0;
    }

    public boolean canStockSection(String sectionName) {
        Section section = sections.get(sectionName);
        return section != null && !section.isStocking();
    }

    public void startStockingSection(String sectionName) {
        Section section = sections.get(sectionName);
        if (section != null) {
            section.startStocking();
        }
    }

    public boolean sectionIsBeingStocked(String sectionName) {
        Section section = sections.get(sectionName);
        return section != null && section.isStocking();
    }

    public void stockSection(String sectionName, int itemCount) {
        Section section = sections.get(sectionName);
        if (section != null) {
            section.addItem(itemCount);
        }
    }

    public void finishStockingSection(String sectionName) {
        Section section = sections.get(sectionName);
        if (section != null) {
            section.finishStocking();
        }
    }

    public boolean buyItemFromSection(String sectionName) {
        Section section = sections.get(sectionName);
        if (section != null) {
            return section.removeItem();
        }
        return false;
    }

    public String[] getSectionNames() {
        return sections.keySet().toArray(new String[0]);
    }

    public int getCurrentTick() {
        return tickCount.get();
    }

    public Object getDeliveryLock() {
        return deliveryLock;
    }

    public synchronized Map<String, Integer> simulateDelivery() {
        Map<String, Integer> delivery = generateRandomDelivery();
        synchronized (deliveryLock) {
            itemsForDelivery.putAll(delivery);
            deliveryLock.notifyAll(); // Notify assistants that new items have arrived
        }
        // Log the delivery
        String deliveryLog = delivery.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(", "));
        System.out.printf("<Tick %d> [Thread %d] Delivery of items: %s%n", getCurrentTick(), Thread.currentThread().getId(), deliveryLog);
        // Return the map of delivered items
        return delivery;
    }



    private Map<String, Integer> generateRandomDelivery() {
        Map<String, Integer> delivery = new HashMap<>();
        int itemsLeft = 10;
        String[] sectionNames = sections.keySet().toArray(new String[0]);
        while (itemsLeft > 0) {
            String section = sectionNames[randgen.nextInt(sectionNames.length)];
            int items = randgen.nextInt(itemsLeft) + 1;
            delivery.put(section, delivery.getOrDefault(section, 0) + items);
            itemsLeft -= items;
        }
        return delivery;
    }

    public void simulateTick() {
        tickCount.incrementAndGet();
        if (randgen.nextDouble() < 1.0 / 100) {
            simulateDelivery();
        }
        if (tickCount.get() % 1000 == 0) {
            System.out.println("<" + tickCount.get() + "> The day has ended.");
            tickCount.set(0);
        }
    }

    public synchronized void initialDelivery() {
        Map<String, Integer> initialDelivery = generateRandomDelivery();
        itemsForDelivery.putAll(initialDelivery);
        String deliveryLog = initialDelivery.entrySet().stream()
                                            .map(e -> e.getKey() + "=" + e.getValue())
                                            .collect(Collectors.joining(", "));
        System.out.printf("<Tick %d> The first delivery: %s%n", getCurrentTick(), deliveryLog);
        initialDelivery.forEach((sectionName, itemCount) -> sections.get(sectionName).addItem(itemCount));
    }

        public static void main(String[] args) throws InterruptedException {
            ThriftStore store = new ThriftStore();

            // Starting the delivery thread
            new Thread(new DeliveryThread(store), "DeliveryThread").start();

            // Starting multiple assistant threads
            for (int i = 0; i < 3; i++) { // Adjust number of assistants as needed
                new Thread(new Assistant(store, store.nextAssistantId.getAndIncrement()), "Assistant-" + (i + 1)).start();
            }

            // Starting customer threads, for completeness of the simulation
            for (int i = 0; i < 4; i++) {
                new Thread(new Customer(store, store.nextCustomerId.getAndIncrement()), "Customer-" + (i + 1)).start();
            }

            // Simulate thrift store operation
            while (true) {
                Thread.sleep(ThriftStore.TICK_TIME_SIZE);
                store.simulateTick();
            }
        }


}
