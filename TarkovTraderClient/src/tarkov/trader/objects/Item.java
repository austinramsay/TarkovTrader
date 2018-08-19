
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
    private String requestedUser;
    private boolean suspended;
    
    // To be 'set' by the server
    private int itemId; // Will be set from the database autoincrement id value  --  NOT IN USE for now
    private ItemStatus itemStatus; // Finalized? Closed? Open?
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
        this.suspended = false;
        this.itemStatus = ItemStatus.OPEN;
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
    
    public ItemStatus getItemStatus()
    {
        return itemStatus;
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
    
    public boolean getSuspensionState()
    {
        return suspended;
    }
    
    public String getRequestedUser()
    {
        return requestedUser;
    }
    
    // Setters:
    
    public void setItemId(int id)
    {
        itemId = id;
    }
    
    public void setItemStatus(ItemStatus itemStatus)
    {
        this.itemStatus = itemStatus;
    }
    
    private void setDate()
    {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        this.date = dateFormat.format(new Date());
    }
    
    public void setSellerState(boolean isOnline)
    {
        if (isOnline)
            this.sellerState = "Online";
        else
            this.sellerState = "Offline";
    }
    
    public void setSuspended(boolean isSuspended)
    {
        this.suspended = isSuspended;
    }
    
    public void setRequestedUser(String username)
    {
        this.requestedUser = username;
    }
    
    
    // Override the equals method to use when comparing upon removing/modifying an item 
    @Override
    public boolean equals(Object item)
    {
        if (!(item instanceof Item))
            return false;
        
        if (item == this)
            return true;
        
        Item compareItem = (Item)item;
        
        if (!compareItem.getDate().equals(this.getDate()))
            return false;
        
        if (!compareItem.getName().equals(this.getName()))
            return false;
        
        if (!compareItem.getIgn().equals(this.getIgn()))
            return false;
        
        if (!compareItem.getItemType().equals(this.getItemType()))
            return false;
        
        if (compareItem.getPrice() != this.getPrice())
            return false;
        
        if (!compareItem.getTradeState().equals(this.getTradeState()))
            return false;
        
        if (compareItem.getRequestedUser() != null && this.getRequestedUser() != null && !compareItem.getRequestedUser().equals(this.getRequestedUser()))
            return false;
        
        return true;
    }
}