public class Config {
    public final int numberOfAssistants;
    public final int numberOfAdditionalSections;
    public final double customerPurchaseProbability;
    public final int deliveryFrequencyTicks;
    public final int maxItemsPerDelivery;
    public final double customerPatienceMultiplier;
    public final int minBreakInterval;
    public final int maxBreakInterval;
    public final int breakDurationTicks;

    public Config(int numberOfAssistants, int numberOfAdditionalSections, double customerPurchaseProbability,
                  int deliveryFrequencyTicks, int maxItemsPerDelivery, double customerPatienceMultiplier,
                  int minBreakInterval, int maxBreakInterval, int breakDurationTicks) {
        this.numberOfAssistants = numberOfAssistants;
        this.numberOfAdditionalSections = numberOfAdditionalSections;
        this.customerPurchaseProbability = customerPurchaseProbability;
        this.deliveryFrequencyTicks = deliveryFrequencyTicks;
        this.maxItemsPerDelivery = maxItemsPerDelivery;
        this.customerPatienceMultiplier = customerPatienceMultiplier;
        this.minBreakInterval = minBreakInterval;
        this.maxBreakInterval = maxBreakInterval;
        this.breakDurationTicks = breakDurationTicks;
    }
}