
package tarkov.trader.objects;

/**
 *
 * @author austin
 */

public class ProfileRequest extends Form {
    
    private String username;
    private Profile returnedProfile;
    
    public ProfileRequest(String username)
    {
        this.type = "profile";
        this.username = username;
    }
    
    public String getUsername()
    {
        return username;
    }
    
    public Profile getReturnedProfile()
    {
        return returnedProfile;
    }
    
    public void setProfile(Profile matchingProfile)
    {
        this.returnedProfile = matchingProfile;
    }
}
