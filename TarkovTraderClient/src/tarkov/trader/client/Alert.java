
package tarkov.trader.client;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene; 
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

public class Alert {
    
    public static void display(String title, String message)
    {
        Stage alertStage = new Stage();
        Scene alertScene;
        
        VBox layout;
        Label alertLabel = new Label(message);
        Button closeButton = new Button("Close");
        
        alertStage.setTitle(title);
        alertStage.initModality(Modality.APPLICATION_MODAL);
        
        layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(alertLabel, closeButton);
        
        closeButton.setOnAction(e -> alertStage.close());
        
        alertScene = new Scene(layout);
        alertScene.getStylesheets().add("/tarkov/trader/client/veneno.css");
        alertStage.setScene(alertScene);
        
        alertStage.showAndWait();
        
    }
    
    public static void displayNotification(String title, String text, int duration)
    {
        Alert alert = new Alert();
        
        ImageView icon = new ImageView(new Image(alert.getClass().getResourceAsStream("/tarkovtradericon.png")));
        
        Notifications notification = Notifications.create()
                .title(title)
                .text(text)
                .graphic(icon)
                .hideAfter(Duration.seconds(duration));
                
        notification.darkStyle();
        notification.show();
    }
    
}