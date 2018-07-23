
package tarkov.trader.objects;

import java.util.ArrayList;

/**
 *
 * @author austin
 */

public class Chat extends Form {
    
    
    public boolean isNew;
    private String origin;   // This string will be a client's username that initiated the chat
    private String destination;   // This string will be a client's username receiving the new chat
    private ArrayList<String> messages;   // The arraylist will hold strings of messages between clients
    
    
    public Chat(String origin, String destination, ArrayList<String> messages)
    {
        this.type = "chat";
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
    
    
    public String getName(String clientName)
    {
        if (origin.equals(clientName))
            return destination;
        else
            return origin;
    }
    
    
    
    // Setters:
    public void setOpened()   // Server calls this upon receiving and recognizing the chat is 'new'
    {
        this.isNew = false;
    }
    
    
    public void setMessages(ArrayList<String> messages)
    {
        this.messages = messages;
    }
    
    
    public void appendMessage(String message)
    {
        if (this.messages == null)
        {
            this.messages = new ArrayList<>();
        }
        
        this.messages.add(message);
    }
    
}
