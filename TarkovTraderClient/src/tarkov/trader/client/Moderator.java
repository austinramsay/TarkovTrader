
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tarkov.trader.objects.Item;
import tarkov.trader.objects.ItemListForm;
import tarkov.trader.objects.ItemModificationRequest;
import tarkov.trader.objects.ProcessedItem;

/**
 *
 * @author austin
 */

public class Moderator {
    
    private TarkovTrader trader;
    private RequestWorker worker;
    
    private Stage moderator;
    private Label moderatorLabel;
    private Label filterLabel;
    private Label actionsLabel;
    private Button clearFiltersButton;
    private Button editButton;
    private Button deleteButton;
    private Button suspendButton;
    private Button refreshButton;
    private Button returnButton;
    private TableView table;
    private ComboBox<String> typeFilter;
    private ComboBox<String> tradeStatusFilter;
    private ComboBox<String> priceRangeFilter;
    
    private Image minusImage;
    private Image editImage;
    private Image deleteImage;
    private Image suspendImage;
    private ImageView minusViewer;
    private ImageView editViewer;
    private ImageView deleteViewer;
    private ImageView suspendViewer;
    
    TableColumn<Item, ImageView> imageColumn;
    TableColumn<Item, String> tradeStateColumn;
    TableColumn<Item, String> typeColumn;
    TableColumn<Item, String> nameColumn;
    TableColumn<Item, String> priceColumn;
    
    private boolean isPopulated;
    
    private final String PRICE_MAX = "50000000";
    
    
    public Moderator(TarkovTrader trader, RequestWorker worker)
    {
        this.trader = trader;
        this.worker = worker;
        
        this.isPopulated = false;
        
        loadResources();
    }
    
    
    private void loadResources()
    {
        minusImage = new Image(this.getClass().getResourceAsStream("/minus.png"), 32, 32, true, true);
        minusViewer = new ImageView(minusImage);   
        
        deleteImage = new Image(this.getClass().getResourceAsStream("/delete.png"), 32, 32, true, true);
        deleteViewer = new ImageView(deleteImage); 
        
        editImage = new Image(this.getClass().getResourceAsStream("/edit.png"), 32, 32, true, true);
        editViewer = new ImageView(editImage); 
        
        suspendImage = new Image(this.getClass().getResourceAsStream("/suspend.png"), 32, 32, true, true);
        suspendViewer = new ImageView(suspendImage);         
    }
    
    
    public void display()   // Browser is used by the search feature, If the browser is being used for a direct search, the 'setSearchFlags' method should be called first, and this should be set true
    {
        moderator = new Stage();
        
        // Build 'Item Browser' label displayed on top left
        moderatorLabel = new Label("Moderator");  // Could use a graphics label instead at some point
        moderatorLabel.getStyleClass().add("windowLabel");
        
        // Build 'Actions' label displayed on lower VBox
        actionsLabel = new Label("Actions");
        actionsLabel.getStyleClass().add("subWindowLabel");
        
        // Build 'Filters:' label displayed on top of filter selection VBox
        filterLabel = new Label("Filters:");
        filterLabel.setPadding(new Insets(20,0,0,0));  // Needed a small extra push downward
        filterLabel.getStyleClass().add("subWindowLabel");
        
        
        // Note: The options in these filter lists directly affect the performance of requesting an item list
        // Note: The options string values are directly pulled from this dropdown upon submission of request and are put into the item request form object to be processed by the server
        tradeStatusFilter = new ComboBox<>();
        tradeStatusFilter.setPromptText("Trade Status");
        tradeStatusFilter.getItems().addAll("All", "WTS", "WTB");
        tradeStatusFilter.setOnAction(e -> requestItemList());
        
      
        // Note: Filter list. See note above
        typeFilter = new ComboBox<>();
        typeFilter.setPromptText("Item Type");
        typeFilter.getItems().addAll("All", "Key", "Keybar", "Docs Case", "Storage Case", "Secure Container", "Weapon", "Weapon Mod", "Armor/Helmet", "Apparel", "Ammo", "Medicine", "Misc");
        typeFilter.setOnAction(e -> requestItemList());

        
        // Note: Filter list. See note above
        priceRangeFilter = new ComboBox<>();
        priceRangeFilter.setPromptText("Price Range");
        priceRangeFilter.getItems().addAll("All", "1 - 50,000", "50,000 - 100,000", "100,000 - 200,000", "200,000 - 300,000", "300,000+");
        priceRangeFilter.setOnAction(e -> requestItemList());
        
        
        // Build buttons
        clearFiltersButton = new Button("Clear Filters");
        clearFiltersButton.setGraphic(minusViewer);
        clearFiltersButton.setTooltip(new Tooltip("Reset Filters to 'All'"));
        clearFiltersButton.setOnAction(e -> clearFilters());
        
        editButton = new Button("Edit Listing");
        editButton.setGraphic(editViewer);
        editButton.setTooltip(new Tooltip("Make Changes to Listing"));
        
        deleteButton = new Button("Delete Listing");
        deleteButton.setGraphic(deleteViewer);
        deleteButton.setTooltip(new Tooltip("Permanantly Remove Listing"));
        deleteButton.setOnAction(e -> requestDelete());
        
        suspendButton = new Button("Suspend Listing");
        suspendButton.setGraphic(suspendViewer);
        suspendButton.setTooltip(new Tooltip("Temporarily Remove Listing"));
        
        refreshButton = new Button("Refresh");
        refreshButton.setGraphic(Resources.refreshIconViewer);
        refreshButton.setOnAction(e -> requestItemList());
        
        returnButton = new Button("Return");
        returnButton.setGraphic(Resources.cancelIconViewer);
        returnButton.setOnAction(e -> close());
        
        
        // Upper display will consist of two seperate HBoxes
        // The left HBox will only house the 'Item Browser' label
        // The right HBox will be set to grow horizontally with priority, and align to the center-right. This will display the 'Tarkov Trader' logo
        HBox upperDisplay = new HBox();
        HBox upperDisplayRight = new HBox();
        upperDisplay.setPadding(new Insets(0,10,0,10));
        upperDisplay.setAlignment(Pos.CENTER);
        upperDisplayRight.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(upperDisplayRight, Priority.ALWAYS);
        moderatorLabel.setPadding(new Insets(0,0,0,20));
        upperDisplayRight.getChildren().add(Resources.outlineLogoViewer);
        upperDisplay.getChildren().addAll(moderatorLabel, upperDisplayRight);
        upperDisplay.getStyleClass().add("hbox");
        
        
        // This VBox will house all filter related nodes
        VBox leftDisplay = new VBox(50);
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
        nameColumn.setMaxWidth(350);
        nameColumn.setMinWidth(350);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));  // Calls getName() in ProcessedItem object
        
        priceColumn = new TableColumn<>("Price");
        priceColumn.setMaxWidth(100);
        priceColumn.setMinWidth(100);
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));  // Calls getPrice() in ProcessedItem object
        
        table = new TableView<>();
        table.getColumns().addAll(imageColumn, typeColumn, tradeStateColumn, nameColumn, priceColumn);
        
        
        VBox centerDisplay = new VBox();
        
        VBox lowerDisplay = new VBox(20);
        lowerDisplay.setAlignment(Pos.CENTER);
        lowerDisplay.setPadding(new Insets(20,20,30,20));
        lowerDisplay.getStyleClass().add("vbox");
        
        HBox buttonBox = new HBox(45);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(clearFiltersButton, editButton, deleteButton, suspendButton);
        
        lowerDisplay.getChildren().addAll(actionsLabel, buttonBox);
        
        centerDisplay.getChildren().addAll(table, lowerDisplay);
         
        
        // Border pane layout: Uses top, left, and center sections
        BorderPane border = new BorderPane();
        border.setTop(upperDisplay);
        border.setLeft(leftDisplay);
        border.setCenter(centerDisplay);
        
        
        // Build the scene and set stage properties
        Scene scene = new Scene(border);
        scene.getStylesheets().add(this.getClass().getResource("veneno.css").toExternalForm());
        moderator.setTitle("Moderator");
        moderator.getIcons().add(Resources.icon);
        moderator.setOnCloseRequest(e -> close());  // For now, only closes the browser stage and reopens the main trader user interface
        moderator.setResizable(false);
        moderator.setScene(scene);
        
        
        // If the browser is performing a specified search, filters are not necessary as they are already specified in the search window
        // We can disable all nodes that control filtering since this is already done
        
        // If the browser has not been populated, do so now
        if (!isPopulated)  
        {
            if (requestItemList())
                this.isPopulated = true;
        }
        
        moderator.show();
    }
    
    
    public void populate(ArrayList<ProcessedItem> itemList)
    {
        // Called by RequestWorker and passed the ArrayList to process 
        // Calls getItemList to retrieve an OberservableList of processed items for CellValueFactory to fetch data
        table.setItems(getItemList(itemList));
    }
    
    
    private ObservableList<ProcessedItem> getItemList(ArrayList<ProcessedItem> itemList)
    {
        return FXCollections.observableArrayList(itemList);
    }

    
    private boolean requestItemList()
    {
        ItemListForm listrequest;
       
        listrequest = new ItemListForm(buildFilterFlags(), true);
        

        // Use worker to send, wait for server response, table will populate upon receiving list
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
        int priceFlag;
 
        String definedStatusFlag;
        String definedTypeFlag;
        
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
            priceFlag = 0;
        else 
            priceFlag = priceRangeFilter.getSelectionModel().getSelectedIndex();

        
        switch(statusFlag)
        {
            case "All":
                definedStatusFlag = "%";
                priceRangeFilter.setDisable(false);
                break;
            case "WTB":
                definedStatusFlag = "WTB";
                priceFlag = 0;
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
            case 1:
                priceMin = "1";
                priceMax = "50000";
                break;
            case 2:
                priceMin = "50000";
                priceMax = "100000";
                break;
            case 3:
                priceMin = "100000";
                priceMax = "200000";
                break;
            case 4:
                priceMin = "200000";
                priceMax = "300000";
                break;
            case 5:
                priceMin = "300000";
                priceMax = PRICE_MAX;
                break;
            default:  // Catches "All" flag  --  Also used by the WTB flag. WTB flag sets Price flag to "All" or else WTB posts will not return in the query results.
                priceMin = "0";
                priceMax = PRICE_MAX;
        }
        
        
        filterFlags.put("itemid", "%");
        filterFlags.put("state", definedStatusFlag);
        filterFlags.put("type", definedTypeFlag);
        filterFlags.put("name", "%");
        filterFlags.put("pricemin", priceMin);
        filterFlags.put("pricemax", priceMax);
        filterFlags.put("ign", "%");
        filterFlags.put("username", TarkovTrader.username);
        filterFlags.put("timezone", "%");
        filterFlags.put("keywords", "%");
        
        return filterFlags;
    }
    
    
    private void clearFilters()
    {
        tradeStatusFilter.getSelectionModel().clearAndSelect(0);
                
        typeFilter.getSelectionModel().clearAndSelect(0);
                
        priceRangeFilter.getSelectionModel().clearAndSelect(0);
    }
    
    
    private boolean requestDelete()
    {
        if (table.getSelectionModel().isEmpty())
        {
            // There is no listing to be deleted
            Platform.runLater(() -> Alert.display(null, "No item listing selected."));
            return false;
        }
        
        ProcessedItem processedItem = (ProcessedItem)table.getSelectionModel().getSelectedItem();
        Item itemToModify = processedItem.getItem();
        
        ItemModificationRequest deleteRequest = new ItemModificationRequest("delete", null, buildFilterFlags(), itemToModify);
        
        worker.sendForm(deleteRequest);
        
        return true;
    }
    
    
    private void close()
    {
        moderator.close();
        trader.drawMainUI();
    }
}