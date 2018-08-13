package tarkov.trader.client;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javax.xml.bind.DatatypeConverter;
import tarkov.trader.objects.LoginForm;
import tarkov.trader.objects.Form;

/**
 *
 * @author austin
 */

public class LoginPrompt {
    
    private TarkovTrader tarkovtrader;
    private RequestWorker worker;
    private Resources resourceLoader;
    
    public static volatile boolean acknowledged;
    public Stage loginStage;
    public TextField usernameInput;
    public PasswordField passwordInput;
    private Label nameLabel;
    private Label passLabel;
    public Button loginButton;
    public Button cancelButton;
    public Button newAccountButton;
    
    
    public LoginPrompt(TarkovTrader tarkovtrader)
    {
        this.tarkovtrader = tarkovtrader;
        this.worker = tarkovtrader.getWorker();
        this.resourceLoader = new Resources();
        resourceLoader.load();
    }
    
    
    public void display() 
    {
        LoginPrompt.acknowledged = false;
        loginStage = new Stage();
        
        
        nameLabel = new Label("Username:");
        passLabel = new Label("Password:");
        usernameInput = new TextField();
        passwordInput = new PasswordField();
        passwordInput.setPromptText("Password");
        passwordInput.setOnAction(e -> submit());
        
        loginButton = new Button("Login");
        loginButton.setOnAction(e -> submit());

        cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> close());
        
        newAccountButton = new Button("New account");
        newAccountButton.setOnAction(e -> launchNewAccountStage());
        
        
        // upperdisplay houses Tarkov Trader logo
        HBox upperDisplay = new HBox();
        upperDisplay.setAlignment(Pos.CENTER);
        upperDisplay.getChildren().add(resourceLoader.getLogo());
        
        
        // Grid construction for labels and input fields
        GridPane.setConstraints(nameLabel, 0, 0);
        GridPane.setConstraints(passLabel, 0, 1);
        GridPane.setConstraints(usernameInput, 1, 0);
        GridPane.setConstraints(passwordInput, 1, 1);
        
        GridPane grid = new GridPane();       
        grid.setVgap(8); 
        grid.setHgap(10); 
        grid.setAlignment(Pos.CENTER);
        grid.getChildren().addAll(nameLabel, passLabel, usernameInput, passwordInput);
        // End grid
        
        
        // lowerdisplay houses the 3 buttons on the bottom of the login prompt
        HBox lowerDisplay = new HBox(10); 
        lowerDisplay.setAlignment(Pos.CENTER);
        lowerDisplay.getChildren().addAll(loginButton, newAccountButton, cancelButton);
        
        
        // Main scene layout is VBox with 12 spacing and 20 padding all around
        VBox layout = new VBox(12);
        layout.setPadding(new Insets(0,0,10,0));
        layout.getChildren().addAll(upperDisplay, grid, lowerDisplay);
        
        
        Scene scene = new Scene(layout);
        scene.getStylesheets().add(this.getClass().getResource("veneno.css").toExternalForm());
        loginStage.setOnCloseRequest(e -> this.close());
        loginStage.setTitle("Tarkov Trader Login");
        loginStage.getIcons().add(resourceLoader.getIcon());
        loginStage.setResizable(false);
        loginStage.setScene(scene);
        loginStage.show();
        
    }
    
    
    public String getUsername()
    {
        return usernameInput.getText();
    }
    
    
    public String getPassword()
    {
        return passwordInput.getText();
    }
    
    
    private void clearFields()
    {
        usernameInput.setText(null);
        passwordInput.setText(null);
    }
    
        
    public void startMainUI()
    {
        loginStage.close();
        tarkovtrader.drawMainUI();
    }
    
    
    public void launchNewAccountStage()
    {
        NewAccountStage accountStage = new NewAccountStage(this, worker);
        loginStage.close();
        accountStage.display();
    }
    
    
    public String getHashedPassword()
    {
        byte[] hashedBytes = null;
        
        try 
        {
            MessageDigest passDigest = MessageDigest.getInstance("SHA-512");
            hashedBytes = passDigest.digest(getPassword().getBytes(StandardCharsets.UTF_8));
            String hashedPassword = DatatypeConverter.printHexBinary(hashedBytes);
            return hashedPassword;
            
        } catch (NoSuchAlgorithmException e)
        {
            Platform.runLater(() -> Alert.display("Security", "Failed to secure password before sending to server."));
            return null;
        }
    }
    
    
    private void submit()
    {
        if (verifyFormIntegrity())
            attemptAuthentication();
        else
            Platform.runLater(() -> Alert.display("Enter Credentials", "Please input a username and password."));
    }
    
    
    private boolean attemptAuthentication()
    {
        Form loginRequest = new LoginForm(getUsername(), getHashedPassword());

        if (worker.sendForm(loginRequest))
        {
            while(awaitingAuthentication())
            {
                ; // Awaiting authentication
            }
            
            // Authentication succeeds -> draw the main user interface
            if (TarkovTrader.authenticated)
            {
                startMainUI();
                return true;
            }
            
            // Authentication failed
            // Error message sent from server-side and has been viewed by client
            // Reset the login fields
            clearFields();
            LoginPrompt.acknowledged = false;
        }
        
        return false;
    }
    
    
    private boolean awaitingAuthentication()
    {
        return !LoginPrompt.acknowledged;
    }
    
    
    private boolean verifyFormIntegrity()
    {
        if ((getUsername().equals("")) || (getPassword().equals("")))
            return false;
        
        return true;
    }
    
    
    private void close()
    {
        loginStage.close();
        tarkovtrader.close();
    }
    
}




