
package tarkov.trader.server;

import java.io.ObjectInputStream;
import java.net.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import tarkov.trader.objects.Item;
import tarkov.trader.objects.ItemListForm;

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
    
    
    public ResultSet queryItems(ItemListForm itemlistform)
    {
        HashMap<String, String> searchFlags = itemlistform.getSearchFlags();
        
        String pricemin;
        String pricemax;
        
        if (searchFlags.get("pricemin").equals("") && searchFlags.get("pricemax").equals("")) // If there was no price range specified, use 1 to 50 million
        {
            pricemin = "1";
            pricemax = "50000000"; 
        }
        else // If price range was specified, use set values
        {
            pricemin = searchFlags.get("pricemin");
            pricemax = searchFlags.get("pricemax");
        }
             
        Connection dbConnection = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        
        String command = "SELECT ItemObject FROM items WHERE ItemId LIKE ? AND State LIKE ? AND Type Like ? AND Name LIKE ? AND Price BETWEEN ? AND ? AND Ign LIKE ? AND Username LIKE ? and Timezone LIKE ? AND Keywords LIKE ?;";
    
        try 
        {
            dbConnection = getDBconnection();
            statement = dbConnection.prepareStatement(command);
            statement.setString(1, searchFlags.get("itemid"));
            statement.setString(2, searchFlags.get("state"));
            statement.setString(3, searchFlags.get("type"));
            statement.setString(4, searchFlags.get("name"));
            statement.setInt(5, Integer.parseInt(pricemin));
            statement.setInt(6, Integer.parseInt(pricemax));
            statement.setString(7, searchFlags.get("ign"));
            statement.setString(8, searchFlags.get("username"));
            statement.setString(9, searchFlags.get("timezone"));
            statement.setString(10, searchFlags.get("keywords"));
            
            System.out.println("using statement: " + statement.toString());
            result = statement.executeQuery();
            return result;
        }
        catch (SQLException e)
        {
            String error = "DBWorker: Item list query failed. Error: " + e.getMessage();
            e.printStackTrace();
            System.out.println(error);
            communicator.sendAlert(error);
            return null;
        }
        finally 
        {
            // Close resources
            /*try {
            if (statement != null)
                statement.close();
            if (result != null)
                result.close();
            if (dbConnection != null)
                dbConnection.close();
            } catch (SQLException e)
            { System.out.println("DBWorker: Couldn't close resources. Error: " + e.getMessage()); }*/
            
            System.out.println("DBWorker: Successfully processed item list query for " + clientIp + ".");
            
        }
    }
    
    public void insertAccountInfo(String values)
    {        
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
    
    
    public boolean insertNewItem(Item newItem)
    {
        String command = "INSERT INTO items (State, Type, Name, Price, Ign, Username, Timezone, Keywords, Notes, DealStatus, ItemObject) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        
        try 
        {
            Connection dbConnection = getDBconnection();
            PreparedStatement statement = dbConnection.prepareStatement(command);
            statement.setString(1, newItem.getTradeState());
            statement.setString(2, newItem.getItemType());
            statement.setString(3, newItem.getName());
            statement.setInt(4, newItem.getPrice());
            statement.setString(5, newItem.getIgn());
            statement.setString(6, newItem.getUsername());
            statement.setString(7, newItem.getTimezone());
            statement.setString(8, newItem.getKeywords());
            statement.setString(9, newItem.getNotes());
            statement.setString(10, newItem.getDealStatus());
            statement.setObject(11, newItem);
            
            statement.executeUpdate();
            statement.close();
            dbConnection.close();
            return true;
        }
        catch (SQLException e)
        {
            String error = "DBWorker: New item insert failed. Error: " + e.getMessage();
            System.out.println(error);
            communicator.sendAlert(error);
            return false;
        }
    }
    
    
}