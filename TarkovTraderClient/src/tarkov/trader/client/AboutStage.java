
package tarkov.trader.client;

import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author austin
 */

public class AboutStage {
    
    private Stage aboutStage;
    
    private Image icon;
    private Image logo;
    private Image cancelIcon;
    private ImageView cancelIconViewer;
    private ImageView logoViewer;
    
    private Button donate;
    private Button cancel;
    
    public AboutStage()
    {
        loadResources();
        display();
    }
    
    
    private void loadResources()
    {
        icon = new Image(this.getClass().getResourceAsStream("/tarkovtradericon.png"));
        logo = new Image(this.getClass().getResourceAsStream("/tarkovtraderlogo.png"), 362, 116, true, true);
        cancelIcon = new Image(this.getClass().getResourceAsStream("/cancel.png"));
        cancelIconViewer = new ImageView(cancelIcon);        
        logoViewer = new ImageView(logo);        
    }
    
    
    private void display()
    {
        aboutStage = new Stage();
        aboutStage.setTitle("About Tarkov Trader");
        
        donate = new Button("Donate");
        donate.setOnAction(e -> 
        new Thread(() -> {
            try {
                Desktop.getDesktop().browse(new URI("https://www.paypal.me/tarkovtrader"));
            } catch (IOException | URISyntaxException ex) {
                Alert.display(null, "Failed to open browser.");
            }
        } ).start());
        
        cancel = new Button("Return");
        cancel.setOnAction(e -> aboutStage.close());
        
        Label aboutMe = new Label();
        aboutMe.setWrapText(true);
        aboutMe.setText(
                "Developed by Austin Ramsay, Tarkov Trader was designed to provide a trading marketplace for Escape From Tarkov.\n\n" +
                "The inspiration was found from lack of willingness to deal with multiple platforms when buying/selling items.\n\n" +
                "While the official 'Auction House' is in progress, Tarkov Trader will provide players a go-to platform for bartering.");
        
        Label iconCredits = new Label();
        iconCredits.setWrapText(true);
        iconCredits.setText("Icon/Graphic Credits");
        iconCredits.setStyle("-fx-underline: true");
        
        Label iconCredit1 = new Label();
        iconCredit1.setText("Icon Artist: xnimrodx - https://www.flaticon.com/authors/xnimrodx");
        
        Label iconCredit2 = new Label();
        iconCredit2.setText("Icon Artist: srip -  https://www.flaticon.com/authors/srip");
        
        Label graphicCredit1 = new Label();
        graphicCredit1.setText("Logo Artist: Logojoy.com");
        
        StackPane logoBox = new StackPane();
        logoBox.getChildren().addAll(logoViewer);
        
        VBox labels = new VBox(20);
        labels.setAlignment(Pos.CENTER);
        labels.getChildren().addAll(aboutMe, iconCredits, iconCredit1, iconCredit2);
        
        HBox buttonbox = new HBox(15);
        buttonbox.setAlignment(Pos.CENTER);
        buttonbox.getChildren().addAll(cancel, donate);
        
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setTop(logoBox);
        root.setCenter(labels);
        root.setBottom(buttonbox);
        
        Scene scene = new Scene(root, 600, 475);
        scene.getStylesheets().add(this.getClass().getResource("veneno.css").toExternalForm());
        aboutStage.setScene(scene);
        aboutStage.getIcons().add(icon);
        aboutStage.setResizable(false);
        aboutStage.show();
    }
    
}
