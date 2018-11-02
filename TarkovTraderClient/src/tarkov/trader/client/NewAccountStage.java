
package tarkov.trader.client;

/**
 *
 * @author austin
 */

import tarkov.trader.objects.NewAccountForm;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.stage.FileChooser.ExtensionFilter;
import javax.xml.bind.DatatypeConverter;
import tarkov.trader.objects.Form;


public class NewAccountStage 
{
    private RequestWorker worker;
    private LoginPrompt login;
    private Resources resourceLoader;
    
    private Stage newAccountStage;
    private Scene scene;
    private Image logo;
    private ImageView logoView;
    private Label nameLabel;
    private Label passLabel;
    private Label confPassLabel;
    private Label firstNameLabel;
    private Label lastNameLabel;
    private Label ignLabel;
    private Label timezoneLabel;
    private Label imageLabel;
    private Label selectedAvatarLabel;
    private TextField usernameInput;
    private PasswordField passwordField;
    private PasswordField confPasswordField;
    private TextField firstNameInput;
    private TextField lastNameInput;
    private TextField ignInput;
    private Button chooseButton;
    private Button createButton;
    private Button cancelButton;
    private ComboBox timezoneDropdown;
    
    private boolean passwordMatch = false;
    private final String noAvatarMessage = "No avatar chosen.\nA default will be used.";
    private File userImageFile;
    
    
    public NewAccountStage(LoginPrompt login, RequestWorker worker)
    {
        this.login = login;
        this.worker = worker;
        this.resourceLoader = new Resources();
    }
    
    
    public void display()
    {      
        Platform.runLater(() -> Alert.displayNotification(null, "Warning: It is not recommended to use your in-game password!", 7));
        
        newAccountStage = new Stage();
        newAccountStage.setTitle("Create a New Account");
 
        
        nameLabel = new Label("Username:");
        passLabel = new Label("Password:");
        confPassLabel = new Label("Confirm password:");
        firstNameLabel = new Label("First name:");
        lastNameLabel = new Label("Last name:");
        ignLabel = new Label("In-game name:");
        timezoneLabel = new Label("Timezone:");
        imageLabel = new Label("Avatar:");
        
        selectedAvatarLabel = new Label(noAvatarMessage);
        selectedAvatarLabel.setWrapText(true);
        
        usernameInput = new TextField();
        usernameInput.setPromptText("Username");
        
        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        
        confPasswordField = new PasswordField();
        confPasswordField.setPromptText("Confirm Password");
        
        firstNameInput = new TextField();
        firstNameInput.setPromptText("First");
        
        lastNameInput = new TextField();
        lastNameInput.setPromptText("Last");
        
        ignInput = new TextField();
        ignInput.setPromptText("IGN");
        
        timezoneDropdown = new ComboBox();
        timezoneDropdown.setEditable(true);
        timezoneDropdown.getItems().addAll("PST", "MST", "CST", "EST");
        timezoneDropdown.setPromptText("Select or Type Here");
        
        chooseButton = new Button("Choose...");
        chooseButton.setOnAction(e -> userImageFile = getAvatar());
        
        createButton = new Button("Create");
        createButton.setOnAction(e -> submit());
        
        cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> close());
        
        GridPane.setConstraints(nameLabel, 0, 0);
        GridPane.setConstraints(passLabel, 0, 1);
        GridPane.setConstraints(confPassLabel, 0, 2);
        GridPane.setConstraints(firstNameLabel, 0, 3);
        GridPane.setConstraints(lastNameLabel, 0, 4);
        GridPane.setConstraints(ignLabel, 0, 5);
        GridPane.setConstraints(timezoneLabel, 0, 6);
        GridPane.setConstraints(imageLabel, 0, 7);
        GridPane.setConstraints(usernameInput, 1, 0);
        GridPane.setConstraints(passwordField, 1, 1);
        GridPane.setConstraints(confPasswordField, 1, 2);
        GridPane.setConstraints(firstNameInput, 1, 3);
        GridPane.setConstraints(lastNameInput, 1, 4);
        GridPane.setConstraints(ignInput, 1, 5);
        GridPane.setConstraints(timezoneDropdown, 1, 6);
        GridPane.setConstraints(chooseButton, 1, 7);
        GridPane.setConstraints(selectedAvatarLabel, 1, 8);
        
        
        // Displays logo
        HBox upperDisplay = new HBox();
        upperDisplay.setAlignment(Pos.CENTER);
        upperDisplay.getChildren().add(resourceLoader.getLogo());
        
        
        // Displays inputs
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8); // Sets the vertical gap between grid cells
        grid.setHgap(10); // Sets the horizontal gap between grid cells
        grid.getChildren().addAll(
                nameLabel,
                passLabel,
                confPassLabel,
                firstNameLabel,
                lastNameLabel,
                ignLabel,
                imageLabel,
                usernameInput,
                passwordField,
                confPasswordField,
                firstNameInput,
                lastNameInput,
                ignInput, 
                chooseButton, 
                selectedAvatarLabel, 
                timezoneLabel, 
                timezoneDropdown);
        
        
        // Displays 'Create' and 'Cancel' buttons
        HBox lowerDisplay = new HBox(13);
        lowerDisplay.setPadding(new Insets(5,0,13,0));
        lowerDisplay.setAlignment(Pos.CENTER);
        lowerDisplay.getChildren().addAll(createButton,cancelButton);
        
        
        BorderPane border = new BorderPane();
        border.setTop(upperDisplay);
        border.setCenter(grid);
        border.setBottom(lowerDisplay);
        
        
        scene = new Scene(border);
        scene.getStylesheets().add(this.getClass().getResource("veneno.css").toExternalForm());
        newAccountStage.getIcons().add(resourceLoader.getIcon());
        newAccountStage.setScene(scene);
        newAccountStage.setResizable(false);
        newAccountStage.show();
    }
    
    
    private File getAvatar()
    {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose an Avatar");
        chooser.getExtensionFilters().addAll(
                new ExtensionFilter("PNG Image", "*.png"),
                new ExtensionFilter("JPEG Image", "*.jpg"));
        
        File avatar = chooser.showOpenDialog(null);
        
        if (avatar != null)
        {
            selectedAvatarLabel.setText(avatar.getName());
            return avatar;
        }
        else 
        {
            selectedAvatarLabel.setText(noAvatarMessage);
            return null;
        }
    }
    
    
    // Getters:
    private String getUsername()
    {
        return usernameInput.getText();
    }
    
    private String getPassword()
    {
        return passwordField.getText();
    }
    
    private String getConfPassword()
    {
        return confPasswordField.getText();
    }
    
    private String getFirstName()
    {
        return firstNameInput.getText();
    }
    
    private String getLastName()
    {
        return lastNameInput.getText();
    }
    
    private String getIgn()
    {
        return ignInput.getText();
    }
    
    private String getTimezone()
    {
        return (String)timezoneDropdown.getValue();
    }
    // End getters
    
    
    private boolean verifiedFormIntegrity()
    {
        passwordMatch = true;
        
        if (getUsername().equals("") || !isBetween(getUsername().length(), 2, 24))
            return false;
        if (getPassword().equals("") || !isBetween(getPassword().length(), 5, 24))
            return false;
        if (getFirstName().equals("") || !isBetween(getFirstName().length(), 2, 24))
            return false;
        if (getLastName().equals("") || !isBetween(getLastName().length(), 2, 24))
            return false;
        if (getIgn().equals("") || !isBetween(getIgn().length(), 2, 24))
            return false;
        if (getTimezone() == null || getTimezone().equals("") || !isBetween(getTimezone().length(), 2, 12))
            return false;
        
        if (!getConfPassword().equals(getPassword()))
        {
            passwordMatch = false;
            return true;
        }

        
        else
            return true;
    }
    
        
    private boolean isBetween(int value, int min, int max)
    {
        // Note: database will not take above 16 chars
        return ((value > min) && (value <= max));
    }
    
    
    private void submit()
    {
        byte[] salt = getSalt();
        
        // Check input fields, submit to server
        if (verifiedFormIntegrity())
        {
            Form newAccountInfo = new NewAccountForm(
                    getUsername(), 
                    getHashedPassword(salt), 
                    salt,
                    getFirstName(), 
                    getLastName(), 
                    getIgn(), 
                    getTimezone(),
                    userImageFile);
            
            submitNewAccount(newAccountInfo);
        }
        else
        {
            if (passwordMatch)
                Alert.display("Check Values", "Make sure fields are between 3 and 24 characters. Password must be minimum 6 characters. Timezone must be 3 to 12 characters.");
            else 
            {
                Alert.display("Confirm Password", "Passwords do not match!");
                passwordField.setText(null);
                confPasswordField.setText(null);
            }
        }
    }
    
    
    public String getHashedPassword(byte[] salt)
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
    
    
    private byte[] getSalt()
    {
        try 
        {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            return salt;
        }
        catch (NoSuchAlgorithmException e)
        {
            Platform.runLater(() -> Alert.display("Security", "Password hash type not supported."));
            return null;
        }
    }
  
    
    private void submitNewAccount(Form newAccountInfo)
    {           
        if (worker.sendForm(newAccountInfo))
            this.close();
    }
    
    
    private void close()
    {
        newAccountStage.close();
        login.display();
    }
}
