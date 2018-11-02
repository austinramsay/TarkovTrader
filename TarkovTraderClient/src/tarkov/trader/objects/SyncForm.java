
package tarkov.trader.objects;

import java.util.ArrayList;

/**
 *
 * @author austin
 */

public class SyncForm extends Form {
    
    
    private ArrayList<String> onlineUserList;
    private ArrayList<String> fullUserList;
    
    
    public SyncForm(ArrayList<String> flags)
    {
        // Flags are mandatory for SyncForm (What is needed to sync? There are multiple options: 'onlineuserlist', 'fulluserlist')
        this.type = "sync";
        this.flags = flags;
    }
    
    
    
    // Getters:
    
    public ArrayList<String> getOnlineUserList()
    {
        return onlineUserList;
    }
    
    
    public ArrayList<String> getFullUserList()
    {
        return fullUserList;
    }
    
    
    
    // Setters:
    
    public void setOnlineUserList(ArrayList<String> list)
    {
        this.onlineUserList = list;
    }
    
    
    public void setFullUserList(ArrayList<String> list)
    {
        this.fullUserList = list;
    }
  
    
}
