
package tarkov.trader.client;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author austin
 */

public class LoadingAnimator {
    
    private Stage stage;
    private boolean running;
    private Label info;
    private ImageView loadingIcon;
    private RotateTransition rotate;
    private HBox root;
    private Scene scene;
    
    public LoadingAnimator()
    {
        loadingIcon = new ImageView(new Image(this.getClass().getResourceAsStream("/hourglass.png")));
        
        stage = new Stage();
        
        rotate = new RotateTransition(Duration.seconds(1), loadingIcon);
        rotate.setByAngle(360);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.setOnFinished(e -> { rotate.play(); });
        
        info = new Label("Awaiting Server Response...");
        
        root = new HBox(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);
        
        root.getChildren().addAll(loadingIcon, info);
        
        scene = new Scene(root);
        scene.getStylesheets().add(this.getClass().getResource("veneno.css").toExternalForm());
        
        stage.setScene(scene);
        stage.setAlwaysOnTop(true);
    }
    
    public void display()
    {
        running = true;
        
        rotate.play();
        
        stage.show();
    }
    
    public boolean isRunning()
    {
        return running;
    }
    
    public void close()
    {
        running = false;
        stage.close();
    }
    
}
