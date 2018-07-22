
package tarkov.trader.client;

import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import tarkov.trader.objects.Chat;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
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
    private Image newIcon;
    private Image cancelIcon;
    private ImageView cancelIconViewer;
    private ImageView outlineLogoViewer;
    private ImageView messagesIconViewer;
    private ImageView newIconViewer;
    
    private Label messengerLabel;
    private Label chatsLabel;
    
    private TextArea chatDisplay;
    private TextField chatInput;
    
    private Button send;
    private Button newChat;
    private Button cancel;
    
    private ListView<Chat> chatListView;
    
    
    public Messenger()
    {
        loadResources();
    }
    
    public Messenger(String destination)
    {
        
    }
    
    private void loadResources()
    {
        icon = new Image(this.getClass().getResourceAsStream("/tarkovtradericon.png"));
        
        outlineLogo = new Image(this.getClass().getResourceAsStream("/tarkovtraderlogooutline.png"), 362, 116, true, true);
        outlineLogoViewer = new ImageView(outlineLogo);
        
        messagesIcon = new Image(this.getClass().getResourceAsStream("/messagesicon.png"), 24, 24, true, true);
        messagesIconViewer = new ImageView(messagesIcon);

        newIcon = new Image(this.getClass().getResourceAsStream("/new.png"), 24, 24, true, true);
        newIconViewer = new ImageView(newIcon);
        
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
        chatListView = new ListView<>();
        chatListView.setCellFactory(param -> new ListCell<Chat>() {
            @Override
            protected void updateItem(Chat chat, boolean empty)
            {
                super.updateItem(chat, empty);
                
                if (empty || chat == null)
                {
                    this.setText(null);
                }
                else
                {
                    this.setText(chat.getOrigin());
                    this.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("/chat.png"), 24, 24, true, true)));
                    this.setPrefHeight(45);
                }
            }
        });
        chatListView.setOnMouseClicked(e -> unpackChatMessages(chatListView.getSelectionModel().getSelectedItem().getMessages()));
        
        
        // Text fields/areas
        chatDisplay = new TextArea();
        chatDisplay.setText("No chat selected.");
        chatDisplay.setEditable(false);
        chatDisplay.setWrapText(true);
        
        chatInput = new TextField();
        
        
        // Buttons
        send = new Button("Send");
        
        newChat = new Button("New Chat");
        newChat.setGraphic(newIconViewer);
        newChat.setOnAction(e -> newChatPrompt().show());
        
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
        // This will contain 'Actions' label, the ListView to show available chats, and an HBox on the bottom for 2 buttons 'New Chat' & 'Return'
        HBox leftButtonBox = new HBox(10);
        leftButtonBox.setAlignment(Pos.CENTER);
        leftButtonBox.getChildren().addAll(newChat, cancel);
        
        VBox leftDisplay = new VBox(30);
        leftDisplay.setPadding(new Insets(25,20,25,20));
        leftDisplay.setAlignment(Pos.CENTER);
        leftDisplay.getChildren().addAll(chatsLabel, chatListView, leftButtonBox);  
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
        
        
        // TESTING TESTING ----------------
        this.populate(null);
        
        
        // After stage is displayed, resize text fields/areas
        resizeFields();
    }
    
    
    private void resizeFields()
    {
        chatInput.setPrefWidth(chatDisplay.getWidth() - send.getWidth() - 10);
        chatDisplay.setPrefSize(centerDisplay.getWidth(), (centerDisplay.getHeight() - buttonBox.getHeight()));
    }
    
    
    private void unpackChatMessages(ArrayList<String> messageList)
    {
        chatDisplay.setText(null);
        
        for (String message : messageList)
        {
            if (chatDisplay.getText() == null)
                chatDisplay.appendText(message);
            else
                chatDisplay.appendText("\n" + message);
        }
    }
    
    
    private void populate(ArrayList<Chat> chatList)
    {
        ArrayList<Chat> testList = new ArrayList<>();
        ArrayList<String> testMessages = new ArrayList<>();
        testMessages.add("this is a message");
        testMessages.add("this is a second message");
        testMessages.add("we are testing the messenger");
        testMessages.add("how much for that epsilon?");
        testList.add(new Chat("supertroopr", "austinramsay", testMessages));
        testList.add(new Chat("deadlyslob", "austinramsay", testMessages));
        chatListView.setItems(getChatList(testList));
    }
        
    
    private ObservableList<Chat> getChatList(ArrayList<Chat> chatList)
    {
        return FXCollections.observableArrayList(chatList);
    }
    
    
    private ObservableList<Chat> getCurrentChatList()
    {
        return chatListView.getItems();
    }
    
    
    private Stage newChatPrompt()
    {
        Stage prompt = new Stage();
        
        Label newChatUsernameLabel = new Label("Username:");
        
        TextField newChatUsernameInput = new TextField();
        newChatUsernameInput.setPromptText("Destination");
        
        Button createNewChat = new Button("Create");
        createNewChat.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("/new.png"), 24, 24, true, true))); 
        createNewChat.setOnAction(e -> 
            {
                if (!newChatUsernameInput.getText().equals(""))
                {
                    buildNewChat();
                    prompt.close();
                }
                else
                    Alert.displayNotification(null, "No username was entered!", 5);
            });
        
        Button cancelNewChat = new Button("Return");
        cancelNewChat.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("/cancel.png"), 24, 24, true, true))); 
        cancelNewChat.setOnAction(e -> prompt.close());
        
        HBox newChatCenterDisplay = new HBox(8);
        newChatCenterDisplay.setAlignment(Pos.CENTER);
        newChatCenterDisplay.getChildren().addAll(newChatUsernameLabel, newChatUsernameInput);
        
        HBox newChatButtonBox = new HBox(10);
        newChatButtonBox.setAlignment(Pos.CENTER);
        newChatButtonBox.getChildren().addAll(createNewChat, cancelNewChat);
        
        VBox newChatRoot = new VBox(8);
        newChatRoot.setPadding(new Insets(10));
        newChatRoot.getChildren().addAll(newChatCenterDisplay, newChatButtonBox);
        
        Scene newChatScene = new Scene(newChatRoot);
        newChatScene.getStylesheets().add(this.getClass().getResource("veneno.css").toExternalForm());
        
        prompt.setScene(newChatScene);
        prompt.setTitle("New Chat");
        prompt.setResizable(false);
        
        return prompt;
    }
    
    
    private boolean buildNewChat()
    {
        return false;
    }
    
    
    private void close()
    {
        messenger.close();
    }
    
}