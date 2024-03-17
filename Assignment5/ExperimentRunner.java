public class ExperimentRunner {
    public static void runExperiments() {
        // Arrays of different configurations to test
        int[] assistantCounts = new int[]{2, 3, 4};
        int[] deliveryFrequencies = new int[]{90, 100, 110};

        for (int assistantCount : assistantCounts) {
            for (int deliveryFrequency : deliveryFrequencies) {
                Config config = new Config(
                    assistantCount, // numberOfAssistants
                    1, 1, 1, 1, 1, 1, // Section counts
                    0.1, 0.2, 0.15, 0.25, 0.05, 0.2, // Purchase probabilities
                    deliveryFrequency,
                    10, // maxItemsPerDelivery
                    1.5, // customerPatienceMultiplier
                    200, 300, 150, 5 // Break and busy thresholds
                );
                ThriftStore store = new ThriftStore(config);
                System.out.printf("Running simulation with %d assistants and delivery frequency of %d ticks.%n", assistantCount, deliveryFrequency);
                runSimulation(store, 10000); // Simulate for 10,000 ticks as an example
                store.generateEnhancedReport();
            }
        }
    }

    private static void runSimulation(ThriftStore store, int maxTicks) {
        while (store.getCurrentTick() < maxTicks) {
            try {
                Thread.sleep(ThriftStore.TICK_TIME_SIZE);
                store.simulateTick();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        runExperiments();
    }
}