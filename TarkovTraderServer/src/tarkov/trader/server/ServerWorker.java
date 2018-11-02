
package tarkov.trader.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import tarkov.trader.objects.Item;
import tarkov.trader.objects.Profile;
import tarkov.trader.objects.Report;
import tarkov.trader.objects.ReportType;

/**
 *
 * @author austin
 */

public class ServerWorker {
    
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
            
            return null;
        }
        catch (SQLException e)
        {
            String error = "Network: SQLException when attempting DB connection. Error: " + e.getMessage();
            TarkovTraderServer.broadcast(error);

            return null;
        }
    }    
    
    
    // Returns single column string values from a command with one parameter
    // For example, fetch timezone for a user
    public String dbQuery(String command, String onlyParameter)
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
                return result.getString(1); // Should only be one column returned here when using this method 
            }
            
            return null;
        }
        catch (SQLException e)
        {
            TarkovTraderServer.broadcast("Server Worker: Failed to perform simple query. Given command: " + command + ". Error: " + e.getMessage());
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
            } catch (SQLException e) { TarkovTraderServer.broadcast("Server Worker: Failed to close simple query resources."); }
        }
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
            
            int count = 0;
            
            while (result.next())
            {
                // Iterate results from the item query
                Object itemObject = convertBlobToObject(result.getBytes(1));
                Item matchingItem = (Item)itemObject;
                itemList.add(matchingItem);
                count++;
            }
            
            TarkovTraderServer.broadcast("Server Worker: " + count + " matching items found for user.");
            
            return itemList;
        }
        catch (SQLException e)
        {
            TarkovTraderServer.broadcast("Server Worker: Failed to retrieve " + username + "'s list of items. Error: " + e.getMessage());
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
            } catch (SQLException e) { TarkovTraderServer.broadcast("Server Worker: Failed to close simple query resources."); }
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
            TarkovTraderServer.broadcast("Server Worker: Failed to perform simple blob query. Given command: " + command + ". Error: " + e.getMessage());
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
            } catch (SQLException e) { TarkovTraderServer.broadcast("Server Worker: Failed to close simple query resources."); }
        }    
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
            TarkovTraderServer.broadcast("Server Worker: Version difference in client versus server. (Attempting to convert blob to object)");
            return null;
        }
        catch (ClassNotFoundException e)
        {
            TarkovTraderServer.broadcast("Server Worker: Class type not found converting blob to object. Error: " + e.getMessage());
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
    
    
    /*
    // ADMIN POWERS
    // Ex. Deleting items
    */
    
    public boolean userExists(String username)
    {
        Connection dbConnection = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        
        String command = "SELECT CASE WHEN EXISTS (SELECT username FROM accounts WHERE username=?) THEN CAST(1 AS UNSIGNED) ELSE CAST(0 AS UNSIGNED) END;";
        
        try 
        {
            dbConnection = this.getDBconnection();
            
            statement = dbConnection.prepareStatement(command);
            statement.setString(1, username);
            result = statement.executeQuery();     // Generic execute method - several types of commands can be pushed here
            if (result.first())
            {
                return (result.getInt(1) == 1);
            }
            
            TarkovTraderServer.broadcast("Server Worker: Possible failure to check username existence.");
            return false;
        }
        catch (SQLException e)
        {
            TarkovTraderServer.broadcast("Server Worker: Failed to perform admin username check command. Given command: " + command + ". Error: " + e.getMessage());
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
            } catch (SQLException e) { TarkovTraderServer.broadcast("Server Worker: Failed to close admin database command execution resources."); }
        }        
    }
    
    public boolean executeDatabaseCmd(String command)
    {
        Connection dbConnection = null;
        PreparedStatement statement = null;
        
        try 
        {
            dbConnection = this.getDBconnection();
            
            statement = dbConnection.prepareStatement(command);
            statement.execute();     // Generic execute method - several types of commands can be pushed here
            
            return true;
        }
        catch (SQLException e)
        {
            TarkovTraderServer.broadcast("Server Worker: Failed to perform admin database command. Given command: " + command + ". Error: " + e.getMessage());
            return false;
        }
        finally 
        {
            try {
                if (statement != null)
                    statement.close();
                if (dbConnection != null)
                    dbConnection.close();
            } catch (SQLException e) { TarkovTraderServer.broadcast("Server Worker: Failed to close admin database command execution resources."); }
        }
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
            
            TarkovTraderServer.broadcast("Server Worker: Profile (" + username + ") updated successfully.");
            
            return true;
        }
        catch (SQLException e)
        {
            String error = "Server Worker: Profile update failed for " + username + ". Error: " + e.getMessage();
            TarkovTraderServer.broadcast(error);
            return false;
        }
        finally 
        {
            try {
                if (statement != null)
                    statement.close();
                if (dbConnection != null)
                    dbConnection.close();
            } catch (SQLException e) { TarkovTraderServer.broadcast("Server Worker: Failed to close resources after inserting notifications for " + username + "."); }         
        }
    }
    
    // End Profiles
    
    
    
    /*
    // Reports
    */
        
    private void acceptReport(Report report)
    {
        // Accepting a report
        // We need to get the reported users profile, append the report to their profile, and finally update in the database
        String reportedUser = report.getUserToReport();
        ReportType type = report.getReportType();
        Profile reportedUserProfile = getProfile(reportedUser);
    }
    
    
    private void declineReport(Report report)
    {
        // Declining a report
        // We need to get the reported users profile, append the report to their profile, and finally update it in the database
        
        
    }
    
    // End Reports
    
}
