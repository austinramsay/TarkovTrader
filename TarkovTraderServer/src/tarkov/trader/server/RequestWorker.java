
package tarkov.trader.server;

import tarkov.trader.objects.NewAccountForm;
import java.net.*;
import java.io.*;
import java.util.HashMap;
import tarkov.trader.objects.Chat;
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
    
    private HashMap<String, Chat> chatMap;  // This will be pulled from DB upon authenticated login
    
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
        dbWorker = new DatabaseWorker(clientIp, communicator, this);
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
                     dbWorker.insertAccountInfo(newAccountInfo, clientIp);
                
                break;
                
                
            case "login":
                LoginForm login = (LoginForm)form;
                authenticateLogin(login);

                break;
                
                
            case "newitem":
                ItemForm newitemform = (ItemForm)form;
                
                if (authenticateRequest())
                    processNewItemRequest(newitemform);
                else
                    sendAuthenticationFailure();
                
                break;
                
                
            case "itemlist":
                ItemListForm itemlistform = (ItemListForm)form;
                
                if (authenticateRequest())
                    dbWorker.processItemListRequest(itemlistform);
                else
                    sendAuthenticationFailure();
                
                break;
                
                
            case "chat":
                Chat chat = (Chat)form;
                
                if (authenticateRequest())
                {
                    if (chat.isNew)
                    {
                        chat.setOpened();
                        processNewChat(chat);
                    }
                }
                else
                    sendAuthenticationFailure();
                
                break;
                
                
            default:
                System.out.println("Received a request from " + clientIp + " but couldn't interpret the type.");
                break;
                
        }
    }
    
    
    private void sendAuthenticationFailure()
    {
        communicator.sendAlert("Failed to authenticate request.");
        System.out.println("Request: Failed to authenticate request for " + clientIp + ".");
    }
    
    
    
    /*
    // The next method is the main go-to for sending all forms back to client
    */
    
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
    
    
    
    /*
    // The next method deals with verifying a new account form by checking for uniqueness
    // The database work is done by the Database Worker
    // DBWorker returns an integer value described below
    // The integer is accessed, and the client is notified accordingly
    */
    
    private boolean verifyClientAccountInfo(NewAccountForm newAccount)
    {
        // Prevents users from creating multiple accounts (scam accounts, etc.)
        
        boolean notified = false;
       
        int verified = dbWorker.existCheck(clientIp, newAccount.getUsername(), newAccount.getIgn());
        // if returns 0, success
        // if returns 1, client ip is taken
        // if returns 2, username is taken
        // if returns 3, ign is taken
        // if returns 4, failed to process
            
        if(!notified && (verified == 1))
        {
            // User has more than 1 account associated with their IP address
            // TODO: SET FLAG FOR POTENTIAL FRAUD ACCOUNT
            communicator.sendAlert("Failed to verify user info. Please contact: tarkovtrader@gmail.com");
            System.out.println("Request: Client " + clientIp + " attempted to register multiple accounts.");
            notified = true;
            return false;
        }
            
        if(!notified && (verified == 2))
        {
            communicator.sendAlert("Username is already in use. Enter a different username.");
            System.out.println("Request: Client " + clientIp + " attempted to register duplicate username.");
            notified = true;
            return false;
        }
            
        if(!notified && (verified == 3))
        {
            communicator.sendAlert("An account is already associated with this in-game name. Contact tarkovtrader@gmail.com if you do not already have an account.");
            System.out.println("Request: Client " + clientIp + " attempted to register an account containing a duplicate IGN.");
            notified = true;
            return false;
        }
        
        if(!notified && (verified == 4))
        {
            communicator.sendAlert("Request: Failed to register new account for " + clientIp + ". A field could not be verified");
            notified = true;
            return false;
        }
            
        if (verified == 0)
            return true;
        
        return false;
    }
    
    
    
    
    /*
    // Authentication section
    // Gets users hashed password, and verifies with received hashed password
    // Verifies login credentials
    // Verifies requests compared to authenticated users on the static map in the main server class
    */
    
    private boolean authenticateLogin(LoginForm login)
    {
       if (dbWorker.loginAuthenticated(login))
       {
            HashMap<String,String> clientInfo = dbWorker.getAuthenticatedClientInfo(login);
            File userImageFile = dbWorker.getUserImageFile(login.getUsername());
            
            login.setAuthenticationState(true);
            login.setIgn(clientInfo.get("ign"));
            login.setTimezone(clientInfo.get("timezone"));
            login.setUserImageFile(userImageFile);
                    
            if (sendForm(login)) 
            {
                TarkovTraderServer.authenticatedUsers.put(login.getUsername(), this);
                this.clientUsername = login.getUsername();
                System.out.println("Request: Successful user authentication for: " + clientIp);
                
                // Pull client chats from the DB
                
                
                return true;
            }
        }
        else
        {
            login.setAuthenticationState(false);
            
            if (sendForm(login))
            {
                System.out.println("Request: Failed user authentication for: " + clientIp);
                communicator.sendAlert("Failed account authentication. Check credentials.");
                return false;
            }
        }
       
        return false;
    }
    
    
    // The next method is used to verify that a user is already placed in the static authenticated users map in the main server class
    // Requests should not be processed if the user is not in the map
    
    private boolean authenticateRequest()
    {
        if (this.clientUsername != null)
        {
            // Should already be verified if the worker knows the username, but just double check the static authenticated users map to match username and IP
            if (TarkovTraderServer.authenticatedUsers.containsKey(clientUsername))
            {
                return (TarkovTraderServer.authenticatedUsers.get(clientUsername).clientIp.equals(clientIp));
            }
            else
                return false;
        }
        else
            return false;
    }
    
    // END authentication section
    
    
    
    /*
    // The next method processes a new item submission by extracting the Item object from the generic NewItemForm
    // And uses the database worker to insert into the 'items' table
    */
    
    private void processNewItemRequest(ItemForm newitemform)
    {
        Item newItem = newitemform.getItem();
        
        if(dbWorker.insertNewItem(newItem))
        {
            communicator.sendAlert("New item successfully created.");
            System.out.println("New item successfully added for " + clientIp + ".");
        }
    }
   
    
    
    /*
    // Chat handler methods
    */
    
    private void processNewChat(Chat chat)
    {
        String destination = chat.getDestination();
        // Check if the user is online
        if (TarkovTraderServer.authenticatedUsers.containsKey(destination))
        {
            // User is online, get the client's worker to forward them the new chat
            RequestWorker destinationWorker = TarkovTraderServer.authenticatedUsers.get(destination);
            if (destinationWorker.sendForm(chat))
            {
                System.out.println("Request: New chat processed from " + chat.getOrigin() + " to " + destination + ".");
            }
            else
                System.out.println("Request: Failed to process new chat from " + chat.getOrigin() + " to " + destination + ".");
            
            destinationWorker = null;
        }
    }
    
}