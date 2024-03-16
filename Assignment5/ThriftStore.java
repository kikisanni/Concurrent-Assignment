import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ThriftStore {
    private final Config config;
    public static final int INITIAL_SECTION_ITEMS = 5;
    public static final int TICK_TIME_SIZE = 50; // 50 ms = 1 tick
    private final Map<String, Section> sections = new ConcurrentHashMap<>();
    private final AtomicInteger tickCount = new AtomicInteger();
    private final Random randgen = new Random();
    private final Object deliveryLock = new Object();
    private Map<String, Integer> itemsForDelivery = new HashMap<>();
    public AtomicInteger nextAssistantId = new AtomicInteger(1);
    public AtomicInteger nextCustomerId = new AtomicInteger(1);
    private List<Assistant> assistantsList = new CopyOnWriteArrayList<>();
    private List<Customer> customersList = new CopyOnWriteArrayList<>();
    // Enhanced metrics tracking
    private List<Integer> customerWaitTimes = new ArrayList<>();
    private List<Integer> assistantWorkTimes = new ArrayList<>();
    private List<Integer> assistantBreakTimes = new ArrayList<>();

    // Method to add an assistant
    public void addAssistant(Assistant assistant) {
        assistantsList.add(assistant);
    }

    // Method to add a customer
    public void addCustomer(Customer customer) {
        customersList.add(customer);
    }

    // Accessor methods for the lists
    public List<Assistant> getAssistantsList() {
        return assistantsList;
    }

    public List<Customer> getCustomersList() {
        return customersList;
    }

    public ThriftStore(Config config) {
        this.config = config;
        initializeSections();
        initialDelivery();
    }
    public Config getConfig() {
        return config;
    }
    private void initializeSections() {
        // Initialize standard sections
        sections.put("electronics", new Section("electronics", INITIAL_SECTION_ITEMS));
        sections.put("clothing", new Section("clothing", INITIAL_SECTION_ITEMS));
        sections.put("furniture", new Section("furniture", INITIAL_SECTION_ITEMS));
        sections.put("toys", new Section("toys", INITIAL_SECTION_ITEMS));
        sections.put("sporting goods", new Section("sporting goods", INITIAL_SECTION_ITEMS));
        sections.put("books", new Section("books", INITIAL_SECTION_ITEMS));
        // Initialize additional sections based on config
        for (int i = 1; i <= config.numberOfAdditionalSections; i++) {
            sections.put("Section " + i, new Section("Section " + i, INITIAL_SECTION_ITEMS));
        }
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
        int itemsLeft = randgen.nextInt(config.maxItemsPerDelivery) + 1; // Adjust maximum items per delivery
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
            System.out.printf("<Tick %d> The day has ended. Preparing for a new day.%n", tickCount.get());
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

    // Enhanced reporting method
    public void generateEnhancedReport() {
        double averageAssistantWorkTime = calculateAverage(assistantWorkTimes);
        double averageCustomerWaitTime = calculateAverage(customerWaitTimes);
        double averageAssistantBreakTime = calculateAverage(assistantBreakTimes);
        
        // Basic statistics
        System.out.printf("Average Assistant Work Time: %.2f ticks\n", averageAssistantWorkTime);
        System.out.printf("Average Customer Wait Time: %.2f ticks\n", averageCustomerWaitTime);
        System.out.printf("Average Assistant Break Time: %.2f ticks\n", averageAssistantBreakTime);
        
        // Additional analysis
        System.out.printf("Distribution of Customer Wait Times: %s\n", calculateDistribution(customerWaitTimes));
        System.out.printf("Distribution of Assistant Work Times: %s\n", calculateDistribution(assistantWorkTimes));
        System.out.printf("Distribution of Assistant Break Times: %s\n", calculateDistribution(assistantBreakTimes));
        
        
        // Trade-off analysis
        double tradeOffRatio = averageCustomerWaitTime / averageAssistantWorkTime;
        System.out.printf("Customer Wait to Assistant Work Ratio: %.2f\n", tradeOffRatio);
        
        
        System.out.println("Work Time Distribution:\n" + generateHistogram(assistantWorkTimes));
        System.out.println("Wait Time Distribution:\n" + generateHistogram(customerWaitTimes));
        System.out.println("Break Time Distribution:\n" + generateHistogram(assistantBreakTimes));
    }

    private String generateHistogram(List<Integer> times) {
        if (times.isEmpty()) {
            return "No data.";
        }

        final int INTERVAL = 10; // Histogram interval
        Map<Integer, Integer> histogram = new TreeMap<>();
        times.forEach(time -> {
            int key = (time / INTERVAL) * INTERVAL;
            histogram.put(key, histogram.getOrDefault(key, 0) + 1);
        });

        StringBuilder histogramString = new StringBuilder();
        histogram.forEach((key, value) -> {
            histogramString.append(String.format("%3d - %3d | %s%n", key, key + INTERVAL - 1, "*".repeat(value)));
        });

        return histogramString.toString();
    }

    private double calculateMedian(List<Integer> times) {
        if (times.isEmpty()) {
            return 0.0;
        }

        Collections.sort(times);
        int middle = times.size() / 2;
        if (times.size() % 2 == 1) {
            return times.get(middle);
        } else {
            return (times.get(middle - 1) + times.get(middle)) / 2.0;
        }
    }

    private int calculateMode(List<Integer> times) {
        if (times.isEmpty()) {
            return 0;
        }

        Map<Integer, Integer> frequencyMap = new HashMap<>();
        times.forEach(time -> frequencyMap.put(time, frequencyMap.getOrDefault(time, 0) + 1));
        return Collections.max(frequencyMap.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    private double calculateAverage(List<Integer> times) {
        if (times.isEmpty()) {
            return 0.0;
        }
        return times.stream().mapToInt(Integer::intValue).average().getAsDouble();
    }

    private String calculateDistribution(List<Integer> times) {
        Map<Integer, Long> distribution = times.stream().collect(Collectors.groupingBy(t -> t, Collectors.counting()));
        return distribution.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> String.format("%d ticks: %d", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(", "));
    }

    // Enhanced methods to update metrics (called from appropriate places in Assistant and Customer classes)
    public synchronized void recordCustomerWaitTime(int waitTime) {
        customerWaitTimes.add(waitTime);
    }

    public synchronized void recordAssistantWorkTime(int workTime) {
        assistantWorkTimes.add(workTime);
    }

    public synchronized void recordAssistantBreakTime(int breakTime) {
        assistantBreakTimes.add(breakTime);
    }

    public static void main(String[] args) throws InterruptedException {
        Config config = new Config(3, 2, 0.5, 100, 10, 1.5, 200, 300, 150); // Example new configuration
        ThriftStore store = new ThriftStore(config);
        
        // Register the shutdown hook here, before the infinite loop
        Runtime.getRuntime().addShutdownHook(new Thread(() -> store.generateEnhancedReport()));

        // Starting the delivery thread
        new Thread(new DeliveryThread(store), "DeliveryThread").start();
    
        // Starting multiple assistant threads based on config.numberOfAssistants
        for (int i = 0; i < config.numberOfAssistants; i++) {
            Assistant assistant = new Assistant(store, store.nextAssistantId.getAndIncrement());
            store.addAssistant(assistant); // Add assistant to the store's list
            new Thread(assistant, "Assistant-" + (i + 1)).start();
        }
    
        // Starting customer threads, adjusting for dynamic conditions based on configuration
        for (int i = 0; i < config.numberOfAssistants * 2; i++) { // Example: twice the number of assistants
            Customer customer = new Customer(store, store.nextCustomerId.getAndIncrement(), config.customerPatienceMultiplier);
            store.addCustomer(customer); // Add customer to the store's list
            new Thread(customer, "Customer-" + (i + 1)).start();
        }
    
        // Simulate thrift store operation
        while (true) {
            Thread.sleep(ThriftStore.TICK_TIME_SIZE);
            store.simulateTick();
        }
    }
}
