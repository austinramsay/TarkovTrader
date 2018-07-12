
package tarkov.trader.server;

import java.util.*;
import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
    
    public static HashMap<String, String> authenticatedUsers;
    
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
        while (true)
        {
            command = scan.next();
        }
    }
    
}
