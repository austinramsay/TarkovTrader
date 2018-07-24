
package tarkov.trader.objects;

/**
 *
 * @author austin
 */

public class Message extends Form {
    
    private String destination;
    private String origin;
    private String message;
    
    public Message(String origin, String destination, String message)
    {
        this.type = "message";
        this.origin = origin;
        this.destination = destination;
        this.message = message;
    }
    
    
    public String getDestination()
    {
        return destination;
    }
    
    
    public String getOrigin()
    {
        return origin;
    }
    
    
    public String getMessage()
    {
        return message;
    }
    
}
