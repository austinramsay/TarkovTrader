
package tarkov.trader.objects;

/**
 *
 * @author austin
 */

import java.io.Serializable;
        
public class LoginForm implements Serializable {
    
    private String username;
    private String password;
    private boolean authenticated;
    
    public LoginForm(String username, String password)
    {
        this.username = username;
        this.password = password;
        this.authenticated = false;
    }
    
    
    public String getUsername()
    {
        return username;
    }
    
    
    public String getPassword()
    {
        return password;
    }
    
    
    public void setUsername(String username)
    {
        this.username = username;
    }
    
    
    public void setPassword(String password)
    {
        this.password = password;
    }
    
    
    public boolean isAuthenticated()
    {
        return authenticated;
    }
    
    
    public void setAuthenticationState(boolean isNowAuthenticated)
    {
        authenticated = isNowAuthenticated;
    }
    
}
