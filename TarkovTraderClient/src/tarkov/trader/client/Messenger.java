
package tarkov.trader.client;

import java.util.ArrayList;
import javafx.application.Platform;
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
import tarkov.trader.objects.ChatListForm;
import tarkov.trader.objects.Form;

/**
 *
 * @author austin
 */

public class Messenger {
        
    private RequestWorker worker;
    public volatile static boolean isOpen;
    
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
    private Button newChatButton;
    private Button cancel;
    
    public ListView<Chat> chatListView;
    
    
    public Messenger(RequestWorker worker)
    {
        this.worker = worker;
        loadResources();
    }
    
    public Messenger(String destination)
    {
        // This should be used for calling 'Contact Seller' in an item listing
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
                    this.setGraphic(null);
                }
                else
                {
                    this.setText(chat.getName(TarkovTrader.username));
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
        chatDisplay.getStyleClass().add("chatDisplay");
        
        chatInput = new TextField();
        
        
        // Buttons
        send = new Button("Send");
        send.setOnAction(e -> send());
        
        newChatButton = new Button("New Chat");
        newChatButton.setGraphic(newIconViewer);
        newChatButton.setOnAction(e -> newChatPrompt().show());
        
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
        leftButtonBox.getChildren().addAll(newChatButton, cancel);
        
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
        Messenger.isOpen = true;
        messenger.show();     
      
        // After stage is displayed, resize text fields/areas
        resizeFields();
        
        
        // Finally, pull fresh chat list from the server
        pullChatList();
    }
    
    
    private void resizeFields()
    {
        chatInput.setPrefWidth(chatDisplay.getWidth() - send.getWidth() - 10);
        chatDisplay.setPrefSize(centerDisplay.getWidth(), (centerDisplay.getHeight() - buttonBox.getHeight()));
    }
    
    
    private boolean unpackChatMessages(ArrayList<String> messageList)
    {
        chatDisplay.setText(null);
        
        if (messageList == null)
            return false;
        
        for (String message : messageList)
        {
            if (chatDisplay.getText() == null)
                chatDisplay.appendText(message);
            else
                chatDisplay.appendText("\n" + message);
        }
        
        return true;
    }
    
    
    public void populate(ArrayList<Chat> chatList)
    {
        chatListView.setItems(getChatList(chatList));
    }
    
    
    private void populateNewChat(ObservableList<Chat> newChatList)
    {
        chatListView.setItems(newChatList);
    }
        
    
    private ObservableList<Chat> getChatList(ArrayList<Chat> chatList)
    {
        return FXCollections.observableArrayList(chatList);
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
                    buildNewChat(newChatUsernameInput.getText());
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
        
        VBox newChatRoot = new VBox(11);
        newChatRoot.setPadding(new Insets(10));
        newChatRoot.getChildren().addAll(newChatCenterDisplay, newChatButtonBox);
        
        Scene newChatScene = new Scene(newChatRoot);
        newChatScene.getStylesheets().add(this.getClass().getResource("veneno.css").toExternalForm());
        
        prompt.setScene(newChatScene);
        prompt.setTitle("New Chat");
        prompt.setResizable(false);
        
        return prompt;
    }
    
    
    private boolean buildNewChat(String destination)
    {
        // TODO: Check if username exists -- and if user is online
        
        Chat newchat = new Chat(TarkovTrader.username, destination, null);
        chatListView.getItems().add(newchat);
        chatListView.getSelectionModel().select(newchat);
        
        chatDisplay.setText("New chat to: " + newchat.getDestination());
        
        return false;
    }
    
    
    private boolean send()
    {
        Chat currentChat = chatListView.getSelectionModel().getSelectedItem();
        String message = chatInput.getText();
        if (message.equals(""))
            return false;
        
        if (currentChat == null)
        {
            Platform.runLater(() -> Alert.display(null, "No chat selected."));
            return false;
        }
            
        if (currentChat.getMessages() == null)
        {
            // Starting a new chat
            currentChat.appendMessage(message);
            worker.sendForm(currentChat);
            return true;
        }
        else if (currentChat.getMessages() != null)
        {
            // Using an open chat
            System.out.println("sending a current chat");
            return true;
        }
        
        return false;
    }
    
    
    private boolean pullChatList()
    {
        // Build a form of type 'chatlist'
        // Server responds with full list and requestworker will populate the listview
        ChatListForm requestchatform = new ChatListForm();
        
        
        if (worker.sendForm(requestchatform))
            return true;
        else
            Platform.runLater(() -> Alert.display(null, "Failed to request chat list from server."));
        
        requestchatform = null;
        return false;
    }
    
    
    private void close()
    {
        Messenger.isOpen = false;
        messenger.close();
    }
    
}
