
package tarkov.trader.server;
import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import tarkov.trader.objects.HeartbeatForm;

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
    
    public static HashMap<String, RequestWorker> authenticatedUsers;   // <Username, Respective worker>
    
    
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
            System.out.print("MySQL Database IP address: ");
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
    
    
    private void openCommandLine()
    {
        System.out.println("\n");
        
        while (true)
        {
            command = scan.nextLine();
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
