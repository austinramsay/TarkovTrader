
package tarkov.trader.objects;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author austin
 */

public class Form implements Serializable {
    
    public String type;
    public ArrayList<String> flags;
    
    
    // Getters:
    
    public String getType()
    {
        return type;
    }
    
    public ArrayList<String> getFlags()
    {
        return flags;
    }
    
    
    // Setters:
    
    public void setType(String type)
    {
        this.type = type;
    }
    
    public void setFlags(ArrayList<String> flags)
    {
        this.flags = flags;
    }
}
