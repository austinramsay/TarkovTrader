
package tarkov.trader.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import tarkov.trader.objects.Profile;

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
            System.out.println(error);
            
            return null;
        }
        catch (SQLException e)
        {
            String error = "Network: SQLException when attempting DB connection. Error: " + e.getMessage();
            System.out.println(error);

            return null;
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
            System.out.println("DBWorker: Failed to perform simple blob query. Given command: " + command + ". Error: " + e.getMessage());
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
            System.out.println("DBWorker: Version difference in client versus server. (Attempting to convert blob to object)");
            return null;
        }
        catch (ClassNotFoundException e)
        {
            System.out.println("DBWorker: Class type not found converting blob to object. Error: " + e.getMessage());
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
            
            System.out.println("Server Worker: Profile (" + username + ") updated successfully.");
            
            return true;
        }
        catch (SQLException e)
        {
            String error = "Server Worker: Profile update failed for " + username + ". Error: " + e.getMessage();
            System.out.println(error);
            return false;
        }
        finally 
        {
            try {
                if (statement != null)
                    statement.close();
                if (dbConnection != null)
                    dbConnection.close();
            } catch (SQLException e) { System.out.println("ServerWorker: Failed to close resources after inserting notifications for " + username + "."); }         
        }
    }
        
    
}
