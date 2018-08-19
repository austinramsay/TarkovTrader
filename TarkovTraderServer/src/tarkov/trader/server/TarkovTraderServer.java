
package tarkov.trader.server;
import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import tarkov.trader.objects.HeartbeatForm;
import tarkov.trader.objects.SyncForm;

public class TarkovTraderServer {

    private Network networking;
    private ServerWorker serverWorker;
    private static Moderator mod;
      
    public static String dbIpAddr;
    public static String dbName;
    public static String dbUsername;
    public static String dbPassword;
    public final static String dbDriver = "com.mysql.cj.jdbc.Driver";
    
    private static volatile ArrayList<String> userList;
    public static volatile HashMap<String, RequestWorker> authenticatedUsers;   // < Username, User's respective worker >
    
    
    public static void main(String[] args) 
    {
        System.out.println("Tarkov Trader Server");
        
        TarkovTraderServer server = new TarkovTraderServer();
         
        mod = new Moderator(server);
    }
    
    
    public void start()
    {
        TarkovTraderServer.authenticatedUsers = new HashMap();
        networking = new Network();
        TarkovTraderServer.syncUserList();
        Thread networker = new Thread(networking);
        networker.start();
        serverWorker = new ServerWorker();
    }
    
    
    public static void broadcast(String logMessage)
    {
        mod.broadcast(logMessage);
    }
    
    
    public boolean verifyDBconnection()
    {
        try 
        {
            Class.forName(TarkovTraderServer.dbDriver);
            
            Connection dbConnection = DriverManager.getConnection("jdbc:mysql://" + TarkovTraderServer.dbIpAddr + ":3306/" + TarkovTraderServer.dbName + "?verifyServerCertificate=false&useSSL=true", TarkovTraderServer.dbUsername, TarkovTraderServer.dbPassword);
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
            String error = "Failed database connection.\n";
            System.out.println(error);
            return false;
        }
    }
    
    
    public ServerWorker getServerWorker()
    {
        return serverWorker;
    }
    
    
    public synchronized static void syncUserList()
    {
        // When a new account is created, or the server is started
        // The ArrayList containing Strings of all usernames should be updated
        // This will be sent to users on login
        // Or pushed to all online clients when a new account is created for the most updated list client-side
        
        // Sync in progress lock
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
    }
    
    
    @SuppressWarnings("empty-statement")
    public synchronized static ArrayList<String> getUserList()
    {
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
    
    /*
    private void processCommand(String command)
    {
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
                
            case "clearprofile":
                modifyProfile();
                break;
                
            case "heartbeat":
                checkClientConnectivity();
                break;
                
            default:
                System.out.println("Command not found.");
                
        }
    }*/
    
    

    
    

    
    

    
    
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
