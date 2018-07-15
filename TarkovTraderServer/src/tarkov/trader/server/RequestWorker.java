
package tarkov.trader.server;

import tarkov.trader.objects.NewAccountForm;
import java.net.*;
import java.io.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import tarkov.trader.objects.LoginForm;
import tarkov.trader.objects.Form;
import tarkov.trader.objects.Item;
import tarkov.trader.objects.ItemForm;
import tarkov.trader.objects.ItemListForm;


public class RequestWorker implements Runnable {
    
    private DatabaseWorker dbWorker;
    private ClientCommunicator communicator;
    
    private Socket client;
    private Socket clientComm;
    private String clientIp;
    private String clientUsername;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private Form form;
    
    
    public RequestWorker(Socket client, Socket clientComm)
    {
        this.client = client;
        this.clientComm = clientComm;
    }
    
    
    @Override
    public void run()
    {
        clientIp = client.getInetAddress().getHostAddress();
        
        communicator = new ClientCommunicator(clientComm);
        dbWorker = new DatabaseWorker(clientIp, communicator);
        try
        {
            openStreams();
            while (true)
            {
                waitForRequests();
            }
        }
        catch (IOException e)
        {
            System.out.println("Client " + clientIp + " has disconnected.");
            if (TarkovTraderServer.authenticatedUsers.containsKey(clientIp))
                TarkovTraderServer.authenticatedUsers.remove(clientIp);
            // TODO: HANDLE A CLOSED CONNECTION
        }
        catch (ClassNotFoundException e)
        {
            System.out.println("Request Worker: Failed to create form. " + e.getMessage());
            communicator.sendAlert("Request Worker: Failed to create form.");
        }
    }
    
    
    private void openStreams() throws IOException
    {
        inputStream = new ObjectInputStream(client.getInputStream());
        outputStream = new ObjectOutputStream(client.getOutputStream());
    }
    
    
    private void waitForRequests() throws IOException, ClassNotFoundException
    {
        // While true loop in run() method
        
        while((form = (Form)inputStream.readObject()) != null)
        {
            if(verifyForm(form))
                unpack(form);
            else
                communicator.sendAlert("Server received an empty request.");
        }
    }
    
    
    private boolean verifyForm(Form form)
    {
        if (form.getType() == null)
            return false;
        else
            return true;
    }
    
    
    private void unpack(Form form)
    {
        String type = form.getType();
        
        switch (type)
        {
            case "newaccount":
                NewAccountForm newAccountInfo = (NewAccountForm)form;
                if (verifyClientAccountInfo(newAccountInfo))
                     processNewAccount(newAccountInfo);
                break;
                
                
            case "login":
                LoginForm loginform = (LoginForm)form;
                
                if (loginAuthenticated(loginform))
                {
                    loginform.setAuthenticationState(true);
                    
                    if (sendForm(loginform)) 
                    {
                        TarkovTraderServer.authenticatedUsers.put(loginform.getUsername(), clientIp);
                        this.clientUsername = loginform.getUsername();
                        System.out.println("Successful user authentication for: " + clientIp);
                    }
                }
                else
                {
                    if (sendForm(loginform))
                    {
                        System.out.println("Failed user authentication for: " + clientIp);
                        communicator.sendAlert("Failed account authentication. Check credentials.");
                    }
                }
                break;
                
                
            case "newitem":
                ItemForm newitemform = (ItemForm)form;
                
                if (authenticateRequest())
                    processNewItemRequest(newitemform);
                
                break;
                
                
            case "itemlist":
                ItemListForm itemlistform = (ItemListForm)form;
                
                if (authenticateRequest())
                    processItemListRequest(itemlistform);
                
                break;
                
            default:
                System.out.println("Received a request from " + clientIp + " but couldn't interpret the type.");
                break;
                
        }
    }
    
    
    public boolean sendForm(Form form)
    {
        try
        {
            outputStream.writeObject(form);
            return true;
        }
        catch (IOException e)
        {
            communicator.sendAlert("Server failed to return processed request.");
            return false;
        }
    }
    
        
    private void processNewAccount(NewAccountForm newAccountInfo)
    {
        String values = 
                "'" + newAccountInfo.getUsername() + "', " +
                "'" + newAccountInfo.getPassword() + "', " +
                "'" + newAccountInfo.getFirst() + "', " +
                "'" + newAccountInfo.getLast() + "', " +
                "'" + newAccountInfo.getIgn() + "', " +
                "'" + clientIp + "'";
        
        dbWorker.insertAccountInfo(values);
    }
    
    
    private boolean verifyClientAccountInfo(NewAccountForm form)
    {
        // Prevents users from creating multiple accounts (scam accounts, etc.)
       boolean ipVerified = false;
       boolean usernameVerified = false;
       boolean ignVerified = false;
       
       try 
       {
            String ipCheckCommand = existsCheckStatementFor("accounts", "ipaddr", clientIp);
            ResultSet checkIp = dbWorker.query(ipCheckCommand);
            checkIp.first(); // Move cursor to the first row
            ipVerified = (checkIp.getInt(1) != 1); // Get value from first column (only one column for this query)
            
            String nameCheckCommand = existsCheckStatementFor("accounts", "username", form.getUsername());
            ResultSet checkName = dbWorker.query(nameCheckCommand);
            checkName.first();
            usernameVerified = (checkName.getInt(1) != 1);
            
            String ignCheckCommand = existsCheckStatementFor("accounts", "ign", form.getIgn());
            ResultSet checkIgn = dbWorker.query(ignCheckCommand);
            checkIgn.first();
            ignVerified = (checkIgn.getInt(1) != 1);
            
            boolean notified = false;
            
            if(!notified && !ipVerified)
            {
                // User has more than 1 account associated with their IP address
                // TODO: SET FLAG FOR POTENTIAL FRAUD ACCOUNT
                communicator.sendAlert("Failed to verify user info. Please contact: tarkovtrader@gmail.com");
                System.out.println("Client " + clientIp + " attempted to register multiple accounts.");
                notified = true;
                return false;
            }
            
            if(!notified && !usernameVerified)
            {
                communicator.sendAlert("Username is already in use. Enter a different username.");
                notified = true;
                return false;
            }
            
            if(!notified && !ignVerified)
            {
                communicator.sendAlert("An account is already associated with this in-game name. Contact tarkovtrader@gmail.com if you do not already have an account.");
                notified = true;
                return false;
            }
            
            return true;
       }
       catch (SQLException e)
       {
           System.out.println("Request Worker: SQL Exception for " + clientIp + ". Error:" + e.getMessage());
           return false;
       }   
    }
    
    
    private String existsCheckStatementFor(String table, String column, String input)
    {
        String command = "SELECT CASE WHEN EXISTS (SELECT " + column + " FROM " + table + " WHERE " + column + " = \"" + input + "\") THEN CAST(1 AS UNSIGNED) ELSE CAST(0 AS UNSIGNED) END;";
        return command;
    }
    
    
    private boolean loginAuthenticated(LoginForm loginform)
    {
        try
        {
            String command = "SELECT password FROM accounts WHERE username=\"" + loginform.getUsername() + "\""; 
            ResultSet authResult = dbWorker.query(command);
            authResult.first();
            return (authResult.getString(1).equals(loginform.getPassword()));
        }
        catch (SQLException e)
        {
            System.out.println("Request Worker: SQL Exception for " + clientIp + " when attempting to authenticate. Error: " + e.getMessage());
            return false;
        }
    }
    
    
    private boolean authenticateRequest()
    {
        if (this.clientUsername != null)
        {
            // Should already be verified if the worker knows the username, but just double check the static authenticated users map to match username and IP
            if (TarkovTraderServer.authenticatedUsers.containsKey(clientUsername))
            {
                return (TarkovTraderServer.authenticatedUsers.get(clientUsername).equals(clientIp));
            }
            else
                return false;
        }
        else
            return false;
    }
    
    
    private void processItemListRequest(ItemListForm itemlistform)
    {
        // Need to return an ItemListForm containing a complete ArrayList of 'Item' for client to unpack and insert into table
        // The ItemListForm contains a HashMap with user specified search flags
        // The flags need to be processed and built into a query string, and then used in dbworker to pull matching results
        // Get the result set from the database, pack into an array of 'Item's
        // 'Set' the ArrayList of 'Item's into the itemlistform, and send back to client
        
        ResultSet matchingItemResults = null;
        ArrayList<Item> matchingItemList = new ArrayList();
        
        try
        {
            matchingItemResults = dbWorker.queryItems(itemlistform);
            matchingItemList = packItems(matchingItemResults);
        }
        catch (SQLException e)
        {
            System.out.println("Request process: Failed to retrieve item objects. " + e.getMessage());
            e.printStackTrace();
        }
        
        itemlistform.setItemList(matchingItemList);
        if (itemlistform.getItemList() == null)
        {
            System.out.println("the arraylist is null");
        }
        else
        {
            System.out.println("arraylist is size: " + itemlistform.getItemList().size());
        }
        
        if (sendForm(itemlistform))
        {
            System.out.println("Request complete: Returned item list to " + clientIp + ".");
        }
    }
    
    
    private ArrayList<Item> packItems(ResultSet matchingItemResults) throws SQLException
    {
        // Pack all results into the 'Item's ArrayList 
        
        ArrayList<Item> matchingItemList = new ArrayList();
        
        while (matchingItemResults.next())
        {
            System.out.println("Result here");
            matchingItemList.add((Item)convertBlobToObject((byte[]) matchingItemResults.getObject("ItemObject")));
            System.out.println("added an object");
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
    
    
    private void processNewItemRequest(ItemForm newitemform)
    {
        Item newItem = newitemform.getItem();
        
        if(dbWorker.insertNewItem(newItem))
        {
            communicator.sendAlert("New item successfully created.");
            System.out.println("New item successfully added for " + clientIp + ".");
        }
    }
   
    
}