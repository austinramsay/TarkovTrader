
package tarkov.trader.objects;

import java.util.HashMap;

/**
 *
 * @author austin
 */

public class ItemModificationRequest extends Form {
    
    private String modificationType;
    private HashMap<String, String> modificationFlags;
    private HashMap<String, String> filterFlags;
    private Item itemToModify;
    
    public ItemModificationRequest(String modificationType, HashMap<String, String> modificationFlags, HashMap<String, String> filterFlags, Item itemToModify)
    {
        this.type = "itemmodification";
        this.modificationType = modificationType;
        this.modificationFlags = modificationFlags;
        this.filterFlags = filterFlags;
        this.itemToModify = itemToModify;
    }
    
    
    public Item getItemToModify()
    {
        return itemToModify;
    }
    
    
    public String getModificationType()
    {
        return modificationType;
    }
    
    
    public HashMap<String, String> getModificationFlags()
    {
        return modificationFlags;
    }
    
    
    public HashMap<String, String> getFilterFlags()
    {
        return filterFlags;
    }
    
}
