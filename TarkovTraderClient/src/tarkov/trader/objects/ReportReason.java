
package tarkov.trader.objects;

/**
 *
 * @author austin
 */

public enum ReportReason {
    
    MOD_REMOVED("Removed By Mod"),
    SELLER_FAILED("Seller Commitment Failure"),
    BUYER_FAILED("Buyer Commitment Failure"),
    BUYER_SCAM("Scammed a Seller"),
    SELLER_SCAM("Scammed a Buyer");
    
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
