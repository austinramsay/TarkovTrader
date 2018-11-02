
package tarkov.trader.objects;

/**
 *
 * @author austin
 */

public class ItemLocation {
    
    private ListLocation list;  // Buy list, Requested sales list
    private int index;          // Index of the matching item in the respective ListLocation
    
    public ItemLocation(ListLocation list, int index)
    {
        this.list = list;
        this.index = index;
    }
    
    public ListLocation getListLocation()
    {
        return list;
    }
    
    public int getIndex()
    {
        return index;
    }
    
}