
package tarkov.trader.server;
import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import tarkov.trader.objects.HeartbeatForm;
import tarkov.trader.objects.SyncForm;

public class TarkovTraderServer {

    private Network networking;
      
    private Scanner scan;
    private String definedDbIpAddr;
    private String definedDbName;
    private String definedDbUsername;
    private String definedDbPassword;
    public static String dbIpAddr;
    public static String dbName;
    public static String dbUsername;
    public static String dbPassword;
    public final static String dbDriver = "com.mysql.cj.jdbc.Driver";
    private String command;
    
    private static volatile ArrayList<String> userList;
    public static volatile HashMap<String, RequestWorker> authenticatedUsers;   // < Username, User's respective worker >
    
    private static boolean userListSyncInProgress = false;
    private static boolean onlineListSyncInProgress = false;
    
    
    public static void main(String[] args) 
    {
        System.out.println("Tarkov Trader Server");
        
        TarkovTraderServer server = new TarkovTraderServer();
         
        server.start();
    }
    
    
    private void start()
    {
        TarkovTraderServer.authenticatedUsers = new HashMap();
        
        scan = new Scanner(System.in);
        networking = new Network();
        
        do 
        {
            // Connect to MySQL database
            System.out.print("Tarkov Trader Database IP: ");
            definedDbIpAddr = scan.next();       
        
            System.out.print("Database name: ");
            definedDbName = scan.next();
        
            System.out.print("Database username: ");
            definedDbUsername = scan.next();
        
            System.out.print("Password: ");
            definedDbPassword = scan.next();
        }
        while (!verifyDBconnection());
        
        setDBparameters(definedDbIpAddr, definedDbName, definedDbUsername, definedDbPassword);
        
        TarkovTraderServer.syncUserList();
        
        Thread networker = new Thread(networking);
        networker.start();
        
        openCommandLine();
    }
    
    
    private void setDBparameters(String dbIpAddr, String dbName, String dbUsername, String dbPassword)
    {
        TarkovTraderServer.dbIpAddr = dbIpAddr;
        TarkovTraderServer.dbName = dbName;
        TarkovTraderServer.dbUsername = dbUsername;
        TarkovTraderServer.dbPassword = dbPassword;        
    }
    
    
    private boolean verifyDBconnection()
    {
        try 
        {
            Class.forName(TarkovTraderServer.dbDriver);
            
            Connection dbConnection = DriverManager.getConnection("jdbc:mysql://" + definedDbIpAddr + ":3306/" + definedDbName + "?verifyServerCertificate=false&useSSL=true", definedDbUsername, definedDbPassword);
            dbConnection.close();
            System.out.println("Database test connection successful. Starting server...");
            return true;
        }
        catch (ClassNotFoundException e)
        {
            String error = "DBConnection: " + e.getMessage() + "\n";
            System.out.println(error);
            e.printStackTrace();
            return false;
        }
        catch (SQLException e)
        {
            String error = "DBConnection: Failed database connection. Reattempting.\n";
            System.out.println(error);
            return false;
        }
    }
    
    
    public synchronized static void syncUserList()
    {
        // When a new account is created, or the server is started
        // The ArrayList containing Strings of all usernames should be updated
        // This will be sent to users on login
        // Or pushed to all online clients when a new account is created for the most updated list client-side
        
        // Sync in progress lock
        TarkovTraderServer.userListSyncInProgress = true;
        
        // Because a new account has been added into the database, get the latest info
        DatabaseWorker dbWorker = new DatabaseWorker();
        ArrayList<String> pulledUserList = dbWorker.pullUserList();
        
        // Set the static arraylist equal to the latest pulled list
        TarkovTraderServer.userList = pulledUserList;
        
        // Build the SyncForm to push to all online clients
        ArrayList<String> syncFlags = new ArrayList<>();
        syncFlags.add("fulluserlist");
        
        SyncForm syncinfo = new SyncForm(syncFlags);
        syncinfo.setFullUserList(TarkovTraderServer.userList);
        
        // Get all online users workers, send the SyncForm
        for (Map.Entry<String, RequestWorker> client : TarkovTraderServer.authenticatedUsers.entrySet())
        {
            RequestWorker tempWorker = client.getValue();
            tempWorker.sendForm(syncinfo);
            tempWorker = null;
        }
        
        // Sync in progress lock
        TarkovTraderServer.userListSyncInProgress = false;
    }
    
    
    @SuppressWarnings("empty-statement")
    public synchronized static ArrayList<String> getUserList()
    {
        // BUG: THIS WILL BLOCK THE MAIN SERVER THREAD IF SYNC IS HAPPENING AND A USER IS WAITING
        while (TarkovTraderServer.userListSyncInProgress)
        {
            // Sync in progress, wait until sync is complete to return correct user list
            ;
        }
        
        return TarkovTraderServer.userList;
    }
    
    
    public synchronized static void syncOnlineList()
    {
        // This method ONLY pushes the latest online list to all available clients

        // Prepare the SyncForm
        ArrayList<String> syncFlags = new ArrayList<>();
        syncFlags.add("onlineuserlist");
        SyncForm syncinfo = new SyncForm(syncFlags);        
        
        // Prepare to pack arraylist to be set into SyncForm
        ArrayList<String> onlineUsers = TarkovTraderServer.getOnlineList();   
        syncinfo.setOnlineUserList(onlineUsers);
        
        // Push to available clients
        for (Map.Entry<String, RequestWorker> client : TarkovTraderServer.authenticatedUsers.entrySet())
        {
            RequestWorker tempWorker = client.getValue();
            tempWorker.sendForm(syncinfo);
            tempWorker = null;
        }
    }
    
    
    public synchronized static ArrayList<String> getOnlineList()
    {
        ArrayList<String> onlineUsers = new ArrayList<>();
        
        for (Map.Entry<String, RequestWorker> entry : TarkovTraderServer.authenticatedUsers.entrySet())
        {
            onlineUsers.add(entry.getKey());
        }        
        
        return onlineUsers;
    }
    
    private void openCommandLine()
    {
        System.out.println("\n");
        
        while (true)
        {
            System.out.print("Command: ");
            command = scan.nextLine();
            if (command.length() > 0)
                processCommand(command);
        }
    }
    
    
    private void processCommand(String command)
    {
        System.out.println("\n");
        System.out.println("****************************************************************************");
        
        switch (command)
        {
            case "kickall": 
                kickAll();
                break;
                
            case "getusers":
                listOnlineUsers();
                break;
                
            case "announce":
                announce(command.substring(9));
                break;
                
            case "heartbeat":
                checkClientConnectivity();
                break;
                
            default:
                System.out.println("Command not found.");
                
        }
        
        System.out.println("****************************************************************************");
        System.out.println("\n");
        
    }
    
    
    private void announce(String announcement)
    {
        for (Map.Entry<String, RequestWorker> entry : TarkovTraderServer.authenticatedUsers.entrySet())
        {
            RequestWorker tempworker = entry.getValue();
            tempworker.communicator.sendAlert(announcement);
        }
    }
    
    
    private void kickAll()
    {
        announce("SERVER: GOING DOWN FOR MAINTENANCE.");
        TarkovTraderServer.authenticatedUsers.clear();
        System.out.println("SERVER: Authenticated user map cleared.");
    }
    
    
    private void listOnlineUsers()
    {
        System.out.println("Online User List:");
        for (Map.Entry<String, RequestWorker> entry : TarkovTraderServer.authenticatedUsers.entrySet())
        {
            System.out.println(" - " + entry.getValue().clientUsername);
        }
    }
    
    
    private void checkClientConnectivity()
    {
        System.out.println("Sending heartbeat checks:");
        for (Map.Entry<String, RequestWorker> entry : TarkovTraderServer.authenticatedUsers.entrySet())
        {
            if (entry.getValue().sendForm(new HeartbeatForm()))
                System.out.println(" - Requested: " + entry.getValue().clientUsername);
            else
            {
                TarkovTraderServer.authenticatedUsers.remove(entry.getValue().clientUsername);
                System.out.println(" - Removed: " + entry.getValue().clientUsername);
            }
        }        
    }
}
