
package tarkov.trader.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tarkov.trader.objects.AccountFlag;
import tarkov.trader.objects.Profile;
import tarkov.trader.objects.Sale;

/**
 *
 * @author austin
 */

public class ProfileDisplay {
    
    private RequestWorker worker;
    private Profile profile;
    
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
    
    private TableView salesTable;
    private TableColumn<Sale, String> buyerNameColumn;
    private TableColumn<Sale, String> conditionColumn;
    private TableColumn<Sale, String> confirmationColumn;
    private TableColumn<Sale, String> itemNameColumn;
    private TableColumn<Sale, String> priceColumn;
    
    public ProfileDisplay(RequestWorker worker, Profile profile)
    {
        this.worker = worker;
        this.profile = profile;
        
        display();
    }
    
    private void display()
    {
        profileDisplay = new Stage();
        
        profileLabel = new Label("Profile");
        profileLabel.getStyleClass().add("windowLabel");      
        
        flagsLabel = new Label("Seller Information:");
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
        if (profile.getUsername().equals(TarkovTrader.username))
            contactButton.setDisable(true);
        
        returnButton = new Button("Return");
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
        upperDisplayRight.getChildren().add(Resources.outlineLogoViewer);
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
        
        for (AccountFlag flag : profile.getFlags())
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
        
        
        // Build Sales Table
        salesTable = new TableView();
        salesTable.setMaxHeight(150);
        //salesTable.setMinWidth(400);
        
        buyerNameColumn = new TableColumn<>("Buyer Name");
        buyerNameColumn.setCellValueFactory(new PropertyValueFactory("buyerName"));
        buyerNameColumn.setMinWidth(120);
        buyerNameColumn.setMaxWidth(120);
        
        conditionColumn = new TableColumn<>("State");
        conditionColumn.setCellValueFactory(new PropertyValueFactory("saleTypeDesc"));
        conditionColumn.setMinWidth(100);
        conditionColumn.setMaxWidth(100);
        
        confirmationColumn = new TableColumn<>("Status");
        confirmationColumn.setCellValueFactory(new PropertyValueFactory("saleStatusDesc"));
        confirmationColumn.setMinWidth(200);
        confirmationColumn.setMaxWidth(200);

        itemNameColumn = new TableColumn<>("Item Name");
        itemNameColumn.setCellValueFactory(new PropertyValueFactory("itemName"));
        itemNameColumn.setMinWidth(300);
        itemNameColumn.setMaxWidth(300);

        priceColumn = new TableColumn<>("Sale Date");
        priceColumn.setCellValueFactory(new PropertyValueFactory("saleDate"));        
        priceColumn.setMinWidth(130);
        priceColumn.setMaxWidth(130);
        
        salesTable.getColumns().addAll(buyerNameColumn, conditionColumn, confirmationColumn, itemNameColumn, priceColumn);
        salesTable.setItems(FXCollections.observableArrayList(profile.getSales()));
        
        // Build Sales display area
        VBox salesDisplay = new VBox(7);
        salesDisplay.getChildren().addAll(salesLabel, salesTable);
        
        
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
        profileDisplay.setScene(scene);
        profileDisplay.setTitle("Profile");
        profileDisplay.setOnCloseRequest(e -> close());
        profileDisplay.setResizable(false);
        scene.getStylesheets().add(this.getClass().getResource("veneno.css").toExternalForm());
        profileDisplay.show();
    }
    
    
    private void close()
    {
        profileDisplay.close();
    }
}
