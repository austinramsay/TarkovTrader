
package tarkov.trader.objects;

import java.text.NumberFormat;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author austin
 */

// The purpose of this class is to process serialized images on the client side only
// Because the server is designed to run on Solaris, and the Solaris release of Java does not contain JavaFX, the server cannot process JavaFX objects e.g. Image & ImageView
// The 'Item' class contains a byte array for a serialized image
// This class de-serializes the image to be displayed in browser
// This class is read by the CellValueFactory to populate the browser item list


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
            case "Keybar":
                itemTypeImage = new ImageView(new Image(this.getClass().getResourceAsStream("/keybar.png"), 32, 32, true, true));
                return itemTypeImage;
            case "Docs Case":
                itemTypeImage = new ImageView(new Image(this.getClass().getResourceAsStream("/docscase.png"), 32, 32, true, true));
                return itemTypeImage;
            case "Storage Case":
                itemTypeImage = new ImageView(new Image(this.getClass().getResourceAsStream("/storagecase.png"), 32, 32, true, true));
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
        Image userImage;
        
        if (item.getImageFile() == null)
        {
            // No image was uploaded with the post, use generic client-side image
            userImage = getGenericImage(this.getItemType());
        }
        else
        {
            userImage = new Image(item.getImageFile().toURI().toString());
        }
        
        ImageView userItemImage = new ImageView(userImage);
        userItemImage.setPreserveRatio(true);
        userItemImage.setSmooth(true);
        userItemImage.setFitWidth(300);  // Try to fit the image to a 300 width, but maintain a height lower than 220 by using method below
        
        while (projectedHeight(userImage, userItemImage) > 220)   // Calculate the projected height or the image by calculating the requested width divided by the (original width/original height)
        {
            userItemImage.setFitWidth(userItemImage.getFitWidth() - 1);   // Lower the fit width if the images ratio can't be preserved according to the requested width
        }
        
        return userItemImage;
    }
    
    
    private double projectedHeight(Image userImage, ImageView userItemImage)
    {
        double height = (userItemImage.getFitWidth() / (userImage.getWidth() / userImage.getHeight()));
        return height;
    }
    
    
    private Image getGenericImage(String type)
    {
        String imageFlag;
        
        switch (type)
        {
            case "Secure Container":
                imageFlag = "/genericsecurecontainer.png";
                break;
            case "Key":
                imageFlag = "/generickey.png";
                break;
            case "Keybar":
                imageFlag = "/generickeybar.png";
                break;
            case "Docs Case":
                imageFlag = "/genericdocscase.png";
                break;
            case "Storage Case":
                imageFlag = "/genericstoragecase.png";
                break;
            case "Weapon":
                imageFlag = "/genericweapon.png";
                break;
            case "Weapon Mod":
                imageFlag = "/genericweaponmod.png";
                break;
            case "Apparel":
                imageFlag = "/genericapparel.png";
                break;
            case "Armor/Helmet":
                imageFlag = "/genericarmor.png";
                break;
            case "Ammo":
                imageFlag = "/genericammo.png";
                break;
            case "Misc":
                imageFlag = "/genericmisc.png";
                break;             
            case "Medicine":
                imageFlag = "/genericmedicine.png";
                break;
            default:
                imageFlag = "/generic.png";
                break;
        }
                      
        return new Image(this.getClass().getResourceAsStream(imageFlag));
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
    
            
    public String getPrice()
    { 
        if (item.getPrice() == 0)
        {
            return null;
        }
        return NumberFormat.getNumberInstance().format(item.getPrice()) + "₽";
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
        if (item.getNotes().equals(""))
            return "No seller notes.";
        
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
    
    
    public String getDate()
    {
        return item.getDate();
    }
    
    
    public String getSellerState()
    {
        return item.getSellerState();
    }
    
    
    public Item getItem()
    {
        return item;
    }
}
