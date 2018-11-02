
package tarkov.trader.objects;

/**
 *
 * @author austin
 */

public enum AccountFlag {
    
    // Negative flags
    MULTIPLE_REG("Registered to a previously recorded in-game name."),
    CONFIRMED_SCAM("Confirmed to have previously scammed ONE buyer."),
    CONFIRMED_MULTIPLE_SCAM("Confirmed to have previously scammed MULTIPLE buyers."),
    
    // Neutral flags
    NEW_ACCOUNT("Less than 2 weeks old."),
    NO_COMPLETED_PURCHASES("Not yet purchased any items."),
    NO_COMPLETED_SALES("Not yet sold any items."),
    
    // Positive flags
    VERIFIED_SUB("Verified Twitch subscriber."),
    CONFIRMED_SELL_REP("Successfully sold over 10 items."),
    CONFIRMED_HIGH_SELL_REP("Successfully sold over 25 items."),
    CONFIRMED_BUY_REP("Completed over 10 successful purchases."),
    CONFIRMED_HIGH_BUY_REP("Completed over 25 successful purchases.");
    
    private String reason;
    
    AccountFlag(String reason)
    {
        this.reason = reason;
    }
    
    public String getReason()
    {
        return reason;
    }
}
