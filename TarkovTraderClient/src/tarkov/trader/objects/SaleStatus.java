
package tarkov.trader.objects;

/**
 *
 * @author austin
 */

public enum SaleStatus {
    
    BUYER_CONFIRMED("+Rep as Buyer"),
    SELLER_CONFIRMED("+Rep as Seller");
    
    private final String description;
    
    SaleStatus(String description)
    {
        this.description = description;
    }
    
    public String getDescription()
    {
        return description;
    }
    
}
