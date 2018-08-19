
package tarkov.trader.server;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import tarkov.trader.objects.Profile;

/**
 *
 * @author austin
 */

public class Moderator {
    
    TarkovTraderServer server;
    ServerWorker serverWorker;
    
    JFrame loginPrompt;
    JFrame moderator;
    
    // Login Prompt Fields
    JTextField ipInput;
    JTextField usernameInput;
    JTextField nameInput;
    JPasswordField passwordInput;
    
    // Moderator Fields
    JTextArea trafficLog;
    
    
    public Moderator(TarkovTraderServer server)
    {
        this.server = server;
        login();
    }
    
    private void login()
    {
        loginPrompt = new JFrame("Server Start");
        
        JPanel mainPanel = new JPanel();
        JPanel formPanel = new JPanel();
        JPanel buttonBox = new JPanel();
        JLabel mainLabel = new JLabel("Tarkov Trader Server", SwingConstants.CENTER);
        JLabel dbIpLabel = new JLabel("Server IP:");
        JLabel dbUsernameLabel = new JLabel("Database Username:");
        JLabel dbNameLabel = new JLabel("Database Name:");
        JLabel dbPasswordLabel = new JLabel("Database Password");
        JButton loginButton = new JButton("Start");
        loginButton.addActionListener(e -> submit());
        JButton cancelButton = new JButton("Close");
        cancelButton.addActionListener(e -> System.exit(0));
        
        ipInput = new JTextField();
        ipInput.setText("192.168.1.125");
        usernameInput = new JTextField();
        usernameInput.setText("tarkovtrader");
        nameInput = new JTextField();
        nameInput.setText("tarkovtrader");
        passwordInput = new JPasswordField();
        passwordInput.setText("eft-tr@der");
        
        // Main panel will only contain the 'Tarkov Trader Server' label
        mainPanel.setLayout(new FlowLayout());
        mainPanel.add(mainLabel);
        
        // Our form panel will use a grid layout for the input labels and text fields
        GridLayout grid = new GridLayout(4, 2, 5, 5);
        Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        
        formPanel.setLayout(grid);
        formPanel.add(dbIpLabel);
        formPanel.add(ipInput);
        formPanel.add(dbUsernameLabel);
        formPanel.add(usernameInput);
        formPanel.add(dbNameLabel);
        formPanel.add(nameInput);
        formPanel.add(dbPasswordLabel);
        formPanel.add(passwordInput);
        formPanel.setBorder(padding);
        
        // Button box
        buttonBox.setLayout(new FlowLayout());
        buttonBox.setAlignmentX(SwingConstants.CENTER);
        buttonBox.add(loginButton);
        buttonBox.add(cancelButton);
        
        BorderLayout root = new BorderLayout();
        loginPrompt.setLayout(root);
        
        loginPrompt.add(mainPanel, BorderLayout.NORTH);
        loginPrompt.add(formPanel, BorderLayout.CENTER);
        loginPrompt.add(buttonBox, BorderLayout.SOUTH);
        loginPrompt.setSize(400, 200);
        loginPrompt.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginPrompt.setVisible(true);
    }
    
    
    private void submit()
    {
        if (ipInput.getText().isEmpty() || nameInput.getText().isEmpty() || usernameInput.getText().isEmpty() || passwordInput.getText().isEmpty())
        {
            JOptionPane.showMessageDialog(null, "One or more fields are not complete.");
            return;
        }
        
        TarkovTraderServer.dbIpAddr = ipInput.getText();
        TarkovTraderServer.dbName = nameInput.getText();
        TarkovTraderServer.dbUsername = usernameInput.getText();
        TarkovTraderServer.dbPassword = passwordInput.getText();        
        
        if (server.verifyDBconnection())
        {
            displayModerator();
            
            server.start();
            serverWorker = server.getServerWorker();
            
            loginPrompt.setVisible(false);
        }
    }
    
    
    private void displayModerator()
    {
        moderator = new JFrame("Tarkov Trader Server");
        
        // Menus
        JMenuBar menubar = new JMenuBar();
        JMenu serverMenu = new JMenu("Server");
        JMenu adminMenu = new JMenu("Admin Actions");
        
        JMenuItem announceMenuItem = new JMenuItem("Announce");
        JMenuItem profileMenuItem = new JMenuItem("Modify Profile");
        JMenuItem kickAllMenuItem = new JMenuItem("Kick All");
        JMenuItem kickMenuItem = new JMenuItem("Kick User");
        profileMenuItem.addActionListener(e -> modifyProfile());
        announceMenuItem.addActionListener(e -> announce());
        kickAllMenuItem.addActionListener(e -> kickAll());
        kickMenuItem.addActionListener(e -> kickUser());
        
        adminMenu.add(announceMenuItem);
        adminMenu.add(profileMenuItem);
        adminMenu.add(kickAllMenuItem);
        adminMenu.add(kickMenuItem);
        
        menubar.add(serverMenu);
        menubar.add(adminMenu);
        // End menus
        
        
        // Logging text area
        trafficLog = new JTextArea(20, 70);
        trafficLog.setEditable(false);
        JScrollPane logPane = new JScrollPane(trafficLog);
        
        // Add the text area to the main log panel
        JPanel logPanel = new JPanel();
        logPanel.add(logPane);
        
        // Build main JFrame
        moderator.add(logPanel);
        moderator.setJMenuBar(menubar);
        moderator.setSize(900, 600);
        moderator.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        moderator.setVisible(true);
    }
    
    
    private void modifyProfile()
    {
        String username = JOptionPane.showInputDialog(null, "Enter Username:", "Modify Profile", JOptionPane.QUESTION_MESSAGE);
        
        // Check if any name was input
        if (username == null || username.isEmpty())
            return;
        
        Profile userProfile = serverWorker.getProfile(username);
        userProfile.setItemList(new ArrayList<>());
        userProfile.setBuyList(new ArrayList<>());
        userProfile.setCompletedSalesList(new ArrayList<>());
        userProfile.setRequestedSalesList(new ArrayList<>());
        userProfile.setCurrentSalesList(new ArrayList<>());
        serverWorker.updateProfile(userProfile, username);
        
        JOptionPane.showMessageDialog(null, "Profile '" + username + "' cleared successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }    
    
    
    private void announce()
    {
        String announcement = JOptionPane.showInputDialog(null, "Announcement:", "Announce Message", JOptionPane.QUESTION_MESSAGE);
        
        int count = 0;
        
        for (Map.Entry<String, RequestWorker> entry : TarkovTraderServer.authenticatedUsers.entrySet())
        {
            RequestWorker tempworker = entry.getValue();
            tempworker.communicator.sendAlert(announcement);
            count++;
        }
        
        JOptionPane.showMessageDialog(null, "Announcement sent to '" + count + "' user(s).", "Success", JOptionPane.INFORMATION_MESSAGE);
    }    
    
    
    private void kickAll()
    {
        int confirm = JOptionPane.showConfirmDialog(null, "There are '" + TarkovTraderServer.authenticatedUsers.size() + "' online. Continue?", "Confirm", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.NO_OPTION)
            return;
        
        // Else, continue
        // Clear the authenticated users map
        int count = 0;
        
        for (Map.Entry<String, RequestWorker> onlineUser : TarkovTraderServer.authenticatedUsers.entrySet())
        {
            RequestWorker tempWorker = onlineUser.getValue();
            tempWorker.communicator.sendAlert("Server reset. Please attempt reconnection soon.");
            tempWorker.disconnect();
            count++;
        }
        
        TarkovTraderServer.authenticatedUsers.clear();
        
        JOptionPane.showMessageDialog(null, "Kicked '" + count + "' user(s) successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }    
    
    
    private void kickUser()
    {
        String username = JOptionPane.showInputDialog(null, "Enter Username:", "Kick User", JOptionPane.QUESTION_MESSAGE);
        
        // Check for an input username
        if (username == null || username.isEmpty())
            return;
        
        RequestWorker tempWorker = null;
        
        if (TarkovTraderServer.authenticatedUsers.containsKey(username))
            tempWorker = TarkovTraderServer.authenticatedUsers.get(username);
        
        if (tempWorker == null)
        {
            JOptionPane.showMessageDialog(null, "Failed to pull worker for this username.", "Failed", JOptionPane.ERROR_MESSAGE); 
            return;
        }
        
        tempWorker.communicator.sendAlert("You have been removed from the server.");
        
        tempWorker.disconnect();
        
        // Should have already been removed, but in case there was an issue just check again
        if (TarkovTraderServer.authenticatedUsers.containsKey(username))
            TarkovTraderServer.authenticatedUsers.remove(username);
        
        JOptionPane.showMessageDialog(null, "Kicked '" + username + "' successfully.", "Success", JOptionPane.INFORMATION_MESSAGE); 
    }
    
    
    public void broadcast(String logMessage)
    {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        if (trafficLog.getText().isEmpty())
            trafficLog.append(dateFormat.format(new Date()) + ": " + logMessage);
        else
            trafficLog.append("\n" + dateFormat.format(new Date()) + ": " + logMessage);
    }
    
}
