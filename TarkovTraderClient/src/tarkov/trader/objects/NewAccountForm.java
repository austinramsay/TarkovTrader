
package tarkov.trader.objects;

/**
 *
 * @author austin
 */

public class NewAccountForm extends Form {
    
    private String username;
    private String hashedPassword;
    private byte[] salt;
    private String firstName;
    private String lastName;
    private String ign;
    private String timezone;
    
    
    public NewAccountForm(String username, String hashedPassword, byte[] salt, String firstName, String lastName, String ign, String timezone)
    {
        this.type = "newaccount";
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.salt = salt;
        this.firstName = firstName;
        this.lastName = lastName;
        this.ign = ign;
        this.timezone = timezone;
    }
    
    
    public String getUsername()
    {
        return username;
    }
    
    
    public String getHashedPassword()
    {
        return hashedPassword;
    }
    
    
    public byte[] getSalt()
    {
        return salt;
    }
    
    
    public String getFirst()
    {
        return firstName;
    }
    
    
    public String getLast()
    {
        return lastName;
    }
    
    
    public String getIgn()
    {
        return ign;
    }
    
    
    public String getTimezone()
    {
        return timezone;
    }
}
