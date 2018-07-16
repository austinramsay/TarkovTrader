
package tarkov.trader.objects;

import java.util.HashMap;

/**
 *
 * @author austin
 */

public class LoginForm extends Form {
    
    private String username;
    private String password;
    private String ign;
    private String timezone;
    private boolean authenticated;
    
    public LoginForm(String username, String password)
    {
        this.type = "login";
        this.flags = new HashMap();
        this.username = username;
        this.password = password;
        this.authenticated = false;
    }
    
    public boolean isAuthenticated()
    {
        return authenticated;
    }
    
    
    public void setAuthenticationState(boolean isNowAuthenticated)
    {
        authenticated = isNowAuthenticated;
    }
    
    
    public String getUsername()
    {
        return username;
    }
    
    
    public String getPassword()
    {
        return password;
    }
    
    public String getIgn()
    {
        return ign;
    }
    
    public String getTimezone()
    {
        return timezone;
    }
    
    
    public void setUsername(String username)
    {
        this.username = username;
    }
    
    
    public void setPassword(String password)
    {
        this.password = password;
    }
    
    
    public void setIgn(String ign)
    {
        this.ign = ign;
    }
    
    public void setTimezone(String timezone)
    {
        this.timezone = timezone;
    }

}
