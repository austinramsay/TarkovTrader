
package tarkov.trader.objects;

import java.io.Serializable;

/**
 *
 * @author austin
 */

public class Item implements Serializable {
    
    private String tradeState;
    private String itemType;
    private String name;
    private int price;
    private String ign;
    private String timezone;
    private String username;
    private String keywords;
    private String notes;
    
    // To be 'set' by the server
    private int itemId; // Will be set from the database autoincrement id value
    private String dealStatus; // Finalized? Closed? Open?
    
    
    public Item(String tradeState, String itemType, String name, int price, String ign, String username, String timezone, String keywords, String notes)
    {
        this.tradeState = tradeState;
        this.itemType = itemType;
        this.name = name;
        this.price = price;
        this.ign = ign;
        this.username = username;
        this.timezone = timezone;
        this.notes = notes;
        this.keywords = keywords;
        this.dealStatus = "open";
    }
    
    
    // Getters:
    
    public String getTradeState()
    {
        return tradeState;
    }
    
    public String getItemType()
    {
        return itemType;
    }
    
    public String getName()
    {
        return name;
    }
            
    public int getPrice()
    {
        return price;
    }
    
    public String getIgn()
    {
        return ign;
    }
    
    public String getUsername()
    {
        return username;
    }
    
    public String getTimezone()
    {
        return timezone;
    }
    
    public int getItemId()
    {
        return itemId;
    }
    
    public String getKeywords()
    {
        return keywords;
    }
    
    public String getNotes()
    {
        return notes;
    }
    
    public String getDealStatus()
    {
        return dealStatus;
    }
    
    // Setters:
    
    public void setItemId(int id)
    {
        itemId = id;
    }
    
    public void setDealStatus(String status)
    {
        this.dealStatus = status;
    }
}
