
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

/**
 *
 * @author austin
 */

public class ItemDisplay {
    
    private ProcessedItem item;
    
    private Image outlineLogo;
    private Image cancelIcon;
    private Image messagesIcon;
    private Image flagIcon;
    private Image itemImage;
    private Image profileIcon;
    private ImageView outlineLogoViewer;
    private ImageView cancelIconViewer;
    private ImageView messagesIconViewer;
    private ImageView flagIconViewer;
    private ImageView itemImageViewer;
    private ImageView profileIconViewer;
    private Image icon;
    
    private Label itemListingLabel;
    private Label actionsLabel;
    private Label sellerInfoLabel;
    private Label ignLabelFinal;
    private Label usernameLabelFinal;
    private Label timezoneLabelFinal;
    private Label itemInfoLabel;
    private Label priceLabelFinal;
    private Label dateLabelFinal;
    
    private Label ignLabel;
    private Label usernameLabel;
    private Label timezoneLabel;
    private Label priceLabel;
    private Label tradeStateLabel;
    private Label itemNameLabel;
    private Label dateLabel;
    private TextArea notesArea;
    
    private Button contactButton;
    private Button profileButton;
    private Button flagButton;
    private Button returnButton;
    
    
    public ItemDisplay(ProcessedItem item)
    {
        this.item = item;
        loadResources();
        display();
    }
    
    
    private void loadResources()
    {
        icon = new Image(this.getClass().getResourceAsStream("/tarkovtradericon.png"));
        
        outlineLogo = new Image(this.getClass().getResourceAsStream("/tarkovtraderlogooutline.png"), 362, 116, true, true);
        outlineLogoViewer = new ImageView(outlineLogo);
            
        cancelIcon = new Image(this.getClass().getResourceAsStream("/cancel.png"));
        cancelIconViewer = new ImageView(cancelIcon);
            
        messagesIcon = new Image(this.getClass().getResourceAsStream("/messagesicon.png"), 24, 24, true, true);
        messagesIconViewer = new ImageView(messagesIcon);
            
        flagIcon = new Image(this.getClass().getResourceAsStream("/flag.png"), 24, 24, true, true);
        flagIconViewer = new ImageView(flagIcon);
            
        profileIcon = new Image(this.getClass().getResourceAsStream("/profileicon.png"), 24, 24, true, true);
        profileIconViewer = new ImageView(profileIcon);
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
        priceLabel = new Label(item.getPrice());
        dateLabel = new Label(item.getDate());
        
        notesArea = new TextArea();
        notesArea.setText("Seller Notes: " + item.getNotes());
        notesArea.setWrapText(true);
        notesArea.setEditable(false);
        
        // Button initialization
        contactButton = new Button("Contact Seller");
        contactButton.setGraphic(messagesIconViewer);
        
        profileButton = new Button("Seller Profile");
        profileButton.setGraphic(profileIconViewer);
        
        flagButton = new Button("Flag Item");
        flagButton.setGraphic(flagIconViewer);
        
        returnButton = new Button("Return");
        returnButton.setGraphic(cancelIconViewer);
        returnButton.setOnAction(e -> itemdisplay.close());
        
        
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
              
        upperDisplayRight.getChildren().add(outlineLogoViewer);
        
        upperDisplay.getChildren().addAll(itemListingLabel, upperDisplayRight);  // Note: itemListingLabel has padding 20 left on initialization
        
        upperDisplay.getStyleClass().add("hbox");
        // End upper display construction
        
        
        // Left display construction
        // This will contain all nodes related to 'actions' for the user
        VBox leftDisplay = new VBox(40);
        leftDisplay.setPadding(new Insets(0,20,20,20));
        leftDisplay.setAlignment(Pos.CENTER);
        leftDisplay.getChildren().addAll(actionsLabel, contactButton, profileButton, flagButton, returnButton);    
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
        GridPane.setConstraints(ignLabel, 1, 1);
        GridPane.setConstraints(usernameLabel, 1, 2);
        GridPane.setConstraints(timezoneLabel, 1, 3);
        
        // Right grid
        GridPane.setConstraints(itemInfoLabel, 0, 0);
        GridPane.setConstraints(priceLabelFinal, 0, 1);
        GridPane.setConstraints(priceLabel, 1, 1);
        GridPane.setConstraints(dateLabelFinal, 0, 2);
        GridPane.setConstraints(dateLabel, 1, 2);
        // End constraints
        
        
        // Left grid construction
        GridPane leftGrid = new GridPane();
        //leftGrid.setPadding(new Insets(0,0,0,0));
        leftGrid.setVgap(20);
        leftGrid.setHgap(20);
        leftGrid.getChildren().addAll(
                centerUpperDisplay,
                sellerInfoLabel,
                ignLabelFinal,
                usernameLabelFinal,
                timezoneLabelFinal,
                ignLabel,
                usernameLabel,
                timezoneLabel);
        
        
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
        //notesArea.setPadding(new Insets(20,0,0,0));
        notesArea.setPrefWidth(border.getWidth());
        notesArea.setPrefHeight(100);
        
        
        // Setup the stage and scene
        Scene scene = new Scene(border);
        scene.getStylesheets().add(this.getClass().getResource("veneno.css").toExternalForm());
        itemdisplay.setScene(scene);
        itemdisplay.setResizable(false);
        itemdisplay.setTitle("Item Viewer");
        itemdisplay.getIcons().add(icon);
        itemdisplay.setOnCloseRequest(e -> itemdisplay.close());
        itemdisplay.show();
    }
    
}


