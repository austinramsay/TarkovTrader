
package tarkov.trader.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import tarkov.trader.objects.Item;
import tarkov.trader.objects.ItemListForm;
import tarkov.trader.objects.LoginForm;
import tarkov.trader.objects.NewAccountForm;

// DatabaseWorker class 
// Handles all database work
// Includes methods for entry insertion, results, and manipulation

public class DatabaseWorker 
{
    private String clientIp;
    private ClientCommunicator communicator;
    private RequestWorker worker;
    
    
    public DatabaseWorker(String clientIp, ClientCommunicator communicator, RequestWorker worker)
    {
        this.clientIp = clientIp;
        this.communicator = communicator;
        this.worker = worker;
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
    
    
    /* 
    // The next section deals with returning simple queries such as simple string client info 
    // For example, fetching a user's stored timezone and ign to be returned in the LoginForm at authentication
    */
    
    private String simpleQuery(String command, String columnName, String onlyParameter)
    {
        Connection dbConnection = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        
        try 
        {
            dbConnection = this.getDBconnection();
            
            statement = dbConnection.prepareStatement(command);
            statement.setString(1, onlyParameter);
            result = statement.executeQuery();
            result.first();
            return result.getString(columnName);
        }
        catch (SQLException e)
        {
            System.out.println("DBWorker: Failed to perform simple query. Given command: " + command + ". Error: " + e.getMessage());
            return null;
        }
        finally 
        {
            try {
                if (result != null)
                    result.close();
                if (statement != null)
                    statement.close();
                if (dbConnection != null)
                    dbConnection.close();
            } catch (SQLException e) { System.out.println("DBWorker: Failed to close simple query resources."); }
        }
    }
    
    
    /*
    // The next section deals with authenticating user logins
    // The users password is matched to their username and returns a boolean value to be used in Request Worker
    */
        
    public boolean loginAuthenticated(LoginForm loginform)
    {
        String command = "SELECT password FROM accounts WHERE username=?;";
        Connection dbConnection = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        
        try
        {
            dbConnection = this.getDBconnection();
            
            statement = dbConnection.prepareStatement(command);
            statement.setString(1, loginform.getUsername());
            result = statement.executeQuery();
            result.first();
            return result.getString(1).equals(loginform.getPassword());
        }
        catch (SQLException e)
        {
            System.out.println("DBWorker: SQL Exception for " + clientIp + " when attempting to authenticate. Error: " + e.getMessage());
            return false;
        }
        finally 
        {
            try {
                if (result != null)
                    result.close();
                if (statement != null)
                    statement.close();
                if (dbConnection != null)
                    dbConnection.close();
            } catch (SQLException e) { System.out.println("DBWorker: Failed to close resources when verifying login for " + clientIp + ". Error: " + e.getMessage()); }
        }
    }
    
    
    public HashMap<String, String> getAuthenticatedClientInfo(LoginForm login)
    {
        String command;
        HashMap<String, String> clientInfo = new HashMap();
        
        command = "SELECT ign FROM accounts WHERE username=?";
        String clientIgn = simpleQuery(command, "ign", login.getUsername());
        
        command = "SELECT timezone FROM accounts WHERE username=?";
        String clientTimezone = simpleQuery(command, "timezone", login.getUsername());
        
        clientInfo.put("ign", clientIgn);
        clientInfo.put("timezone", clientTimezone);
        
        return clientInfo;
    }
    
    
    
    /* 
    // The next section deals with submitting a new account to the database
    // The clients requested username, ign, and ip address are verified for uniqueness
    // The request worker handles verification and uses the existCheck() method before calling for an insert of data
    */
    
    public int existCheck(String newIp, String newUsername, String newIgn)
    {
        String command;
        Connection dbConnection = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        
        boolean ipVerified = false;
        boolean usernameVerified = false;
        boolean ignVerified = false;
        
        try
        {
            dbConnection = this.getDBconnection();
            
            // Verify IP address is unique
            command = "SELECT CASE WHEN EXISTS (SELECT ipaddr FROM accounts WHERE ipaddr=?) THEN CAST(1 AS UNSIGNED) ELSE CAST(0 AS UNSIGNED) END;";
            statement = dbConnection.prepareStatement(command);
            statement.setString(1, newIp);
            result = statement.executeQuery();
            result.first(); // Move cursor to the first row
            ipVerified = (result.getInt(1) != 1); // Get value from first column (only one column for this query)
            
            // Verify username is unique
            command = "SELECT CASE WHEN EXISTS (SELECT username FROM accounts WHERE username=?) THEN CAST(1 AS UNSIGNED) ELSE CAST(0 AS UNSIGNED) END;";
            statement = null;
            statement = dbConnection.prepareStatement(command);
            statement.setString(1, newUsername);
            result = statement.executeQuery();
            result.first();
            usernameVerified = (result.getInt(1) != 1);
            
            // Verify in-game name is unique
            command = "SELECT CASE WHEN EXISTS (SELECT ign FROM accounts WHERE ign=?) THEN CAST(1 AS UNSIGNED) ELSE CAST(0 AS UNSIGNED) END;";
            statement = null;
            statement = dbConnection.prepareStatement(command);
            statement.setString(1, newIgn);
            result = statement.executeQuery();
            result.first();
            ignVerified = (result.getInt(1) != 1);       
            
            if (!ipVerified)
                return 1;
            if (!usernameVerified)
                return 2;
            if (!ignVerified)
                return 3;
            else
                return 0;
        } 
        catch (SQLException e)
        {
            System.out.println("DBWorker: Failed to test for field existence on new account request for " + clientIp + ". Error: " + e.getMessage());
            return 4;
        }
        finally 
        {
            try {
                if (result != null)
                    result.close();
                if (statement != null)
                    statement.close();
                if (dbConnection != null)
                    dbConnection.close();
            } catch (SQLException e) { System.out.println("DBWorker: Failed to close resources when verifying existing fields for " + clientIp + ". Error: " + e.getMessage()); }
        }
    }
    
    
    public void insertAccountInfo(NewAccountForm newAccount, String clientIp)
    {        
        String command = "INSERT INTO accounts (username, password, firstname, lastname, ign, timezone, ipaddr) VALUES (?, ?, ?, ?, ?, ?, ?);";
        Connection dbConnection = null;
        PreparedStatement statement = null;
        
        try
        {
            dbConnection = this.getDBconnection();

            statement = dbConnection.prepareStatement(command);
            statement.setString(1, newAccount.getUsername());
            statement.setString(2, newAccount.getPassword());
            statement.setString(3, newAccount.getFirst());
            statement.setString(4, newAccount.getLast());
            statement.setString(5, newAccount.getIgn());
            statement.setString(6, newAccount.getTimezone());
            statement.setString(7, clientIp);
            statement.executeUpdate();
            
            System.out.println("DBWorker: New account success from: " + clientIp);
            communicator.sendAlert("Account successfully created.");
        }
        catch (SQLException e)
        {
            String error = "DBWorker: New account insert failed. Error: " + e.getMessage();
            System.out.println(error);
            communicator.sendAlert(error);
        }
        finally // Close resources
        {
            try {
               if (statement != null)
                   statement.close();
               if (dbConnection != null)
                   dbConnection.close();
            } catch (SQLException e) { System.out.println("DBWorker: Failed to close resources for new account insert. Error: " + e.getMessage()); }
        }
    }
    
    /* 
    // END NEW ACCOUNT SECTION
    */
    
    
    
    /*
    // The next section deals with inserting a new item into the 'items' table
    */
    
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
 
    // END item insertion section
    
    
    
    /*
    // The following section deals with handling a client request for a list of items that match the search flags sent with the request
    // The method of returning a matching list is as follows:
    // 1. The request worker initiates the request by calling the processItemListRequest method
    // 2. The database connection is established
    // 3. The method calls queryItems and passes the database connection
    // 4. A ResultSet is returned with matching entries
    // 5. The matchingItemList ArrayList is packed by packItems with the passed ResultSet
    // 6. The ItemListForm is set with the packed ArrayList, and returned to the client
    */
    
    public void processItemListRequest(ItemListForm itemlistform)
    {
        // These variables will be passed to the queryItems ResultSet getter
        Connection dbConnection = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        // And closed after work is complete in this method
        
        ResultSet matchingItemResults = null;
        ArrayList<Item> matchingItemList = new ArrayList();
        
        try
        {
            dbConnection = getDBconnection();
            matchingItemResults = this.queryItems(dbConnection, statement, result, itemlistform);
            matchingItemList = packItems(matchingItemResults);
        }
        catch (SQLException e)
        {
            System.out.println("Request process: Failed to retrieve item objects. " + e.getMessage());
            e.printStackTrace();
        }
        finally 
        {
            // Close resources
            try {
                if (result != null)
                    result.close();
                if (statement != null)
                    statement.close();
                if (dbConnection != null)
                    dbConnection.close();
            } catch (SQLException e)
            { System.out.println("DBWorker: Couldn't close resources. Error: " + e.getMessage()); }
             
            
        }
        
        itemlistform.setItemList(matchingItemList);
        
        if (worker.sendForm(itemlistform))
        {
            System.out.println("Request complete: Returned item list to " + clientIp + ".");
        }
    }
    
    
    private ResultSet queryItems(Connection dbConnection, PreparedStatement statement, ResultSet result, ItemListForm itemlistform)
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
        
        String command = "SELECT ItemObject FROM items WHERE ItemId LIKE ? AND State LIKE ? AND Type Like ? AND Name LIKE ? AND Price BETWEEN ? AND ? AND Ign LIKE ? AND Username LIKE ? and Timezone LIKE ? AND Keywords LIKE ?;";
    
        try 
        {
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
    }
    
    
    private ArrayList<Item> packItems(ResultSet matchingItemResults) throws SQLException
    {
        // Pack all results into the 'Item's ArrayList 
        
        ArrayList<Item> matchingItemList = new ArrayList();
        
        while (matchingItemResults.next())
        {
            byte[] blobObject = matchingItemResults.getBytes("ItemObject");
            matchingItemList.add((Item)convertBlobToObject(blobObject));
        }
        
        return matchingItemList;
    }
    
    
    private Object convertBlobToObject(byte[] blob)
    {
        Object convertedObject = null;
        ByteArrayInputStream byteInput = null;
        ObjectInputStream objectInput = null;
        
        try
        {
            byteInput = new ByteArrayInputStream(blob);
            objectInput = new ObjectInputStream(byteInput);
            
            convertedObject = objectInput.readObject();
            return convertedObject;
        }
        catch (IOException e)
        {
            System.out.println("Request worker: IO Exception converting blob to object. Error: " + e.getMessage());
            return null;
        }
        catch (ClassNotFoundException e)
        {
            System.out.println("Request worker: Class type not found converting blob to object. Error: " + e.getMessage());
            return null;
        }
        finally 
        {
            try 
            {
                if (byteInput != null)
                    byteInput.close();
                if (objectInput != null)
                    objectInput.close();
            }
            catch (IOException e)
            {
                System.out.println("Request worker: Failed to close blob to object resources.");
            }
        }
    }
    
    // END item list request section
    
    
}