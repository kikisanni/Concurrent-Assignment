public class Config {
    public int numberOfAssistants;
    public final int numberOfElectronicsSections;
    public final int numberOfClothingSections;
    public final int numberOfFurnitureSections;
    public final int numberOfToysSections;
    public final int numberOfSportingGoodsSections;
    public final int numberOfBooksSections;
    public final double customerPurchaseProbabilityElectronics;
    public final double customerPurchaseProbabilityClothing;
    public final double customerPurchaseProbabilityFurniture;
    public final double customerPurchaseProbabilityToys;
    public final double customerPurchaseProbabilitySportingGoods;
    public final double customerPurchaseProbabilityBooks;
    public int deliveryFrequencyTicks;
    public final int maxItemsPerDelivery;
    public final double customerPatienceMultiplier;
    public final int minBreakInterval;
    public final int maxBreakInterval;
    public final int breakDurationTicks;
    public final int busyCustomerThreshold;
    
    public Config(
            int numberOfAssistants, 
            int numberOfElectronicsSections, 
            int numberOfClothingSections, 
            int numberOfFurnitureSections, 
            int numberOfToysSections, 
            int numberOfSportingGoodsSections, 
            int numberOfBooksSections, 
            double customerPurchaseProbabilityElectronics, 
            double customerPurchaseProbabilityClothing, 
            double customerPurchaseProbabilityFurniture, 
            double customerPurchaseProbabilityToys, 
            double customerPurchaseProbabilitySportingGoods, 
            double customerPurchaseProbabilityBooks, 
            int deliveryFrequencyTicks, 
            int maxItemsPerDelivery, 
            double customerPatienceMultiplier, 
            int minBreakInterval, 
            int maxBreakInterval, 
            int breakDurationTicks,
            int busyCustomerThreshold) {
        this.numberOfAssistants = numberOfAssistants;
        this.numberOfElectronicsSections = numberOfElectronicsSections;
        this.numberOfClothingSections = numberOfClothingSections;
        this.numberOfFurnitureSections = numberOfFurnitureSections;
        this.numberOfToysSections = numberOfToysSections;
        this.numberOfSportingGoodsSections = numberOfSportingGoodsSections;
        this.numberOfBooksSections = numberOfBooksSections;
        this.customerPurchaseProbabilityElectronics = customerPurchaseProbabilityElectronics;
        this.customerPurchaseProbabilityClothing = customerPurchaseProbabilityClothing;
        this.customerPurchaseProbabilityFurniture = customerPurchaseProbabilityFurniture;
        this.customerPurchaseProbabilityToys = customerPurchaseProbabilityToys;
        this.customerPurchaseProbabilitySportingGoods = customerPurchaseProbabilitySportingGoods;
        this.customerPurchaseProbabilityBooks = customerPurchaseProbabilityBooks;
        this.deliveryFrequencyTicks = deliveryFrequencyTicks;
        this.maxItemsPerDelivery = maxItemsPerDelivery;
        this.customerPatienceMultiplier = customerPatienceMultiplier;
        this.minBreakInterval = minBreakInterval;
        this.maxBreakInterval = maxBreakInterval;
        this.breakDurationTicks = breakDurationTicks;
        this.busyCustomerThreshold = busyCustomerThreshold;
        
    }
        // Add setters for parameters you want to be able to modify dynamically
        public void setNumberOfAssistants(int numberOfAssistants) {
            this.numberOfAssistants = numberOfAssistants;
        }
    
        public void setDeliveryFrequencyTicks(int deliveryFrequencyTicks) {
            this.deliveryFrequencyTicks = deliveryFrequencyTicks;
        }
}