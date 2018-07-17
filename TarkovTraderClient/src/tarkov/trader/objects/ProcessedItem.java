
package tarkov.trader.objects;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author austin
 */

// The purpose of this class is to process serialized images on the client side only
// Because the server is designed to run on Solaris, and the Solaris release of Java does not contain JavaFX, the server cannot process JavaFX objects
// The 'Item' class contains a byte array for a serialized image
// This class de-serializes the image to be displayed in browser
// This class is read by the CellValueFactory to populate the browser


public class ProcessedItem {
    
    private Item item;
    
    
    public ProcessedItem(Item item)
    {
        this.item = item;
    }
    
    
    public ImageView getItemTypeImage()
    {
        ImageView itemTypeImage;

        switch (item.getItemType())
        {
            case "Key":
                itemTypeImage = new ImageView(new Image(this.getClass().getResourceAsStream("/key.png"), 32, 32, true, true));
                return itemTypeImage;
            case "Secure Container":
                itemTypeImage = new ImageView(new Image(this.getClass().getResourceAsStream("/securecontainer.png"), 32, 32, true, true));
                return itemTypeImage;
            case "Weapon":
                itemTypeImage = new ImageView(new Image(this.getClass().getResourceAsStream("/weapon.png"), 32, 32, true, true));
                return itemTypeImage;
            case "Weapon Mod":
                itemTypeImage = new ImageView(new Image(this.getClass().getResourceAsStream("/weaponmod.png"), 32, 32, true, true));
                return itemTypeImage;
            case "Armor/Helmet":
                itemTypeImage = new ImageView(new Image(this.getClass().getResourceAsStream("/helmet.png"), 32, 32, true, true));
                return itemTypeImage;
            case "Apparel":
                itemTypeImage = new ImageView(new Image(this.getClass().getResourceAsStream("/apparel.png"), 32, 32, true, true));
                return itemTypeImage;
            case "Ammo":
                itemTypeImage = new ImageView(new Image(this.getClass().getResourceAsStream("/ammo.png"), 32, 32, true, true));
                return itemTypeImage;               
            case "Medicine":
                itemTypeImage = new ImageView(new Image(this.getClass().getResourceAsStream("/medicine.png"), 32, 32, true, true));
                return itemTypeImage;
            case "Misc":
                itemTypeImage = new ImageView(new Image(this.getClass().getResourceAsStream("/misc.png"), 32, 32, true, true));
                return itemTypeImage;
            default:
                itemTypeImage = new ImageView(new Image(this.getClass().getResourceAsStream("/misc.png"), 32, 32, true, true));
                return itemTypeImage;
        }
    }
    
    
    public ImageView getUserItemImage()
    {
        Image userImage = new Image(item.getImageFile().toURI().toString());
        ImageView userItemImage = new ImageView(userImage);
        return userItemImage;
    }
    
    
    public String getTradeState()
    {
        return item.getTradeState();
    }
    
    
    public String getItemType()
    {
        return item.getItemType();
    }
    
    
    public String getName()
    {
        return item.getName();
    }
    
            
    public int getPrice()
    {
        return item.getPrice();
    }
    
    
    public String getIgn()
    {
        return item.getIgn();
    }
    
    
    public String getUsername()
    {
        return item.getUsername();
    }
    
    
    public String getTimezone()
    {
        return item.getTimezone();
    }
    
    
    public String getKeywords()
    {
        return item.getKeywords();
    }
    
    
    public String getNotes()
    {
        return item.getNotes();
    }
    
    
    public String getDealStatus()
    {
        return item.getDealStatus();
    }
        
    public int getItemId()
    {
        return item.getItemId();
    }
}
