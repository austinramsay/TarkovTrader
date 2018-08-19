
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
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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
import tarkov.trader.objects.ItemAction;
import tarkov.trader.objects.ItemModificationRequest;
import tarkov.trader.objects.ItemStatus;
import tarkov.trader.objects.ItemStatusModRequest;
import tarkov.trader.objects.ProcessedItem;
import tarkov.trader.objects.Profile;
import tarkov.trader.objects.ProfileRequest;

/**
 *
 * @author austin
 */

public class Moderator {
    
    private TarkovTrader trader;
    private RequestWorker worker;
    private Resources resourceLoader;
    
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
    private Button acceptRequestButton;
    private Button declineRequestButton;
    private Button reportRequestButton;
    private Button removeRequestButton;
    private Button removeBuyItemButton;
    private TableView sellingTable;
    private TableView buyingTable;
    private TableView requestsTable;
    private ComboBox<String> typeFilter;
    private ComboBox<String> tradeStatusFilter;
    private ComboBox<String> priceRangeFilter;
    private TabPane tableSelector;
    private Tab sellingTab;
    private Tab buyingTab;
    private Tab requestsTab;
    private HBox buttonBox;
    
    private TableColumn<ProcessedItem, ImageView> imageColumn;
    private TableColumn<ProcessedItem, String> tradeStateColumn;
    private TableColumn<ProcessedItem, String> typeColumn;
    private TableColumn<ProcessedItem, String> nameColumn;
    private TableColumn<ProcessedItem, String> priceColumn;
    private TableColumn<ProcessedItem, String> suspensionColumn;
    private TableColumn<ProcessedItem, String> timezoneColumn;
    private TableColumn<ProcessedItem, String> ignColumn;
    private TableColumn<ProcessedItem, String> itemStatusColumn;
    private TableColumn<ProcessedItem, String> requestedUserColumn;
    
    private boolean isPopulated;
    
    private final String PRICE_MAX = "50000000";
    
    
    public Moderator(TarkovTrader trader)
    {
        this.trader = trader;
        this.worker = trader.getWorker();
        this.resourceLoader = new Resources();
        
        this.isPopulated = false;
    }
    
    
    public void display()  
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
        tradeStatusFilter.setOnAction(e -> requestProfile());
        
      
        // Note: Filter list. See note above
        typeFilter = new ComboBox<>();
        typeFilter.setPromptText("Item Type");
        typeFilter.getItems().addAll("All", "Key", "Keybar", "Docs Case", "Storage Case", "Secure Container", "Weapon", "Weapon Mod", "Armor/Helmet", "Apparel", "Ammo", "Medicine", "Misc");
        typeFilter.setOnAction(e -> requestProfile());

        
        // Note: Filter list. See note above
        priceRangeFilter = new ComboBox<>();
        priceRangeFilter.setPromptText("Price Range");
        priceRangeFilter.getItems().addAll("All", "1 - 50,000", "50,000 - 100,000", "100,000 - 200,000", "200,000 - 300,000", "300,000+");
        priceRangeFilter.setOnAction(e -> requestProfile());
        
        
        // Build buttons
        clearFiltersButton = new Button("Clear Filters");
        clearFiltersButton.setGraphic(resourceLoader.getMinusIcon());
        clearFiltersButton.setTooltip(new Tooltip("Reset Filters to 'All'"));
        clearFiltersButton.setOnAction(e -> clearFilters());
        
        editButton = new Button("Edit Listing");
        editButton.setGraphic(resourceLoader.getEditIcon());
        editButton.setTooltip(new Tooltip("Make Changes to Listing"));
        editButton.setOnAction(e -> openEditor());
        
        deleteButton = new Button("Delete Listing");
        deleteButton.setGraphic(resourceLoader.getDeleteIcon());
        deleteButton.setTooltip(new Tooltip("Permanantly Remove Listing"));
        deleteButton.setOnAction(e -> requestDelete());
        
        suspendButton = new Button("Suspend Listing");
        suspendButton.setGraphic(resourceLoader.getSuspendIcon());
        suspendButton.setTooltip(new Tooltip("Temporarily Remove Listing"));
        suspendButton.setOnAction(e -> requestSuspension());
        
        acceptRequestButton = new Button("Accept Sale");
        acceptRequestButton.setGraphic(resourceLoader.getAcceptIcon());
        acceptRequestButton.setTooltip(new Tooltip("Accept if the user has bought the item."));
        acceptRequestButton.setOnAction(e -> markSaleRequest(ItemStatus.CONFIRMED));
        
        declineRequestButton = new Button("Decline Sale");
        declineRequestButton.setGraphic(resourceLoader.getDeclineIcon());
        declineRequestButton.setTooltip(new Tooltip("Decline if the user did not buy the item."));
        declineRequestButton.setOnAction(e -> markSaleRequest(ItemStatus.DECLINED));
        
        reportRequestButton = new Button("Report Sale");
        reportRequestButton.setGraphic(resourceLoader.getBigFlagIcon());
        reportRequestButton.setTooltip(new Tooltip("Report if scam or incident occured."));
        reportRequestButton.setOnAction(e -> markSaleRequest(null));
        
        removeRequestButton = new Button("Remove Request");
        removeRequestButton.setGraphic(resourceLoader.getDeleteIcon());
        removeRequestButton.setTooltip(new Tooltip("Remove request -after- being marked."));
        removeRequestButton.setOnAction(e -> actOnSaleRequest(ItemAction.REMOVE_FROM_REQUESTS));
        
        removeBuyItemButton = new Button("Remove Item");
        removeBuyItemButton.setGraphic(resourceLoader.getDeleteIcon());
        removeBuyItemButton.setTooltip(new Tooltip("Items can only be removed after a seller has marked the item."));
        removeBuyItemButton.setOnAction(e -> actOnBuyItem(ItemAction.REMOVE_FROM_BUY_LIST));
        
        refreshButton = new Button("Refresh");
        refreshButton.setGraphic(resourceLoader.getRefreshIcon());
        refreshButton.setOnAction(e -> requestProfile());
        
        returnButton = new Button("Return");
        returnButton.setGraphic(resourceLoader.getCancelIcon());
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
        upperDisplayRight.getChildren().add(resourceLoader.getOutlineLogo());
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
        
        suspensionColumn = new TableColumn<>("Suspended");
        suspensionColumn.setMaxWidth(100);
        suspensionColumn.setMinWidth(100);
        suspensionColumn.setCellValueFactory(new PropertyValueFactory<>("formattedSuspensionState"));  // Calls getSuspensionState() in ProcessedItem object        
        
        // The IGN, timezone, and Item Status columns are unique to the buying table
        ignColumn = new TableColumn<>("In-game Name");
        ignColumn.setMaxWidth(220);
        ignColumn.setMinWidth(220);
        ignColumn.setCellValueFactory(new PropertyValueFactory<>("ign"));  // Calls getIgn() in ProcessedItem object
        
        timezoneColumn = new TableColumn<>("Timezone");
        timezoneColumn.setMaxWidth(100);
        timezoneColumn.setMinWidth(100);
        timezoneColumn.setCellValueFactory(new PropertyValueFactory<>("timezone"));  // Calls getTimezone() in ProcessedItem object     
        
        itemStatusColumn = new TableColumn<>("Status");
        itemStatusColumn.setCellValueFactory(new PropertyValueFactory<>("itemStatusDesc"));
        itemStatusColumn.setMinWidth(120);
        itemStatusColumn.setMaxWidth(120);
        
        requestedUserColumn = new TableColumn<>("Requested By");
        requestedUserColumn.setCellValueFactory(new PropertyValueFactory<>("requestedUser"));
        requestedUserColumn.setMinWidth(200);
        requestedUserColumn.setMaxWidth(200);
        
        // Selling Table
        sellingTable = new TableView<>();
        sellingTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null)
            {
                ProcessedItem selectedItem = (ProcessedItem)sellingTable.getSelectionModel().getSelectedItem();
                setSuspendButton(selectedItem.getSuspensionState());
            }
            else
                setSuspendButton(false);
        });
        
        
        // Buying table
        buyingTable = new TableView<>();
        buyingTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
           if (newSelection != null)
           {
               ProcessedItem selectedItem = (ProcessedItem)buyingTable.getSelectionModel().getSelectedItem();
               setBuyItemDeleteButton(selectedItem.getItemStatus());
           }
        });
        
        // Requests Table
        requestsTable = new TableView<>();
        
        
        // Tabs
        tableSelector = new TabPane();
        
        sellingTab = new Tab("Selling");
        sellingTab.setClosable(false);
        
        buyingTab = new Tab("Buying");
        buyingTab.setClosable(false);
        
        requestsTab = new Tab("Sale Requests");
        requestsTab.setClosable(false);
        
        tableSelector.getTabs().addAll(sellingTab, buyingTab, requestsTab);
        sellingTab.setContent(sellingTable);
        buyingTab.setContent(buyingTable);
        requestsTab.setContent(requestsTable);
        
        
        // Button box
        buttonBox = new HBox(45);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(clearFiltersButton, editButton, deleteButton, suspendButton);
        
        
        // Tab Pane
        tableSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            
            sellingTable.getColumns().clear();
            buyingTable.getColumns().clear();       
            requestsTable.getColumns().clear();
            
            if (newSelection == sellingTab)
            {
                sellingTable.getColumns().addAll(imageColumn, typeColumn, tradeStateColumn, nameColumn, priceColumn, suspensionColumn);
                suspendButton.setDisable(false);
                editButton.setDisable(false);
                buttonBox.getChildren().clear();
                buttonBox.getChildren().addAll(clearFiltersButton, editButton, deleteButton, suspendButton);
            }
            else if (newSelection == buyingTab)
            {
                buyingTable.getColumns().addAll(imageColumn, itemStatusColumn, nameColumn, priceColumn, ignColumn, timezoneColumn);
                buttonBox.getChildren().clear();
                buttonBox.getChildren().addAll(clearFiltersButton, removeBuyItemButton);
            }
            else if (newSelection == requestsTab)
            {
                requestsTable.getColumns().addAll(imageColumn, requestedUserColumn, itemStatusColumn, nameColumn, priceColumn);
                buttonBox.getChildren().clear();
                buttonBox.getChildren().addAll(clearFiltersButton, acceptRequestButton, declineRequestButton, reportRequestButton, removeRequestButton);
            }
            
        });
        
        tableSelector.getSelectionModel().clearSelection();
        tableSelector.getSelectionModel().select(sellingTab);
        
        
        // Main content
        VBox centerDisplay = new VBox();
        
        VBox lowerDisplay = new VBox(20);
        lowerDisplay.setAlignment(Pos.CENTER);
        lowerDisplay.setPadding(new Insets(20,20,30,20));
        lowerDisplay.getStyleClass().add("vbox");
        
        lowerDisplay.getChildren().addAll(actionsLabel, buttonBox);
        
        centerDisplay.getChildren().addAll(tableSelector, lowerDisplay);
        
        
        // Border pane layout: Uses top, left, and center sections
        BorderPane border = new BorderPane();
        border.setTop(upperDisplay);
        border.setLeft(leftDisplay);
        border.setCenter(centerDisplay);
        
        
        // Build the scene and set stage properties
        Scene scene = new Scene(border);
        scene.getStylesheets().add(this.getClass().getResource("veneno.css").toExternalForm());
        
        moderator.setTitle("Moderator");
        moderator.getIcons().add(resourceLoader.getIcon());
        moderator.setOnCloseRequest(e -> close());
        moderator.setResizable(false);
        moderator.setScene(scene);
        
        // If the browser is performing a specified search, filters are not necessary as they are already specified in the search window
        // We can disable all nodes that control filtering since this is already done
        
        // If the browser has not been populated, do so now
        if (!isPopulated)  
        {
            if (requestProfile())
                this.isPopulated = true;
        }
        
        moderator.show();
        moderator.setWidth(1300);
        moderator.centerOnScreen();
    }
    
    
    private void setSuspendButton(boolean isSuspended)
    {
        if (isSuspended)
            suspendButton.setText("Unsuspend Listing");
        else
            suspendButton.setText("Suspend Listing");
    }
    
    
    private void setBuyItemDeleteButton(ItemStatus currentStatus)
    {
        if (currentStatus == ItemStatus.AWAITING_RESPONSE || currentStatus == ItemStatus.OPEN)
            removeBuyItemButton.setDisable(true);
        else
            removeBuyItemButton.setDisable(false);
    }
    
    
    public void populate(Profile profile)
    {
        // Called by RequestWorker and passed the ArrayList to process 
        // Calls getItemList to retrieve an OberservableList of processed items for CellValueFactory to fetch data
        
        Tab selectedTab = tableSelector.getSelectionModel().getSelectedItem();
        
        sellingTable.setItems(getItemList(getProcessedItemList(profile.getCurrentSales())));
        buyingTable.setItems(getItemList(getProcessedItemList(profile.getBuyList())));
        requestsTable.setItems(getItemList(getProcessedItemList(profile.getRequestedSales())));
    }
    
    
    private ArrayList<ProcessedItem> getProcessedItemList(ArrayList<Item> itemList)
    {
        ArrayList<ProcessedItem> processedItemList = new ArrayList<>();
        
        for (Item item : itemList)
        {
            processedItemList.add(new ProcessedItem(item));
        }
        
        return processedItemList;
    }
        
    
    private ObservableList<ProcessedItem> getItemList(ArrayList<ProcessedItem> itemList)
    {
        return FXCollections.observableArrayList(itemList);
    }
    
    
    private boolean requestProfile()
    {
        ProfileRequest profileRequest = new ProfileRequest(TarkovTrader.username);
        ArrayList<String> flags = new ArrayList<>();
        flags.add("moderator");
        profileRequest.setFlags(flags);
        
        // Use worker to send, wait for server response, tables will populate upon receiving profile
        if (!worker.sendForm(profileRequest))
        {
            Platform.runLater(() -> Alert.display("Request Failed", "Failed to send item list request to server."));
            return false;
        }  
        
        return true;
    }
    
    
    public HashMap<String, String> buildFilterFlags()
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
        Tab selectedTab = tableSelector.getSelectionModel().getSelectedItem();
             
        // Selling table is selected
        if (selectedTab == sellingTab)
        {
            if (sellingTable.getSelectionModel().isEmpty())
            {
                // There is no listing to be deleted
                Platform.runLater(() -> Alert.display(null, "No item listing selected."));
                return false;
            }
        
            ProcessedItem selectedItem = (ProcessedItem)sellingTable.getSelectionModel().getSelectedItem();
            Item itemToModify = selectedItem.getItem();
        
            ItemModificationRequest deleteRequest = new ItemModificationRequest("delete", buildFilterFlags(), itemToModify);
        
            if(!worker.sendForm(deleteRequest))
                Platform.runLater(() -> Alert.display(null, "Failed to send request."));
        }
        
        return true;
    }
    
    
    private boolean requestSuspension()
    {
        if (sellingTable.getSelectionModel().isEmpty())
        {
            // There is no listing to be deleted
            Platform.runLater(() -> Alert.display(null, "No item listing selected."));
            return false;
        }

        ProcessedItem selectedItem = (ProcessedItem)sellingTable.getSelectionModel().getSelectedItem();
        Item itemToModify = selectedItem.getItem();
        Item preModifiedItem = itemToModify;
        
        boolean toSuspend = !itemToModify.getSuspensionState();
        
        preModifiedItem.setSuspended(toSuspend);
        
        ItemModificationRequest suspendRequest = new ItemModificationRequest("suspend", buildFilterFlags(), itemToModify, preModifiedItem);
        
        worker.sendForm(suspendRequest);
        
        return true;
    }
    
    
    private boolean openEditor()
    {
        if (sellingTable.getSelectionModel().isEmpty())
        {
            // There is no listing to be edited
            Platform.runLater(() -> Alert.display(null, "No item listing selected."));
            return false;            
        }
        
        ProcessedItem selectedItem = (ProcessedItem)sellingTable.getSelectionModel().getSelectedItem();
        
        Item itemToEdit = selectedItem.getItem();
        
        ItemEditor editor = new ItemEditor(this, worker, itemToEdit);
        
        return true;
    }
    
    
    private boolean markSaleRequest(ItemStatus status)
    {
        // Any item selected?
        if (requestsTable.getSelectionModel().isEmpty())
        {
            Platform.runLater(() -> Alert.display(null, "No sale request selected."));
            return false;
        }
        
        ProcessedItem selectedItem = (ProcessedItem)requestsTable.getSelectionModel().getSelectedItem();
        Item itemToModify = selectedItem.getItem();
        
        // Is the item still awaiting response, or has it been marked already?
        if (itemToModify.getItemStatus() != ItemStatus.AWAITING_RESPONSE)
        {
            String markedStatus = itemToModify.getItemStatus().getReason();
            Platform.runLater(() -> Alert.display(null, "This sale has already been marked as: " + markedStatus + "."));
            return false;
        }
        
        // Confirm the user would like to mark as sale (FINAL WARNING)
        
        ItemStatusModRequest modRequest = new ItemStatusModRequest(ItemAction.MODIFY_STATUS, status, itemToModify);
        
        if (!worker.sendForm(modRequest))
        {
            Platform.runLater(() -> Alert.display(null, "Failed to send request."));
            return false;
        }
        
        return true;
    }
    
    
    private boolean actOnSaleRequest(ItemAction action)
    {
        if (requestsTable.getSelectionModel().isEmpty())
        {
            Platform.runLater(() -> Alert.display(null, "No sale request selected."));
            return false;
        }
        
        ProcessedItem selectedItem = (ProcessedItem)requestsTable.getSelectionModel().getSelectedItem();
        Item itemToModify = selectedItem.getItem();
        ItemStatusModRequest modRequest = null;
        
        if (action == ItemAction.REMOVE_FROM_REQUESTS)
        {
            modRequest = new ItemStatusModRequest(action, null, itemToModify);
        }
        
        if (modRequest == null || !worker.sendForm(modRequest))
        {
            Platform.runLater(() -> Alert.display(null, "Failed to send request."));
            return false;
        }
        
        return true;
    }
    
    
    private boolean actOnBuyItem(ItemAction action)
    {
        if (buyingTable.getSelectionModel().isEmpty())
        {
            Platform.runLater(() -> Alert.display(null, "No item selected."));
            return false;
        }
        
        ProcessedItem selectedItem = (ProcessedItem)buyingTable.getSelectionModel().getSelectedItem();
        Item itemToModify = selectedItem.getItem();
        ItemStatusModRequest modRequest = null;
        
        if (action == ItemAction.REMOVE_FROM_BUY_LIST)
        {
            modRequest = new ItemStatusModRequest(action, null, itemToModify);
        }
        
        if (modRequest == null || !worker.sendForm(modRequest))
        {
            Platform.runLater(() -> Alert.display(null, "Failed to send request."));
            return false;
        }
        
        return true;
    }
    
    
    private void close()
    {
        moderator.close();
        trader.drawMainUI();
    }
}