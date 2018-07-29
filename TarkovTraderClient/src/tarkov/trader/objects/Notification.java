
package tarkov.trader.objects;

/**
 *
 * @author austin
 */

public class Notification extends Form {
    
    private String notificationType;
    private String originUsername;
    private int count;
    
    
    public Notification(String notificationType, String originUsername)
    {
        this.type = "notification";
        this.notificationType = notificationType;
        this.originUsername = originUsername;
    }
  
    
    public String getNotificationType()
    {
        return notificationType;
    }
    
    
    public String getOriginUsername()
    {
        return originUsername;
    }
    
    
    public int getCount()
    {
        return count;
    }
 
    
    public void setCount(int count)
    {
        this.count = count;
    }
}