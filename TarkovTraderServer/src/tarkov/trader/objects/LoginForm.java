
package tarkov.trader.objects;

import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author austin
 */

public class LoginForm extends Form {
    
    private String username;
    private String hashedPassword;
    private String ign;
    private String timezone;
    private boolean authenticated;
    private File userImageFile;
    
    private ArrayList<String> userList;
    private ArrayList<String> onlineList;
    
    
    public LoginForm(String username, String hashedPassword)
    {
        this.type = "login";
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.authenticated = false;
        userList = new ArrayList<>();
        onlineList = new ArrayList<>();
    }
    
    
    // Getters:
    
    public boolean isAuthenticated()
    {
        return authenticated;
    }
   
    
    public String getUsername()
    {
        return username;
    }
    
    
    public String getHashedPassword()
    {
        return hashedPassword;
    }
    
    
    public String getIgn()
    {
        return ign;
    }
    
    
    public String getTimezone()
    {
        return timezone;
    }
    
    
    public File getUserImageFile()
    {
        return userImageFile;
    }
    
    
    public ArrayList<String> getUserList()
    {
        return userList;
    }
    
    
    public ArrayList<String> getOnlineList()
    {
        return onlineList;
    }
    
    
    
    // Setters:
    
    public void setAuthenticationState(boolean isNowAuthenticated)
    {
        authenticated = isNowAuthenticated;
    }
    
    
    public void setUsername(String username)
    {
        this.username = username;
    }
    
    
    public void setHashedPassword(String hashedPassword)
    {
        this.hashedPassword = hashedPassword;
    }
    
    
    public void setIgn(String ign)
    {
        this.ign = ign;
    }
    
    
    public void setTimezone(String timezone)
    {
        this.timezone = timezone;
    }
    
    
    public void setUserImageFile(File userImageFile)
    {
        this.userImageFile = userImageFile;
    }
    
    
    public void setUserList(ArrayList<String> userList)
    {
        this.userList = userList;
    }
    
    
    public void setOnlineList(ArrayList<String> onlineList)
    {
        this.onlineList = onlineList;
    }

}
