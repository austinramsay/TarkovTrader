
package tarkov.trader.client;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

public class Alert {
    
    public static void display(String title, String text)
    {
        displayNotification(title, text, 5);
    }
    
    
    public static void displayNotification(String title, String text, int duration)
    {
        Alert alert = new Alert();
        
        ImageView icon = new ImageView(new Image(alert.getClass().getResourceAsStream("/tarkovtradericon.png")));
        
        Notifications notification = Notifications.create()
                .title(null)
                .text(text)
                .graphic(icon)
                .hideAfter(Duration.seconds(duration));
                
        notification.darkStyle();
        notification.show();
    }
    
}
