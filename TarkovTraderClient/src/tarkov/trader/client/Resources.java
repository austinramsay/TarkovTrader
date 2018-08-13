
package tarkov.trader.client;

import java.io.InputStream;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author austin
 */


// IMPORTANT: Because these resources are static and shared between instances, they may have values applied to them such as margins and alignments needed for other stages
// IMPORTANT: This may cause alignment issues unexpectedly
// IMPORTANT: Example - the main user interface pushes the outlineLogoViewer 350 pixels right. The spacing will need to be reevaluated at use for each specific stage after drawing the main UI


public class Resources 
{
      
    private Image logo;
    private Image outlineLogo;
    private Image icon;
    private Image avatar;
    private Image searchIcon;
    private Image browserIcon;
    private Image addIcon;
    private Image messagesIcon;
    private Image profileIcon;
    private Image myListingsIcon;
    private Image refreshIcon;
    private Image cancelIcon;
    private Image flagIcon;
    private Image exitIcon;
    
    private ImageView logoViewer;
    private ImageView outlineLogoViewer;
    private ImageView searchIconViewer;
    private ImageView browserIconViewer;
    private ImageView addIconViewer;
    private ImageView messagesIconViewer;
    private ImageView profileIconViewer;
    private ImageView myListingsIconViewer;
    private ImageView flagIconViewer;
    private ImageView refreshIconViewer;
    private ImageView cancelIconViewer;
    private ImageView exitIconViewer;
    
    public void load()
    {         
        icon = new Image(this.getResourceStream("/tarkovtradericon.png"));
            
        logo = new Image(getResourceStream("/tarkovtraderlogo.png"), 362, 116, true, true);
        logoViewer = new ImageView(logo);
          
        outlineLogo = new Image(getResourceStream("/tarkovtraderlogooutline.png"), 362, 116, true, true);
        outlineLogoViewer = new ImageView(outlineLogo);

        searchIcon = new Image(getResourceStream("/searchicon.png"), 128, 128, true, true);
        searchIconViewer = new ImageView(searchIcon);
           
        browserIcon = new Image(getResourceStream("/browsericon.png"), 128, 128, true, true);
        browserIconViewer = new ImageView(browserIcon);
           
        addIcon = new Image(getResourceStream("/addicon.png"), 128, 128, true, true);
        addIconViewer = new ImageView(addIcon);
            
        messagesIcon = new Image(getResourceStream("/messagesicon.png"), 32, 32, true, true);
        messagesIconViewer = new ImageView(messagesIcon);
            
        profileIcon = new Image(getResourceStream("/profileicon.png"), 32, 32, true, true);
        profileIconViewer = new ImageView(profileIcon);
            
        myListingsIcon = new Image(getResourceStream("/mylistings.png"), 32, 32, true, true);
        myListingsIconViewer = new ImageView(myListingsIcon);            
           
        refreshIcon = new Image(getResourceStream("/refresh.png"));
        refreshIconViewer = new ImageView(refreshIcon);
        
        flagIcon = new Image(this.getClass().getResourceAsStream("/flag.png"), 24, 24, true, true);
        flagIconViewer = new ImageView(flagIcon);        
            
        cancelIcon = new Image(getResourceStream("/cancel.png"));
        cancelIconViewer = new ImageView(cancelIcon);
            
        exitIcon = new Image(getResourceStream("/exiticon.png"), 32, 32, true, true);
        exitIconViewer = new ImageView(exitIcon);            
    }
    
    
    private InputStream getResourceStream(String resourceName)
    {
        return this.getClass().getResourceAsStream(resourceName);
    }
    
    public Image getIcon()
    {
        return icon;
    }
    
    public ImageView getLogo()
    {
        return logoViewer;
    }
    
    public ImageView getOutlineLogo()
    {
        return outlineLogoViewer;
    }
    
    public ImageView getsearchIcon()
    {
        return searchIconViewer;
    }
    
    public ImageView getBrowserIcon()
    {
        return browserIconViewer;
    }
    
    public ImageView getAddIcon()
    {
        return addIconViewer;
    }
    
    public ImageView getMessagesIcon()
    {
        return messagesIconViewer;
    }
    
    public ImageView getFlagIcon()
    {
        return flagIconViewer;
    }
    
    public ImageView getSmallMessagesIcon()
    {
        ImageView smallMessagesIcon = messagesIconViewer;
        smallMessagesIcon.setFitHeight(24);
        smallMessagesIcon.setFitWidth(24);
        return smallMessagesIcon;
    }
    
    public ImageView getSmallProfileIcon()
    {
        ImageView smallProfileIcon = profileIconViewer;
        smallProfileIcon.setFitHeight(24);
        smallProfileIcon.setFitWidth(24);
        return smallProfileIcon;
    }
    
    public ImageView getProfileIcon()
    {
        return profileIconViewer;
    }
    
    public ImageView getListingsIcon()
    {
        return myListingsIconViewer;
    }
    
    public ImageView getRefreshIcon()
    {
        return refreshIconViewer;
    }
    
    public ImageView getCancelIcon()
    {
        return cancelIconViewer;
    }
    
    public ImageView getExitIcon()
    {
        return exitIconViewer;
    }
}



