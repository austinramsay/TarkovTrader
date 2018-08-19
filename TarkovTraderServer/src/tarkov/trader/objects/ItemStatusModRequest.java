
package tarkov.trader.objects;

/**
 *
 * @author austin
 */

public class ItemStatusModRequest extends Form {
    
    private Item item;
    private ItemStatus status;
    private ItemAction action;
    
    public ItemStatusModRequest(ItemAction action, ItemStatus status, Item item)
    {
        this.type = "itemstatusmod";
        this.action = action;
        this.status = status;
        this.item = item;
    }
    
    public ItemStatus getStatus()
    {
        return status;
    }
    
    public ItemAction getAction()
    {
        return action;
    }
    
    public Item getItem()
    {
        return item;
    }
    
}
