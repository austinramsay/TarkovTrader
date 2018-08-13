
package tarkov.trader.server;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 *
 * @author austin
 */

public class Moderator extends JFrame {
    
    public Moderator() 
    {
        super("Tarkov Trader Server");
        this.display();
    }
    
    
    public void display()
    {
        JLabel serverLabel = new JLabel("Tarkov Trader Server", SwingConstants.CENTER);

        BorderLayout root = new BorderLayout();
        root.addLayoutComponent(serverLabel, BorderLayout.NORTH);
        
        super.setSize(800, 500);
        super.setVisible(true);
        
    }
    
}
