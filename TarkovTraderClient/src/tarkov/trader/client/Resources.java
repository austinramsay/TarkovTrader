/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
      
    public static Image logo;
    public static Image outlineLogo;
    public static Image icon;
    public static Image avatar;
    public static Image searchIcon;
    public static Image browserIcon;
    public static Image addIcon;
    public static Image messagesIcon;
    public static Image profileIcon;
    public static Image refreshIcon;
    public static Image cancelIcon;
    public static Image exitIcon;
    public static Image keyBrowserIcon;
    
    public static ImageView logoViewer;
    public static ImageView outlineLogoViewer;
    public static ImageView searchIconViewer;
    public static ImageView browserIconViewer;
    public static ImageView addIconViewer;
    public static ImageView messagesIconViewer;
    public static ImageView profileIconViewer;
    public static ImageView refreshIconViewer;
    public static ImageView cancelIconViewer;
    public static ImageView exitIconViewer;
    public static ImageView keyBrowserIconViewer;
    
    public static void load()
    {
            Resources resources = new Resources();
                                
            icon = new Image(resources.getResourceStream("/tarkovtradericon.png"));
            
            logo = new Image(resources.getResourceStream("/tarkovtraderlogo.png"), 362, 116, true, true);
            logoViewer = new ImageView(logo);
            
            outlineLogo = new Image(resources.getResourceStream("/tarkovtraderlogooutline.png"), 362, 116, true, true);
            outlineLogoViewer = new ImageView(outlineLogo);

            searchIcon = new Image(resources.getResourceStream("/searchicon.png"), 128, 128, true, true);
            searchIconViewer = new ImageView(searchIcon);
            
            browserIcon = new Image(resources.getResourceStream("/browsericon.png"), 128, 128, true, true);
            browserIconViewer = new ImageView(browserIcon);
            
            addIcon = new Image(resources.getResourceStream("/addicon.png"), 128, 128, true, true);
            addIconViewer = new ImageView(addIcon);
            
            messagesIcon = new Image(resources.getResourceStream("/messagesicon.png"), 32, 32, true, true);
            messagesIconViewer = new ImageView(messagesIcon);
            
            profileIcon = new Image(resources.getResourceStream("/profileicon.png"), 32, 32, true, true);
            profileIconViewer = new ImageView(profileIcon);
            
            refreshIcon = new Image(resources.getResourceStream("/refresh.png"));
            refreshIconViewer = new ImageView(refreshIcon);
            
            cancelIcon = new Image(resources.getResourceStream("/cancel.png"));
            cancelIconViewer = new ImageView(cancelIcon);
            
            exitIcon = new Image(resources.getResourceStream("/exiticon.png"), 32, 32, true, true);
            exitIconViewer = new ImageView(exitIcon);            
            
    }
    
    
    private InputStream getResourceStream(String resourceName)
    {
        return this.getClass().getResourceAsStream(resourceName);
    }
    
}



