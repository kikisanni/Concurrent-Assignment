import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Represents a thrift store managing deliveries, assistants, and customers behaviour.
 * It simulates store operations including stocking and purchasing items.
 */

public class ThriftStore {
    private final Config config; // Store configuration
    private ThriftStoreGUI gui; // Graphical user interface for the store
    public static final int INITIAL_SECTION_ITEMS = 5; // Initial items per section
    public static final int TICK_TIME_SIZE = 50; // Duration of a tick in milliseconds
    private final Map<String, Section> sections = new ConcurrentHashMap<>(); // Sections in the store
    private final AtomicInteger tickCount = new AtomicInteger(); // Global tick count for simulation
    private final Random randgen = new Random(); // Random generator for various operations
    private final Object deliveryLock = new Object(); // Lock for synchronizing delivery operations
    private Map<String, Integer> itemsForDelivery = new HashMap<>(); // Items waiting to be stocked
    public AtomicInteger nextAssistantId = new AtomicInteger(1); // ID generator for assistants
    public AtomicInteger nextCustomerId = new AtomicInteger(1); // ID generator for customers
    private List<Assistant> assistantsList = new CopyOnWriteArrayList<>(); // List of store assistants
    private List<Customer> customersList = new CopyOnWriteArrayList<>(); // List of store customers
    private final List<Integer> customerWaitTimes = Collections.synchronizedList(new ArrayList<>()); // An array to store customer wait times
    private final List<Integer> assistantWorkTimes = Collections.synchronizedList(new ArrayList<>()); // An array to store assistant work times
    private List<Integer> assistantBreakTimes = new ArrayList<>(); // An array to store assistant breaks
    private AtomicInteger totalWaitTicks = new AtomicInteger(); // Initialiasing customer total wait ticks
    private AtomicInteger totalWalkTicks = new AtomicInteger(); // Initialising assistant total walk ticks
    private AtomicInteger totalStockTicks = new AtomicInteger(); // Initialising assistant total stock ticks
    private AtomicInteger totalBreakTicks = new AtomicInteger(); // Initialising assistant total break ticks
    private AtomicInteger totalWorkTicks = new AtomicInteger(); // Initialising assistant total work ticks


    /**
     * Creates a ThriftStore object with the specified configurations.
     *
     * @param config Configuration parameters for the thriftstore.
     */

    public ThriftStore(Config config) {
        this.config = config;
        initializeSections();
        initialDelivery();
        gui = new ThriftStoreGUI(); // Initialise GUI
    }

    public ThriftStoreGUI getGui() {
        return this.gui;
    }
    
     /**
     * Adds ticks to the total wait ticks.
     *
     * @param ticks The number of ticks to add.
     */
    public synchronized void addWaitTicks(int ticks) {
        totalWaitTicks.addAndGet(ticks);
    }

    //add current ticks to total walk ticks
    public synchronized void addWalkTicks(int ticks) {
        totalWalkTicks.addAndGet(ticks);
    }

    //add current ticks to total stock ticks
    public synchronized void addStockTicks(int ticks) {
        totalStockTicks.addAndGet(ticks);
    }

    //add current ticks to total break ticks
    public synchronized void addBreakTicks(int ticks) {
        totalBreakTicks.addAndGet(ticks);
    }

    //add current ticks to total work ticks
    public synchronized void addWorkTicks(int ticks) {
        totalWorkTicks.addAndGet(ticks);
    }
    
    
    /**
     * Adds an assistant to the store.
     *
     * @param assistant The assistant to add.
     */
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

    //getters for customer list
    public List<Customer> getCustomersList() {
        return customersList;
    }

    //getters for config
    public Config getConfig() {
        return config;
    }

    /**
     * Initializes the sections of the store based on the configuration.
     */
    private void initializeSections() {
        // Initialize each section type based on its configured number
        addSections("electronics", config.numberOfElectronicsSections, INITIAL_SECTION_ITEMS);
        addSections("clothing", config.numberOfClothingSections, INITIAL_SECTION_ITEMS);
        addSections("furniture", config.numberOfFurnitureSections, INITIAL_SECTION_ITEMS);
        addSections("toys", config.numberOfToysSections, INITIAL_SECTION_ITEMS);
        addSections("sporting goods", config.numberOfSportingGoodsSections, INITIAL_SECTION_ITEMS);
        addSections("books", config.numberOfBooksSections, INITIAL_SECTION_ITEMS);
    }
    
     /**
     * Adds sections to the store.
     *
     * @param baseName     The base name of the section.
     * @param count        The number of sections to add.
     * @param initialItems The initial number of items for each section.
     */
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

    //method for taking items from the delivery box
    public Map<String, Integer> takeItemsFromDelivery() {
        synchronized (deliveryLock) {
            Map<String, Integer> itemsToStock = new HashMap<>(itemsForDelivery);
            itemsForDelivery.clear();
            return itemsToStock;
        }
    }

    //meyhod for checking if a section needs to be restocked
    public boolean sectionsNeedRestocking() {
        return sections.values().stream().anyMatch(section -> section.isLowOnStock());
    }


    //method for checking if a deklivery boc is empty
    public boolean deliveryBoxIsEmpty() {
        return itemsForDelivery.isEmpty();
    }

    //method for checking if a section is low on items
    public boolean sectionIsLowOnStock(String sectionName) {
        Section section = sections.get(sectionName);
        return section != null && section.isLowOnStock();
    }

    // method for checking if a section has items 
    public boolean sectionHasItems(String sectionName) {
        Section section = sections.get(sectionName);
        return section != null && section.getItemCount() > 0;
    }

    //method for checking if the section can be stocked, that is, if it is not being stocked by another assistant
    public boolean canStockSection(String sectionName) {
        Section section = sections.get(sectionName);
        return section != null && !section.isStocking();
    }

    //method for customers to start stocking a section
    public void startStockingSection(String sectionName) {
        Section section = sections.get(sectionName);
        if (section != null) {
            section.startStocking();
        }
    }

    //method to check if a section is currently being stocked
    public boolean sectionIsBeingStocked(String sectionName) {
        Section section = sections.get(sectionName);
        return section != null && section.isStocking();
    }

    //method for stocking a section
    public void stockSection(String sectionName, int itemCount) {
        Section section = sections.get(sectionName);
        if (section != null) {
            section.addItem(itemCount);
        }
    }

    //method for checking if an assistant hjas finished stocking
    public void finishStockingSection(String sectionName) {
        Section section = sections.get(sectionName);
        if (section != null) {
            section.finishStocking();
        }
    }

    //method for customers to buy items from a section
    public boolean buyItemFromSection(String sectionName) {
        Section section = sections.get(sectionName);
        if (section != null) {
            return section.removeItem();
        }
        return false;
    }

    //mnethod for getting the name of sections
    public String[] getSectionNames() {
        return sections.keySet().toArray(new String[0]);
    }

    // Getter method for the current tick count
    public int getCurrentTick() {
        return tickCount.get();
    }

    //method for getting delivery lock
    public Object getDeliveryLock() {
        return deliveryLock;
    }

    //method for simulating delivery
    public synchronized void simulateDelivery(Map<String, Integer> delivery) {
        itemsForDelivery.putAll(delivery);
        logDelivery(delivery); // This method should log the delivery details.
    }
    

    // private Map<String, Integer> generateRandomDelivery() {
    //     Map<String, Integer> delivery = new HashMap<>();
    //     int itemsLeft = randgen.nextInt(config.maxItemsPerDelivery) + 1; // Adjust maximum items per delivery
    //     String[] sectionNames = sections.keySet().toArray(new String[0]);
    //     while (itemsLeft > 0) {
    //         String section = sectionNames[randgen.nextInt(sectionNames.length)];
    //         int items = randgen.nextInt(itemsLeft) + 1;
    //         delivery.put(section, delivery.getOrDefault(section, 0) + items);
    //         itemsLeft -= items;
    //     }
    //     return delivery;
    // }


    //method for incrementing tick count
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
        gui.updateTick(tickCount.get());
    
        // 1000 ticks represent one day in the simulation,
        // log a message at the end of each day
        if (tickCount.get() % 1000 == 0) {
            System.out.printf("<Tick %d> The day has ended. Preparing for a new day.%n", tickCount.get());
            generateEnhancedReport();
        }
        
    }
    
    
    
    // generating the initial delivery of items
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
    

    //the first delivery of the day
    public synchronized void initialDelivery() {
        Map<String, Integer> initialDelivery = generateInitialDelivery(); // Adjusted line
        itemsForDelivery.putAll(initialDelivery);
        String deliveryLog = initialDelivery.entrySet().stream()
                                            .map(e -> e.getKey() + "=" + e.getValue())
                                            .collect(Collectors.joining(", "));
        System.out.printf("<Tick %d> The first delivery: %s%n", getCurrentTick(), deliveryLog);
        initialDelivery.forEach((sectionName, itemCount) -> sections.get(sectionName).addItem(itemCount));
    }
    
    //process the delivery
    public synchronized void processDelivery(Map<String, Integer> delivery) {
        itemsForDelivery.putAll(delivery);
        logDelivery(delivery); // Log the delivery
        gui.updateDeliveryInfo(delivery.toString());
    }
    
    //log delivery actions
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

    // The enhanced report generation method
    public void generateEnhancedReport() {
        double averageCustomerWaitTime = calculateAverage(customerWaitTimes);
        double averageAssistantWorkTime = calculateAverage(assistantWorkTimes);
        double averageAssistantBreakTime = calculateAverage(assistantBreakTimes);

        String report = String.format("End of Day Report:\n" +
                "Average Customer Wait Time: %.2f ticks\n" +
                "Average Assistant Work Time: %.2f ticks\n" +
                "Average Assistant Break Time: %.2f ticks\n" +
                "\n", averageCustomerWaitTime, averageAssistantWorkTime, averageAssistantBreakTime);

        // Logging to the terminal
        System.out.println(report);

        // Updating the GUI with the report
        if (gui != null) {
            gui.updateAnalysisReport(report);
        }
    }

    //calculate the averages
    private double calculateAverage(List<Integer> times) {
        if (times.isEmpty()) return 0.0;
        return times.stream().mapToInt(i -> i).average().orElse(0.0);
    }

    //store customer wait time
    public synchronized void recordCustomerWaitTime(int waitTime) {
        System.out.println("Recording wait time: " + waitTime); // Debug log
        customerWaitTimes.add(waitTime);
    }

    
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

    //store the assistant work time
    public synchronized void recordAssistantWorkTime(int workTime) {
        assistantWorkTimes.add(workTime);
    }

    public synchronized void recordAssistantBreakTime(int breakTime) {
        assistantBreakTimes.add(breakTime);
    }
    
    // check if the store is busy
    public boolean isStoreBusy() {
       // store is busy if the number of active customers exceeds a threshold
        return getActiveCustomerCount() > config.busyCustomerThreshold;
    }
    
    public int getActiveCustomerCount() {
        // Active customers could be determined by customers currently in the store or attempting purchases
        return customersList.size(); // Simplified example
    }


    /**
     * Main method to simulate thrift store operation.
     *
     * @param args Command-line arguments
     * @throws InterruptedException if the simulation thread is interrupted.
     */
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
