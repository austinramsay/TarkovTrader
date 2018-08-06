
package tarkov.trader.objects;

/**
 *
 * @author austin
 */

public enum SaleCondition {
    
    SOLD("Sold"),
    PENDING("Pending"),
    UNAVAILABLE("Awaiting Moderator"),
    REMOVED("Removed"),
    AVAILABLE("Available");
    
    private final String description;
    
    SaleCondition(String description)
    {
        this.description = description;
    }
    
    public String getDescription()
    {
        return description;
    }
    
}