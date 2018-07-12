
package tarkov.trader.server;

import java.net.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

// DatabaseWorker class 
// Handles all database networking
// Includes methods for entry insertion, results, and manipulation

public class DatabaseWorker 
{
    private String clientIp;
    private ClientCommunicator communicator;
    
    
    public DatabaseWorker(String clientIp, ClientCommunicator communicator)
    {
        this.clientIp = clientIp;
        this.communicator = communicator;
    }
    
    
    public Connection getDBconnection()
    {
        try 
        {
            Class.forName(TarkovTraderServer.dbDriver);
            Connection dbConnection = DriverManager.getConnection("jdbc:mysql://" + TarkovTraderServer.dbIpAddr + ":3306/" + TarkovTraderServer.dbName + "?verifyServerCertificate=false&useSSL=true", TarkovTraderServer.dbUsername, TarkovTraderServer.dbPassword);
            return dbConnection;
        }
        catch (ClassNotFoundException e)
        {
            String error = "Network: Connection to database failed. Error: " + e.getMessage();
            System.out.println(error);
            communicator.sendAlert(error);
            
            return null;
        }
        catch (SQLException e)
        {
            String error = "Network: SQLException when attempting DB connection. Error: " + e.getMessage();
            System.out.println(error);
            communicator.sendAlert(error);
            return null;
        }
    }
    
    
    public ResultSet query(String statement) throws SQLException
    {
        Connection dbConnection = getDBconnection();
        PreparedStatement prepStatement = dbConnection.prepareStatement(statement);
        return prepStatement.executeQuery();
    }
    
    
    public void insertAccountInfo(LinkedHashMap<String, String> account)
    {
        Iterator<Map.Entry<String, String>> iterator = account.entrySet().iterator();
        String values = "";
        
        // Build the SQL statement with account info in the array list
        
        while (iterator.hasNext())
        {
            Map.Entry<String, String> entry = iterator.next();
            if (iterator.hasNext())
            {
                if (entry.getKey().equals("password"))
                {
                    // Use this to decrypt password when feature is implemented
                    values += "'" + entry.getValue() + "', ";
                }
                else
                {
                    values += "'" + entry.getValue() + "', ";
                }
            }
            else // If there's no next entry, a comma should not be appended to the last entry to match SQL syntax.
            {
                values += "'" + entry.getValue() + "'";
            }
        }
        
        try
        {
            Connection dbConnection = getDBconnection();
            PreparedStatement statement = dbConnection.prepareStatement("INSERT INTO accounts (username, password, firstname, lastname, ign, ipaddr) VALUES (" + values + ");");
            statement.executeUpdate();
            statement.close();
            dbConnection.close();
            System.out.println("DBWorker: New account success from: " + clientIp);
            communicator.sendAlert("Account successfully created.");
        }
        catch (SQLException e)
        {
            String error = "DBWorker: New account insert failed. Error: " + e.getMessage();
            System.out.println(error);
            communicator.sendAlert(error);
        }
    }
    
    
}
