
package tarkov.trader.objects;

import java.util.HashMap;

/**
 *
 * @author austin
 */

public class ItemModificationRequest extends Form {
    
    private String modificationType;
    private HashMap<String, String> filterFlags;
    private Item itemToModify;
    private Item modifiedItem;
    
    public ItemModificationRequest(String modificationType, HashMap<String, String> filterFlags, Item itemToModify)
    {
        this.type = "itemmodification";
        this.modificationType = modificationType;
        this.filterFlags = filterFlags;
        this.itemToModify = itemToModify;
    }
    
    public ItemModificationRequest(String modificationType, HashMap<String, String> filterFlags, Item itemToModify, Item modifiedItem)
    {
        this.type = "itemmodification";
        this.modificationType = modificationType;
        this.filterFlags = filterFlags;
        this.itemToModify = itemToModify;        
        this.modifiedItem = modifiedItem;
    }
    
    
    public Item getItemToModify()
    {
        return itemToModify;
    }
    
    
    public Item getPreModifiedItem()
    {
        return modifiedItem;
    }
    
    
    public String getModificationType()
    {
        return modificationType;
    }
    
    
    public HashMap<String, String> getFilterFlags()
    {
        return filterFlags;
    }
    
}
