
package tarkov.trader.client;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import tarkov.trader.objects.Chat;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author austin
 */

public class Messenger {
    
    
    Stage messenger = new Stage();
    
    private VBox centerDisplay;
    private HBox buttonBox;
    
    private Image icon;
    private Image outlineLogo;
    private Image messagesIcon;
    private Image cancelIcon;
    private ImageView cancelIconViewer;
    private ImageView outlineLogoViewer;
    private ImageView messagesIconViewer;
    
    private Label messengerLabel;
    private Label chatsLabel;
    
    private TextArea chatDisplay;
    private TextField chatInput;
    
    private Button send;
    private Button cancel;
    
    private ListView<Chat> chatList;
    
    
    public Messenger()
    {
        loadResources();
    }
    
    
    private void loadResources()
    {
        icon = new Image(this.getClass().getResourceAsStream("/tarkovtradericon.png"));
        
        outlineLogo = new Image(this.getClass().getResourceAsStream("/tarkovtraderlogooutline.png"), 362, 116, true, true);
        outlineLogoViewer = new ImageView(outlineLogo);
        
        messagesIcon = new Image(this.getClass().getResourceAsStream("/messagesicon.png"), 24, 24, true, true);
        messagesIconViewer = new ImageView(messagesIcon);
        
        cancelIcon = new Image(this.getClass().getResourceAsStream("/cancel.png"));
        cancelIconViewer = new ImageView(cancelIcon);
    }
    
    
    public void display()
    {
        messenger = new Stage();
        
        
        // Labels
        messengerLabel = new Label("Messenger");
        messengerLabel.setPadding(new Insets(0,0,0,20));
        messengerLabel.getStyleClass().add("windowLabel");
        
        chatsLabel = new Label("Chats:");
        chatsLabel.setGraphic(messagesIconViewer);
        chatsLabel.getStyleClass().add("subWindowLabel");
        
        // List view 
        chatList = new ListView<>();
        
        
        // Text fields/areas
        chatDisplay = new TextArea();
        chatDisplay.setText("No chat selected.");
        chatDisplay.setEditable(false);
        chatDisplay.setWrapText(true);
        
        chatInput = new TextField();
        
        
        // Buttons
        send = new Button("Send");
        
        cancel = new Button("Return");
        cancel.setGraphic(cancelIconViewer);
        cancel.setOnAction(e -> close());
        
        
        //Layout construction begin
        
        // North upper display construction
        HBox upperDisplay = new HBox();
        upperDisplay.setAlignment(Pos.CENTER);
        
        HBox upperDisplayRight = new HBox();
        HBox.setHgrow(upperDisplayRight, Priority.ALWAYS);
        upperDisplayRight.setAlignment(Pos.CENTER_RIGHT);
              
        upperDisplayRight.getChildren().add(outlineLogoViewer);
        
        upperDisplay.getChildren().addAll(messengerLabel, upperDisplayRight);  // Note: itemListingLabel has padding 20 left on initialization
        
        upperDisplay.getStyleClass().add("hbox");
        // End upper display construction
        
        
        // Left display construction
        // This will contain 'Actions' label and the ListView to show available chats
        VBox leftDisplay = new VBox(30);
        leftDisplay.setPadding(new Insets(25,20,25,20));
        leftDisplay.setAlignment(Pos.CENTER);
        leftDisplay.getChildren().addAll(chatsLabel, chatList, cancel);  
        leftDisplay.getStyleClass().add("vbox");
        
        
        // Lower HBox to mount the chat input text field and send button
        buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(chatInput, send);
        
        
        // Center VBox to mount the chat display text area and buttonbox
        centerDisplay = new VBox(13);
        centerDisplay.setPadding(new Insets(25,15,20,20));
        centerDisplay.getChildren().addAll(chatDisplay, buttonBox);        
        
        
        // Main border pane construction
        BorderPane border = new BorderPane();
        border.setTop(upperDisplay);
        border.setLeft(leftDisplay);
        border.setCenter(centerDisplay);
        

        // Setup the stage and scene
        Scene scene = new Scene(border);
        scene.getStylesheets().add(this.getClass().getResource("veneno.css").toExternalForm());
        
        messenger.setScene(scene);
        messenger.setResizable(false);
        messenger.setTitle("Messenger");
        messenger.getIcons().add(icon);
        messenger.setOnCloseRequest(e -> messenger.close());
        messenger.show();
        
        
        // After stage is displayed, resize text fields/areas
        resizeFields();
    }
    
    
    private void resizeFields()
    {
        chatInput.setPrefWidth(chatDisplay.getWidth() - send.getWidth() - 10);
        chatDisplay.setPrefSize(centerDisplay.getWidth(), (centerDisplay.getHeight() - buttonBox.getHeight()));
    }
    
    
    private void close()
    {
        messenger.close();
    }
    
}
