
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
import javax.swing.JOptionPane;
import tarkov.trader.objects.Item;
import tarkov.trader.objects.Notification;
import tarkov.trader.objects.NotificationType;
import tarkov.trader.objects.Profile;
import tarkov.trader.objects.Report;

/**
 *
 * @author austin
 */

public class ServerWorker {
    
    private final String USERNAME = "Tarkov Trader";
    
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
        
    public void acceptReport(Report report)
    {
        // Accepting a report
        // We need to get the reported users profile, append the report to their profile, and finally update in the database
        String reportedUser = report.getUserToReport();
        Profile reportedUserProfile = getProfile(reportedUser);
        
        
        // Did we get the user profile successfully?
        if (reportedUserProfile == null)
        {
            // Failed to retrieve the reported user profile
            JOptionPane.showMessageDialog(null, "Failed to append report. Profile null.");
            return;
        }
        
        
        // Append the report to the profile and if failed, exit the method
        if (!reportedUserProfile.appendReport(report))
        {
            JOptionPane.showMessageDialog(null, "Failed to append report. Already exists?");
            return;
        }
        
        
        // The profile is now ready to be updated
        // Update the profile in the database
        if (!updateProfile(reportedUserProfile, reportedUser))
        {
            JOptionPane.showMessageDialog(null, "Report was appended, but profile update failed.", "Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        
        // TODO TODO TODO
        // REMOVE THE SALE REQUEST / BUY FROM EACH USERS PROFILE
        
        
        // Now we can remove the report from the report log, no longer needed
        if (!TarkovTraderServer.reportLog.removeReport(report))
        {
            JOptionPane.showMessageDialog(null, "Profile was updated, but failed to remove from report log.", "Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        
        // At this point, all processes are complete
        JOptionPane.showMessageDialog(null, "Report successfully accepted.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    
    public void declineReport(Report report)
    {
        // Declining a report
        
    }
    
    // End Reports
    
    
    
    /*
    // Notifications
    */
    
    private void sendNotification(NotificationType type, String sendNotificationTo, String displayOnNotify)
    {
        String type_flag = null;
        switch(type)
        {
            case CHAT:
                type_flag = "chat";
                break;
                
            case REPUTATION:
                type_flag = "repupdate";
                break; 
                
            case REPORT:
                type_flag = "report";
                break;
                
            case SALE_REQUEST_UPDATE:
                type_flag = "salerequestupdate";
                break;
                
            case NEW_SALE_REQUEST:
                type_flag = "newsalerequest";
                break;
                
            case MESSAGE:
                type_flag = "message";
                break;
        }
        
        // Build new notification
        Notification newNotification = new Notification(type_flag, displayOnNotify);
        
        
        // If the notification is for a message, we need to set the count - because this is a live notification we can set to 1
        if (type == NotificationType.MESSAGE)
            newNotification.setCount(1);
        
        
        // Distribute newly built notification
        if(isOnline(sendNotificationTo))
        {
            // The intended user is NOT this user
            // User is online, we'll send a notification now through their worker
            RequestWorker destinationWorker = TarkovTraderServer.authenticatedUsers.get(sendNotificationTo);
            destinationWorker.sendForm(newNotification);             
        }
        else
        {
            // User is offline, push to the intended user's database
            ArrayList<Notification> destinationNotifications = getNotifications(sendNotificationTo);                                // Get the current destination user's notifications list
            
            ArrayList<Notification> destinationUpdatedNotifications = addNotification(destinationNotifications, type_flag, displayOnNotify);    // Append our new chat notification 
            
            insertNotifications(sendNotificationTo, destinationUpdatedNotifications);                                               // Update the list for the user's DB            
        }
    }
 
    
    // Append a notification to a users notification list
    private ArrayList<Notification> addNotification(ArrayList<Notification> notificationsList, String notificationType, String displayOnNotify)
    {
        // We are using this method when creating a new notification to add to another user's list
        // This method finds related notifications and modifies it (For example, a user sends multiple messages - we can increase the message count rather than creating multiple notifications)
        // Or creates one if needed
        // Usually used when inserting back into another users DB 
        
        Notification tempNotification = null;
        
        for (Notification notification : notificationsList)
        {
            // Look through the current notification list
            // If we find a notification that matches the username and type of notification, remove it first so we don't end up with multiples
            if (notification.getOriginUsername() != null && notification.getOriginUsername().equals(displayOnNotify) && notification.getNotificationType().equals(notificationType))
            {
                tempNotification = notification;
                notificationsList.remove(notification);
                break;
            }
        }
        
        // No matching notification found
        if (tempNotification == null)
        {
            // No existing notification found, create a new one
            tempNotification = new Notification(notificationType, displayOnNotify);
        }
        
        // There was a matching notification found for a message from a user, increase the message count instead of creating duplicate
        if (notificationType.equals("message"))
        {
            int count = tempNotification.getCount();
            count++;
            tempNotification.setCount(count);
        }
        
        // Finally, add back into the arraylist to be returned
        notificationsList.add(tempNotification);
        
        return notificationsList;
    }    
    
    
    // Get client notifications
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

    
    // Update the notifications list for a client
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
            String error = "Server Worker: New notifications insert failed. Error: " + e.getMessage();
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
            } catch (SQLException e) { TarkovTraderServer.broadcast("Server worker: Failed to close resources after inserting notifications."); }
        }        
    }        
    
    // End notifications
    
    
    
    /*
    // Client Connection Handling
    */
    
    private boolean isOnline(String username)
    {
        if (TarkovTraderServer.authenticatedUsers.containsKey(username))
            return true;
        else
            return false;
    }    
    
    // End client connection handling
}
