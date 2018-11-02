
package tarkov.trader.objects;

/**
 *
 * @author austin
 */

public enum ReportReason {
    
    MOD_REMOVED("Removed By Mod"),
    SELLER_FAILED("Seller Failed to Complete"),
    BUYER_FAILED("Buyer Failed to Complete"),
    PURCHASE_SCAM("Scammed Purchase"),
    SELL_SCAM("Scammed Sale");
    
    private final String desc;
    
    private ReportReason(String desc)
    {
        this.desc = desc;
    }
    
    public String getDesc()
    {
        return desc;
    }
}
