package tarkov.trader.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tarkov.trader.objects.Notification;

/**
 *
 * @author austin
 * NOTES: Profile avatars must be under 65kb large (will also be downsized to 128x128)
 * NOTES: All fields specified in creating a new account must be 16 characters or less (besides IGN which is 24 chars)
 */

public class TarkovTrader extends Application {
    
    // Object declarations
    private RequestWorker worker;
    public LoginPrompt mainLogin;
    private Browser browser;
    private AddItemStage addItemStage;
    private NewSearch newSearch;
    private Messenger messenger;
    private NotificationManager notificationManager;
    private Moderator moderator;
    private Thread workerThread;
    
    // JavaFX variable declarations
    private Stage primaryStage;
    private Scene mainUIscene;
    private MenuBar menubar;
    private Menu ttMenu;
    private MenuItem aboutMenuItem;
    private MenuItem logoutMenuItem;
    private Image avatar;
    private ImageView avatarViewer;
    private Label usernameDisplay;
    private Button messageButton;
    private Button myListingsButton;
    private Button profileButton;
    private Button logoutButton;
    private Button browseButton;
    private Button searchButton;
    private Button addItemButton;
    // End JavaFX variable declarations
    
    
    // Logical variable declarations
    public static String username;
    public static String ign;
    public static String timezone;
    public static File userImageFile;
    public static boolean connected;
    public static volatile boolean authenticated;
    public static AtomicBoolean syncInProgress;
    
    public static volatile ArrayList<String> currentChats;
    public static volatile ArrayList<String> userList;
    public static volatile ArrayList<String> onlineList;
    public static volatile ArrayList<Notification> notificationsList;
    // End logical variable declarations
    
    
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        TarkovTrader.connected = false;
        TarkovTrader.authenticated = false;
        syncInProgress = new AtomicBoolean(false);
        
        currentChats = new ArrayList<>();
        userList = new ArrayList<>();
        
        // Initialize Notification Manager
        startNotificationManager();
        
        // All resources are loaded into static fields one time and shared across the application
        Resources.load();
        
        // Networking is needed at launch for login or creating a new account
        startWorker();
        
        // On application start, bring up login prompt
        displayLogin();
        
        // Login prompt will call the main user interface to be drawn and shown
    }
    
    
    private void displayLogin()
    {
        mainLogin = new LoginPrompt(this, worker);
        mainLogin.display();
    }
    
    
    public void drawMainUI()
    {   
         // Load all images and initialize ImageViews
        loadResources();
        
        
        // Build menubar and menus
        menubar = new MenuBar();
        ttMenu = new Menu("Tarkov Trader");
        menubar.getMenus().addAll(ttMenu);
        
        aboutMenuItem = new MenuItem("About");
        aboutMenuItem.setOnAction(e -> new AboutStage());
        
        logoutMenuItem = new MenuItem("Logout");
        logoutMenuItem.setOnAction(e -> close());
        
        ttMenu.getItems().addAll(aboutMenuItem, logoutMenuItem);
        
        // Username display label setup
        usernameDisplay = new Label(username);
        usernameDisplay.getStyleClass().add("usernameDisplay");
        
        
        // Logo display setup
        HBox upperDisplayRight = new HBox();
        HBox.setHgrow(upperDisplayRight, Priority.ALWAYS);
        upperDisplayRight.setAlignment(Pos.CENTER_RIGHT);
        upperDisplayRight.getChildren().add(Resources.outlineLogoViewer);
        
        
        // Building upper display for avatar image, username, and HBox for logo
        HBox upperDisplay = new HBox(20);
        upperDisplay.setAlignment(Pos.CENTER); // This allows the username text label to be placed in between the top and bottom of the upper display HBox
        upperDisplay.setPadding(new Insets(10,0,10,10));
        upperDisplay.getStyleClass().add("hbox");
        upperDisplay.getChildren().addAll(avatarViewer, usernameDisplay, upperDisplayRight);
        
       
        // Building buttons
        messageButton = new Button("Messages");
        myListingsButton = new Button("My Listings");
        profileButton = new Button("Profile");
        logoutButton = new Button("Logout");
        browseButton = new Button("Browse All");
        searchButton = new Button("New Search");
        addItemButton = new Button("Add Item");
        
        
        // Set button graphics for left display
        messageButton.setGraphic(Resources.messagesIconViewer);
        
        profileButton.setGraphic(Resources.profileIconViewer);
        
        myListingsButton.setGraphic(Resources.myListingsIconViewer);
        
        logoutButton.setGraphic(Resources.exitIconViewer);
        
        
        // Set button graphics for main central display
        searchButton.setGraphic(Resources.searchIconViewer);
        searchButton.getStyleClass().add("centralDisplayButton");
        
        browseButton.setGraphic(Resources.browserIconViewer);
        browseButton.getStyleClass().add("centralDisplayButton");
        
        addItemButton.setGraphic(Resources.addIconViewer);
        addItemButton.getStyleClass().add("centralDisplayButton");
        
        
        // Set button logic
        logoutButton.setOnAction(e -> close());
        browseButton.setOnAction(e -> displayBrowser());
        addItemButton.setOnAction(e -> displayAddItemStage());
        searchButton.setOnAction(e -> displayNewSearch());
        messageButton.setOnAction(e -> displayMessenger());
        myListingsButton.setOnAction(e -> displayModerator());
        
        
        // Building left display
        VBox leftDisplay = new VBox(80);   // Old value 120
        leftDisplay.setAlignment(Pos.CENTER);
        leftDisplay.getStyleClass().add("vbox");
        leftDisplay.setPadding(new Insets(70,30,85,20));
        leftDisplay.getChildren().addAll(messageButton, myListingsButton, profileButton, logoutButton);
        
        
        // Building center display
        HBox centerDisplay = new HBox(60);
        centerDisplay.setPadding(new Insets(0, 70, 0, 70));
        centerDisplay.setAlignment(Pos.CENTER);
        centerDisplay.getChildren().addAll(browseButton, addItemButton, searchButton);
        
        
        // Build VBox for Nenu bar and upper HBox support
        VBox mainUpperDisplay = new VBox();
        mainUpperDisplay.getChildren().addAll(menubar, upperDisplay);
        
        
        // Building BorderPane for main UI structure
        BorderPane border = new BorderPane();
        border.setTop(mainUpperDisplay);
        border.setLeft(leftDisplay);
        border.setCenter(centerDisplay);
        
        
        mainUIscene = new Scene(border);
        mainUIscene.getStylesheets().add(this.getClass().getResource("veneno.css").toExternalForm());
        
        primaryStage.setOnCloseRequest(e -> close());
        primaryStage.setResizable(false);
        primaryStage.setScene(mainUIscene);
        primaryStage.setTitle("Tarkov Trader");
        primaryStage.getIcons().add(Resources.icon);
        primaryStage.show();
        
        if (!TarkovTrader.notificationsList.isEmpty())
        {
            notificationManager.processNotificationsList();
        }
    }
    
    
    public void displayDrawnUI()  // UNUSED BECAUSE OF STATIC SHARED RESOURCES, CHANGE LATER?
    {
        primaryStage.show();
    }
    
    
    private void displayBrowser()
    {
        browser = new Browser(this, worker);
        browser.display(false);
        primaryStage.close();
    }
    
    
    private void displayAddItemStage()
    {
        addItemStage = new AddItemStage(this, worker);
        addItemStage.display();
        primaryStage.close();
    }
    
    
    private void displayNewSearch()
    {
        browser = new Browser(this, worker);
        newSearch = new NewSearch(this, browser);
        newSearch.display();
        primaryStage.close();
    }
    
    
    public void displayMessenger()
    {
        messenger = new Messenger(this, worker);
        messenger.display();
    }
    
    
    private void displayModerator()
    {
        moderator = new Moderator(this, worker);
        moderator.display();
        primaryStage.close();
    }
    
    
    private void loadResources()
    {
        // In the main UI, the only resource to be loaded so far is the avatar (unique to each account)
        // All other resources are generic and can be loaded at launch in the Resources class 'load' method
        
        if (TarkovTrader.userImageFile != null)
        {
            try {
                // TESTING HERE
                //avatar = new Image(TarkovTrader.userImageFile.toURI().toString());
                avatar = new Image(new FileInputStream(TarkovTrader.userImageFile));
            } catch (FileNotFoundException ex) {
                Platform.runLater(() -> Alert.display(null, "Failed load avatar image."));
            }
        }
        else
            avatar = new Image(this.getClass().getResourceAsStream("/eftlogo.jpg"));
        
        avatarViewer = new ImageView(avatar); 
        avatarViewer.setPreserveRatio(false);
        avatarViewer.setFitHeight(128);
        avatarViewer.setFitWidth(180);
    }
    
    
    private void startWorker()
    {
        worker = new RequestWorker(this);
        workerThread = new Thread(worker);
        workerThread.start();
    }
    
    
    public Browser getBrowser()
    {
        return this.browser;
    }
    
    
    public Moderator getModerator()
    {
        return this.moderator;
    }
    
    
    public RequestWorker getWorker()
    {
        return this.worker;
    }
    
    
    public Messenger getMessenger()
    {
        return this.messenger;
    }
    
    
    public NotificationManager getNotificationManager()
    {
        return this.notificationManager;
    }
    
    
    public void setMessenger(Messenger messenger)
    {
        this.messenger = messenger;
    }
    
    
    public void startNotificationManager()
    {
        notificationManager = new NotificationManager(this);
    }
    
    
    public void setNewMessagesButton(boolean toFlag)
    {
        if (toFlag)
        {
            if (!messageButton.getStyleClass().contains("newMessageButton") && !Messenger.isOpen)
                messageButton.getStyleClass().add("newMessageButton");
        }
        else
        {
            if (messageButton.getStyleClass().contains("newMessageButton"));
                messageButton.getStyleClass().remove("newMessageButton");
        }
    }
    
    
    public void close()
    {
        worker.closeNetwork();
        //workerThread.stop();
        Platform.exit();
    }
}
