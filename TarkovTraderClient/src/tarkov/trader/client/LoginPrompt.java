package tarkov.trader.client;

import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import tarkov.trader.objects.LoginForm;

/**
 *
 * @author austin
 */

public class LoginPrompt {
    
    private TarkovTrader tarkovtrader;
    private RequestWorker worker;
    
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
        
        loginStage = new Stage();
        
        loginStage.setTitle("Tarkov Trader Login");
        
        nameLabel = new Label("Username:");
        passLabel = new Label("Password:");
        usernameInput = new TextField();
        passwordInput = new PasswordField();
        passwordInput.setPromptText("Password");
        passwordInput.setOnAction(e -> attemptAuthentication());
        
        loginButton = new Button("Login");
        loginButton.setOnAction(e -> attemptAuthentication());

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
        loginStage.getIcons().add(Resources.icon);
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
        
    
    public void startMainUI()
    {
            tarkovtrader.setUsername(getUsername());
            loginStage.close();
            
            tarkovtrader.drawMainUI();
    }
    
    
    public void launchNewAccountStage()
    {
        NewAccountStage accountStage = new NewAccountStage(this, worker);
        loginStage.close();
        accountStage.display();
    }
    
    
    private void attemptAuthentication()
    {
        LoginForm packedLogin = new LoginForm(getUsername(), getPassword());
        LinkedHashMap<String, Object> loginMap = new LinkedHashMap();
        loginMap.put("login", packedLogin);
        
        if (worker.sendForm(loginMap))
        {
            System.out.println("sent: " + packedLogin.getUsername() + packedLogin.getPassword());
            while (!TarkovTrader.authenticated)
            {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run()
                    {
                        //Platform.runLater(() -> Alert.display("Login", "Authentication failed. Check username and password."));
                    }
                }, 2000);    
            }
            if (TarkovTrader.authenticated)
                startMainUI();
        }
    }
    
    
    private void close()
    {
        tarkovtrader.close();
    }
    
}
