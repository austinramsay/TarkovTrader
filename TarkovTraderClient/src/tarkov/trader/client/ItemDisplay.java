
package tarkov.trader.client;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tarkov.trader.objects.ProcessedItem;
import tarkov.trader.objects.ProfileRequest;

/**
 *
 * @author austin
 */

public class ItemDisplay {
    
    private ProcessedItem item;
    private RequestWorker worker;
    private TarkovTrader trader;
    private Resources resourceLoader;
    
    private ImageView itemImageViewer;
    
    private Label itemListingLabel;
    private Label actionsLabel;
    private Label sellerInfoLabel;
    private Label ignLabelFinal;
    private Label usernameLabelFinal;
    private Label timezoneLabelFinal;
    private Label sellerStateLabelFinal;
    private Label itemInfoLabel;
    private Label priceLabelFinal;
    private Label dateLabelFinal;
    
    private Label ignLabel;
    private Label usernameLabel;
    private Label timezoneLabel;
    private Label sellerStateLabel;
    private Label priceLabel;
    private Label tradeStateLabel;
    private Label itemNameLabel;
    private Label dateLabel;
    private TextArea notesArea;
    
    private Button contactButton;
    private Button profileButton;
    private Button addToBuyListButton;
    private Button flagButton;
    private Button returnButton;
    
    private String sellerUsername;
    
    public ItemDisplay(TarkovTrader trader, ProcessedItem item)
    {
        this.trader = trader;
        this.worker = trader.getWorker();
        this.item = item;
        this.resourceLoader = new Resources();
        this.resourceLoader.load();
        
        display();
    }

    
    private void display()
    {
        Stage itemdisplay = new Stage();
        
        // Main window 'Item Listing' label on top left
        itemListingLabel = new Label("Item Listing");
        itemListingLabel.getStyleClass().add("windowLabel");
        itemListingLabel.setPadding(new Insets(0,0,0,20));
        
        // Build label for 'Actions:' on top of left VBox
        actionsLabel = new Label("Actions:");
        actionsLabel.getStyleClass().add("subWindowLabel");
        
        
        // Generic window labels
        sellerInfoLabel = new Label("Seller Info:");
        sellerInfoLabel.setStyle("-fx-underline: true;");
        
        itemInfoLabel = new Label("Item Info:");
        itemInfoLabel.setStyle("-fx-underline: true;");    
        
        ignLabelFinal = new Label("In-game Name:");
        usernameLabelFinal = new Label("Username:");
        timezoneLabelFinal = new Label("Timezone:");
        sellerStateLabelFinal = new Label("Seller Status: ");
        priceLabelFinal = new Label("Price:");
        dateLabelFinal = new Label("Posting date:");
        
        
        // Labels unique to referenced item information
        itemImageViewer = item.getUserItemImage();
        
        itemNameLabel = new Label(item.getName());
        itemNameLabel.getStyleClass().add("itemNameLabel");
        
        tradeStateLabel = new Label(item.getTradeState() + ": ");
        tradeStateLabel.getStyleClass().add("itemNameLabel");
        
        ignLabel = new Label(item.getIgn());
        usernameLabel = new Label(item.getUsername());
        timezoneLabel = new Label(item.getTimezone());
        sellerStateLabel = new Label(item.getSellerState());
        priceLabel = new Label(item.getPrice());
        dateLabel = new Label(item.getDate());
        
        // Set the seller username for contacting/requesting profile
        sellerUsername = item.getUsername();
        
        notesArea = new TextArea();
        notesArea.setText("Seller Notes: " + item.getNotes());
        notesArea.setWrapText(true);
        notesArea.setEditable(false);
        
        // Button initialization
        contactButton = new Button("Contact Seller");
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
                messenger = new Messenger(trader, worker);
                messenger.display();
                trader.setMessenger(messenger);      
            }
            
            Messenger.contactSeller(messenger, sellerUsername, true, item.getName());
            
            itemdisplay.close();
            
        });
        
        profileButton = new Button("Seller Profile");
        profileButton.setGraphic(resourceLoader.getSmallProfileIcon());
        profileButton.setOnAction(e -> {
            
            // Build a profile request for the seller's profile
            ProfileRequest profileRequest = new ProfileRequest(sellerUsername);
            worker.sendForm(profileRequest);
            
            // Request worker will handle from this point
            
        });
        
        addToBuyListButton = new Button("Add To Buy List");
        addToBuyListButton.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("/addmid.png"), 24, 24, true, true)));
        addToBuyListButton.setOnAction(e -> addItemToBuyList());
        
        flagButton = new Button("Flag Item");
        flagButton.setGraphic(resourceLoader.getFlagIcon());
        
        returnButton = new Button("Return");
        returnButton.setGraphic(resourceLoader.getCancelIcon());
        returnButton.setOnAction(e -> itemdisplay.close());
        
        if (TarkovTrader.username.equals(item.getUsername()))
        {
            // If the posting's username is the client, disable all action buttons
            contactButton.setDisable(true); 
            flagButton.setDisable(true);
        }
        
        
        // Layout deisgn
        // BorderPane is the main layout for the scene
        // Left actions area is a VBox
        // Upper display contains two HBoxes for the 'Item Listing' label and the Trader logo
        // The center section is a grid pane
        //     Note: Contains an HBox and VBox for the item image and two labels to the right of the image
        
        
        // North upper display construction
        HBox upperDisplay = new HBox();
        upperDisplay.setAlignment(Pos.CENTER);
        
        HBox upperDisplayRight = new HBox();
        HBox.setHgrow(upperDisplayRight, Priority.ALWAYS);
        upperDisplayRight.setAlignment(Pos.CENTER_RIGHT);
              
        upperDisplayRight.getChildren().add(resourceLoader.getOutlineLogo());
        
        upperDisplay.getChildren().addAll(itemListingLabel, upperDisplayRight);  // Note: itemListingLabel has padding 20 left on initialization
        
        upperDisplay.getStyleClass().add("hbox");
        // End upper display construction
        
        
        // Left display construction
        // This will contain all nodes related to 'actions' for the user
        VBox leftDisplay = new VBox(40);
        leftDisplay.setPadding(new Insets(0,20,20,20));
        leftDisplay.setAlignment(Pos.CENTER);
        leftDisplay.getChildren().addAll(actionsLabel, contactButton, profileButton, addToBuyListButton, flagButton, returnButton);    
        leftDisplay.getStyleClass().add("vbox");
        
        
        // Center display: Upper section to house item image and 'Item Name'
        HBox centerUpperDisplay = new HBox();
        centerUpperDisplay.setPadding(new Insets(10, 0, 0, 0));
        centerUpperDisplay.setAlignment(Pos.CENTER);
        
        HBox centerUpperRightDisplay = new HBox();
        centerUpperRightDisplay.setPadding(new Insets(0,20,0,20));
        centerUpperRightDisplay.setAlignment(Pos.CENTER);
        HBox.setHgrow(centerUpperRightDisplay, Priority.ALWAYS);
        centerUpperRightDisplay.getChildren().addAll(tradeStateLabel, itemNameLabel);
        
        centerUpperDisplay.getChildren().addAll(itemImageViewer, centerUpperRightDisplay);
        
        
        // Set constraints for all the grid nodes  
        // Left grid
        GridPane.setConstraints(sellerInfoLabel, 0, 0);
        GridPane.setConstraints(ignLabelFinal, 0, 1);
        GridPane.setConstraints(usernameLabelFinal, 0, 2);
        GridPane.setConstraints(timezoneLabelFinal, 0, 3);
        GridPane.setConstraints(sellerStateLabelFinal, 0, 4);
        GridPane.setConstraints(ignLabel, 1, 1);
        GridPane.setConstraints(usernameLabel, 1, 2);
        GridPane.setConstraints(timezoneLabel, 1, 3);
        GridPane.setConstraints(sellerStateLabel, 1, 4);
        
        // Right grid
        GridPane.setConstraints(itemInfoLabel, 0, 0);
        GridPane.setConstraints(priceLabelFinal, 0, 1);
        GridPane.setConstraints(priceLabel, 1, 1);
        GridPane.setConstraints(dateLabelFinal, 0, 2);
        GridPane.setConstraints(dateLabel, 1, 2);
        // End constraints
        
        
        // Left grid construction
        GridPane leftGrid = new GridPane();
        leftGrid.setVgap(20);
        leftGrid.setHgap(20);
        leftGrid.getChildren().addAll(
                centerUpperDisplay,
                sellerInfoLabel,
                ignLabelFinal,
                usernameLabelFinal,
                timezoneLabelFinal,
                sellerStateLabelFinal,
                ignLabel,
                usernameLabel,
                timezoneLabel,
                sellerStateLabel);
        
        
        // Right grid construction
        GridPane rightGrid = new GridPane();
        rightGrid.setVgap(20);
        rightGrid.setHgap(20);
        rightGrid.getChildren().addAll(
                itemInfoLabel,
                priceLabel,
                priceLabelFinal,
                dateLabelFinal,
                dateLabel);
 
        
        // Grid constraints for main center section
        GridPane mainCenterGrid = new GridPane();
        mainCenterGrid.setHgap(90);
        GridPane.setConstraints(leftGrid, 0, 0);
        GridPane.setConstraints(rightGrid, 1, 0);
        // Add the left and right grids to main grid
        mainCenterGrid.getChildren().addAll(leftGrid, rightGrid);
        
        
        // Center display: VBox to conatin built upper display and grid underneath
        VBox centerDisplay = new VBox(20);
        centerDisplay.setPadding(new Insets(10,15,20,15));
        centerDisplay.getChildren().addAll(centerUpperDisplay, mainCenterGrid, notesArea);
        
        
        // Main border pane construction
        BorderPane border = new BorderPane();
        border.setTop(upperDisplay);
        border.setLeft(leftDisplay);
        border.setCenter(centerDisplay);
        
        
        // Now that the border is built, we can expand the notes area across the bottom 
        notesArea.setPrefWidth(border.getWidth());
        notesArea.setPrefHeight(100);
        
        
        // Setup the stage and scene
        Scene scene = new Scene(border);
        scene.getStylesheets().add(this.getClass().getResource("veneno.css").toExternalForm());
        
        itemdisplay.setScene(scene);
        itemdisplay.setResizable(false);
        itemdisplay.setTitle("Item Viewer");
        itemdisplay.getIcons().add(resourceLoader.getIcon());
        itemdisplay.setOnCloseRequest(e -> itemdisplay.close());
        itemdisplay.show();
    }
    
    
    private void addItemToBuyList()
    {
        
    }
    
}


