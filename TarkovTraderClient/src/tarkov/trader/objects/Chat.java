
package tarkov.trader.objects;

import java.util.ArrayList;

/**
 *
 * @author austin
 */

public class Chat {
    
    private boolean isNew;
    private String origin;
    private String destination;
    private ArrayList<String> messages;
    
    public Chat(String origin, String destination, ArrayList<String> messages)
    {
        this.isNew = true;
        this.origin = origin;
        this.destination = destination;
        this.messages = messages;
    }
   
    
    // Getters:
    public String getOrigin()
    {
        return origin;
    }
    
    
    public String getDestination()
    {
        return destination;
    }
    
    
    public ArrayList<String> getMessages()
    {
        return messages;
    }
    
    
    
    // Setters:
    public void setOpened()
    {
        this.isNew = false;
    }
    
    
    public void setMessages(ArrayList<String> messages)
    {
        this.messages = messages;
    }
    
}
