
package tarkov.trader.server;

import tarkov.trader.objects.NewAccountForm;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import tarkov.trader.objects.Chat;
import tarkov.trader.objects.ChatListForm;
import tarkov.trader.objects.LoginForm;
import tarkov.trader.objects.Form;
import tarkov.trader.objects.Item;
import tarkov.trader.objects.ItemForm;
import tarkov.trader.objects.ItemListForm;
import tarkov.trader.objects.Message;


public class RequestWorker implements Runnable {
    
    private DatabaseWorker dbWorker;
    public ClientCommunicator communicator;
    
    private Socket client;
    private Socket clientComm;
    private String clientIp;
    public String clientUsername;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private Form form;
    
    private HashMap<String, Chat> chatmap;  // This will be pulled from DB upon authenticated login and when it needs to sync upon receiving new chat
    private HashMap<String, ArrayList<String>> messageCache;  // The cache will be pushed to database upon disconnection   (Username, Message)
    
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
            
            if (TarkovTraderServer.authenticatedUsers.containsKey(clientUsername))
            {
                syncCache();
                TarkovTraderServer.authenticatedUsers.remove(clientUsername);
            }
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
                    processChat(chat);
                }
                else
                    sendAuthenticationFailure();
                
                break;
                
                
            case "chatlist":
                syncChats();
                
                break;
                
                
            case "message":
                Message messageform = (Message)form;
                
                if (authenticateRequest())
                {
                    processMessage(messageform);
                }
                else
                    sendAuthenticationFailure();
                
                break;
                
                
            case "heartbeat":
                System.out.println(" - " + clientUsername + " is alive.");
                
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
                chatmap = dbWorker.pullChatMap(login.getUsername());   // Will never need to initialize because this is stored upon account creation
                messageCache = new HashMap<>();
                
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
    // For processing new chats, messages, and dealing with the message cache
    */
    
    private void processChat(Chat chat)
    {
        if (chat.isNew)
        {
            chat.setOpened();
            
            // The new chat is from this worker's user, we need to forward to the other user if they are online. We will also need to put the chat into the user's DB row regardless if offline/online
            String destination = chat.getDestination();  // Username new chat is being sent to
            HashMap<String, Chat> syncchatmap;           // We'll use this temp object to pull this users as well as the destination users chat map in the database
            
            // Insert chat into both users DB
            // First, update this user's chat map and sync
            syncchatmap = dbWorker.pullChatMap(clientUsername);          // Pull most recent chat map for this user (should really be the same as the chatmap object in this class anyway, if not, something happened
            syncchatmap.put(chat.getDestination(), chat);                // Insert this new chat into the map
            dbWorker.insertChatMap(clientUsername, syncchatmap);         // Resinert to this users DB. Now we can sync
            syncChats();                                                 // Sync just for good measure
            syncchatmap = null;                                          // Close this map
                    
            // Now, update the receiving user's -- sync only if the user is online
            syncchatmap = dbWorker.pullChatMap(chat.getDestination());   // We pull the destination users chat map
            syncchatmap.put(chat.getOrigin(), chat);                     // Add this new chat
            dbWorker.insertChatMap(chat.getDestination(), syncchatmap);  // Reinsert to destination users DB. Can now sync if online or pull new info upon login if offline
            syncchatmap = null;
            
            // Check if the user is online
            if (isOnline(destination))                            // If the user is online, they will be in the authenticated users map
            {
                // User is online, get the client's worker to forward them the new chat
                RequestWorker destinationWorker = TarkovTraderServer.authenticatedUsers.get(destination);  // Get the destination users RequestWorker
                destinationWorker.syncChats();                                                             // Make the destination users worker resync and push update to client
                destinationWorker.communicator.sendAlert("New message from: " + chat.getOrigin());         // Send notification to destination user that a new chat is received
                communicator.sendAlert(chat.getDestination() + " is online. Notification sent.");          // Let THIS user know that the destination client is online and has received successfully
                destinationWorker = null;                                                                  // Close the destinationWorker
            }
            
            System.out.println("Request: New chat processed from " + chat.getOrigin() + " to " + destination + ".");
        }
    }
    
    
    // Pulls the most recent chat map stored in the database, and iterates through all the chats to add into an arraylist. Then pushes the most recent list to the client
    public void syncChats()
    {
        
        if (!messageCache.isEmpty())
        {
            syncCache();
        }
        
        this.chatmap = dbWorker.pullChatMap(clientUsername);   // BUG: Pulling stored DB chatmap when there are cached messages that havent synced yet
        ChatListForm chatlistform = new ChatListForm();
        ArrayList<Chat> chatlist = new ArrayList<>();
        
        for (Map.Entry<String, Chat> entry : chatmap.entrySet())
        {
            chatlist.add(entry.getValue());
        }
        
        chatlistform.setChatList(chatlist);
        
        this.sendForm(chatlistform);
    }
    
    
    // Inbound message for an established chat. Could be sent by this user or a this could be the destination user
    public synchronized void processMessage(Message messageform)
    {
        String origin = messageform.getOrigin();
        String destination = messageform.getDestination();
        String message = messageform.getMessage();
        
        
        if (destination.equals(clientUsername))   // If the inbound message destination is this user, forward this message to this user's client - this means another users request worker found this client is online..and is using
        {
            submitMessageToCache(origin, message);
            sendForm(messageform);
        }
        else   // The user has sent this message to someone else, forward to them
        {
            submitMessageToCache(destination, message);
            if (isOnline(destination))
            {
                // No need to push to other users DB, their message cache will handle this itself
                RequestWorker destinationWorker = TarkovTraderServer.authenticatedUsers.get(destination);
                destinationWorker.processMessage(messageform);
                destinationWorker = null;
            }
            else
            {
                // Just push to the other users DB
                dbWorker.insertNewMessage(destination, clientUsername, message);
            }
        }
    }
    
    
    private void submitMessageToCache(String username, String message)
    {
        // If the origin's username already exists in the current message cache, just add to the String arraylist
        
        if (messageCache.containsKey(username))
            messageCache.get(username).add(message);
        
        else
        {
            // The user hasn't dealt with this username in the message cache for this session.
            // Create a temp arraylist to put into the message cache which can then be added to for future messages later
            
            ArrayList<String> tempMessageCache = new ArrayList<>();
            tempMessageCache.add(message);
            messageCache.put(username, tempMessageCache);
            tempMessageCache = null;
        }
    }
    
    
    private boolean syncCache()
    {
        // Need to update the current instance's 'chatmap' with the message cache, and then sync with DB   --  Message Cache; Hashmap<String, ArrayList<String>>
        // Hashmap with key being the other parties name, and the arraylist of messages
        
        if (messageCache.isEmpty())
            return false;
        
                
        System.out.println("Syncing cached messages for " + clientIp + "...");
        int counter = 0;
        
        // Need to push this sessions messages into the corresponding chat in the chatmap
        for (Map.Entry<String, ArrayList<String>> entry : messageCache.entrySet())    // The key will be the other users name, the value will contain this sessions messages
        {
            String destination = entry.getKey();
            ArrayList<String> cachedMessages = entry.getValue();
 
            Chat syncChat = chatmap.get(destination);
            ArrayList<String> currentMessages = syncChat.getMessages();
            
            for (String cached : cachedMessages)
            {
                counter++;
                currentMessages.add(cached);
            }
            
            // Current messages done syncing
            // Reinsert messages into the chat
            syncChat.setMessages(currentMessages);
            chatmap.put(destination, syncChat);
        }
        
        dbWorker.insertChatMap(clientUsername, chatmap);
        
        messageCache.clear();
        
        System.out.println("Sync complete... " + counter + " messages pushed.");
        
        return true;
    }
    
    
    private boolean isOnline(String username)
    {
        if (TarkovTraderServer.authenticatedUsers.containsKey(username))
            return true;
        else
            return false;
    }
    
}