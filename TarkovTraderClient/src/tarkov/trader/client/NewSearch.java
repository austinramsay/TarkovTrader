
package tarkov.trader.client;

import java.util.HashMap;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author austin
 */

public class NewSearch {
    
    private TarkovTrader trader;
    private Browser browser;
    
    private Stage searchStage;
    private Scene scene;
    
    private Label searchTipLabel;
    private Label postTypeLabel;
    private Label itemTypeLabel;
    private Label itemNameLabel;
    private Label priceRangeLabel;
    private Label ignLabel;
    private Label usernameLabel;
    private Label timezoneLabel;
    private Label keywordLabel;
    
    private ComboBox<String> postTypeDropdown;
    private ComboBox<String> itemTypeDropdown;
    private ComboBox<String> timezoneDropdown;
    
    private TextField itemNameInput;
    private TextField priceMinInput;
    private TextField priceMaxInput;
    private TextField usernameInput;
    private TextField ignInput;
    private TextField keywordInput;
    
    private Button search;
    private Button cancel;
    
    
    public NewSearch(TarkovTrader trader, Browser browser)
    {
        this.trader = trader;
        this.browser = browser;
    }
    
    
    public void display()
    {
        searchStage = new Stage();
        
        searchTipLabel = new Label("Not all input fields are necessary.");
        searchTipLabel.setStyle("-fx-underline: true");
        
        postTypeLabel = new Label("Listing type:");
        
        itemTypeLabel = new Label("Type of item:");
        
        itemNameLabel = new Label("Name of item:");
        
        priceRangeLabel = new Label("Price range:");
        
        ignLabel = new Label("Seller's IGN:");
        
        usernameLabel = new Label("Seller username:");
        
        timezoneLabel = new Label("Timezone:");
        
        keywordLabel = new Label("Search Keyword:");
        
        postTypeDropdown = new ComboBox<>();
        postTypeDropdown.getItems().addAll("All", "WTS", "WTB");
        postTypeDropdown.setPromptText("Trade Status");  // WTS is default value
        
        itemTypeDropdown = new ComboBox<>();
        itemTypeDropdown.getItems().addAll("All", "Key", "Secure Container", "Weapon", "Weapon Mod", "Armor/Helmet", "Apparel", "Ammo", "Medicine", "Misc");
        itemTypeDropdown.setPromptText("Item Type");
        itemTypeDropdown.setMinWidth(200);
        
        itemNameInput = new TextField();
        itemNameInput.setPromptText("Name");
        
        priceMinInput = new TextField();
        priceMinInput.setPromptText("Minimum");
        
        priceMaxInput = new TextField();
        priceMaxInput.setPromptText("Maximum");
        
        usernameInput = new TextField();
        usernameInput.setPromptText("Seller username");
        
        ignInput = new TextField();
        ignInput.setPromptText("Seller IGN");
        
        timezoneDropdown = new ComboBox();
        timezoneDropdown.setEditable(true);
        timezoneDropdown.getItems().addAll("PST", "MST", "CST", "EST");
        timezoneDropdown.setPromptText("Select or Type Here");
        
        keywordInput = new TextField();
        keywordInput.setPromptText("Ex. Interchange");
        
        search = new Button("Search");
        search.setOnAction(e -> submit());
        
        cancel = new Button("Cancel");
        cancel.setOnAction(e -> close());
        
        
        // Layout construction
        
        // Upper display will house the logo
        VBox upperDisplay = new VBox(15);
        upperDisplay.setAlignment(Pos.CENTER);
        upperDisplay.getChildren().addAll(Resources.logoViewer, searchTipLabel);
        
        
        // Set positions for all objects
        GridPane.setConstraints(postTypeLabel, 0, 0);
        GridPane.setConstraints(itemTypeLabel, 0, 1);
        GridPane.setConstraints(itemNameLabel, 0, 2);
        GridPane.setConstraints(priceRangeLabel, 0, 3);
        GridPane.setConstraints(usernameLabel, 0, 5);
        GridPane.setConstraints(ignLabel, 0, 6);
        GridPane.setConstraints(timezoneLabel, 0, 7);
        GridPane.setConstraints(keywordLabel, 0, 8);
        GridPane.setConstraints(postTypeDropdown, 1, 0);
        GridPane.setConstraints(itemTypeDropdown, 1, 1);
        GridPane.setConstraints(itemNameInput, 1, 2);
        GridPane.setConstraints(priceMinInput, 1, 3);
        GridPane.setConstraints(priceMaxInput, 1, 4);
        GridPane.setConstraints(usernameInput, 1, 5);
        GridPane.setConstraints(ignInput, 1, 6);
        GridPane.setConstraints(timezoneDropdown, 1, 7);
        GridPane.setConstraints(keywordInput, 1, 8);
        
        
        // GridPane displays input fields and labels
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15, 10, 10, 10));
        grid.setVgap(13); // Sets the vertical gap between grid cells
        grid.setHgap(15); // Sets the horizontal gap between grid cells
        grid.getChildren().addAll(
                postTypeLabel, 
                itemTypeLabel, 
                itemNameLabel, 
                priceRangeLabel, 
                usernameLabel, 
                ignLabel, 
                timezoneLabel,
                keywordLabel, 
                postTypeDropdown, 
                itemTypeDropdown,
                itemNameInput, 
                priceMinInput, 
                priceMaxInput, 
                usernameInput,
                ignInput,
                timezoneDropdown, 
                keywordInput);
        
        
        // Displays 'Create' and 'Cancel' buttons
        HBox lowerDisplay = new HBox(25);
        lowerDisplay.setPadding(new Insets(8,0,13,0));
        lowerDisplay.setAlignment(Pos.CENTER);
        lowerDisplay.getChildren().addAll(search, cancel);
        
        
        BorderPane border = new BorderPane();
        border.setTop(upperDisplay);
        border.setCenter(grid);
        border.setBottom(lowerDisplay);
        
        
        scene = new Scene(border);
        scene.getStylesheets().add(this.getClass().getResource("veneno.css").toExternalForm());
        searchStage.setTitle("Start New Search");
        searchStage.getIcons().add(Resources.icon);
        searchStage.setScene(scene);
        searchStage.setResizable(false);
        searchStage.setOnCloseRequest(e -> close());
        searchStage.show();        
    }
    
    
    private void submit()
    {
        browser.setSearchFlags(buildSearchFlags());
        browser.display(true);
        searchStage.close();
    }
    
    
    private HashMap<String, String> buildSearchFlags()
    {
        // Trade status, Type, and Price filters
        
        HashMap<String, String> searchFlags = new HashMap();
        
        String priceMin = priceMinInput.getText();
        String priceMax = priceMaxInput.getText();
        
        String nameFlag = itemNameInput.getText();
        String ignFlag = ignInput.getText();
        String usernameFlag = usernameInput.getText();
        String keywordsFlag = keywordInput.getText();
        
        String definedStatusFlag;
        String definedTypeFlag;
        String definedNameFlag;
        String definedIgnFlag;
        String definedUsernameFlag;
        String definedTimezoneFlag;
        String definedKeywordsFlag;
        
        
        if (priceMin.equals(""))
            priceMin = "0";
        if (priceMax.equals(""))
            priceMax = "50000000";
        
        
        if (postTypeDropdown.getSelectionModel().isEmpty() || postTypeDropdown.getSelectionModel().getSelectedIndex() == 0)
            definedStatusFlag = "%";
        else
            definedStatusFlag = postTypeDropdown.getSelectionModel().getSelectedItem();
        
        
        if (itemTypeDropdown.getSelectionModel().isEmpty() || itemTypeDropdown.getSelectionModel().getSelectedIndex() == 0)
            definedTypeFlag = "%";
        else
            definedTypeFlag = itemTypeDropdown.getSelectionModel().getSelectedItem();
        
        
        if (timezoneDropdown.getSelectionModel().isEmpty())
            definedTimezoneFlag = "%";
        else
            definedTimezoneFlag = timezoneDropdown.getSelectionModel().getSelectedItem();

        
        if (nameFlag.equals(""))
            definedNameFlag = "%";
        else
            definedNameFlag = "%" + nameFlag + "%";
        
        
        if (ignFlag.equals(""))
            definedIgnFlag = "%";
        else 
            definedIgnFlag = "%" + ignFlag + "%";
        
        
        if (usernameFlag.equals(""))
            definedUsernameFlag = "%";
        else
            definedUsernameFlag = "%" + usernameFlag + "%";
        
        
        if (keywordsFlag.equals(""))
            definedKeywordsFlag = "%";
        else
            definedKeywordsFlag = "%" + keywordsFlag + "%";
        
        
        searchFlags.put("itemid", "%");
        searchFlags.put("state", definedStatusFlag);
        searchFlags.put("type", definedTypeFlag);
        searchFlags.put("name", definedNameFlag);
        searchFlags.put("pricemin", priceMin);
        searchFlags.put("pricemax", priceMax);
        searchFlags.put("ign", definedIgnFlag);
        searchFlags.put("username", definedUsernameFlag);
        searchFlags.put("timezone", definedTimezoneFlag);
        searchFlags.put("keywords", definedKeywordsFlag);
        System.out.println(searchFlags.toString());
        return searchFlags;
    }
    
    
    private void close()
    {
        searchStage.close();
        trader.drawMainUI();
    }
    
    
}
