
package tarkov.trader.objects;

import java.io.Serializable;
import java.util.HashMap;

/**
 *
 * @author austin
 */

public class Form implements Serializable {
    
    public String type;
    public HashMap<String, String> flags;
    
    
    // Getters
    
    public String getType()
    {
        return type;
    }
    
    public HashMap<String, String> getFlags()
    {
        return flags;
    }
    
    
    // Setters
    
    public void setType(String type)
    {
        this.type = type;
    }
    
    public void setFlags(HashMap<String, String> flags)
    {
        this.flags = flags;
    }
}
