
package tarkov.trader.objects;

/**
 *
 * @author austin
 */

public class ChatDelete extends Form {
    
    
    private String usernameToRemove;
    
    
    public ChatDelete(String usernameToRemove)
    {
        this.type = "chatdelete";
        this.usernameToRemove = usernameToRemove;
    }
    
    
    public String getUsernameToRemove()
    {
        return usernameToRemove;
    }
    
    
}
