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
    private AtomicInteger totalWaitTicks = new AtomicInteger();
    private AtomicInteger totalWalkTicks = new AtomicInteger();
    private AtomicInteger totalStockTicks = new AtomicInteger();
    private AtomicInteger totalBreakTicks = new AtomicInteger();
    private AtomicInteger totalWorkTicks = new AtomicInteger();


    public ThriftStore(Config config) {
        this.config = config;
        initializeSections();
        initialDelivery();
    }

    public synchronized void addWaitTicks(int ticks) {
        totalWaitTicks.addAndGet(ticks);
    }

    public synchronized void addWalkTicks(int ticks) {
        totalWalkTicks.addAndGet(ticks);
    }

    public synchronized void addStockTicks(int ticks) {
        totalStockTicks.addAndGet(ticks);
    }

    public synchronized void addBreakTicks(int ticks) {
        totalBreakTicks.addAndGet(ticks);
    }

    public synchronized void addWorkTicks(int ticks) {
        totalWorkTicks.addAndGet(ticks);
    }
    
    
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

    public Config getConfig() {
        return config;
    }
    private void initializeSections() {
        // Initialize each section type based on its configured number
        addSections("electronics", config.numberOfElectronicsSections, INITIAL_SECTION_ITEMS);
        addSections("clothing", config.numberOfClothingSections, INITIAL_SECTION_ITEMS);
        addSections("furniture", config.numberOfFurnitureSections, INITIAL_SECTION_ITEMS);
        addSections("toys", config.numberOfToysSections, INITIAL_SECTION_ITEMS);
        addSections("sporting goods", config.numberOfSportingGoodsSections, INITIAL_SECTION_ITEMS);
        addSections("books", config.numberOfBooksSections, INITIAL_SECTION_ITEMS);
    }
    
    private void addSections(String baseName, int count, int initialItems) {
        for (int i = 1; i <= count; i++) {
            String sectionName = baseName + (count > 1 ? " " + i : "");
            sections.put(sectionName, new Section(sectionName, initialItems));
        }
    }
    public boolean isSectionPopular(String sectionName) {
        // Example threshold for popularity; adjust based on simulation needs
        final double popularityThreshold = 0.15;
    
        // Map section names to their purchase probability
        Map<String, Double> purchaseProbabilities = new HashMap<>();
        purchaseProbabilities.put("electronics", config.customerPurchaseProbabilityElectronics);
        purchaseProbabilities.put("clothing", config.customerPurchaseProbabilityClothing);
        purchaseProbabilities.put("furniture", config.customerPurchaseProbabilityFurniture);
        purchaseProbabilities.put("toys", config.customerPurchaseProbabilityToys);
        purchaseProbabilities.put("sporting goods", config.customerPurchaseProbabilitySportingGoods);
        purchaseProbabilities.put("books", config.customerPurchaseProbabilityBooks);
    
        // Identify the base name of the section to check against the purchase probabilities
        String baseSectionName = sectionName.replaceAll("\\s\\d+$", ""); // Remove trailing numbers (e.g., "electronics 1" -> "electronics")
    
        // Determine if the section is popular based on its purchase probability
        return purchaseProbabilities.getOrDefault(baseSectionName, 0.0) > popularityThreshold;
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

    // Getter method for the current tick count
    public int getCurrentTick() {
        return tickCount.get();
    }

    public Object getDeliveryLock() {
        return deliveryLock;
    }

    public synchronized void simulateDelivery(Map<String, Integer> delivery) {
        itemsForDelivery.putAll(delivery);
        logDelivery(delivery); // This method should log the delivery details.
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


    public void incrementTickCountBy(int ticks) {
        for (int i = 0; i < ticks; i++) {
            tickCount.incrementAndGet();
            // Optionally simulate real-time passing with Thread.sleep(TICK_TIME_SIZE);
        }
        System.out.printf("Global tick count increased by %d, total now %d ticks.\n", ticks, tickCount.get());
    }


    // Method to increment the tick count
    public void incrementTickCount() {
        tickCount.incrementAndGet();
    }


    public void simulateTick() {
        // Increment the tick count for each simulation tick
        tickCount.incrementAndGet();
    
        // Assuming 1000 ticks represent one day in the simulation,
        // log a message at the end of each day
        if (tickCount.get() % 1000 == 0) {
            System.out.printf("<Tick %d> The day has ended. Preparing for a new day.%n", tickCount.get());
            // Here, you can also invoke any end-of-day processing or reporting methods
            generateEnhancedReport(); // Example call to an end-of-day reporting method
        }
        
        // Additional simulation logic can be placed here, if necessary
    }
    
    
    

    private Map<String, Integer> generateInitialDelivery() {
        Map<String, Integer> initialDelivery = new HashMap<>();
        String[] categories = {"electronics", "clothing", "toys", "sporting goods", "furniture", "books"};
        int itemsLeft = 10; // Ensure the total items for the initial delivery is 10
    
        Random random = new Random();
        while (itemsLeft > 0) {
            for (String category : categories) {
                if (itemsLeft == 0) break;
                int items = random.nextInt(itemsLeft) + 1; // Distribute items randomly
                initialDelivery.put(category, initialDelivery.getOrDefault(category, 0) + items);
                itemsLeft -= items;
                if (itemsLeft <= 0) break;
            }
        }
        return initialDelivery;
    }
    

    public synchronized void initialDelivery() {
        Map<String, Integer> initialDelivery = generateInitialDelivery(); // Adjusted line
        itemsForDelivery.putAll(initialDelivery);
        String deliveryLog = initialDelivery.entrySet().stream()
                                            .map(e -> e.getKey() + "=" + e.getValue())
                                            .collect(Collectors.joining(", "));
        System.out.printf("<Tick %d> The first delivery: %s%n", getCurrentTick(), deliveryLog);
        initialDelivery.forEach((sectionName, itemCount) -> sections.get(sectionName).addItem(itemCount));
    }
    


    public synchronized void processDelivery(Map<String, Integer> delivery) {
        itemsForDelivery.putAll(delivery);
        logDelivery(delivery); // Log the delivery
    }
    
    
    private void logDelivery(Map<String, Integer> delivery) {
        if (delivery.isEmpty()) {
            System.out.println("<Tick " + getCurrentTick() + "> No items were delivered.");
            return;
        }
        String deliveryDetails = delivery.entrySet().stream()
                                         .map(entry -> entry.getKey() + ": " + entry.getValue())
                                         .collect(Collectors.joining(", "));
        System.out.printf("<Tick %d> Delivery received: %s%n", getCurrentTick(), deliveryDetails);
    }
    
        

    // Enhanced reporting method
    // public void generateEnhancedReport() {
    //     double averageAssistantWorkTime = calculateAverage(assistantWorkTimes);
    //     double averageCustomerWaitTime = calculateAverage(customerWaitTimes);
    //     double averageAssistantBreakTime = calculateAverage(assistantBreakTimes);
        
    //     System.out.printf("Average Assistant Work Time: %.2f ticks\n", averageAssistantWorkTime);
    //     System.out.printf("Average Customer Wait Time: %.2f ticks\n", averageCustomerWaitTime);
    //     System.out.printf("Average Assistant Break Time: %.2f ticks\n", averageAssistantBreakTime);
        
    //     System.out.println("Distribution of Customer Wait Times: " + calculateDistribution(customerWaitTimes));
    //     System.out.println("Distribution of Assistant Work Times: " + calculateDistribution(assistantWorkTimes));
    //     System.out.println("Distribution of Assistant Break Times: " + calculateDistribution(assistantBreakTimes));
    
    //     double customerWaitToWorkRatio = averageCustomerWaitTime / averageAssistantWorkTime;
    //     System.out.printf("Customer Wait to Assistant Work Ratio: %.2f\n", customerWaitToWorkRatio);
    
    //     System.out.println("Work Time Distribution:\n" + generateHistogram(assistantWorkTimes));
    //     System.out.println("Wait Time Distribution:\n" + generateHistogram(customerWaitTimes));
    //     System.out.println("Break Time Distribution:\n" + generateHistogram(assistantBreakTimes));
    // }

    public void generateEnhancedReport() {
        System.out.println("End of Day Report:");
        System.out.printf("Total Wait Ticks: %d%n", totalWaitTicks.get());
        System.out.printf("Total Work Ticks: %d%n", totalWorkTicks.get());
        System.out.printf("Total Break Ticks: %d%n", totalBreakTicks.get());
        // Include additional analysis and metrics as needed.
    }
    
    
    // private double calculateAverage(List<Integer> times) {
    //     if (times.isEmpty()) return 0.0;
    //     return times.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    // }
    
    // private String calculateDistribution(List<Integer> times) {
    //     if (times.isEmpty()) return "No data";
    //     Map<Integer, Long> distribution = times.stream().collect(Collectors.groupingBy(t -> t, Collectors.counting()));
    //     return distribution.entrySet().stream()
    //             .sorted(Map.Entry.comparingByKey())
    //             .map(entry -> entry.getKey() + " ticks: " + entry.getValue())
    //             .collect(Collectors.joining(", "));
    // }
    
    // private String generateHistogram(List<Integer> times) {
    //     if (times.isEmpty()) return "No data";
    //     final int INTERVAL = 10;
    //     Map<Integer, Integer> histogram = new TreeMap<>();
    //     times.forEach(time -> {
    //         int key = (time / INTERVAL) * INTERVAL;
    //         histogram.put(key, histogram.getOrDefault(key, 0) + 1);
    //     });
    //     StringBuilder histogramBuilder = new StringBuilder();
    //     histogram.forEach((key, value) ->
    //         histogramBuilder.append(String.format("%3d - %3d | %s\n", key, key + INTERVAL - 1, "*".repeat(value)))
    //     );
    //     return histogramBuilder.toString();
    // }

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
    public boolean isStoreBusy() {
        // Example: Consider the store busy if the number of active customers exceeds a threshold
        return getActiveCustomerCount() > config.busyCustomerThreshold;
    }
    
    public int getActiveCustomerCount() {
        // Active customers could be determined by customers currently in the store or attempting purchases
        return customersList.size(); // Simplified example
    }

    public static void main(String[] args) throws InterruptedException {
            Config config = new Config(
            3, // numberOfAssistants
            1, // numberOfElectronicsSections
            1, // numberOfClothingSections
            1, // numberOfFurnitureSections
            1, // numberOfToysSections
            1, // numberOfSportingGoodsSections
            1, // numberOfBooksSections
            0.1, // customerPurchaseProbabilityElectronics
            0.2, // customerPurchaseProbabilityClothing
            0.15, // customerPurchaseProbabilityFurniture
            0.25, // customerPurchaseProbabilityToys
            0.05, // customerPurchaseProbabilitySportingGoods
            0.2, // customerPurchaseProbabilityBooks
            100, // deliveryFrequencyTicks
            10, // maxItemsPerDelivery
            1.5, // customerPatienceMultiplier
            200, // minBreakInterval
            300, // maxBreakInterval
            150, // breakDurationTicks
            1
    );
        ThriftStore store = new ThriftStore(config);


        // Starting the delivery thread
        // new Thread(new DeliveryThread(store), "DeliveryThread").start();
        new Thread(new DeliveryThread(store)).start();
    
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
            store.simulateTick(); // Process the tick
        }

    }
}
