
package tarkov.trader.objects;

/**
 *
 * @author austin
 */

import java.io.Serializable;

public class NewAccountForm extends Form {
    
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String ign;
    
    
    public NewAccountForm(String user, String pass, String first, String last, String ign)
    {
        this.type = "newaccount";
        this.username = user;
        this.password = pass;
        this.firstName = first;
        this.lastName = last;
        this.ign = ign;
    }
    
    
    public String getUsername()
    {
        return username;
    }
    
    
    public String getPassword()
    {
        return password;
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
    
}
