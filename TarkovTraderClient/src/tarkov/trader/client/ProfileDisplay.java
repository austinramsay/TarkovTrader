
package tarkov.trader.client;

import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tarkov.trader.objects.AccountFlag;
import tarkov.trader.objects.Item;
import tarkov.trader.objects.ProcessedItem;
import tarkov.trader.objects.Profile;
import tarkov.trader.objects.Sale;

/**
 *
 * @author austin
 */

public class ProfileDisplay {
    
    private TarkovTrader trader;
    private Profile profile;
    private Resources resourceLoader;
    
    private Stage profileDisplay;
    private Label profileLabel;
    private Label usernameLabelFinal;
    private Label ignLabelFinal;
    private Label timezoneLabelFinal;
    private Label usernameLabel;
    private Label ignLabel;
    private Label timezoneLabel;
    private Label flagsLabel;
    private Label negativeLabel;
    private Label neutralLabel;
    private Label positiveLabel;
    private Label salesLabel;
    private Label repPointsLabel; 
    private Label registrationDateLabel;
    
    private Button contactButton;
    private Button returnButton;
    
    private TableView completedSalesTable;
    private TableView currentSalesTable;
    
    private boolean allowEdit;
    
    /*
    // Completed Sales Table Columns
    */
    
    private TableColumn<Sale, String> buyerNameColumn;
    private TableColumn<Sale, String> conditionColumn;
    private TableColumn<Sale, String> confirmationColumn;
    private TableColumn<Sale, String> itemNameColumn;
    private TableColumn<Sale, String> saleDateColumn;
    
    
    /*
    // Current Sales Table Columns
    */
    
    TableColumn<ProcessedItem, ImageView> imageColumn;
    TableColumn<ProcessedItem, String> tradeStateColumn;
    TableColumn<ProcessedItem, String> typeColumn;
    TableColumn<ProcessedItem, String> nameColumn;
    TableColumn<ProcessedItem, String> itemPriceColumn;
    
    public ProfileDisplay(TarkovTrader trader, Profile profile, boolean allowEdit)
    {
        this.trader = trader;
        this.profile = profile;
        this.allowEdit = allowEdit;
        this.resourceLoader = new Resources();
        this.resourceLoader.load();

        display();
    }

    
    private void display()
    {
        profileDisplay = new Stage();
        
        profileLabel = new Label("Profile");
        profileLabel.getStyleClass().add("windowLabel");      
        
        flagsLabel = new Label("Account Information:");
        flagsLabel.getStyleClass().add("subWindowLabel");
        
        registrationDateLabel = new Label("Registered since: " + profile.getRegistrationDate());
        
        repPointsLabel = new Label("Reputation Points: " + profile.getRepPoints());
        
        negativeLabel = new Label("\nNegative:");
        
        neutralLabel = new Label("Neutral:");
        
        positiveLabel = new Label("Positive:");
        
        salesLabel = new Label("Sales:");
        salesLabel.getStyleClass().add("subWindowLabel");
        
        usernameLabelFinal = new Label("Username:");
        usernameLabel = new Label(profile.getUsername());
        usernameLabelFinal.getStyleClass().add("subWindowLabel");
        
        ignLabelFinal = new Label("In-game Name:");
        ignLabel = new Label(profile.getIgn());
        ignLabelFinal.getStyleClass().add("subWindowLabel");
        
        timezoneLabelFinal = new Label("Timezone:");
        timezoneLabel = new Label(profile.getTimezone());
        timezoneLabelFinal.getStyleClass().add("subWindowLabel");
        
        contactButton = new Button("Contact");
        if (profile.getUsername().equals(TarkovTrader.username))   // We are looking at this users profile, we can disable the contact button
            contactButton.setDisable(true);
        contactButton.setGraphic(resourceLoader.getSmallMessagesIcon());
        contactButton.setOnAction(e -> {
            
            Messenger messenger;

            if (Messenger.isOpen)
            {
                // Get messenger
                messenger = trader.getMessenger();
            }
            else
            {
                messenger = new Messenger(trader, trader.getWorker());
                messenger.display();
                trader.setMessenger(messenger);      
            }

            Messenger.contactSeller(messenger, profile.getUsername(), false, null);
            
        });        
        
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
        profileLabel.setPadding(new Insets(0,0,0,20));
        upperDisplayRight.getChildren().add(resourceLoader.getOutlineLogo());
        upperDisplay.getChildren().addAll(profileLabel, upperDisplayRight);
        upperDisplay.getStyleClass().add("hbox");
        
        
        // Left display to house user info and buttons for contact/return
        VBox leftDisplay = new VBox(25);
        leftDisplay.setPadding(new Insets(0,20,20,20));
        leftDisplay.getStyleClass().add("vbox");
        leftDisplay.setAlignment(Pos.CENTER);
        
        VBox buttonBox = new VBox(22);
        buttonBox.setPadding(new Insets(25,0,0,0));
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(contactButton, returnButton);
        
        leftDisplay.getChildren().addAll(usernameLabelFinal, usernameLabel, ignLabelFinal, ignLabel, timezoneLabelFinal, timezoneLabel, buttonBox);        
        
        
        // Seller information display area
        HBox sellerInfoDisplay = new HBox();
        VBox sellerInfoLabels = new VBox(12);
        sellerInfoLabels.setAlignment(Pos.CENTER_LEFT);
        sellerInfoLabels.getChildren().addAll(flagsLabel, registrationDateLabel, repPointsLabel);
        
        HBox imageHousing = new HBox();
        imageHousing.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(imageHousing, Priority.ALWAYS);
        Image avatar = new Image(this.getClass().getResourceAsStream("/eftlogo.jpg"));
        ImageView avatarViewer = new ImageView(avatar);
        avatarViewer.setPreserveRatio(false);
        avatarViewer.setFitHeight(128);
        avatarViewer.setFitWidth(180); 
        imageHousing.getChildren().addAll(avatarViewer);
        
        sellerInfoDisplay.getChildren().addAll(sellerInfoLabels, imageHousing);
        
        
        // Build flag display area
        VBox flagDisplay = new VBox(12);
        ArrayList<Label> negativeLabels = new ArrayList<>();
        ArrayList<Label> neutralLabels = new ArrayList<>();
        ArrayList<Label> positiveLabels = new ArrayList<>();
        
        for (AccountFlag flag : profile.getAccountFlags())
        {
            Label flagLabel = new Label("\t\t- " + flag.getReason());
            
            if (flag == AccountFlag.CONFIRMED_MULTIPLE_SCAM || flag == AccountFlag.CONFIRMED_SCAM || flag == AccountFlag.MULTIPLE_REG)
            {
                // Flag is negative
                negativeLabels.add(flagLabel);
            }
            else if (flag == AccountFlag.CONFIRMED_BUY_REP || flag == AccountFlag.CONFIRMED_HIGH_BUY_REP || flag == AccountFlag.CONFIRMED_HIGH_SELL_REP || flag == AccountFlag.CONFIRMED_SELL_REP || flag == AccountFlag.VERIFIED_SUB)
            {
                // Flag is positive
                positiveLabels.add(flagLabel);
            }
            else if (flag == AccountFlag.NEW_ACCOUNT || flag == AccountFlag.NO_COMPLETED_PURCHASES || flag == AccountFlag.NO_COMPLETED_SALES)
            {
                // Flag is neutral
                neutralLabels.add(flagLabel);
            }
        }
        
        if (negativeLabels.isEmpty())
            negativeLabels.add(new Label("\t\t- None"));
        if (neutralLabels.isEmpty())
            neutralLabels.add(new Label("\t\t- None"));
        if (positiveLabels.isEmpty())
            positiveLabels.add(new Label("\t\t- None"));
        
        flagDisplay.getChildren().add(negativeLabel);
        flagDisplay.getChildren().addAll(negativeLabels);
        flagDisplay.getChildren().add(neutralLabel);
        flagDisplay.getChildren().addAll(neutralLabels);
        flagDisplay.getChildren().add(positiveLabel);
        flagDisplay.getChildren().addAll(positiveLabels);
        
        
        // Build Completed Sales Table
        completedSalesTable = new TableView();
        completedSalesTable.setMaxHeight(150);
        
        buyerNameColumn = new TableColumn<>("Buyer Name");
        buyerNameColumn.setCellValueFactory(new PropertyValueFactory("buyerName"));
        buyerNameColumn.setMinWidth(120);
        buyerNameColumn.setMaxWidth(120);
        
        conditionColumn = new TableColumn<>("State");
        conditionColumn.setCellValueFactory(new PropertyValueFactory("saleTypeDesc"));
        conditionColumn.setMinWidth(100);
        conditionColumn.setMaxWidth(100);
        
        confirmationColumn = new TableColumn<>("Feedback");
        confirmationColumn.setCellValueFactory(new PropertyValueFactory("saleStatusDesc"));
        confirmationColumn.setMinWidth(200);
        confirmationColumn.setMaxWidth(200);

        itemNameColumn = new TableColumn<>("Item Name");
        itemNameColumn.setCellValueFactory(new PropertyValueFactory("itemName"));
        itemNameColumn.setMinWidth(300);
        itemNameColumn.setMaxWidth(300);

        saleDateColumn = new TableColumn<>("Sale Date");
        saleDateColumn.setCellValueFactory(new PropertyValueFactory("saleDate"));        
        saleDateColumn.setMinWidth(130);
        saleDateColumn.setMaxWidth(130);
        
        completedSalesTable.getColumns().addAll(buyerNameColumn, conditionColumn, confirmationColumn, itemNameColumn, saleDateColumn);
        completedSalesTable.setItems(FXCollections.observableArrayList(profile.getCompletedSales()));
        
                
        // Build Current Sales Table
        currentSalesTable = new TableView();
        currentSalesTable.setMaxHeight(150);      
        
        imageColumn = new TableColumn<>();
        imageColumn.setMaxWidth(50);
        imageColumn.setMinWidth(50);
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("itemTypeImage"));        // Calls getItemTypeImage() in ProcessedItem object -- Generic item type icon stored client-side
        
        typeColumn = new TableColumn<>("Type"); 
        typeColumn.setMaxWidth(150);
        typeColumn.setMinWidth(150);
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("itemType"));              // Calls getItemType() in ProcessedItem object
        
        tradeStateColumn = new TableColumn<>("State");
        tradeStateColumn.setMaxWidth(50);
        tradeStateColumn.setCellValueFactory(new PropertyValueFactory<>("tradeState"));      // Calls getTradeState() in ProcessedItem object
        
        nameColumn = new TableColumn<>("Item Name");
        nameColumn.setMaxWidth(400);
        nameColumn.setMinWidth(400);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));                  // Calls getName() in ProcessedItem object
        
        itemPriceColumn = new TableColumn<>("Price");
        itemPriceColumn.setMaxWidth(100);
        itemPriceColumn.setMinWidth(100);
        itemPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));            // Calls getPrice() in ProcessedItem object
        
        currentSalesTable.getColumns().addAll(imageColumn, typeColumn, tradeStateColumn, nameColumn, itemPriceColumn);
        currentSalesTable.setItems(FXCollections.observableArrayList(getProcessedItemList(profile.getCurrentSales())));    // Translate the ArrayList<Item> into an Observable List of ProcessedItems
        currentSalesTable.setOnMouseClicked(me -> displaySelectedItem(me));
        
        // Setup TabPane to switch between current listings and previously completed sales
        TabPane saleTableSelection = new TabPane();
        Tab completedSalesTab = new Tab(String.format("Completed Sales (%d)", profile.getCompletedSales().size()));
        Tab currentSalesTab = new Tab(String.format("Current Sales (%d)", profile.getCurrentSales().size()));
        
        completedSalesTab.setContent(completedSalesTable);
        currentSalesTab.setContent(currentSalesTable);
        
        saleTableSelection.getTabs().addAll(completedSalesTab, currentSalesTab);
        
        
        // Build Sales display area
        VBox salesDisplay = new VBox(7);
        salesDisplay.getChildren().addAll(salesLabel, saleTableSelection);
        
        
        // Combine flags display and sales display
        VBox centerDisplay = new VBox(10);
        centerDisplay.setPadding(new Insets(20));
        centerDisplay.getChildren().addAll(sellerInfoDisplay, flagDisplay, salesDisplay);
             
        
        // Build BorderPane root layout
        BorderPane root = new BorderPane();
        root.setTop(upperDisplay);
        root.setLeft(leftDisplay);
        root.setCenter(centerDisplay);
        
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add(this.getClass().getResource("veneno.css").toExternalForm());
        
        profileDisplay.setScene(scene);
        profileDisplay.setTitle("Profile");
        profileDisplay.setOnCloseRequest(e -> close());
        profileDisplay.setResizable(false);
        profileDisplay.getIcons().add(resourceLoader.getIcon());
        profileDisplay.show();
    }
    
    
    private ArrayList<ProcessedItem> getProcessedItemList(ArrayList<Item> itemList)
    {
        // Get the list of items from the form to begin        
        ArrayList<ProcessedItem> processedItemList = new ArrayList<>();
        
        for (Item item : itemList)
        {
            processedItemList.add(new ProcessedItem(item));
        }
        
        return processedItemList;
    }    
    
    
    private void displaySelectedItem(MouseEvent e)
    {
        if (currentSalesTable.getSelectionModel().isEmpty())
            return;
        
        if (e.getClickCount() == 2)
        {
            ItemDisplay itemdisplay = new ItemDisplay(trader, (ProcessedItem)currentSalesTable.getSelectionModel().getSelectedItem());
        }
    }    
    
    
    private void close()
    {
        profileDisplay.close();
    }
}
