package tarkov.trader.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
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
import tarkov.trader.objects.Profile;
import tarkov.trader.objects.ProfileRequest;

/**
 *
 * @author austin
 * NOTES: Profile avatars must be under 65kb large (will also be downsized to 128x128)
 * NOTES: All fields specified in creating a new account must be 16 characters or less (besides IGN which is 24 chars)
 */

public class TarkovTrader extends Application {
    
    // Object declarations
    private RequestWorker worker;
    private Resources resourceLoader;
    public LoginPrompt mainLogin;
    private Browser browser;
    private AddItemStage addItemStage;
    private NewSearch newSearch;
    private Messenger messenger;
    private NotificationManager notificationManager;
    private Moderator moderator;
    private ProfileDisplay profiler;
    private Thread workerThread;
    private LoadingAnimator loadingPrompt;
    
    // JavaFX variable declarations
    private Stage primaryStage;
    private Scene mainUIscene;
    private MenuBar menubar;
    private Menu ttMenu;
    private MenuItem aboutMenuItem;
    private MenuItem toggleAutoLoginMenuItem;
    private MenuItem logoutMenuItem;
    private Image avatar;
    private ImageView avatarViewer;
    private Image outlineLogo;
    private ImageView outlineLogoViewer;
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
    public static boolean connected;
    public static volatile boolean authenticated;
    public static AtomicBoolean syncInProgress;  
    
    public static volatile ArrayList<String> currentChats;
    public static volatile ArrayList<String> userList;
    public static volatile ArrayList<String> onlineList;
    public static volatile ArrayList<Notification> notificationsList;
    public static HashMap<Preferences, String> userPrefs;
    // End logical variable declarations
    
    // File variables
    private static final File PREFS_FILE = new File(System.getProperty("user.home") + "/.tarkovtrader_prefs");
    public static File userImageFile;
    
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        TarkovTrader.connected = false;
        TarkovTrader.authenticated = false;
        syncInProgress = new AtomicBoolean(false);
        
        currentChats = new ArrayList<>();
        userList = new ArrayList<>();
        
        // Load the preferences file information into the userPrefs HashMap
        loadPrefs();
        
        // Initialize Notification Manager
        startNotificationManager();
        
        // Initialize resource loader and the loading prompt
        resourceLoader = new Resources();
        loadingPrompt = new LoadingAnimator();
        
        // Networking is needed at launch for login or creating a new account
        startWorker();
        
        // On application start, bring up login prompt
        displayLogin();
        
        // Login prompt will call the main user interface to be drawn and shown
    }
    
    
    private void loadPrefs()
    {
        ObjectInputStream ois = null;
        
        try 
        {
            if (!TarkovTrader.PREFS_FILE.exists())
            {
                createNewPrefsFile();
            }
            
            ois = new ObjectInputStream(new FileInputStream(TarkovTrader.PREFS_FILE));
            userPrefs = (HashMap)ois.readObject();
        }
        catch (ClassNotFoundException e)
        {
            Alert.display(null, "Failed to cast preferences.");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            Alert.display(null, "Failed to load preferences.");
            e.printStackTrace();
        }
        finally 
        {
            try {
                if (ois != null)
                    ois.close();
            } catch(IOException e) {  e.printStackTrace();  }
        }        
    }
    
    
    private void createNewPrefsFile()
    {
        ObjectOutputStream oos = null;
        
        try 
        {
            TarkovTrader.PREFS_FILE.createNewFile();
            oos = new ObjectOutputStream(new FileOutputStream(TarkovTrader.PREFS_FILE));
            HashMap<Preferences, String> newPrefsMap = new HashMap<>();
            newPrefsMap.put(Preferences.AUTO_LOGIN, "off");
            newPrefsMap.put(Preferences.REMEMBER_ME, "off");
            newPrefsMap.put(Preferences.USERNAME, "");
            newPrefsMap.put(Preferences.PASSWORD, "");
            oos.writeObject(newPrefsMap);
            Platform.runLater(() -> Alert.display("First Launch", "Created new preferences file."));
        }
        catch (IOException e)
        {
            Alert.display(null, "Failed to create preferences file.");
            e.printStackTrace();
        }
        finally 
        {
            try {
                if (oos != null)
                    oos.close();
            } catch(IOException e) {  e.printStackTrace();  }
        }        
    }
    
    
    public static void updatePrefs(Preferences prefs, String value)
    {
        ObjectOutputStream oos = null;
        
        try 
        {
            oos = new ObjectOutputStream(new FileOutputStream(TarkovTrader.PREFS_FILE));
            if (TarkovTrader.userPrefs.containsKey(prefs))
                userPrefs.replace(prefs, userPrefs.get(prefs), value);
            
            oos.writeObject(userPrefs);
        }
        catch (IOException e)
        {
            Alert.display(null, "Failed to create edit preferences file.");
            e.printStackTrace();
        }        
        finally 
        {
            try {
                if (oos != null)
                    oos.close();
            } catch(IOException e) {  e.printStackTrace();  }
        }
    }
    
    
    private void displayLogin()
    {
        mainLogin = new LoginPrompt(this);
        mainLogin.display();
    }
    
    
    public void drawMainUI()
    {   
        mainLogin = null;
        
         // Load all images and initialize ImageViews
        loadResources();
        
        
        // Build menubar and menus
        menubar = new MenuBar();
        ttMenu = new Menu("Tarkov Trader");
        menubar.getMenus().addAll(ttMenu);
        
        aboutMenuItem = new MenuItem("About");
        aboutMenuItem.setOnAction(e -> new AboutStage());
        
        toggleAutoLoginMenuItem = new MenuItem("Disable Auto Login");
        if (TarkovTrader.userPrefs.get(Preferences.AUTO_LOGIN).equals("off"))
            toggleAutoLoginMenuItem.setDisable(true);
        toggleAutoLoginMenuItem.setOnAction(e -> {
            TarkovTrader.updatePrefs(Preferences.AUTO_LOGIN, "off");
            toggleAutoLoginMenuItem.setDisable(true);
        });
        
        logoutMenuItem = new MenuItem("Logout");
        logoutMenuItem.setOnAction(e -> close());
        
        ttMenu.getItems().addAll(aboutMenuItem, toggleAutoLoginMenuItem, logoutMenuItem);
        
        
        // Username display label setup
        usernameDisplay = new Label(username);
        usernameDisplay.getStyleClass().add("usernameDisplay");
        
        
        // Logo display setup
        HBox upperDisplayRight = new HBox();
        HBox.setHgrow(upperDisplayRight, Priority.ALWAYS);
        upperDisplayRight.setAlignment(Pos.CENTER_RIGHT);
        upperDisplayRight.getChildren().add(outlineLogoViewer);
        
        
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
        messageButton.setGraphic(resourceLoader.getMessagesIcon());
        
        profileButton.setGraphic(resourceLoader.getProfileIcon());
        
        myListingsButton.setGraphic(resourceLoader.getListingsIcon());
        
        logoutButton.setGraphic(resourceLoader.getExitIcon());
        
        
        // Set button graphics for main central display
        searchButton.setGraphic(resourceLoader.getSearchIcon());
        searchButton.getStyleClass().add("centralDisplayButton");
        
        browseButton.setGraphic(resourceLoader.getBrowserIcon());
        browseButton.getStyleClass().add("centralDisplayButton");
        
        addItemButton.setGraphic(resourceLoader.getAddIcon());
        addItemButton.getStyleClass().add("centralDisplayButton");
        
        
        // Set button logic
        logoutButton.setOnAction(e -> close());
        browseButton.setOnAction(e -> displayBrowser());
        addItemButton.setOnAction(e -> displayAddItemStage());
        searchButton.setOnAction(e -> displayNewSearch());
        messageButton.setOnAction(e -> displayMessenger());
        myListingsButton.setOnAction(e -> displayModerator());
        profileButton.setOnAction(e -> requestProfile(TarkovTrader.username));
        
        
        // Building left display
        VBox leftDisplay = new VBox(80);   
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
        primaryStage.getIcons().add(resourceLoader.getIcon());
        primaryStage.show();
        
        if (!TarkovTrader.notificationsList.isEmpty())
        {
            notificationManager.processNotificationsList();
        }
    }
    
    
    private void displayBrowser()
    {
        browser = new Browser(this);
        browser.display(false);
        primaryStage.close();
    }
    
    
    private void displayAddItemStage()
    {
        addItemStage = new AddItemStage(this);
        addItemStage.display();
        primaryStage.close();
    }
    
    
    private void displayNewSearch()
    {
        browser = new Browser(this);
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
        moderator = new Moderator(this);
        moderator.display();
        primaryStage.close();
    }
    
    
    public void requestProfile(String username)
    {
        loadingPrompt.display();
        
        ProfileRequest profileRequest = new ProfileRequest(TarkovTrader.username);
        System.out.println(profileRequest.getUsername() + TarkovTrader.username);
        
        worker.sendForm(profileRequest);
    }
    
    
    public void displayProfile(Profile profile, boolean allowEdit)
    {       
        profiler = new ProfileDisplay(this, profile, allowEdit);
    }
    
    
    private void loadResources()
    {
        outlineLogo = new Image(this.getClass().getResourceAsStream("/tarkovtraderlogooutline.png"), 362, 116, true, true);
        outlineLogoViewer = new ImageView(outlineLogo);
        
        if (TarkovTrader.userImageFile != null)
        {
            try {
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
    
    
    public LoadingAnimator getLoadingPrompt()
    {
        return this.loadingPrompt;
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
        workerThread.stop();
        Platform.exit();
    }
}
