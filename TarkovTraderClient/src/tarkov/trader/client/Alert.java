
package tarkov.trader.client;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene; 
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
    
}