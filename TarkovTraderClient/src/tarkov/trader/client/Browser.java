
package tarkov.trader.client;

import java.util.ArrayList;
import java.util.HashMap;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tarkov.trader.objects.Item;
import tarkov.trader.objects.ItemListForm;
import tarkov.trader.objects.ProcessedItem;

/**
 *
 * @author austin
 */

public class Browser {
    
    private TarkovTrader trader;
    private RequestWorker worker;
    
    private Stage browser;
    private Label browserLabel;
    private Label filterLabel;
    private Button refreshButton;
    private Button returnButton;
    private TableView table;
    private MenuBar menubar;
    private Menu newSearch;
    private ComboBox<String> typeFilter;
    private ComboBox<String> tradeStatusFilter;
    private ComboBox<String> priceRangeFilter;
    
    TableColumn<Item, ImageView> imageColumn;
    TableColumn<Item, String> tradeStateColumn;
    TableColumn<Item, String> typeColumn;
    TableColumn<Item, String> nameColumn;
    TableColumn<Item, String> priceColumn;
    TableColumn<Item, String> ignColumn;
    TableColumn<Item, String> timezoneColumn;
    
    private HashMap<String, String> searchFlags;   // This should only be used for a direct search called by the search feature. It should be set using 'setSearchFlags' prior to calling 'display(true)'
    
    private boolean isPopulated;
    private boolean pullSearchFlags = false;
    
    
    public Browser(TarkovTrader trader, RequestWorker worker)
    {
        this.trader = trader;
        this.worker = worker;
        
        this.isPopulated = false;
    }
    
    
    public void display(boolean pullSearchFlags)   // Browser is used by the search feature, If the browser is being used for a direct search, the 'setSearchFlags' method should be called first, and this should be set true
    {
        this.pullSearchFlags = pullSearchFlags;
        
        browser = new Stage();
        browser.setOnCloseRequest(e -> close());  // For now, only closes the browser stage and reopens the main trader user interface
        
        // Build MenuBar
        menubar = new MenuBar();
        newSearch = new Menu("Start a New Search");  // TODO: Implement setOnAction when search feature is implemented
        menubar.getMenus().addAll(newSearch);
        
        // Build 'Item Browser' label displayed on top left
        browserLabel = new Label("Item Browser");  // Could use a graphics label instead at some point
        browserLabel.getStyleClass().add("windowLabel");
        
        // Build 'Filters:' label displayed on top of filter selection VBox
        filterLabel = new Label("Filters:");
        filterLabel.setPadding(new Insets(20,0,0,0));  // Needed a small extra push downward
        filterLabel.getStyleClass().add("subWindowLabel");
        
        
        // Note: The options in these filter lists directly affect the performance of requesting an item list
        // Note: The options string values are directly pulled from this dropdown upon submission of request and are put into the item request form object to be processed by the server
        tradeStatusFilter = new ComboBox<>();
        tradeStatusFilter.setPromptText("Trade Status");
        tradeStatusFilter.getItems().addAll("All", "WTS", "WTB");
        tradeStatusFilter.setOnAction(e -> requestItemList(buildFilterFlags()));
        
      
        // Note: Filter list. See note above
        typeFilter = new ComboBox<>();
        typeFilter.setPromptText("Item Type");
        typeFilter.getItems().addAll("All", "Key", "Secure Container", "Weapon", "Weapon Mod", "Armor/Helmet", "Apparel", "Ammo", "Medicine", "Misc");
        typeFilter.setOnAction(e -> requestItemList(buildFilterFlags()));

        
        // Note: Filter list. See note above
        priceRangeFilter = new ComboBox<>();
        priceRangeFilter.setPromptText("Price Range");
        priceRangeFilter.getItems().addAll("All", "1 - 50,000", "50,000 - 100,000", "100,000 - 200,000", "200,000 - 300,000", "300,000+");
        priceRangeFilter.setOnAction(e -> requestItemList(buildFilterFlags()));
        
        
        // Build buttons on bottom of filter selection VBox
        refreshButton = new Button("Refresh");
        refreshButton.setGraphic(Resources.refreshIconViewer);
        refreshButton.setOnAction(e -> requestItemList(buildFilterFlags()));
        
        returnButton = new Button("Return");
        returnButton.setGraphic(Resources.cancelIconViewer);
        returnButton.setOnAction(e -> close());
        
        
        // Upper display will consist of two seperate HBoxes
        // The left HBox will only house the 'Item Browser' label
        // The right HBox will be set to grow horizontally with priority, and align to the center-right. This will display the 'Tarkov Trader' logo
        HBox upperDisplayLeft = new HBox();
        HBox upperDisplayRight = new HBox();
        upperDisplayLeft.setPadding(new Insets(0,10,0,10));
        upperDisplayLeft.setAlignment(Pos.CENTER);
        upperDisplayRight.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(upperDisplayRight, Priority.ALWAYS);
        browserLabel.setPadding(new Insets(0,0,0,20));
        upperDisplayRight.getChildren().add(Resources.outlineLogoViewer);
        upperDisplayLeft.getChildren().addAll(browserLabel, upperDisplayRight);
        upperDisplayLeft.getStyleClass().add("hbox");
        
        
        // This VBox will house the menu bar and the 2 HBoxes built for the 'Item Browser' label and the 'Tarkov Trader' logo
        VBox mainUpperDisplay = new VBox();
        mainUpperDisplay.getChildren().addAll(menubar, upperDisplayLeft);
        
        
        // This VBox will house all filter related nodes
        VBox leftDisplay = new VBox(40);
        leftDisplay.setPadding(new Insets(0,20,20,20));
        leftDisplay.getStyleClass().add("vbox");
        leftDisplay.setAlignment(Pos.CENTER);
        leftDisplay.getChildren().addAll(filterLabel, tradeStatusFilter, typeFilter, priceRangeFilter, refreshButton, returnButton);
        
        
        // Build the TableView
        imageColumn = new TableColumn<>();
        imageColumn.setMaxWidth(50);
        imageColumn.setMinWidth(50);
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("itemTypeImage"));  // Calls getItemTypeImage() in ProcessedItem object -- Generic item type icon stored client-side
        
        typeColumn = new TableColumn<>("Type"); 
        typeColumn.setMaxWidth(130);
        typeColumn.setMinWidth(130);
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("itemType"));  // Calls getItemType() in ProcessedItem object
        
        tradeStateColumn = new TableColumn<>("State");
        tradeStateColumn.setMaxWidth(50);
        tradeStateColumn.setCellValueFactory(new PropertyValueFactory<>("tradeState"));  // Calls getTradeState() in ProcessedItem object
        
        nameColumn = new TableColumn<>("Item Name");
        nameColumn.setMaxWidth(275);
        nameColumn.setMinWidth(275);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));  // Calls getName() in ProcessedItem object
        
        priceColumn = new TableColumn<>("Price");
        priceColumn.setMaxWidth(100);
        priceColumn.setMinWidth(100);
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));  // Calls getPrice() in ProcessedItem object
        
        ignColumn = new TableColumn<>("In-game Name");
        ignColumn.setMaxWidth(220);
        ignColumn.setMinWidth(220);
        ignColumn.setCellValueFactory(new PropertyValueFactory<>("ign"));  // Calls getIgn() in ProcessedItem object
        
        timezoneColumn = new TableColumn<>("Timezone");
        timezoneColumn.setMaxWidth(100);
        timezoneColumn.setMinWidth(100);
        timezoneColumn.setCellValueFactory(new PropertyValueFactory<>("timezone"));  // Calls getTimezone() in ProcessedItem object
        
        table = new TableView<>();
        table.getColumns().addAll(imageColumn, typeColumn, tradeStateColumn, nameColumn, priceColumn, ignColumn, timezoneColumn);
        
        table.setOnMousePressed(e -> displaySelectedItem(e));  // Checks for 2 mouse clicks and opens new window with item information using ProcessedItem object
        // End building TableView
 
        
        // Border pane layout: Uses top, left, and center sections
        BorderPane border = new BorderPane();
        border.setTop(mainUpperDisplay);
        border.setLeft(leftDisplay);
        border.setCenter(table);
        
        
        // Build the scene and set stage properties
        Scene scene = new Scene(border);
        scene.getStylesheets().add(this.getClass().getResource("veneno.css").toExternalForm());
        browser.setTitle("Item Browser");
        browser.getIcons().add(Resources.icon);
        browser.setResizable(false);
        browser.setScene(scene);
        
        
        // If the browser is performing a specified search, filters are not necessary as they are already specified in the search window
        // We can disable all nodes that control filtering since this is already done
        if (pullSearchFlags)
        {
            tradeStatusFilter.setDisable(true);
            typeFilter.setDisable(true);
            priceRangeFilter.setDisable(true);
            if(requestItemList(getSearchFlags()))
                this.isPopulated = true;
            else
                this.isPopulated = false;
        }
        
        // If the browser has not been populated, do so now
        if (!isPopulated)  
        {
            tradeStatusFilter.setDisable(false);
            typeFilter.setDisable(false);
            priceRangeFilter.setDisable(false);
            if (requestItemList(buildFilterFlags()))
                this.isPopulated = true;
        }
        
        browser.show();
    }
    
    
    private void displaySelectedItem(MouseEvent e)
    {
        if (e.getClickCount() == 2)
        {
            ItemDisplay itemdisplay = new ItemDisplay((ProcessedItem)table.getSelectionModel().getSelectedItem());
        }
    }
    
    
    public void populate(ArrayList<ProcessedItem> itemList)
    {
        // Calls getItemList to retrieve an OberservableList of processed items for CellValueFactory to fetch data
        table.setItems(getItemList(itemList));
    }
    
    
    private ObservableList<ProcessedItem> getItemList(ArrayList<ProcessedItem> itemList)
    {
        return FXCollections.observableArrayList(itemList);
    }
    
    
    private void submitRequest()
    {
        if (pullSearchFlags)
            requestItemList(getSearchFlags());
        else
            requestItemList(buildFilterFlags());
    }
    
    
    private boolean requestItemList(HashMap<String, String> flagMap)
    {
        ItemListForm listrequest = new ItemListForm(flagMap);
        
        // use worker to send, wait for server response, table will populate upon receiving list
        if (!worker.sendForm(listrequest))
        {
            Platform.runLater(() -> Alert.display("Request Failed", "Failed to send item list request to server."));
            return false;
        }
        
        return true;
    }
    
    
    private HashMap<String, String> buildFilterFlags()
    {
        // Trade status, Type, and Price filters
        
        HashMap<String, String> filterFlags = new HashMap();
        
        String statusFlag;
        String typeFlag;
        String priceFlag;
 
        String definedStatusFlag;
        String definedTypeFlag;
        String definedPriceFlag;
        
        String priceMin;
        String priceMax;
        
        
        if (tradeStatusFilter.getSelectionModel().isEmpty())
            statusFlag = "All";
        else
            statusFlag = tradeStatusFilter.getSelectionModel().getSelectedItem();
        
        
        if (typeFilter.getSelectionModel().isEmpty())
            typeFlag = "All";
        else
            typeFlag = typeFilter.getSelectionModel().getSelectedItem();
        
        
        if (priceRangeFilter.getSelectionModel().isEmpty())
            priceFlag = "All";
        else 
            priceFlag = priceRangeFilter.getSelectionModel().getSelectedItem();

        
        switch(statusFlag)
        {
            case "All":
                definedStatusFlag = "%";
                priceRangeFilter.setDisable(false);
                break;
            case "WTB":
                definedStatusFlag = "WTB";
                priceFlag = "All";
                priceRangeFilter.setDisable(true);
                break;
            default:
                definedStatusFlag = statusFlag;
                priceRangeFilter.setDisable(false);
                break;
        }
        
        
        switch(typeFlag)
        {
            case "All":
                definedTypeFlag = "%";
                break;
            default:
                definedTypeFlag = typeFlag;
                break;
        }
        
        
        switch(priceFlag)
        {
            case "1 - 50,000":
                priceMin = "1";
                priceMax = "50000";
                break;
            case "50,000 - 100,000":
                priceMin = "50000";
                priceMax = "100000";
                break;
            case "100,000 - 200,000":
                priceMin = "100000";
                priceMax = "200000";
                break;
            case "200,000 - 300,000":
                priceMin = "200000";
                priceMax = "300000";
                break;
            default:  // Catches "All" flag  --  Also used by the WTB flag. WTB flag sets Price flag to "All" or else WTB posts will not return in the query results.
                priceMin = "0";
                priceMax = "50000000";
        }
        
        
        filterFlags.put("itemid", "%");
        filterFlags.put("state", definedStatusFlag);
        filterFlags.put("type", definedTypeFlag);
        filterFlags.put("name", "%");
        filterFlags.put("pricemin", priceMin);
        filterFlags.put("pricemax", priceMax);
        filterFlags.put("ign", "%");
        filterFlags.put("username", "%");
        filterFlags.put("timezone", "%");
        filterFlags.put("keywords", "%");
        
        return filterFlags;
    }
    
    
    public void setSearchFlags(HashMap<String, String> searchFlags)
    {
        this.searchFlags = searchFlags;
    }
    
    
    private HashMap<String, String> getSearchFlags()
    {
        return this.searchFlags;
    }
    
    
    private void close()
    {
        browser.close();
        trader.drawMainUI();
    }
}