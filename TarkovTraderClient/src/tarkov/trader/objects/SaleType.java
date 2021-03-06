
package tarkov.trader.objects;

/**
 *
 * @author austin
 */

public enum SaleType {
    
    SOLD("Sold"),
    BOUGHT("Purchased");
    
    private final String description;
    
    SaleType(String description)
    {
        this.description = description;
    }
    
    public String getDescription()
    {
        return description;
    }
    
}