
package tarkov.trader.server;

import java.awt.BorderLayout;
import static java.awt.Component.CENTER_ALIGNMENT;
import static java.awt.Component.LEFT_ALIGNMENT;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
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
import javax.swing.border.EmptyBorder;
import tarkov.trader.objects.AccountFlag;
import tarkov.trader.objects.Item;
import tarkov.trader.objects.Profile;
import tarkov.trader.objects.Report;

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
        JLabel dbPasswordLabel = new JLabel("Database Password:");
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
        loginPrompt.setLocationRelativeTo(null);
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
        
        // Server Menu Items
        JMenuItem closeServerMenuItem = new JMenuItem("Close Server");
        closeServerMenuItem.addActionListener(e -> System.exit(0));
        
        // Admin Menu Items
        JMenuItem announceMenuItem = new JMenuItem("Announce");
        JMenuItem profileMenuItem = new JMenuItem("Modify Profile");
        JMenuItem kickAllMenuItem = new JMenuItem("Kick All");
        JMenuItem kickMenuItem = new JMenuItem("Kick User");
        JMenuItem clearItemsMenuItem = new JMenuItem("Delete All Items");
        profileMenuItem.addActionListener(e -> modifyProfile());
        announceMenuItem.addActionListener(e -> announce());
        kickAllMenuItem.addActionListener(e -> kickAll());
        kickMenuItem.addActionListener(e -> kickUser());
        clearItemsMenuItem.addActionListener(e -> deleteAllItems());
        
        // Add to server menu
        serverMenu.add(closeServerMenuItem);
        
        // Add to admin menu
        adminMenu.add(announceMenuItem);
        adminMenu.add(profileMenuItem);
        adminMenu.add(kickAllMenuItem);
        adminMenu.add(kickMenuItem);
        adminMenu.add(clearItemsMenuItem);
        
        // Add both menus to the menu bar
        menubar.add(serverMenu);
        menubar.add(adminMenu);
        // End menus
        
        
        // Logging text area
        trafficLog = new JTextArea();
        trafficLog.setEditable(false);
        trafficLog.setLineWrap(true);
        JScrollPane logPane = new JScrollPane(trafficLog);
        logPane.setAlignmentX(LEFT_ALIGNMENT);
        
        
        // Add the text area to the main log panel
        JPanel logPanel = new JPanel();
        logPanel.setLayout(new BoxLayout(logPanel, BoxLayout.PAGE_AXIS));
        JLabel activityLabel = new JLabel("Activity Log:");
        logPanel.add(activityLabel);
        logPanel.add(Box.createRigidArea(new Dimension(0,5)));
        logPanel.add(logPane);
        logPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        
        // Button box
        JPanel buttonBox = new JPanel();
        JButton onlineUsersButton = new JButton("Online User List");
        JButton pendingReportsButton = new JButton("Pending Reports");
        pendingReportsButton.addActionListener(e -> displayReports());
        buttonBox.setLayout(new BoxLayout(buttonBox, BoxLayout.LINE_AXIS));
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.add(onlineUsersButton);
        buttonBox.add(Box.createRigidArea(new Dimension(12,0)));
        buttonBox.add(pendingReportsButton);
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        
        
        // Build main JFrame
        moderator.add(logPanel, BorderLayout.CENTER);
        moderator.add(buttonBox, BorderLayout.PAGE_END);
        moderator.setJMenuBar(menubar);
        moderator.setSize(900, 600);
        moderator.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        moderator.setLocationRelativeTo(null);
        moderator.setVisible(true);
    }
    
    
    private void displayReports()
    {
        JFrame reportsFrame = new JFrame();
        ReportsTable reportsTable = new ReportsTable();
        
        reportsTable.setReports(TarkovTraderServer.reportLog.getReports());
        
        JPanel buttonBox = new JPanel();
        JButton viewButton = new JButton("View Report");
        JButton deleteButton = new JButton("Delete");
        buttonBox.setLayout(new BoxLayout(buttonBox, BoxLayout.LINE_AXIS));
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.add(viewButton);
        buttonBox.add(Box.createRigidArea(new Dimension(12,0)));
        buttonBox.add(deleteButton);
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        viewButton.addActionListener(e -> viewReport(reportsTable.getSelectedReport()));
        deleteButton.addActionListener(e -> 
        {
            if (deleteReport(reportsTable.getSelectedReport()))
            {
                reportsTable.refresh();
            }
        });
        reportsFrame.add(reportsTable, BorderLayout.CENTER);
        reportsFrame.add(buttonBox, BorderLayout.PAGE_END);
        reportsFrame.setSize(350, 500);
        reportsFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        reportsFrame.setLocationRelativeTo(null);
        reportsFrame.setTitle("Current Reports");
        reportsFrame.setVisible(true);
    }
    
    
    private void viewReport(Report report)
    {
        if (report == null)
        {
            JOptionPane.showMessageDialog(null, "No report selected.", "Select a Report", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JDialog reportDialog = new JDialog();
        
        JPanel commentsArea = new JPanel();
        commentsArea.setLayout(new BoxLayout(commentsArea, BoxLayout.PAGE_AXIS));
        
        JPanel labelBox = new JPanel();
        labelBox.setLayout(new BoxLayout(labelBox, BoxLayout.PAGE_AXIS));
        
        JLabel reportedByLabel = new JLabel("Reported from: " + report.getByUsername());
        reportedByLabel.setAlignmentX(CENTER_ALIGNMENT);
        JLabel userToReportLabel = new JLabel("User reported: " + report.getUserToReport());
        userToReportLabel.setAlignmentX(CENTER_ALIGNMENT);
        labelBox.add(Box.createRigidArea(new Dimension(0,20)));
        labelBox.add(reportedByLabel);
        labelBox.add(Box.createRigidArea(new Dimension(0,10)));
        labelBox.add(userToReportLabel);
        labelBox.add(Box.createRigidArea(new Dimension(0,20)));
        labelBox.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        
        JLabel commentsLabel = new JLabel("Comments:");
        commentsLabel.setAlignmentX(LEFT_ALIGNMENT);
        JTextArea commentSection = new JTextArea(report.getComments());
        commentSection.setAlignmentX(LEFT_ALIGNMENT);
        commentSection.setPreferredSize(new Dimension(300,200));
        commentSection.setEditable(false);
        
        commentsArea.add(Box.createRigidArea(new Dimension(0,10)));
        commentsArea.add(commentsLabel);
        commentsArea.add(Box.createRigidArea(new Dimension(0,10)));
        commentsArea.add(commentSection);
        commentsArea.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        
        
        JButton decline = new JButton("Decline");
        JButton accept = new JButton("Accept");
        JPanel buttonBox = new JPanel();
        buttonBox.setLayout(new BoxLayout(buttonBox, BoxLayout.LINE_AXIS));
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.add(accept);
        buttonBox.add(Box.createRigidArea(new Dimension(12, 0)));
        buttonBox.add(decline);
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.setBorder(new EmptyBorder(10,0,10,0));
                
        reportDialog.getContentPane().add(labelBox, BorderLayout.PAGE_START);
        reportDialog.getContentPane().add(new JScrollPane(commentsArea), BorderLayout.CENTER);
        reportDialog.getContentPane().add(buttonBox, BorderLayout.PAGE_END);
        reportDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        reportDialog.setLocationRelativeTo(null);
        reportDialog.pack();
        reportDialog.setTitle("Report from " + report.getByUsername());
        reportDialog.setVisible(true);
    }
    
    
    private boolean deleteReport(Report report)
    {
        if (report == null)
        {
            JOptionPane.showMessageDialog(null, "No report selected.", "Select a Report", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (!TarkovTraderServer.reportLog.getReports().contains(report))
        {
            JOptionPane.showMessageDialog(null, "Report does not exist in report log.");
        }
        
        int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        
        if (confirm == JOptionPane.NO_OPTION)
            return false;
        
        if (TarkovTraderServer.reportLog.removeReport(report))
        {
            JOptionPane.showMessageDialog(null, "Report deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            // TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO
            // When removing report, notify the reportee
            
            return true;
        }
        else
        {
            JOptionPane.showMessageDialog(null, "Failed to delete report.", "Failed", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    
    private void modifyProfile()
    {
        String username = JOptionPane.showInputDialog(null, "Enter Username:", "Modify Profile", JOptionPane.QUESTION_MESSAGE);
        
        // Check if any name was input
        if (username == null || username.isEmpty())
            return;
        
        if (!serverWorker.userExists(username))
        {
            JOptionPane.showMessageDialog(null, "User does not exist.", "Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Profile userProfile = serverWorker.getProfile(username);
        
        if (userProfile == null)
        {
            int rebuild = JOptionPane.showConfirmDialog(null, "The pulled profile is null. Client profile version may differ from the current profile build. Force rebuild?", "Failed", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
            if (rebuild == JOptionPane.YES_OPTION)
                rebuildProfile(username);
            else
                return;
            
            return;
        }
        
        // The user may have existing items for sale, pull from database and set the list in the new profile
        ArrayList<Item> itemList = serverWorker.getUserItems(username);
        
        userProfile.setCurrentSalesList(itemList);
        userProfile.setBuyList(new ArrayList<>());
        userProfile.setCompletedSalesList(new ArrayList<>());
        userProfile.setRequestedSalesList(new ArrayList<>());
        userProfile.setReportsList(new ArrayList<>());
        
        serverWorker.updateProfile(userProfile, username);
        
        JOptionPane.showMessageDialog(null, "Profile '" + username + "' cleared successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }    
    
    
    private void rebuildProfile(String username)
    {
        TarkovTraderServer.broadcast("Moderator: Rebuilding profile for " + username + ".");
        
        String ignCommand = "SELECT ign FROM accounts WHERE username=?";
        String timezoneCommand = "SELECT timezone FROM accounts WHERE username=?";
        
        String ign = serverWorker.dbQuery(ignCommand, username);
        String timezone = serverWorker.dbQuery(timezoneCommand, username);
        
        if (ign == null || timezone == null)
        {
            JOptionPane.showMessageDialog(null, "Failed to retrieve user information (IGN, timezone).", "Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Build a profile with the unique username, IGN, and set their timezone
        Profile profile = new Profile(username, ign, timezone);
        
        // Append generic new account flags
        profile.appendFlag(AccountFlag.NEW_ACCOUNT);
        profile.appendFlag(AccountFlag.NO_COMPLETED_PURCHASES);
        profile.appendFlag(AccountFlag.NO_COMPLETED_SALES);
        
        // The user may have items for sale, we need to include these in the profile
        ArrayList<Item> itemList = serverWorker.getUserItems(username);
        
        // Set the list in the profile itself
        profile.setCurrentSalesList(itemList);
        
        // Send updated profile to database
        if (serverWorker.updateProfile(profile, username))
            JOptionPane.showMessageDialog(null, "Profile (" + username + ") successfully rebuilt.", "Success", JOptionPane.INFORMATION_MESSAGE);
        else
            JOptionPane.showMessageDialog(null, "Profile failed to update.", "Failed", JOptionPane.ERROR_MESSAGE);
    }
    
    
    private void deleteAllItems()
    {
        int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to clear the items table?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        
        if (confirm == JOptionPane.NO_OPTION)
            return;
        
        String command = "DELETE FROM items";   // Clear all items from 'items' table
        
        // Execute using ServerWorker method
        if (serverWorker.executeDatabaseCmd(command))
            JOptionPane.showMessageDialog(null, "Items table successfully cleared.", "Success", JOptionPane.INFORMATION_MESSAGE);
        else
            JOptionPane.showMessageDialog(null, "Failed to clear items table.", "Failed", JOptionPane.ERROR_MESSAGE);
    }
    
    
    private void announce()
    {
        String announcement = JOptionPane.showInputDialog(null, "Announcement:", "Announce Message", JOptionPane.QUESTION_MESSAGE);
        
        if (announcement == null || announcement.isEmpty())
            return;
        
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
        
        if (confirm == JOptionPane.NO_OPTION || confirm == JOptionPane.CANCEL_OPTION)
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
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        if (trafficLog.getText().isEmpty())
            trafficLog.append(dateFormat.format(new Date()) + ": " + logMessage);
        else
            trafficLog.append("\n" + dateFormat.format(new Date()) + ": " + logMessage);
    }
    
}
