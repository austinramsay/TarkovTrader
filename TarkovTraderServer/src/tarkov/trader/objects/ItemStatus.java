
package tarkov.trader.objects;

/**
 *
 * @author austin
 */

public enum ItemStatus {
    
    OPEN("Available"),
    AWAITING_RESPONSE("Awaiting response"),
    DECLINED("Declined"),
    CONFIRMED("Accepted");
    
    private String reason;
    
    private ItemStatus(String reason)
    {
        this.reason = reason;
    }
    
    public String getReason()
    {
        return reason;
    }
    
}
