
package tarkov.trader.objects;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author austin
 */
public class ItemListForm extends Form {
    
    private ArrayList<Item> itemList;
    private HashMap<String, String> searchFlags;
    
    public ItemListForm(HashMap<String, String> searchFlags)
    {
        this.searchFlags = searchFlags;
        this.type = "itemlist";
    }
    
    
    // Getters:
    
    public ArrayList<Item> getItemList()
    {
        return itemList;
    }
    
    public HashMap<String, String> getSearchFlags()
    {
        return searchFlags;
    }
    
    // Setters:
    
    public void setSearchFlags(HashMap<String, String> searchFlags)
    {
        this.searchFlags = searchFlags;
    }
    
    public void setItemList(ArrayList<Item> itemList)
    {
        this.itemList = itemList;
    }
    
    
    // Add functions
    
    public void addItemToList(Item itemToAdd)
    {
        itemList.add(itemToAdd);
    }
}
