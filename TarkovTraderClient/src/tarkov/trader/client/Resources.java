
package tarkov.trader.client;

import java.io.InputStream;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author austin
 */

public class Resources 
{
    private InputStream getResourceStream(String resourceName)
    {
        return this.getClass().getResourceAsStream(resourceName);
    }
    
    public Image getIcon()
    {
        Image icon = new Image(this.getResourceStream("/tarkovtradericon.png"));
        return icon;
    }
    
    public ImageView getLogo()
    {
        Image logo = new Image(getResourceStream("/tarkovtraderlogo.png"), 362, 116, true, true);
        ImageView logoViewer = new ImageView(logo);        
        return logoViewer;
    }
    
    public ImageView getOutlineLogo()
    {
        Image outlineLogo = new Image(getResourceStream("/tarkovtraderlogooutline.png"), 362, 116, true, true);
        ImageView outlineLogoViewer = new ImageView(outlineLogo);        
        return outlineLogoViewer;
    }
    
    public ImageView getSearchIcon()
    {
        Image searchIcon = new Image(getResourceStream("/searchicon.png"), 128, 128, true, true);
        ImageView searchIconViewer = new ImageView(searchIcon);        
        return searchIconViewer;
    }
    
    public ImageView getBrowserIcon()
    {
        Image browserIcon = new Image(getResourceStream("/browsericon.png"), 128, 128, true, true);
        ImageView browserIconViewer = new ImageView(browserIcon);        
        return browserIconViewer;
    }
    
    public ImageView getAddIcon()
    {
        Image addIcon = new Image(getResourceStream("/addicon.png"), 128, 128, true, true);
        ImageView addIconViewer = new ImageView(addIcon);        
        return addIconViewer;
    }
    
    public ImageView getMessagesIcon()
    {
        Image messagesIcon = new Image(getResourceStream("/messagesicon.png"), 32, 32, true, true);
        ImageView messagesIconViewer = new ImageView(messagesIcon);        
        return messagesIconViewer;
    }
    
    public ImageView getFlagIcon()
    {
        Image flagIcon = new Image(this.getClass().getResourceAsStream("/flag.png"), 24, 24, true, true);
        ImageView flagIconViewer = new ImageView(flagIcon);           
        return flagIconViewer;
    }
    
    public ImageView getBigFlagIcon()
    {
        Image flagIcon = new Image(this.getClass().getResourceAsStream("/flag_big.png"), 32, 32, true, true);
        ImageView flagIconViewer = new ImageView(flagIcon);
        return flagIconViewer;
    }
    
    public ImageView getSmallMessagesIcon()
    {
        ImageView smallMessagesIcon = new ImageView(new Image(this.getClass().getResourceAsStream("/messagesicon.png"), 24, 24, true, true));
        return smallMessagesIcon;
    }
    
    public ImageView getSmallProfileIcon()
    {
        ImageView smallProfileIcon = new ImageView(new Image(this.getClass().getResourceAsStream("/profileicon.png"), 24, 24, true, true));
        return smallProfileIcon;
    }
    
    public ImageView getProfileIcon()
    {
        Image profileIcon = new Image(getResourceStream("/profileicon.png"), 32, 32, true, true);
        ImageView profileIconViewer = new ImageView(profileIcon);             
        return profileIconViewer;
    }
    
    public ImageView getListingsIcon()
    {
        Image myListingsIcon = new Image(getResourceStream("/mylistings.png"), 32, 32, true, true);
        ImageView myListingsIconViewer = new ImageView(myListingsIcon);           
        return myListingsIconViewer;
    }
    
    public ImageView getRefreshIcon()
    {
        Image refreshIcon = new Image(getResourceStream("/refresh.png"));
        ImageView refreshIconViewer = new ImageView(refreshIcon);        
        return refreshIconViewer;
    }
    
    public ImageView getCancelIcon()
    {
        Image cancelIcon = new Image(getResourceStream("/cancel.png"));
        ImageView cancelIconViewer = new ImageView(cancelIcon);        
        return cancelIconViewer;
    }
    
    public ImageView getExitIcon()
    {
        Image exitIcon = new Image(getResourceStream("/exiticon.png"), 32, 32, true, true);
        ImageView exitIconViewer = new ImageView(exitIcon);             
        return exitIconViewer;
    }
    
    public ImageView getMinusIcon()
    {
        Image minusImage = new Image(this.getClass().getResourceAsStream("/minus.png"), 32, 32, true, true);
        ImageView minusViewer = new ImageView(minusImage);   
        return minusViewer;
    }
        
    public ImageView getDeleteIcon()
    {
        Image deleteImage = new Image(this.getClass().getResourceAsStream("/delete.png"), 32, 32, true, true);
        ImageView deleteViewer = new ImageView(deleteImage); 
        return deleteViewer;
    }
    
    public ImageView getEditIcon()
    {  
        Image editImage = new Image(this.getClass().getResourceAsStream("/edit.png"), 32, 32, true, true);
        ImageView editViewer = new ImageView(editImage); 
        return editViewer;
    }
     
    public ImageView getSuspendIcon()
    {
        Image suspendImage = new Image(this.getClass().getResourceAsStream("/suspend.png"), 32, 32, true, true);
        ImageView suspendViewer = new ImageView(suspendImage);    
        return suspendViewer;
    }
    
    public ImageView getAcceptIcon()
    {
        Image acceptImage = new Image(this.getClass().getResourceAsStream("/checkmark.png"), 32, 32, true, true);
        ImageView acceptViewer = new ImageView(acceptImage);
        return acceptViewer;
    }
    
    public ImageView getDeclineIcon()
    {
        Image declineImage = new Image(this.getClass().getResourceAsStream("/decline.png"), 32, 32, true, true);
        ImageView declineViewer = new ImageView(declineImage);
        return declineViewer;
    }
}



