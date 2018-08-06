
package tarkov.trader.objects;

/**
 *
 * @author austin
 */

public enum SaleStatus {
    
    BUYER_CONFIRMED("+Rep as Buyer"),
    SELLER_CONFIRMED("+Rep as Seller"),
    UNCONFIRMED("Awaiting response"),
    FAILED("Removed By Mod -Rep"),
    PURCHASE_SCAM("Scammed Purchase -Rep"),
    SELL_SCAM("Scammed Sale -Rep");
    
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
