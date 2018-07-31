
package tarkov.trader.client;

import java.io.File;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tarkov.trader.objects.Item;
import tarkov.trader.objects.ItemForm;

/**
 *
 * @author austin
 */

public class AddItemStage {
    
    private TarkovTrader trader;
    private RequestWorker worker;
    
    private Stage addItemStage;
    private Scene scene;
    private Label postTypeLabel;
    private Label itemTypeLabel;
    private Label itemNameLabel;
    private Label priceLabel;
    private Label ignLabel;
    private Label ignLabelFinal;
    private Label usernameLabel;
    private Label usernameLabelFinal;
    private Label timezoneLabelFinal;
    private Label timezoneLabel;
    private Label keywordsLabel;
    private Label imageLabel;
    private Label selectedImageLabel;
    private Label notesLabel;
    private ChoiceBox<String> postTypeDropdown;
    private ChoiceBox<String> itemTypeDropdown;
    private TextField itemNameInput;
    private TextField priceInput;
    private TextField keywordsInput;
    private TextArea notesInput;
    private Button chooseImage;
    private Button create;
    private Button cancel;
    
    private String postType;
    private String itemType;
    private String itemName;
    private int price;
    private String ign;
    private String username;
    private String timezone;
    private String keywords;
    private String notes;
    private File itemImageFile;
    
    
    public AddItemStage(TarkovTrader trader, RequestWorker worker)
    {
        this.trader = trader;
        this.worker = worker;
    }
    
    
    public void display()
    {
        addItemStage = new Stage();
        
        postTypeLabel = new Label("Listing type:");
        
        itemTypeLabel = new Label("Type of item:");
        
        itemNameLabel = new Label("Name of item:");
        
        priceLabel = new Label("Price:");
        
        ignLabel = new Label(TarkovTrader.ign);
        ignLabel.setStyle("-fx-font-weight: normal;");
        
        ignLabelFinal = new Label("Your IGN:");
        
        usernameLabel = new Label(TarkovTrader.username);
        usernameLabel.setStyle("-fx-font-weight: normal;");
        
        usernameLabelFinal = new Label("Trader username:");
        
        timezoneLabelFinal = new Label("Timezone:");
        
        timezoneLabel = new Label(TarkovTrader.timezone);
        timezoneLabel.setStyle("-fx-font-weight: normal;");
        
        keywordsLabel = new Label("Search Keywords:");
        
        imageLabel = new Label("Image:");
        
        selectedImageLabel = new Label("No image chosen.");
        selectedImageLabel.setStyle("-fx-font-weight: normal;");
        
        notesLabel = new Label("Notes:");
        
        postTypeDropdown = new ChoiceBox<>();
        postTypeDropdown.getItems().addAll("WTS", "WTB");
        postTypeDropdown.getSelectionModel().select(0);  // WTS is default value
        postTypeDropdown.setOnAction(e -> adjustPriceInput());
        
        itemTypeDropdown = new ChoiceBox<>();
        itemTypeDropdown.getItems().addAll("Key", "Keybar", "Docs Case", "Storage Case", "Secure Container", "Weapon", "Weapon Mod", "Armor/Helmet", "Apparel", "Ammo", "Medicine", "Misc");
        itemTypeDropdown.setMinWidth(200);
        
        itemNameInput = new TextField();
        itemNameInput.setPromptText("Name");
        
        priceInput = new TextField();
        priceInput.setPromptText("Price");
        
        keywordsInput = new TextField();
        keywordsInput.setPromptText("Ex. Interchange, Customs");
        
        notesInput = new TextArea();
        notesInput.setPrefHeight(125);
        notesInput.setPrefWidth(175);
        notesInput.setPromptText("Ex. Will trade for bitcoin");
        notesInput.setWrapText(true);
        
        chooseImage = new Button("Choose...");
        chooseImage.setOnAction(e -> itemImageFile = getImage());
        
        create = new Button("Create");
        create.setOnAction(e -> submit());
        
        cancel = new Button("Cancel");
        cancel.setOnAction(e -> close());
        
        
        // Layout construction
        
        // Upper display will house the logo
        HBox upperDisplay = new HBox();
        upperDisplay.setAlignment(Pos.CENTER);
        upperDisplay.getChildren().add(Resources.logoViewer);
        
        
        // Set positions for all objects
        GridPane.setConstraints(postTypeLabel, 0, 0);
        GridPane.setConstraints(itemTypeLabel, 0, 1);
        GridPane.setConstraints(itemNameLabel, 0, 2);
        GridPane.setConstraints(priceLabel, 0, 3);
        GridPane.setConstraints(ignLabelFinal, 0, 4);
        GridPane.setConstraints(usernameLabelFinal, 0, 5);
        GridPane.setConstraints(timezoneLabelFinal, 0, 6);
        GridPane.setConstraints(keywordsLabel, 0, 7);
        GridPane.setConstraints(imageLabel, 0, 8);
        GridPane.setConstraints(notesLabel, 0, 10);
        GridPane.setConstraints(postTypeDropdown, 1, 0);
        GridPane.setConstraints(itemTypeDropdown, 1, 1);
        GridPane.setConstraints(itemNameInput, 1, 2);
        GridPane.setConstraints(priceInput, 1, 3);
        GridPane.setConstraints(ignLabel, 1, 4);
        GridPane.setConstraints(usernameLabel, 1, 5);
        GridPane.setConstraints(timezoneLabel, 1, 6);
        GridPane.setConstraints(keywordsInput, 1, 7);
        GridPane.setConstraints(selectedImageLabel, 1, 8);
        GridPane.setConstraints(chooseImage, 1, 9);
        GridPane.setConstraints(notesInput, 1, 10);
        
        
        // GridPane displays input fields and labels
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(15); // Sets the vertical gap between grid cells
        grid.setHgap(15); // Sets the horizontal gap between grid cells
        grid.getChildren().addAll(postTypeLabel, 
                itemTypeLabel, 
                itemNameLabel, 
                itemNameInput, 
                priceLabel, 
                ignLabelFinal, 
                usernameLabelFinal, 
                timezoneLabelFinal,
                keywordsLabel, 
                imageLabel, 
                notesLabel, 
                postTypeDropdown, 
                itemTypeDropdown, 
                priceInput,
                ignLabel,
                usernameLabel, 
                timezoneLabel, 
                keywordsInput, 
                selectedImageLabel, 
                chooseImage, 
                notesInput);
        
        
        // Displays 'Create' and 'Cancel' buttons
        HBox lowerDisplay = new HBox(25);
        lowerDisplay.setPadding(new Insets(5,0,13,0));
        lowerDisplay.setAlignment(Pos.CENTER);
        lowerDisplay.getChildren().addAll(create,cancel);
        
        
        BorderPane border = new BorderPane();
        border.setTop(upperDisplay);
        border.setCenter(grid);
        border.setBottom(lowerDisplay);
        
        
        scene = new Scene(border);
        scene.getStylesheets().add(this.getClass().getResource("veneno.css").toExternalForm());
        addItemStage.setTitle("Create an Item Listing");
        addItemStage.getIcons().add(Resources.icon);
        addItemStage.setOnCloseRequest(e -> close());
        addItemStage.setScene(scene);
        addItemStage.setResizable(false);
        addItemStage.show();
    }
    
    
    private void adjustPriceInput()
    {
        if (postTypeDropdown.getSelectionModel().getSelectedItem().equals("WTB"))
        {
            priceInput.setDisable(true);
            priceInput.setPromptText("Not necessary");
        }
        else
        {
            priceInput.setDisable(false);
            priceInput.setPromptText("Price");
        }
    }
    
    
    private File getImage()
    {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose an Avatar");
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PNG Image", "*.png"),
                new FileChooser.ExtensionFilter("JPEG Image", "*.jpg"));
        
        File avatar = chooser.showOpenDialog(null);
        
        if (avatar != null)
        {
            selectedImageLabel.setText(avatar.getName());
            return avatar;
        }
        else 
        {
            selectedImageLabel.setText("No image chosen.");
            return null;
        }
    }
    
    
    private boolean verifiedFormIntegrity()
    {
        // This method gets the text fields and applies them to the corresponding Strings to be prepared for a new item entry
        // Each field will be checked for integrity
        
        postType = postTypeDropdown.getSelectionModel().getSelectedItem();
        itemType = itemTypeDropdown.getSelectionModel().getSelectedItem();
        itemName = itemNameInput.getText();
        ign = TarkovTrader.ign;
        username = TarkovTrader.username;
        timezone = TarkovTrader.timezone;
        keywords = keywordsInput.getText();
        notes = notesInput.getText();
        
        
        
        if (itemType == null) // Not really necessary to check string length, options are pre-built
        {
            Platform.runLater(() -> Alert.display("Required Field", "No item type was selected."));
            return false;
        }
        
        
        if (itemName.equals("") || !isBetween(itemName.length(), 2, 38))  // Not really necessary but just in case
        {
            Platform.runLater(() -> Alert.display("Name Length", "Item name must be between 3 and 38 characters."));
            return false;
        }
        
        
        if (!isBetween(keywords.length(), 0, 255))
        {
            Platform.runLater(() -> Alert.display("Keywords Length", "Keywords must have a maximum length of 255 characters."));
            return false;
        }
        
        
        if (!isBetween(notes.length(), 0, 255))
        {
            Platform.runLater(() -> Alert.display("Notes Length", "Notes must have a maximum length of 255 characters."));
            return false;            
        }
        
        
        if (!postType.equals("WTB"))
            try { price = Integer.parseInt(priceInput.getText()); } catch (NumberFormatException e) { Platform.runLater(() -> Alert.display("Name Length", "A number was not found in the price input field.")); return false; }
        
        
        if (!postType.equals("WTB") && (price < 1) || (price > 50000000))
        {
            Platform.runLater(() -> Alert.display("Price Range", "Specified price is out of range. Enter a number greater than 1 and less than 50 million."));
            return false;
        }
        
        return true;
    }
    
    
    private boolean isBetween(int value, int min, int max)
    {
        // Note: database will not take above 16 chars
        return ((value >= min) && (value <= max));
    }
    
    
    private boolean submit()
    {
        if (!verifiedFormIntegrity())
            return false;
        
        Item newItem = new Item(itemImageFile, postType, itemType, itemName, price, ign, username, timezone, keywords, notes);
                
        ItemForm newItemForm = new ItemForm(newItem);
        
        if (worker.sendForm(newItemForm))
        {
            this.close();
        }
        
        return true;
    }

    
    private void close()
    {
        addItemStage.close();
        trader.drawMainUI();
    }
        
    
}
