
package tarkov.trader.objects;

/**
 *
 * @author austin
 */

public enum ItemStatus {
    
    OPEN("Available"),
    AWAITING_RESPONSE("Awaiting Response"),
    REPORT_PENDING("Report Pending"),
    DECLINED("Seller Declined."),
    CONFIRMED("Seller Accepted.");
    
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
