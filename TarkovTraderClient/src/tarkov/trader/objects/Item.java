
package tarkov.trader.objects;

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author austin
 */

public class Item implements Serializable {
    
    private File userImage;
    private String tradeState;
    private String itemType;
    private String name;
    private int price;
    private String ign;
    private String timezone;
    private String username;
    private String keywords;
    private String notes;
    private String date;
    
    // To be 'set' by the server
    private int itemId; // Will be set from the database autoincrement id value  --  NOT IN USE for now
    private String dealStatus; // Finalized? Closed? Open?
    private String sellerState; // Seller online or offline? 
    
    
    public Item(File userImage, String tradeState, String itemType, String name, int price, String ign, String username, String timezone, String keywords, String notes)
    {
        this.userImage = userImage;
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
        setDate();
    }
    
    
    // Getters:
    
    public File getImageFile()
    {
        return userImage;     
    }
    
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
    
    public String getDate()
    {
        return date;
    }
    
    public String getSellerState()
    {
        if (sellerState != null)
            return sellerState;
        else
            return null;
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
    
    private void setDate()
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        this.date = dateFormat.format(new Date());
    }
    
    public void setSellerState(boolean isOnline)
    {
        if (isOnline)
            this.sellerState = "Online";
        else
            this.sellerState = "Offline";
    }
}
