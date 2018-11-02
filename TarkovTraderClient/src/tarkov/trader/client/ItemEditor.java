
package tarkov.trader.client;

import java.io.File;
import java.text.NumberFormat;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tarkov.trader.objects.Item;
import tarkov.trader.objects.ItemModificationRequest;

/**
 *
 * @author austin
 */

public class ItemEditor {
    
    private Moderator moderator;
    private RequestWorker worker;
    private Item item;
    
    private Stage itemEditor;
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
    private Button submit;
    private Button cancel;
    private Image logo;
    private Image icon;
    private ImageView logoViewer;
    
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
    
    
    public ItemEditor(Moderator moderator, RequestWorker worker, Item item)
    {
        this.moderator = moderator;
        this.worker = worker;
        this.item = item;
        loadResources();
        display();
    }
    
    
    private void loadResources()
    {
        icon = new Image(this.getClass().getResourceAsStream("/tarkovtradericon.png"));
        
        logo = new Image(this.getClass().getResourceAsStream("/tarkovtraderlogo.png"), 362, 116, true, true);
        logoViewer = new ImageView(logo);        
    }
    
    
    private void display()
    {
        itemEditor = new Stage();
        
        postTypeLabel = new Label("Listing type:");
        
        itemTypeLabel = new Label("Type of item:");
        
        itemNameLabel = new Label("Name of item:");
        
        priceLabel = new Label("Price:");
        
        ignLabel = new Label(item.getIgn());
        ignLabel.setStyle("-fx-font-weight: normal;");
        
        ignLabelFinal = new Label("Your IGN:");
        
        usernameLabel = new Label(item.getUsername());
        usernameLabel.setStyle("-fx-font-weight: normal;");
        
        usernameLabelFinal = new Label("Trader username:");
        
        timezoneLabelFinal = new Label("Timezone:");
        
        timezoneLabel = new Label(item.getTimezone());
        timezoneLabel.setStyle("-fx-font-weight: normal;");
        
        keywordsLabel = new Label("Search Keywords:");
        
        imageLabel = new Label("Image:");
        
        selectedImageLabel = new Label();
        if (item.getImageFile() == null)
            selectedImageLabel.setText("No Image Chosen");
        else
            selectedImageLabel.setText(item.getImageFile().getName());
        selectedImageLabel.setStyle("-fx-font-weight: normal;");
        
        notesLabel = new Label("Notes:");
        
        postTypeDropdown = new ChoiceBox<>();
        postTypeDropdown.getItems().addAll("WTS", "WTB");
        postTypeDropdown.getSelectionModel().select(item.getTradeState());  // WTS is default value
        postTypeDropdown.setOnAction(e -> adjustPriceInput());
        
        itemTypeDropdown = new ChoiceBox<>();
        itemTypeDropdown.getItems().addAll("Key", "Keybar", "Docs Case", "Storage Case", "Secure Container", "Weapon", "Weapon Mod", "Armor/Helmet", "Apparel", "Ammo", "Medicine", "Misc");
        itemTypeDropdown.setMinWidth(200);
        itemTypeDropdown.getSelectionModel().select(item.getItemType());
        
        itemNameInput = new TextField();
        itemNameInput.setPromptText("Name");
        itemNameInput.setText(item.getName());
        
        priceInput = new TextField();
        priceInput.setPromptText("Ex. 250,000");
        
        priceInput.textProperty().addListener(new ChangeListener<String>() {
        @Override 
        public void changed(ObservableValue<? extends String> observable, String oldValue, 
            String newValue) {
            if (!priceInput.isDisabled()) {
                if (newValue.matches("[0-9,]*")) {
                    priceInput.setText(newValue);
                } else {
                    priceInput.setText(oldValue);
            } }
        }
        });
        
        keywordsInput = new TextField();
        keywordsInput.setPromptText("Ex. Interchange, Customs");
        keywordsInput.setText(item.getKeywords());
        
        notesInput = new TextArea();
        notesInput.setPrefHeight(125);
        notesInput.setPrefWidth(175);
        notesInput.setPromptText("Ex. Will trade for bitcoin");
        notesInput.setText(item.getNotes());
        notesInput.setWrapText(true);
        
        chooseImage = new Button("Choose...");
        chooseImage.setOnAction(e -> itemImageFile = getImage());
        
        // Set the image from the paramter Item
        itemImageFile = item.getImageFile();
        
        submit = new Button("Submit Changes");
        submit.setOnAction(e -> submit());
        
        cancel = new Button("Cancel");
        cancel.setOnAction(e -> close());
        
        
        // Layout construction
        
        // Upper display will house the logo
        HBox upperDisplay = new HBox();
        upperDisplay.setAlignment(Pos.CENTER);
        upperDisplay.getChildren().add(logoViewer);
        
        
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
        lowerDisplay.getChildren().addAll(submit,cancel);
        
        
        BorderPane border = new BorderPane();
        border.setTop(upperDisplay);
        border.setCenter(grid);
        border.setBottom(lowerDisplay);
        
        
        scene = new Scene(border);
        scene.getStylesheets().add(this.getClass().getResource("veneno.css").toExternalForm());
        itemEditor.setTitle("Edit an Item Listing");
        itemEditor.getIcons().add(icon);
        itemEditor.setOnCloseRequest(e -> close());
        itemEditor.setScene(scene);
        itemEditor.setResizable(false);
        itemEditor.show();
        
        adjustPriceInput();
    }
    
    
    private void adjustPriceInput()
    {
        if (postTypeDropdown.getSelectionModel().getSelectedItem().equals("WTB"))
        {
            priceInput.setDisable(true);
            priceInput.setText(null);
            priceInput.setPromptText("Not necessary");
        }
        else
        {
            priceInput.setDisable(false);
            priceInput.setText(NumberFormat.getNumberInstance().format(item.getPrice()));
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
        ign = item.getIgn();
        username = item.getUsername();
        timezone = item.getTimezone();
        keywords = keywordsInput.getText();
        notes = notesInput.getText();
        
               
        if (itemType == null) // Not really necessary to check string length, options are pre-built
        {
            Platform.runLater(() -> Alert.display(null, "No item type was selected."));
            return false;
        }
        
        
        if (itemName.equals("") || !isBetween(itemName.length(), 2, 38))  // Not really necessary but just in case
        {
            Platform.runLater(() -> Alert.display(null, "Item name must be between 3 and 38 characters."));
            return false;
        }
        
        
        if (!isBetween(keywords.length(), 0, 255))
        {
            Platform.runLater(() -> Alert.display(null, "Keywords must have a maximum length of 255 characters."));
            return false;
        }
        
        
        if (!isBetween(notes.length(), 0, 255))
        {
            Platform.runLater(() -> Alert.display(null, "Notes must have a maximum length of 255 characters."));
            return false;            
        }
        
        
        if (!postType.equals("WTB"))
            try { 
                String unformattedPrice = priceInput.getText();
                String formattedPrice = unformattedPrice.replaceAll("[,]", "");
                price = Integer.parseInt(formattedPrice); 
            } catch (NumberFormatException e) { Platform.runLater(() -> Alert.display(null, "A number was not found in the price input field.")); return false; }
        
        
        if (!postType.equals("WTB") && (price < 1) || (price > 50000000))
        {
            Platform.runLater(() -> Alert.display(null, "Specified price is out of range. Enter a number greater than 1 and less than 50 million."));
            return false;
        }
        
        return true;
    }
    
    
    private boolean isBetween(int value, int min, int max)
    {
        // Note: database will not take above 16 chars
        return ((value >= min) && (value <= max));
    }
    
    
    private boolean itemWasEdited()
    {
        if (!item.getTradeState().equals(postType))
            return true;
        if (!item.getItemType().equals(itemType))
            return true;
        if (!item.getName().equals(itemName))
            return true;
        if (!item.getKeywords().equals(keywords))
            return true;
        if (!item.getNotes().equals(notes))
            return true;
        if (item.getImageFile() != itemImageFile)
            return true;
        if (!postType.equals("WTB"))
        {
            if (item.getPrice() != price)
                return true;
        }
        
        return false;
    }
    
    
    private boolean submit()
    {
        if (!verifiedFormIntegrity())
            return false;
        
        if (!itemWasEdited())
        {
            Platform.runLater(() -> Alert.display(null, "No changes to submit."));
            return false;
        }
        
        Item editedItem = new Item(itemImageFile, postType, itemType, itemName, price, ign, username, timezone, keywords, notes);
        
        ItemModificationRequest itemEditRequest = new ItemModificationRequest("edit", moderator.buildFilterFlags(), item, editedItem);
        
        if (worker.sendForm(itemEditRequest))
        {
            this.close();
        }
        
        return true;
    }

    
    private void close()
    {
        itemEditor.close();
    }
        
    
}
