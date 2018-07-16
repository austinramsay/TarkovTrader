
package tarkov.trader.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
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
    private MenuButton typeFilter;
    private MenuButton tradeStatusFilter;
    private MenuButton priceRangeFilter;
    
    TableColumn<Item, ImageView> imageColumn;
    TableColumn<Item, String> tradeStateColumn;
    TableColumn<Item, String> typeColumn;
    TableColumn<Item, String> nameColumn;
    TableColumn<Item, String> priceColumn;
    TableColumn<Item, String> ignColumn;
    TableColumn<Item, String> timezoneColumn;
    
    private boolean isPopulated;
    
    
    public Browser(TarkovTrader trader, RequestWorker worker)
    {
        this.trader = trader;
        this.worker = worker;
        
        this.isPopulated = false;
    }
    
    
    public void display()
    {
        browser = new Stage();
        browser.setOnCloseRequest(e -> close());
        
        
        menubar = new MenuBar();
        newSearch = new Menu("Start a New Search");
        menubar.getMenus().addAll(newSearch);
        
        browserLabel = new Label("Item Browser");
        browserLabel.getStyleClass().add("browserLabel");
        
        filterLabel = new Label("Filters:");
        filterLabel.getStyleClass().add("filterLabel");
        
        tradeStatusFilter = new MenuButton("Trade State");
        tradeStatusFilter.getStyleClass().add("filterMenu");
        
        typeFilter = new MenuButton("Item Type");
        typeFilter.getStyleClass().add("filterMenu");
        
        priceRangeFilter = new MenuButton("Price Range");
        priceRangeFilter.getStyleClass().add("filterMenu");
        
        refreshButton = new Button("Refresh");
        refreshButton.setGraphic(Resources.refreshIconViewer);
        refreshButton.setOnAction(e -> requestItemList());
        
        returnButton = new Button("Return");
        returnButton.setGraphic(Resources.cancelIconViewer);
        returnButton.setOnAction(e -> close());
        
        
        // Upper display will contain the 'Browser' label to be displayed on the left, and the 'Tarkov Trader' logo on the right
        HBox upperDisplay = new HBox();
        upperDisplay.setPadding(new Insets(0,20,0,20));
        upperDisplay.setAlignment(Pos.CENTER);
        upperDisplay.getStyleClass().add("hbox");
        HBox.setMargin(Resources.outlineLogoViewer, new Insets(10,10,10,450)); // This allows the logo to be pushed to the far right in the upper display
        upperDisplay.getChildren().addAll(browserLabel, Resources.outlineLogoViewer);
        upperDisplay.getStyleClass().add("hbox");
        
        
        // This VBox will house the menu bar and the upper display that contains the browser label and logo image
        VBox mainUpperDisplay = new VBox();
        mainUpperDisplay.getChildren().addAll(menubar, upperDisplay);
        
        
        VBox leftDisplay = new VBox(40);
        leftDisplay.setPadding(new Insets(0,20,20,20));
        leftDisplay.getStyleClass().add("vbox");
        leftDisplay.setAlignment(Pos.CENTER);
        leftDisplay.getChildren().addAll(filterLabel, tradeStatusFilter, typeFilter, priceRangeFilter, refreshButton, returnButton);
        
        
        // Build the TableView
        imageColumn = new TableColumn<>();
        imageColumn.setMinWidth(50);
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("itemTypeImage"));
        
        typeColumn = new TableColumn<>("Item Type"); 
        typeColumn.setMinWidth(100);
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("itemType"));
        
        tradeStateColumn = new TableColumn<>("State");
        tradeStateColumn.setMinWidth(50);
        tradeStateColumn.setCellValueFactory(new PropertyValueFactory<>("tradeState"));
        
        nameColumn = new TableColumn<>("Item Name");
        nameColumn.setMinWidth(275);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        priceColumn = new TableColumn<>("Price");
        priceColumn.setMinWidth(75);
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        
        ignColumn = new TableColumn<>("In-game Name");
        ignColumn.setMinWidth(200);
        ignColumn.setCellValueFactory(new PropertyValueFactory<>("ign"));
        
        timezoneColumn = new TableColumn<>("Timezone");
        timezoneColumn.setMinWidth(60);
        timezoneColumn.setCellValueFactory(new PropertyValueFactory<>("timezone"));
        
        table = new TableView<>();
        table.getColumns().addAll(imageColumn, typeColumn, tradeStateColumn, nameColumn, priceColumn, ignColumn, timezoneColumn);
        // End building TableView
        
        
        BorderPane border = new BorderPane();
        border.setTop(mainUpperDisplay);
        border.setLeft(leftDisplay);
        border.setCenter(table);
        
        
        Scene scene = new Scene(border);
        scene.getStylesheets().add(this.getClass().getResource("veneno.css").toExternalForm());
        browser.getIcons().add(Resources.icon);
        browser.setResizable(false);
        browser.setScene(scene);
        if (!isPopulated)
        {
            requestItemList();
            this.isPopulated = true;
        }
        browser.show();
    }
    
    
    public void populate(ArrayList<ProcessedItem> itemList)
    {
        table.setItems(getItemList(itemList));
    }
    
    
    private ObservableList<ProcessedItem> getItemList(ArrayList<ProcessedItem> itemList)
    {
        return FXCollections.observableArrayList(itemList);
    }
    
    
    private void requestItemList()
    {
        ItemListForm listrequest = new ItemListForm(buildFlagMap());
        
        // use worker to send, wait for server response, table will populate upon receiving list
        if (!worker.sendForm(listrequest))
            Platform.runLater(() -> Alert.display("Request Failed", "Failed to send item list request to server."));
    }
    
    
    private HashMap<String, String> buildFlagMap()
    {
        HashMap<String, String> searchFlags = new HashMap();
        
        searchFlags.put("itemid", "%");
        searchFlags.put("state", "%");
        searchFlags.put("type", "%");
        searchFlags.put("name", "%");
        searchFlags.put("pricemin", "");
        searchFlags.put("pricemax", "");
        searchFlags.put("ign", "%");
        searchFlags.put("username", "%");
        searchFlags.put("timezone", "%");
        searchFlags.put("keywords", "%");
        
        return searchFlags;
    }
    
    
    private void close()
    {
        browser.close();
        trader.drawMainUI();
    }
}
