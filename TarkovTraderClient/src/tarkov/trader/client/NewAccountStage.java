
package tarkov.trader.client;

/**
 *
 * @author austin
 */
import tarkov.trader.objects.NewAccountForm;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.stage.FileChooser.ExtensionFilter;
import tarkov.trader.objects.Form;


public class NewAccountStage 
{
    private RequestWorker worker;
    private LoginPrompt login;
    
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
    
    private String username;
    private String password;
    private String confirmedPassword;
    private String firstName;
    private String lastName;
    private String ign;
    
    private boolean passwordMatch = false;
    
    
    public NewAccountStage(LoginPrompt login, RequestWorker worker)
    {
        this.login = login;
        this.worker = worker;
    }
    
    
    public void display()
    {      
        newAccountStage = new Stage();
        newAccountStage.setTitle("Create a New Account");
 
        
        nameLabel = new Label("Username:");
        passLabel = new Label("Password:");
        confPassLabel = new Label("Confirm password:");
        firstNameLabel = new Label("First name:");
        lastNameLabel = new Label("Last name:");
        ignLabel = new Label("In-game name:");
        imageLabel = new Label("Avatar:");
        selectedAvatarLabel = new Label("No avatar chosen.");
        
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
        
        chooseButton = new Button("Choose...");
        chooseButton.setOnAction(e -> getAvatar());
        
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
        GridPane.setConstraints(imageLabel, 0, 6);
        GridPane.setConstraints(usernameInput, 1, 0);
        GridPane.setConstraints(passwordField, 1, 1);
        GridPane.setConstraints(confPasswordField, 1, 2);
        GridPane.setConstraints(firstNameInput, 1, 3);
        GridPane.setConstraints(lastNameInput, 1, 4);
        GridPane.setConstraints(ignInput, 1, 5);
        GridPane.setConstraints(chooseButton, 1, 6);
        GridPane.setConstraints(selectedAvatarLabel, 1, 7);
        
        
        // Displays logo
        HBox upperDisplay = new HBox();
        upperDisplay.setAlignment(Pos.CENTER);
        upperDisplay.getChildren().add(Resources.logoViewer);
        
        
        // Displays inputs
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8); // Sets the vertical gap between grid cells
        grid.setHgap(10); // Sets the horizontal gap between grid cells
        grid.getChildren().addAll(nameLabel,passLabel,confPassLabel,firstNameLabel,lastNameLabel,ignLabel,imageLabel,usernameInput,passwordField,confPasswordField,firstNameInput,lastNameInput,ignInput, chooseButton, selectedAvatarLabel);
        
        
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
        newAccountStage.getIcons().add(Resources.icon);
        newAccountStage.setScene(scene);
        newAccountStage.setResizable(false);
        newAccountStage.show();
    }
    
    
    private File getAvatar()
    {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose an Avatar");
        chooser.getExtensionFilters().addAll(new ExtensionFilter("PNG Image", "*.png"));
        
        File avatar = chooser.showOpenDialog(null);
        
        if (avatar != null)
        {
            selectedAvatarLabel.setText(avatar.getName());
            return avatar;
        }
        else 
        {
            selectedAvatarLabel.setText("No avatar chosen.");
            return null;
        }
    }
    
    
    private void submit()
    {
        username = usernameInput.getText();
        password = passwordField.getText();
        confirmedPassword = confPasswordField.getText();
        firstName = firstNameInput.getText();
        lastName = lastNameInput.getText();
        ign = ignInput.getText();
        
        // Check input fields, submit to server
        if (verifiedFormIntegrity())
        {
            NewAccountForm newAccountInfo = new NewAccountForm(username, password, firstName, lastName, ign);
            submitNewAccount(newAccountInfo);
        }
        else
        {
            if (passwordMatch)
                Alert.display("Check Values", "Make sure all fields are between 3 and 16 characters. Password must be 6 to 18 characters.");
            else 
            {
                Alert.display("Confirm Password", "Passwords do not match!");
                passwordField.setText(null);
                confPasswordField.setText(null);
            }
        }
    }
    
    
    private boolean verifiedFormIntegrity()
    {
        passwordMatch = true;
        
        if (username.equals("") || !isBetween(username.length(), 2, 15))
            return false;
        if (password.equals("") || !isBetween(password.length(), 5, 17))
            return false;
        if (firstName.equals("") || !isBetween(firstName.length(), 2, 15))
            return false;
        if (lastName.equals("") || !isBetween(lastName.length(), 2, 15))
            return false;
        if (ign.equals("") || !isBetween(ign.length(), 2, 15))
            return false;
        if (!confirmedPassword.equals(password))
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
    
    
    private void submitNewAccount(NewAccountForm newAccountInfo)
    {   
        Form form = new NewAccountForm(username, password, firstName, lastName, ign);
        
        if (worker.sendForm(form))
            this.close();
    }
    
    
    private void close()
    {
        newAccountStage.close();
        login.display();
    }
}
