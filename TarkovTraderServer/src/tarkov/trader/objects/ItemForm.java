
package tarkov.trader.objects;

/**
 *
 * @author austin
 */

public class ItemForm extends Form {
    
    private Item item;
    
    public ItemForm(Item item)
    {
        this.type = "newitem";
        this.item = item;
    }
    
    public Item getItem()
    {
        return item;
    }
    
}
