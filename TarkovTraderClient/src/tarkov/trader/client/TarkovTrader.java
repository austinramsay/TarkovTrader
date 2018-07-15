package tarkov.trader.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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
    private Thread workerThread;
    
    // JavaFX variable declarations
    private Stage primaryStage;
    private Scene mainUIscene;
    private MenuBar menubar;
    private Menu ttMenu;
    private Menu moreMenu;
    private MenuItem aboutMenuItem;
    private MenuItem logoutMenuItem;
    private MenuItem exitMenuItem;
    private Image avatar;
    private ImageView avatarViewer;
    private Label usernameDisplay;
    private Button messageButton;
    private Button profileButton;
    private Button logoutButton;
    private Button browseButton;
    private Button searchButton;
    private Button addItemButton;
    // End JavaFX variable declarations
    
    
    // Logical variable declarations
    private String username;
    public static boolean connected;
    public static volatile boolean authenticated;
    // End logical variable declarations
    
    
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        TarkovTrader.connected = false;
        TarkovTrader.authenticated = false;
        
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
        // NOTE: Avatar image must be 128x128 size
        // NOTE: BorderPane is main layout for UI
        primaryStage.setOnCloseRequest(e -> this.close());

        // Load all images and initialize ImageViews
        loadResources();
        
        
        // Build menubar and menus
        menubar = new MenuBar();
        ttMenu = new Menu("Tarkov Trader");
        moreMenu = new Menu("More");
        menubar.getMenus().addAll(ttMenu, moreMenu);
        
        aboutMenuItem = new MenuItem("About");
        logoutMenuItem = new MenuItem("Logout");
        exitMenuItem = new MenuItem("Exit");
        
        ttMenu.getItems().addAll(aboutMenuItem, logoutMenuItem, exitMenuItem);
        
        // Username display label setup
        usernameDisplay = new Label(username);
        usernameDisplay.getStyleClass().add("usernameDisplay");
        
        
        // Logo display setup
        HBox.setMargin(Resources.outlineLogoViewer, new Insets(10,10,10,350)); // This allows the logo to be pushed to the far right in the upper display
        
        
        // Building upper display for avatar image, username, and logo
        HBox upperDisplay = new HBox();
        upperDisplay.setAlignment(Pos.CENTER); // This allows the username text label to be placed in between the top and bottom of the upper display HBox
        upperDisplay.getStyleClass().add("hbox");
        upperDisplay.getChildren().addAll(avatarViewer, usernameDisplay, Resources.outlineLogoViewer);
        
       
        // Building buttons
        messageButton = new Button("Messages");
        profileButton = new Button("Profile");
        logoutButton = new Button("Logout");
        browseButton = new Button("Browse All");
        searchButton = new Button("New Search");
        addItemButton = new Button("Add Item");
        
        
        // Set button graphics for left display
        messageButton.setGraphic(Resources.messagesIconViewer);
        
        profileButton.setGraphic(Resources.profileIconViewer);
        
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
        
        
        // Building left display
        VBox leftDisplay = new VBox(120);
        leftDisplay.setAlignment(Pos.CENTER);
        leftDisplay.getStyleClass().add("vbox");
        leftDisplay.setPadding(new Insets(70,30,85,20));
        leftDisplay.getChildren().addAll(messageButton, profileButton, logoutButton);
        
        
        // Building center display
        HBox centerDisplay = new HBox(50);
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
        
        primaryStage.setResizable(false);
        primaryStage.setScene(mainUIscene);
        primaryStage.getIcons().add(Resources.icon);
        primaryStage.show();
    }
    
    
    public void displayDrawnUI()  // UNUSED BECAUSE OF STATIC SHARED RESOURCES, CHANGE LATER?
    {
        primaryStage.show();
    }
    
    
    private void displayBrowser()
    {
        browser = new Browser(this, worker);
        browser.display();
        primaryStage.close();
    }
    
    
    private void displayAddItemStage()
    {
        addItemStage = new AddItemStage(this, worker);
        addItemStage.display();
        primaryStage.close();
    }
    
    
    private void loadResources()
    {
        // In the main UI, the only resource to be loaded so far is the avatar (unique to each account)
        // All other resources are generic and can be loaded at launch in the Resources class 'load' method
                   
        avatar = new Image(this.getClass().getResourceAsStream("/testavatar.png"));
        avatarViewer = new ImageView(avatar); 
    }
    
    
    public void setUsername(String username)
    {
        // Should only be called from the login prompt after a successful login
        this.username = username;
    }
    
    
    public String getUsername()
    {
        return this.username;
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
    
    
    public void close()
    {
        worker.closeNetwork();
        workerThread.stop();
        Platform.exit();
    }
}
