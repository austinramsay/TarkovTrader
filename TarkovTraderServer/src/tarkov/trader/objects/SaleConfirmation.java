
package tarkov.trader.objects;

/**
 *
 * @author austin
 */

public enum SaleConfirmation {
    
    CONFIRMED("Marked successful by the buyer."),
    UNCONFIRMED("Not yet been marked successful by the buyer."),
    REPORTED("Sale reported. Pending moderator confirmation."),
    FAILED("Sale confirmed failed due to incorrect post information."),
    SCAM("Sale confirmed to be a scam.");
    
    private final String description;
    
    SaleConfirmation(String description)
    {
        this.description = description;
    }
    
    public String getDescription()
    {
        return description;
    }
    
}
