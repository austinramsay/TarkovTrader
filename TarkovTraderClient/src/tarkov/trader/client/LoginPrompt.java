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
    
    public static volatile boolean acknowledged;
    public Stage loginStage;
    public TextField usernameInput;
    public PasswordField passwordInput;
    private Label nameLabel;
    private Label passLabel;
    public Button loginButton;
    public Button cancelButton;
    public Button newAccountButton;
    
    
    public LoginPrompt(TarkovTrader tarkovtrader, RequestWorker worker)
    {
        this.tarkovtrader = tarkovtrader;
        this.worker = worker;
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
        
        
        
        HBox upperDisplay = new HBox();
        upperDisplay.setAlignment(Pos.CENTER);
        upperDisplay.getChildren().add(Resources.logoViewer);
        
        GridPane.setConstraints(nameLabel, 0, 0);
        GridPane.setConstraints(passLabel, 0, 1);
        GridPane.setConstraints(usernameInput, 1, 0);
        GridPane.setConstraints(passwordInput, 1, 1);
        
        HBox lowerDisplay = new HBox(10); // Horizontal box on the last row of the gridpane to center the login button
        lowerDisplay.setAlignment(Pos.CENTER);
        lowerDisplay.getChildren().addAll(loginButton, newAccountButton, cancelButton);
        BorderPane.setMargin(lowerDisplay, new Insets(5,0,0,0));
        
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));          
        grid.setVgap(8); // Sets the vertical gap between grid cells
        grid.setHgap(10); // Sets the horizontal gap between grid cells
        grid.getChildren().addAll(nameLabel, passLabel, usernameInput, passwordInput);
        
        BorderPane border = new BorderPane();
        border.setPadding(new Insets(20, 20, 20, 20));
        border.setTop(upperDisplay);
        border.setCenter(grid);
        border.setBottom(lowerDisplay);
        
        Scene scene = new Scene(border);
        scene.getStylesheets().add(this.getClass().getResource("veneno.css").toExternalForm());
        loginStage.setOnCloseRequest(e -> this.close());
        loginStage.setTitle("Tarkov Trader Login");
        loginStage.getIcons().add(Resources.icon);
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




