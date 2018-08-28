
package tarkov.trader.server;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.bind.DatatypeConverter;
import tarkov.trader.objects.Chat;
import tarkov.trader.objects.Item;
import tarkov.trader.objects.ItemListForm;
import tarkov.trader.objects.ItemModificationRequest;
import tarkov.trader.objects.ItemStatus;
import tarkov.trader.objects.LoginForm;
import tarkov.trader.objects.NewAccountForm;
import tarkov.trader.objects.Notification;
import tarkov.trader.objects.Profile;

// DatabaseWorker class 
// Handles all database work
// Includes methods for entry insertion, results, and manipulation

public class DatabaseWorker 
{
    private String clientIp;
    private ClientCommunicator communicator;
    private RequestWorker worker;
    
    
    public DatabaseWorker()
    {
        // Server use only
    }
    
    
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
            TarkovTraderServer.broadcast(error);
            communicator.sendAlert(error);
            
            return null;
        }
        catch (SQLException e)
        {
            String error = "Network: SQLException when attempting DB connection. Error: " + e.getMessage();
            TarkovTraderServer.broadcast(error);
            communicator.sendAlert(error);
            return null;
        }
    }
    
    
    /* 
    // SIMPLE QUERY METHODS
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
            if (result != null)
            {
                result.first();
                return result.getString(columnName);
            }
            return null;
        }
        catch (SQLException e)
        {
            TarkovTraderServer.broadcast("DBWorker: Failed to perform simple query. Given command: " + command + ". Error: " + e.getMessage());
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
            } catch (SQLException e) { TarkovTraderServer.broadcast("DBWorker: Failed to close simple query resources."); }
        }
    }
    
    
    private byte[] simpleBlobQuery(String command, String columnName, String onlyParameter)
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
            if (result.next())
            {
                return result.getBytes(columnName);
            }
            else
                return null;
        }
        catch (SQLException e)
        {
            TarkovTraderServer.broadcast("DBWorker: Failed to perform simple blob query. Given command: " + command + ". Error: " + e.getMessage());
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
            } catch (SQLException e) { TarkovTraderServer.broadcast("DBWorker: Failed to close simple query resources."); }
        }    
    }

    
    public ArrayList<String> pullUserList()
    {
        String command = "SELECT username FROM accounts;";
        ArrayList<String> userList = new ArrayList<>();
        
        Connection dbConnection = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        
        try 
        {
            dbConnection = this.getDBconnection();
            
            statement = dbConnection.prepareStatement(command);
            
            result = statement.executeQuery();
            
            if (result == null)
            {
                // No users in the database
                return userList;
            }
            
            while (result.next())
            {
                // Iterate results of usernames, add to the arraylist to be returned
                userList.add(result.getString("username"));
            }
            
            return userList;
        }
        catch (SQLException e)
        {
            TarkovTraderServer.broadcast("DBWorker: Failed to perform simple result query. Given command: " + command + ". Error: " + e.getMessage());
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
            } catch (SQLException e) { TarkovTraderServer.broadcast("DBWorker: Failed to close simple query resources."); }
        }
    }
        
        
    
    /*
    // LOGIN INFORMATION / AUTHENTICATION
    // The users password is matched to their username and returns a boolean value to be used in Request Worker
    */
        
    public boolean loginAuthenticated(LoginForm loginform)
    {
        String saltCommand = "SELECT salt FROM accounts WHERE BINARY username=?;";
        String passwordCommand = "SELECT password FROM accounts WHERE BINARY username=?;";
        
        Connection dbConnection = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        
        // Pull the unique salt for the user from the DB
        // Hash the received password from loginform with the unique salt
        // Compare to stored hashed password in database for authentication
        
        byte[] storedSalt;
        
        try
        {
            dbConnection = this.getDBconnection();
            
            // Get salt first to hash the users input password
            statement = dbConnection.prepareStatement(saltCommand);
            statement.setString(1, loginform.getUsername());
            result = statement.executeQuery();
            
            if (!result.first())
                return false;
            
            storedSalt = result.getBytes(1);
            
            // Get hashed password from DB
            statement = null;
            statement = dbConnection.prepareStatement(passwordCommand);
            statement.setString(1, loginform.getUsername());
            result = statement.executeQuery();
            
            if (!result.first())
                return false;
            
            return(this.getHashedPassword(loginform.getHashedPassword(), storedSalt).equals(result.getString(1)));
        }
        catch (SQLException e)
        {
            TarkovTraderServer.broadcast("DBWorker: SQL Exception for " + clientIp + " when attempting to authenticate. Error: " + e.getMessage());
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
            } catch (SQLException e) { TarkovTraderServer.broadcast("DBWorker: Failed to close resources when verifying login for " + clientIp + ". Error: " + e.getMessage()); }
        }
    }
    
    
    public String getHashedPassword(String tobehashed, byte[] salt)
    {
        byte[] hashedBytes = null;
        
        try 
        {         
            MessageDigest passDigest = MessageDigest.getInstance("SHA-512");
            passDigest.update(salt);
            hashedBytes = passDigest.digest(tobehashed.getBytes(StandardCharsets.UTF_8));
            String hashedPassword = DatatypeConverter.printHexBinary(hashedBytes);
            return hashedPassword;
            
        } catch (NoSuchAlgorithmException e)
        {
            TarkovTraderServer.broadcast("Security: Failed to hash password with specified algorithm.");
            return null;
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
    
    
    public File getUserImageFile(String username)
    {
        String command = "SELECT image FROM accounts WHERE username=?";
        String columnName = "image";
        String onlyParameter = username;
        
        byte[] fileBytes = simpleBlobQuery(command, columnName, onlyParameter);
        
        if (fileBytes == null)
            return null;
        
        Object fileObject = convertBlobToObject(fileBytes);
        
        return (File)fileObject;
    }
    
    
    // Return an ArrayList of items the user has for sale. This is used to rebuild a broken profile
    // The user's completed sales and buy list will be lost however
    public ArrayList<Item> getUserItems(String username)
    {
        String command = "SELECT ItemObject FROM items WHERE username=?";
        String onlyParameter = username;

        ArrayList<Item> itemList = new ArrayList<>();
        
        Connection dbConnection = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        
        try 
        {
            dbConnection = this.getDBconnection();
            statement = dbConnection.prepareStatement(command);
            statement.setString(1, username);
            result = statement.executeQuery();
            
            if (result == null)
            {
                // No items in the database
                return itemList;
            }
            
            while (result.next())
            {
                // Iterate results from the item query
                Object itemObject = convertBlobToObject(result.getBytes(1));
                Item matchingItem = (Item)itemObject;
                itemList.add(matchingItem);
            }
            
            return itemList;
        }
        catch (SQLException e)
        {
            TarkovTraderServer.broadcast("DBWorker: Failed to retrieve " + username + "'s list of items. Error: " + e.getMessage());
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
            } catch (SQLException e) { TarkovTraderServer.broadcast("DBWorker: Failed to close simple query resources."); }
        }        
    }
    
    
    
    /* 
    // NEW ACCOUNTS
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
            TarkovTraderServer.broadcast("DBWorker: Failed to test for field existence on new account request for " + clientIp + ". Error: " + e.getMessage());
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
            } catch (SQLException e) { TarkovTraderServer.broadcast("DBWorker: Failed to close resources when verifying existing fields for " + clientIp + ". Error: " + e.getMessage()); }
        }
    }
    
    
    public void insertAccountInfo(NewAccountForm newAccount, Profile newProfile, String clientIp)
    {        
        String accountcommand = "INSERT INTO accounts (username, password, salt, firstname, lastname, ign, timezone, image, ipaddr, profile) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        String chatcommand = "INSERT INTO chats (Username, ChatMap) VALUES (?, ?);";
        String notificationscommand = "INSERT INTO notifications (Username, Notifications) VALUES (?, ?);";
        
        HashMap<String, Chat> newaccountchatmap = new HashMap<>();
        ArrayList<Notification> newnotificationlist = new ArrayList<>();
        
        Connection dbConnection = null;
        PreparedStatement statement = null;
        
        try
        {
            dbConnection = this.getDBconnection();

            statement = dbConnection.prepareStatement(accountcommand);
            statement.setString(1, newAccount.getUsername());
            statement.setString(2, this.getHashedPassword(newAccount.getHashedPassword(), newAccount.getSalt()));   // We will receive a one time hashed SHA-512 password. Hash this with the unique salt to store into DB
            statement.setBytes(3, newAccount.getSalt());
            statement.setString(4, newAccount.getFirst());
            statement.setString(5, newAccount.getLast());
            statement.setString(6, newAccount.getIgn());
            statement.setString(7, newAccount.getTimezone());
            statement.setObject(8, newAccount.getUserImageFile());
            statement.setString(9, clientIp);
            statement.setObject(10, newProfile);
            statement.executeUpdate();
            
            statement = null;
            statement = dbConnection.prepareStatement(chatcommand);
            statement.setString(1, newAccount.getUsername());
            statement.setObject(2, newaccountchatmap);
            statement.executeUpdate();
            
            statement = null;
            statement = dbConnection.prepareStatement(notificationscommand);
            statement.setString(1, newAccount.getUsername());
            statement.setObject(2, newnotificationlist);
            statement.executeUpdate();
            
            TarkovTraderServer.broadcast("DBWorker: New account success for: " + clientIp);
            communicator.sendAlert("Account successfully created.");
        }
        catch (SQLException e)
        {
            String error = "DBWorker: New account insert failed. Error: " + e.getMessage();
            TarkovTraderServer.broadcast(error);
            communicator.sendAlert(error);
        }
        finally // Close resources
        {
            try {
               if (statement != null)
                   statement.close();
               if (dbConnection != null)
                   dbConnection.close();
            } catch (SQLException e) { TarkovTraderServer.broadcast("DBWorker: Failed to close resources for new account insert. Error: " + e.getMessage()); }
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
        String command = "INSERT INTO items (State, Type, Name, Price, Ign, Username, Timezone, Keywords, Notes, ItemObject) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        
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
            statement.setObject(10, newItem);
            
            statement.executeUpdate();
            statement.close();
            dbConnection.close();
            return true;
        }
        catch (SQLException e)
        {
            String error = "DBWorker: New item insert failed. Error: " + e.getMessage();
            TarkovTraderServer.broadcast(error);
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
            TarkovTraderServer.broadcast("Request process: Failed to retrieve item objects. " + e.getMessage());
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
            { TarkovTraderServer.broadcast("DBWorker: Couldn't close resources. Error: " + e.getMessage()); }
             
            
        }
        
        itemlistform.setItemList(matchingItemList);
        
        if (worker.sendForm(itemlistform))
        {
            TarkovTraderServer.broadcast("Request complete: Returned item list to " + clientIp + ".");
        }
    }
    
    
    private ResultSet queryItems(Connection dbConnection, PreparedStatement statement, ResultSet result, ItemListForm itemlistform)
    {       
        HashMap<String, String> searchFlags = itemlistform.getSearchFlags();
        
        String pricemin;
        String pricemax;
        
        if (searchFlags.get("pricemin").equals("") && searchFlags.get("pricemax").equals("")) // If there was no price range specified, use 1 to 50 million
        {
            pricemin = "0";
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
            TarkovTraderServer.broadcast(error);
            communicator.sendAlert(error);
            return null;
        }
        catch (NumberFormatException e)
        {
            TarkovTraderServer.broadcast("DBWorker: Number format exception parsing price flag.");
            return null;
        }
    }
    
    
    private ArrayList<Item> packItems(ResultSet matchingItemResults) throws SQLException
    {
        // Pack all results into the 'Item's ArrayList 
        ArrayList<Item> matchingItemList = new ArrayList();
        
        try 
        {
            while (matchingItemResults.next())
            {
                byte[] blobObject = matchingItemResults.getBytes("ItemObject");
                Item tempItem = (Item)convertBlobToObject(blobObject);
                tempItem.setSellerState(TarkovTraderServer.authenticatedUsers.containsKey(tempItem.getUsername()));   // Set the 'Seller State' in the item before sending to client. 
                matchingItemList.add(tempItem);
            }
            return matchingItemList;
        }
        catch (NullPointerException e)
        {
            communicator.sendAlert("No results found!");
            return null;
        }
        
    }
    
    // END item list request section
    
    
    
    /*
    // Item Modification Section
    */
    
    public boolean processItemModification(ItemModificationRequest itemModRequest, String compareUsername)
    {
        String modificationType = itemModRequest.getModificationType();
        Item itemToModify = itemModRequest.getItemToModify();
        Item preModifiedItem = itemModRequest.getPreModifiedItem();
        String itemUsername = itemToModify.getUsername();
        
        String command; 
        Connection dbConnection = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        
        if (!itemModRequest.getItemToModify().getUsername().equals(compareUsername))
        {
            TarkovTraderServer.broadcast("DBWorker: Failed to authenticate matching user to modify item. Item username is " + itemUsername + " compared to attempted user " + compareUsername + ".");
            communicator.sendAlert("Authentication failure. Failed to delete listing.");
            return false;
        }
        
        // Pull the client's profile to update after said modification
        Profile currentProfile = getProfile(itemUsername);
        if (currentProfile == null)
        {
            TarkovTraderServer.broadcast("DBWorker: Item modification failed. Profile is null.");
            communicator.sendAlert("Failed to process item modification.");
            return false;
        }
        
        if (itemToModify.getItemStatus() != ItemStatus.OPEN)
        {
            TarkovTraderServer.broadcast("DBWorker: Item modification not permitted. Item status is not 'open' (" + itemToModify.getItemStatus().getReason() +")" + ".");
            communicator.sendAlert("Modification not permitted. Item status not 'open'.");
            return false;
        }
        
        try 
        {
            dbConnection = getDBconnection();
                        
            switch (modificationType)
            {
                case "delete":
                    
                    command = "DELETE FROM items WHERE State LIKE ? AND Type LIKE ? AND Name LIKE ? AND Ign LIKE ? AND Username LIKE ?;";
                    
                    // TODO TODO TODO COMPARE DATE OF STORED ITEM TO REQUESTED ITEM
                    // TODO TODO TODO TODO
                    statement = dbConnection.prepareStatement(command);
                    statement.setString(1, itemToModify.getTradeState());
                    statement.setString(2, itemToModify.getItemType());
                    statement.setString(3, itemToModify.getName());
                    statement.setString(4, itemToModify.getIgn());
                    statement.setString(5, itemToModify.getUsername());
                    int deleteCount = statement.executeUpdate();
                    
                    // Sync client's profile
                    currentProfile.removeItem(itemToModify);
                    updateProfile(currentProfile, itemUsername);
                    // Done
                    
                    TarkovTraderServer.broadcast("DBWorker: " + deleteCount + " item(s) deleted for " + itemUsername + ".");
                    communicator.sendAlert(deleteCount + " item(s) deleted successfully.");
                    break;
                    
                    
                case "edit":
                    
                    // First, update the ItemObject in the database
                    command = "UPDATE items SET ItemObject = ?, ItemId = (SELECT @update_id := ItemId) "
                            + "WHERE State LIKE ? AND Type LIKE ? AND Name LIKE ? AND Ign LIKE ? AND Username LIKE ?;";
                    
                    // Before running the command, create our update_id variable so we know what the ID is after it is updated
                    statement = dbConnection.prepareStatement("SET @update_id := 0;");
                    statement.execute();
                    // Variable ready for use
                    
                    // Perform modifications
                    statement = dbConnection.prepareStatement(command);
                    statement.setObject(1, preModifiedItem);
                    statement.setString(2, itemToModify.getTradeState());
                    statement.setString(3, itemToModify.getItemType());
                    statement.setString(4, itemToModify.getName());
                    statement.setString(5, itemToModify.getIgn());
                    statement.setString(6, itemToModify.getUsername());               
                    int objectEditCount = statement.executeUpdate();
                    // Done
                    
                    // Now, update the entry in the database
                    // Get our last updated ItemID 
                    statement = dbConnection.prepareStatement("SELECT @update_id;");
                    result = statement.executeQuery();
                    int itemId = 0;
                    if (result.first())
                        itemId = result.getInt(1);
                    
                    command = "UPDATE items SET State=?, Type=?, Name=?, Price=?, Keywords=?, Notes=? WHERE ItemId=?;";
                    statement = dbConnection.prepareStatement(command);
                    statement.setString(1, preModifiedItem.getTradeState());
                    statement.setString(2, preModifiedItem.getItemType());
                    statement.setString(3, preModifiedItem.getName());
                    statement.setInt(4, preModifiedItem.getPrice());
                    statement.setString(5, preModifiedItem.getKeywords());
                    statement.setString(6, preModifiedItem.getNotes());
                    statement.setInt(7, itemId);
                    int entryEditCount = statement.executeUpdate();
                    // Done
                    
                    // Sync the client's profile
                    currentProfile.removeItem(itemToModify);
                    currentProfile.appendItem(preModifiedItem);
                    updateProfile(currentProfile, itemUsername);
                    // Done
                    
                    if (entryEditCount != objectEditCount)
                    {
                        communicator.sendAlert("Item was modified but there was an issue applying all changes.");
                        TarkovTraderServer.broadcast("DBWorker: Modification request exception. " + entryEditCount + " entries were modifed with " + objectEditCount + " object(s) edited.");
                        return false;
                    }
                    
                    TarkovTraderServer.broadcast("DBWorker: " + entryEditCount + " item(s) edited for " + itemUsername);
                    communicator.sendAlert(entryEditCount + " item(s) successfully edited.");
                    break;
                    
                    
                case "suspend":
                    
                    command = "UPDATE items SET ItemObject=? WHERE State LIKE ? AND Type LIKE ? AND Name LIKE ? AND Ign LIKE ? AND Username LIKE ?;";
                    
                    // TODO TODO TODO COMPARE DATE OF STORED ITEM TO REQUESTED ITEM
                    // TODO TODO TODO TODO
                    statement = dbConnection.prepareStatement(command);
                    statement.setObject(1, preModifiedItem);
                    statement.setString(2, preModifiedItem.getTradeState());
                    statement.setString(3, preModifiedItem.getItemType());
                    statement.setString(4, preModifiedItem.getName());
                    statement.setString(5, preModifiedItem.getIgn());
                    statement.setString(6, preModifiedItem.getUsername());
                    int suspendCount = statement.executeUpdate();
                    
                    boolean isSuspended = preModifiedItem.getSuspensionState();
                    
                    // Sync the client's profile
                    currentProfile.removeItem(itemToModify);
                    currentProfile.appendItem(preModifiedItem);
                    updateProfile(currentProfile, itemUsername);
                    // Done
                    
                    // Communication
                    if (isSuspended)
                    {
                        TarkovTraderServer.broadcast("DBWorker: " + suspendCount + " item(s) suspended for " + itemUsername + ".");
                        communicator.sendAlert(suspendCount + " item(s) suspended.");
                    }
                    else
                    {
                        TarkovTraderServer.broadcast("DBWorker: " + suspendCount + " item(s) unsuspended for " + itemUsername + ".");
                        communicator.sendAlert(suspendCount + " item(s) unsuspended.");                        
                    }
                    
                    break;                    
                }
            
            return true;
        }
        catch (SQLException e)
        {
            String error = "Item modification failed. Error: " + e.getMessage();
            e.printStackTrace();
            TarkovTraderServer.broadcast(error);
            communicator.sendAlert(error);
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
            } catch (SQLException e) { TarkovTraderServer.broadcast("DBWorker: Failed to close resources after processing item modification for " + compareUsername + "."); }
        }
    }
    
    
    
    /*
    // Chats: deals with inserting new chats, and pulling chat list requests
    */
    
    public boolean insertChatMap(String username, HashMap<String, Chat> chatmap, boolean insert)
    {
        String updateCommand = "UPDATE chats SET ChatMap = ? WHERE Username = ?;";
        String insertCommand = "INSERT INTO chats (ChatMap, Username) VALUES (?, ?);";
        String command;
        
        if (insert)
            command = insertCommand;
        else
            command = updateCommand;
        
        Connection dbConnection = null;
        PreparedStatement statement = null;
        
        try 
        {
            dbConnection = getDBconnection();
            statement = dbConnection.prepareStatement(command);   
            
            statement.setObject(1, chatmap);            
            statement.setString(2, username);                
            
            statement.executeUpdate();
            
            return true;
        }
        catch (SQLException e)
        {
            String error = "DBWorker: New chat map insert failed. Error: " + e.getMessage();
            TarkovTraderServer.broadcast(error);
            communicator.sendAlert(error);
            return false;
        }
        finally 
        {
            try {
                if (statement != null)
                    statement.close();
                if (dbConnection != null)
                    dbConnection.close();
            } catch (SQLException e) { TarkovTraderServer.broadcast("DBWorker: Failed to close resources after inserting chat map for " + clientIp + "."); }
        }
    }
    
    
    // Pull the chatmap upon authenticated user login
    public HashMap<String, Chat> pullChatMap(String username)
    {
        HashMap<String, Chat> chatmap;
        
        String command = "SELECT ChatMap FROM chats WHERE Username=?;";
        
        byte[] chatMapBytes = simpleBlobQuery(command, "ChatMap", username);
        
        if (chatMapBytes != null)
        {
            chatmap = (HashMap)convertBlobToObject(chatMapBytes);
        }
        else
        {
            chatmap = new HashMap<>();
            insertChatMap(username, chatmap, true);
        }
        
        return chatmap;
    }
    
    
    // For inserting a new message into a chat (could be used by message cache flush or if a user is offline and we need to push the message
    public boolean insertNewMessage(String pullusername, String chatusername, String message)
    {
        String pullcommand = "SELECT ChatMap FROM chats WHERE Username=?;";
        String updatecommand = "UPDATE chats SET ChatMap = ? WHERE Username = ?;";
        boolean createdNewChat = false;
        
        Connection dbConnection = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        
        try 
        {
            dbConnection = getDBconnection();
            statement = dbConnection.prepareStatement(pullcommand);   
            
            statement.setString(1, pullusername);
            
            result = statement.executeQuery();
            result.first();
            
            byte[] chatmapbytes = result.getBytes(1);
            
            HashMap<String, Chat> tempChatMap = (HashMap)convertBlobToObject(chatmapbytes);
                        
            // Check for existence of chat for other user
            // This will happen if the destination user is offline, and the online user is attempting to send a message to a client who has deleted this chat
            Chat tempChat;
            if (tempChatMap.containsKey(chatusername))
            {
                tempChat = tempChatMap.get(chatusername);
                tempChat.appendMessage(message);
            }
            else
            {
                // The chat may not exist..create it now to be reinserted
                tempChat = new Chat(pullusername, chatusername, null);
                tempChat.appendMessage(message);
                createdNewChat = true;
            }
                                    
            tempChatMap.put(chatusername, tempChat);
            
            statement = null;
            statement = dbConnection.prepareStatement(updatecommand);
            
            statement.setObject(1, tempChatMap);
            statement.setString(2, pullusername);
            
            statement.executeUpdate();
            
            if (createdNewChat)
                return false;
            else
                return true;
        }
        catch (SQLException e)
        {
            String error = "DBWorker: New message insert failed for " + clientIp + ". Error: " + e.getMessage();
            TarkovTraderServer.broadcast(error);
            communicator.sendAlert(error);
            return false;
        }
        finally 
        {
            try {
                if (statement != null)
                    statement.close();
                if (result != null)
                    result.close();
                if (dbConnection != null)
                    dbConnection.close();
            } catch (SQLException e) { TarkovTraderServer.broadcast("DBWorker: Failed to close resources after inserting message for " + clientIp + "."); }
        }
    }
    
    // END CHAT SECTION
    
    
    
    //
    // This method is used to convert blobs back into objects, needed universally throughout the DBWorker
    //
    
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
            TarkovTraderServer.broadcast("DBWorker: Version difference in client versus server. (Attempting to convert blob to object)");
            return null;
        }
        catch (ClassNotFoundException e)
        {
            TarkovTraderServer.broadcast("DBWorker: Class type not found converting blob to object. Error: " + e.getMessage());
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
                TarkovTraderServer.broadcast("Request worker: Failed to close blob to object resources.");
            }
        }
    }
    
    
    
    /*
    // NOTIFICATIONS SECTION
    */
    
    public ArrayList<Notification> getNotifications(String username)
    {
        String command = "SELECT Notifications FROM notifications WHERE username=?";
        String columnName = "Notifications";
        String onlyParameter = username;
        
        byte[] notificationsBytes = simpleBlobQuery(command, columnName, onlyParameter);
        
        if (notificationsBytes == null)
            return null;
        
        Object notificationsObject = convertBlobToObject(notificationsBytes);
        
        return (ArrayList<Notification>)notificationsObject;
    }

    
    public boolean insertNotifications(String username, ArrayList<Notification> notificationsList)
    {
        String command = "UPDATE notifications SET Notifications=? WHERE Username=?";
        
        Connection dbConnection = null;
        PreparedStatement statement = null;
        
        try 
        {
            dbConnection = getDBconnection();
            statement = dbConnection.prepareStatement(command);   
            
            statement.setObject(1, notificationsList);            
            statement.setString(2, username);
            
            statement.executeUpdate();
            
            return true;
        }
        catch (SQLException e)
        {
            String error = "DBWorker: New notifications insert failed. Error: " + e.getMessage();
            TarkovTraderServer.broadcast(error);
            communicator.sendAlert(error);
            return false;
        }
        finally 
        {
            try {
                if (statement != null)
                    statement.close();
                if (dbConnection != null)
                    dbConnection.close();
            } catch (SQLException e) { TarkovTraderServer.broadcast("DBWorker: Failed to close resources after inserting notifications for " + clientIp + "."); }
        }        
    }
    
    
    public boolean clearNotifications(String username)
    {
        ArrayList<Notification> tempNotifications = this.getNotifications(username);
        
        // Empty the notifications list
        tempNotifications.clear();
        
        this.insertNotifications(username, tempNotifications);
        
        return true;
    }
    
    
    
    /*
    // PROFILES
    */
    
    public Profile getProfile(String username)
    {
        String command = "SELECT profile FROM accounts WHERE username=?";
        String columnName = "profile";
        String onlyParameter = username;
        
        byte[] profileBytes = simpleBlobQuery(command, columnName, onlyParameter);
        
        if (profileBytes == null)
            return null;
        
        Object profileObject = convertBlobToObject(profileBytes);
        
        return (Profile)profileObject;
    }
    
    
    public boolean updateProfile(Profile profile, String username)
    {
        String command = "UPDATE accounts SET profile=? WHERE username=?";
        
        Connection dbConnection = null;
        PreparedStatement statement = null;
        
        try 
        {
            dbConnection = getDBconnection();
            statement = dbConnection.prepareStatement(command);   
            
            statement.setObject(1, profile);            
            statement.setString(2, username);
            
            statement.executeUpdate();
            
            TarkovTraderServer.broadcast("DBWorker: Profile (" + username + ") updated successfully.");
            
            return true;
        }
        catch (SQLException e)
        {
            String error = "DBWorker: Profile update failed for " + username + ". Error: " + e.getMessage();
            TarkovTraderServer.broadcast(error);
            communicator.sendAlert(error);
            return false;
        }
        finally 
        {
            try {
                if (statement != null)
                    statement.close();
                if (dbConnection != null)
                    dbConnection.close();
            } catch (SQLException e) { TarkovTraderServer.broadcast("DBWorker: Failed to close resources after inserting notifications for " + clientIp + "."); }
            
            // Finally, sync the 'current' profile
            worker.updateCurrentProfile(profile);            
        }
    }
    
}


