
package tarkov.trader.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
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
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import tarkov.trader.objects.ChatListForm;
import tarkov.trader.objects.Message;

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

    
    private void loadResources()
    {
        icon = new Image(this.getClass().getResourceAsStream("/tarkovtradericon.png"));
        
        outlineLogo = new Image(this.getClass().getResourceAsStream("/tarkovtraderlogooutline.png"), 362, 116, true, true);
        outlineLogoViewer = new ImageView(outlineLogo);
        
        messagesIcon = new Image(this.getClass().getResourceAsStream("/messagesicon.png"), 24, 24, true, true);
        messagesIconViewer = new ImageView(messagesIcon);

        newIcon = new Image(this.getClass().getResourceAsStream("/addmid.png"), 24, 24, true, true);
        newIconViewer = new ImageView(newIcon);
        
        cancelIcon = new Image(this.getClass().getResourceAsStream("/cancel.png"));
        cancelIconViewer = new ImageView(cancelIcon);
    }
    
    
    public boolean display()
    {
        if (Messenger.isOpen)
        {
            Alert.display(null, "Messenger already open.");
            return false;
        }
        
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
                    this.setGraphicTextGap(15);
                    this.setPrefHeight(45);
                    this.setTooltip(new Tooltip("Right Click for Options"));
                    this.setOnMouseClicked(e -> { 
                        unpackChatMessages(chat.getMessages());
                    });
                }
            }
        });
        
        
        // Text fields/areas
        chatDisplay = new TextArea();
        chatDisplay.setText("No chat selected.");
        chatDisplay.setEditable(false);
        chatDisplay.setWrapText(true);
        chatDisplay.getStyleClass().add("chatDisplay");
        
        chatInput = new TextField();
        chatInput.setOnAction(e -> send());
        
        
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
        messenger.setOnCloseRequest(e -> close());
        messenger.show();     
        Messenger.isOpen = true;
        
        // After stage is displayed, resize text fields/areas
        resizeFields();
        
        // Finally, pull fresh chat list from the server
        sync();
        
        return true;
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
            append(message);
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
        
        
        // TabPane for switching between All Users / Online Users
        TabPane userLookupPane = new TabPane();
        Tab fullUsersTab = new Tab("All Users");
        Tab onlineUsersTab = new Tab("Online Users:");
        userLookupPane.getTabs().addAll(fullUsersTab, onlineUsersTab);         
        
        
        // Search label
        Label searchLabel = new Label("Search:");
        
        
        // Search input text field
        TextField searchInput = new TextField();
        searchInput.setPromptText("Username");
        
        
        // Create button
        Button createNewChat = new Button("Create");
        createNewChat.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("/addsmall.png"), 16, 16, true, true))); 
        
        
        // Cancel button
        Button cancelNewChat = new Button("Return");
        cancelNewChat.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("/cancelsmall.png"), 16, 16, true, true))); 
        cancelNewChat.setOnAction(e -> prompt.close());
        
        
        
        //     *****UNIVERSAL LIST VIEW CELL FACTORY*****
        //     To be used by cell factories of both All/Online user list views
        Callback usernameCellCallback = new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param)
            {
                ListCell<String> cell = new ListCell<String>()
                {
                    @Override
                    protected void updateItem(String username, boolean empty)
                    {
                        if (username != null) 
                        {
                            super.updateItem(username, empty);
                        
                            if (!username.equals(""))
                            {
                                this.setText(username);
                                this.setPrefHeight(35);
                                this.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                    @Override
                                    public void handle(MouseEvent e)
                                    {
                                        if (e.getClickCount() == 2)
                                        {
                                            // Check if this chat is already created
                                            if (!chatExists(username))
                                            {
                                                // Chat doesn't exist, build now
                                                buildNewChat(username);
                                                prompt.close();
                                            }
                                            else
                                            {
                                                // This chat already exists..
                                                Platform.runLater(() -> Alert.display(null, "A chat for " + username + " already exists."));          
                                            }
                                        }
                                    }
                                });
                            }   
                        }
                        else
                        {
                            super.updateItem(null, empty);
                            this.setText(null);
                            this.setOnMouseClicked(null);
                        }
                    }
                };
                return cell;        
            }
        };
        
        //     *****END CELL FACTORY*****
        //     *****************************************************
        
        
        // Observable and filtered lists for both ListView's (ALL & ONLINE)
        ObservableList<String> fullList = FXCollections.observableArrayList(TarkovTrader.userList);
        if (fullList.contains(TarkovTrader.username))
            fullList.remove(TarkovTrader.username);
        
        ObservableList<String> onlineList = FXCollections.observableArrayList(TarkovTrader.onlineList);
        if (onlineList.contains(TarkovTrader.username))
            onlineList.remove(TarkovTrader.username);
        
        FilteredList<String> allFilteredList = new FilteredList<>(fullList, getAllUsernamesPredicate());
        FilteredList<String> onlineFilteredList = new FilteredList<>(onlineList, getAllUsernamesPredicate());
        
        
        // Upon text change in the search filter box being changed, apply the necessary predicate to the filtered list
        searchInput.textProperty().addListener(obs ->
        {
            String filter = searchInput.getText(); 
            
            if(filter == null || filter.length() == 0) 
            {
                allFilteredList.setPredicate(getAllUsernamesPredicate());
                onlineFilteredList.setPredicate(getAllUsernamesPredicate());
            }
            
            else 
            {
                allFilteredList.setPredicate(getFilteredPredicate(searchInput.getText()));
                onlineFilteredList.setPredicate(getFilteredPredicate(searchInput.getText()));
            }
        });    
        
        
        // Sorted list for the completed ALL USERS filtered list
        SortedList<String> fullUsersSortedList = new SortedList<>(allFilteredList, new Comparator<String>() {
            @Override
            public int compare(String arg1, String arg2)
            {
                return arg1.compareToIgnoreCase(arg2);
            }
        });
        
        
        // Sorted list for the completed ONLINE filtered list
        SortedList<String> onlineUsersSortedList = new SortedList<>(onlineFilteredList, new Comparator<String>() {
            @Override
            public int compare(String arg1, String arg2)
            {
                return arg1.compareToIgnoreCase(arg2);
            }
        });       
        
        
        // Build the list view of ALL usernames to use the sorted and filtered list -- Build the cells
        ListView<String> fullUserList = new ListView<>();
        fullUserList.setItems(allFilteredList);
        fullUserList.setCellFactory(usernameCellCallback);
        
        
        // Build the list view of ONLINE usernames to use the sorted and filtered list -- Build the cells
        ListView<String> onlineUserList = new ListView<>();
        onlineUserList.setItems(onlineUsersSortedList);
        onlineUserList.setCellFactory(usernameCellCallback);
        
        
        // HBox housing the Search label and input field
        HBox searchDisplay = new HBox(8);
        searchDisplay.setAlignment(Pos.CENTER);
        searchDisplay.getChildren().addAll(searchLabel, searchInput);
        
        
        // Root layout VBox
        VBox newChatRoot = new VBox(11);
        newChatRoot.setPadding(new Insets(10));
        newChatRoot.getChildren().addAll(searchDisplay, userLookupPane);
        
        
        // Now that content has been built for the tabs, set the tab content
        onlineUsersTab.setContent(onlineUserList);
        fullUsersTab.setContent(fullUserList);
        
        
        // Scene / Stage setup
        Scene newChatScene = new Scene(newChatRoot);
        newChatScene.getStylesheets().add(this.getClass().getResource("veneno.css").toExternalForm());
        
        prompt.setScene(newChatScene);
        prompt.setTitle("New Chat");
        prompt.setResizable(false);
        
        return prompt;
    }
    
   
    // Predicate returns true for all usernames
    private Predicate<String> getAllUsernamesPredicate()
    {
        Predicate<String> predicate = new Predicate<String>() {
            
            @Override
            public boolean test(String username)
            {
                return true;
            } 
            
        };
        
        return predicate;
    }
        
        
    // Predicate returns true if the username list contains a name according to search filter input
    Predicate<String> getFilteredPredicate(String searchInput) 
    {
        Predicate<String> predicate = new Predicate<String>() {
        
            @Override
            public boolean test(String username)
            {
                return (username.contains(searchInput));
            }
            
        };
        
        return predicate;
    };         
    
    
    private boolean buildNewChat(String destination)
    {
        Chat newchat = new Chat(TarkovTrader.username, destination, null);
        chatListView.getItems().add(newchat);
        chatListView.getSelectionModel().select(newchat);
        
        chatDisplay.setText("New chat to: " + newchat.getDestination());
        
        return true;
    }
    
    
    private boolean chatExists(String destination)
    {
        // Checks if a chat with the corresponding destination name already exists
        for (Chat openChat : chatListView.getItems())
        {
            if (openChat.getName(TarkovTrader.username).equals(destination))
                return true;
        }
        
        return false;
    }
    
    
    private boolean send()
    {
        Chat currentChat = chatListView.getSelectionModel().getSelectedItem();
        String message = TarkovTrader.username + ": " + chatInput.getText();
              
        if (chatInput.getText().isEmpty())
            return false;
        
        if (currentChat == null)
        {
            Platform.runLater(() -> Alert.display(null, "No chat selected."));
            chatInput.setText("");
            return false;
        }
            
        // Determine if we are starting a new chat or sending a message
        if (currentChat.isNew)
        {
            // Starting a new chat, append the message before sending to server
            currentChat.appendMessage(message);
            
            if (worker.sendForm(currentChat))
            {
                append(message);
                chatInput.clear();
                return true;
            }
            
        }
        else
        {
            // Using an open chat
            Message messageform = new Message(TarkovTrader.username, currentChat.getName(TarkovTrader.username), message);
            if (worker.sendForm(messageform))
            {  
                System.out.println("sent message");
                append(message);
                chatListView.getSelectionModel().getSelectedItem().appendMessage(message);
            }
            chatInput.clear();
            return true;
        }
        
        return false;
    }
    
    
    private void append(String message)
    {
        if (chatDisplay.getText() == null)
            chatDisplay.appendText(message);
        else
            chatDisplay.appendText("\n\n" + message);     
    }
    
    
    private boolean sync()
    {
        // Build a form of type 'chatlist'
        // Server responds with full list and requestworker will populate the listview
        ChatListForm requestchatform = new ChatListForm();
        
        
        if (worker.sendForm(requestchatform))
        {       
            TarkovTrader.syncInProgress.compareAndSet(false, true);  
            return true;
        }
        
        
        Platform.runLater(() -> Alert.display(null, "Failed to request chat list from server."));
        
        requestchatform = null;
        return false;
    }
    
    
    public synchronized void processMessage(Message messageform)
    {
        String sender = messageform.getOrigin();
        String message = messageform.getMessage();
        
        if (chatListView.getItems() != null)
        {
            if (chatListView.getSelectionModel().getSelectedItem().getName(TarkovTrader.username).equals(sender))
            {
                chatListView.getSelectionModel().getSelectedItem().appendMessage(message);
                append(message);
            }
            else
            {
                for (Chat chat : chatListView.getItems())
                {
                    if (chat.getName(TarkovTrader.username).equals(sender))
                         chat.appendMessage(message);
                }
            }
        }
    }
    
    
    public static void contactSeller(Messenger messenger, String destination, String itemName)
    {
        Task<Void> waitForSync = new Task<Void>() {
            @Override
            public Void call()
            {
                while (TarkovTrader.syncInProgress.get())
                {
                    ; // Wait until sync is complete
                }
                return null;
            }
        };
        
        waitForSync.setOnSucceeded(e -> {
            
            if (messenger.chatExists(destination))
            {
                // Chat exists, select the chat for the user
                for (Chat openChat : messenger.chatListView.getItems())
                {
                    if (openChat.getName(TarkovTrader.username).equals(destination))
                    {
                        messenger.chatListView.getSelectionModel().select(openChat);
                        messenger.unpackChatMessages(openChat.getMessages());
                        break;
                    }   
                }
            }
            else 
            {
                messenger.buildNewChat(destination);
            }
        
            messenger.chatInput.setText("Hey " + destination + ". Interested in your '" + itemName + "'.");
            messenger.chatInput.setOnMouseClicked(me -> messenger.chatInput.clear());        
        
        });
        
        Thread t = new Thread(waitForSync);
        t.setDaemon(true);
        t.start();
    }
    
    
    private void close()
    {
        Messenger.isOpen = false;
        messenger.close();
    }
    
}





