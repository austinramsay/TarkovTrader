
package tarkov.trader.server;

import tarkov.trader.objects.NewAccountForm;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import tarkov.trader.objects.AccountFlag;
import tarkov.trader.objects.Chat;
import tarkov.trader.objects.ChatDelete;
import tarkov.trader.objects.ChatListForm;
import tarkov.trader.objects.LoginForm;
import tarkov.trader.objects.Form;
import tarkov.trader.objects.Item;
import tarkov.trader.objects.ItemAction;
import tarkov.trader.objects.ItemForm;
import tarkov.trader.objects.ItemListForm;
import tarkov.trader.objects.ItemLocation;
import tarkov.trader.objects.ItemModificationRequest;
import tarkov.trader.objects.ItemStatus;
import tarkov.trader.objects.ItemStatusModRequest;
import tarkov.trader.objects.ListLocation;
import tarkov.trader.objects.Message;
import tarkov.trader.objects.Notification;
import tarkov.trader.objects.NotificationType;
import tarkov.trader.objects.Profile;
import tarkov.trader.objects.ProfileRequest;
import tarkov.trader.objects.Report;
import tarkov.trader.objects.Sale;
import tarkov.trader.objects.SaleStatus;
import tarkov.trader.objects.SaleType;


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
    
    private HashMap<String, String> clientInfo;               // Information pulled upon successful login
    private HashMap<String, Chat> chatmap;                    // This will be pulled from DB upon authenticated login and when it needs to sync upon receiving new chat
    private HashMap<String, ArrayList<String>> messageCache;  // The cache will be pushed to database upon disconnection   (Username, Message)
    
    private Profile currentProfile;
    
    private final String PRICE_MIN = "0";
    private final String PRICE_MAX = "50000000";
    
    
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
        
        try {
            openStreams(); 
        } catch (IOException e) { TarkovTraderServer.broadcast("Request Worker: Failed to open streams for new client connection " + clientIp + "."); return; }
        
        while (true) 
        {
            try
            {
                waitForRequests();
            }
            catch (IOException e)
            {
                disconnect();
                break;
            }
            catch (ClassNotFoundException e)
            {
                TarkovTraderServer.broadcast("Request Worker: Failed to create form. " + e.getMessage());
                communicator.sendAlert("Request Worker: Failed to create form.");
            }
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
                {
                    Profile newProfile = buildProfile(newAccountInfo);
                    dbWorker.insertAccountInfo(newAccountInfo, newProfile, clientIp);
                    TarkovTraderServer.syncUserList();  // When a new account is added, the main server needs to be updated so an accurate username list is sent to new client logins
                }
                
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
                
                
            case "itemmodification":
                ItemModificationRequest itemModRequest = (ItemModificationRequest)form;
                
                if (authenticateRequest())
                {
                    processItemModRequest(itemModRequest);
                }
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
                
                if (authenticateRequest())
                {
                    syncChats();
                }
                else
                    sendAuthenticationFailure();
                
                break;
                
                
            case "message":
                Message messageform = (Message)form;
                
                if (authenticateRequest())
                {
                    processMessage(messageform, true);                          // Process this message, AND cache it (true)
                }
                else
                    sendAuthenticationFailure();
                
                break;
                
                
            case "chatdelete":
                ChatDelete chatdeleterequest = (ChatDelete)form;
                
                if (authenticateRequest())
                {
                    processChatDelete(chatdeleterequest.getUsernameToRemove());
                }
                else 
                    sendAuthenticationFailure();
                
                break;
                
                
            case "profile":
                ProfileRequest profileRequest = (ProfileRequest)form;
                
                if (authenticateRequest())
                {
                    processProfileRequest(profileRequest);
                }
                else
                    sendAuthenticationFailure();
                
                break;
                
                
            case "itemstatusmod":
                ItemStatusModRequest statusModRequest = (ItemStatusModRequest)form;
                
                if (authenticateRequest())
                {
                    processStatusModRequest(statusModRequest);
                }
                else
                    sendAuthenticationFailure();
                
                break;
                
                
            case "report":
                Report report = (Report)form;
                
                if (authenticateRequest())
                {
                    processReport(report);
                }
                else
                    sendAuthenticationFailure();
                
                break;
                

            case "heartbeat":
                TarkovTraderServer.broadcast(" - " + clientUsername + " is alive.");
                
                break;
                
                
            default:
                TarkovTraderServer.broadcast("Received a request from " + clientIp + " but couldn't interpret the type.");
                break;
                
        }
    }
    
    
    private void sendAuthenticationFailure()
    {
        communicator.sendAlert("Failed to authenticate request.");
        TarkovTraderServer.broadcast("Request: Failed to authenticate request for " + clientIp + ".");
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
    // NEW ACCOUNTS:
    // Verifies new account information
    // Creates a new profile to be inserted by DBWorker
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
            TarkovTraderServer.broadcast("Request: Client " + clientIp + " attempted to register multiple accounts.");
            notified = true;
            return false;
        }
            
        if(!notified && (verified == 2))
        {
            communicator.sendAlert("Username is already in use. Enter a different username.");
            TarkovTraderServer.broadcast("Request: Client " + clientIp + " attempted to register duplicate username.");
            notified = true;
            return false;
        }
            
        if(!notified && (verified == 3))
        {
            communicator.sendAlert("An account is already associated with this in-game name. Contact tarkovtrader@gmail.com if you do not already have an account.");
            TarkovTraderServer.broadcast("Request: Client " + clientIp + " attempted to register an account containing a duplicate IGN.");
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
    
    
    private Profile buildProfile(NewAccountForm newAccountInfo)
    {
        // Build a profile with the unique username, IGN, and set their timezone
        Profile profile = new Profile(newAccountInfo.getUsername(), newAccountInfo.getIgn(), newAccountInfo.getTimezone());
        
        // The account is new, so we'll mark a flag to warn that the account is under 2 weeks old, and has not yet completed any sales/purchases
        profile.appendFlag(AccountFlag.NEW_ACCOUNT);
        profile.appendFlag(AccountFlag.NO_COMPLETED_PURCHASES);
        profile.appendFlag(AccountFlag.NO_COMPLETED_SALES);
        
        return profile;
    }
    
    
    
    
    /*
    // Authentication section
    // Gets users hashed password, and verifies with received hashed password
    // Verifies login credentials
    // Verifies requests compared to authenticated users on the static map in the main server class
    */
    
    private boolean authenticateLogin(LoginForm login)
    {
        // Multiple logins won't authenticate, check if the username already exists in the map
        if (TarkovTraderServer.authenticatedUsers.containsKey(login.getUsername()))
        {
            login.setAuthenticationState(false);
            
            if (sendForm(login))
            {
                TarkovTraderServer.broadcast("Request: Multiple logins for: " + login.getUsername() + ". Current assigned IP is: " + TarkovTraderServer.authenticatedUsers.get(login.getUsername()).clientIp + ". Attempted IP: " + clientIp);
                communicator.sendAlert("This username is already signed in.");
                return false;
            }
        }
        
        
        if (dbWorker.loginAuthenticated(login))
        {
            // Upon client authentication, send all necessary information needed by client features through the login form to be unpacked by client request worker
            
            clientUsername = login.getUsername();
            clientInfo = dbWorker.getAuthenticatedClientInfo(login);
            File userImageFile = dbWorker.getUserImageFile(login.getUsername());
            ArrayList<Notification> notifications = dbWorker.getNotifications(login.getUsername());
            ArrayList<String> userList = TarkovTraderServer.getUserList();
            ArrayList<String> onlineList = TarkovTraderServer.getOnlineList();
            
            login.setAuthenticationState(true);
            login.setIgn(clientInfo.get("ign"));
            login.setTimezone(clientInfo.get("timezone"));
            login.setUserImageFile(userImageFile);
            login.setNotificationsList(notifications);
            login.setUserList(userList);
            login.setOnlineList(onlineList);
            
            if (sendForm(login)) 
            {
                TarkovTraderServer.authenticatedUsers.put(login.getUsername(), this);
                TarkovTraderServer.broadcast("Request: Successful user authentication for: " + clientUsername + " at " + clientIp + ".");
                
                // Pull client chats from the DB
                chatmap = dbWorker.pullChatMap(login.getUsername());   // Will never need to initialize because this is stored upon account creation
                currentProfile = dbWorker.getProfile(clientUsername);
                
                if (currentProfile == null)
                {
                    // The client may have a different profile version than the latest server version, just create a new one and update it in the DB
                    resolveBrokenProfile();
                }
                
                messageCache = new HashMap<>();
                
                dbWorker.clearNotifications(login.getUsername());
                
                TarkovTraderServer.syncOnlineList();
                
                return true;
            }
        }
        else
        {
            login.setAuthenticationState(false);
            
            if (sendForm(login))
            {
                TarkovTraderServer.broadcast("Request: Failed user authentication for: " + clientIp);
                communicator.sendAlert("Check credentials. Credentials are case-sensitive.");
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
            if (isOnline(clientUsername))
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
    // NEW ITEM SUBMISSION HANDLING
    // Uses the database worker to insert into the 'items' table AND update the user's profile
    */
    
    private void processNewItemRequest(ItemForm newitemform)
    {
        Item newItem = newitemform.getItem();
        
        if(dbWorker.insertNewItem(newItem) && appendCurrentSale(newItem))   // Append current sale method in PROFILES section in this class
        {
            communicator.sendAlert("New item successfully created.");
            TarkovTraderServer.broadcast("New item successfully added for " + clientIp + ".");
        }
    }

    
    
    /*
    // CHAT HANDLER methods
    // For processing new chats, messages, and dealing with the message cache
    */
    
    private boolean processChat(Chat chat)
    {
        if (chat.isNew)
        {
            // Acknowledge the chat server-side
            chat.setOpened();
            
            
            // The new chat is from this worker's user, we need to forward to the other user if they are online. We will also need to put the chat into the user's DB row regardless if offline/online
            String destination = chat.getDestination();  // Username new chat is being sent to
            String origin = chat.getOrigin();
            
            // Insert chat into both users DB
            
            // First, update this user's chat map and sync
            chatmap.put(destination, chat);                              // Insert this new chat into the map
            dbWorker.insertChatMap(clientUsername, chatmap, false);      // Resinert to this users DB. Now we can sync
            syncChats();                                                 // Sync just for good measure               
                    
            // ------------------------------------------------------------------------------------------------------------
            
            // Now, update the receiving user's -- sync only if the user is online
            HashMap<String, Chat> syncchatmap = null;                    // We'll use this temp object to pull the destination users chat map from the database
            syncchatmap = dbWorker.pullChatMap(chat.getDestination());   // We pull the destination users chat map
            
            
            // The destination client may already have this chat, check and process accordingly
            if (syncchatmap.containsKey(clientUsername))
            {
                // This request worker's user must have deleted the chat while the destination still has it
                // We'll translate this chat into a message for the destination client instead
                convertChatToMessage(chat);
                return true;
                // End here, chat was processed as message for destination client
            }
            
            
            // Continue normally if the destination client does not have this chat yet
            syncchatmap.put(origin, chat);                            // Add this new chat
            dbWorker.insertChatMap(destination, syncchatmap, false);  // Reinsert to destination users DB. Can now sync if online or pull new info upon login if offline
            syncchatmap = null;
            
            
            
            // Check if the user is online
            if (isOnline(destination))                                                                     // If the user is online, they will be in the authenticated users map
            {
                // User is online, get the client's worker to forward them the new chat
                RequestWorker destinationWorker = TarkovTraderServer.authenticatedUsers.get(destination);  // Get the destination users RequestWorker
                destinationWorker.syncChats();                                                             // Force the destination users worker resync and force update to client
                
                communicator.sendAlert(chat.getDestination() + " is online. Notification sent.");          // Let THIS user know that the destination client is online and has received successfully
                destinationWorker = null;                                                                  // Close the destinationWorker
            }
            
            // Send notifications to destination's DB or through their worker depending if they are online/offline. This method will handle client state automatically
            sendNotification(NotificationType.CHAT, destination, clientUsername);
            
            TarkovTraderServer.broadcast("Request: New chat processed from " + chat.getOrigin() + " to " + destination + ".");
        }
        
        return true;
    }
    
    
    // This is necessary when a chat exists only on one end and a new Chat is attempted from the side that does not have it
    // The chat is converted to a message for the client still containing the Chat
    // At this point, the returned Message here is sent to processMessage 
    private boolean convertChatToMessage(Chat chat)
    {
        String origin = chat.getOrigin();
        String destination = chat.getDestination();
        ArrayList<String> chatMessages;
        
        if (chat.getMessages() != null)
            chatMessages = chat.getMessages();
        else
        {
            TarkovTraderServer.broadcast("Request: Failed to convert chat to message from " + origin + " to " + destination + ".");
            return false;
        }
        
        
        // Really should only be one message in the chat since it is new
        String message = chatMessages.get(0);
        
        Message convertedMessage = new Message(origin, destination, message);
        
        processMessage(convertedMessage, false);    // We want to process this as a normal message for the other client, and we DO NOT want to cache this message (it will cause a doubled message for this client)
        
        TarkovTraderServer.broadcast("Request: New chat processed for " + chat.getOrigin() + ".. converted to message for " + chat.getDestination() + ".");
        
        return true;
    }
    
    
    // Inbound message for an established chat. Could be sent by this user or a this could be the destination user
    public synchronized boolean processMessage(Message messageform, boolean shouldCache)
    {
        String origin = messageform.getOrigin();
        String destination = messageform.getDestination();
        String message = messageform.getMessage();
        
        
        if (destination.equals(clientUsername))              // Inbound message destination is this user, forward this message to this user's client - this means another users request worker found this client is online..and is using
        {
                                                             // Ensure this client does have this chat -- if not, create it and force sync before sending message
            if (!chatmap.containsKey(origin))
            {
                // The user does NOT have this chat -- convert this message to a chat
                convertMessageToChat(messageform);
                return true;
                // End here, the message was translated to a new chat for this client
            }
            
                                                             // The client DOES have this chat (now, if not already), continue to process the message normally 
            if (shouldCache)
                submitMessageToCache(origin, message);
            
                                                             // Forward to client
            sendForm(messageform);
            
                                                             // Handle new message notification
            sendNotification(NotificationType.MESSAGE, clientUsername, origin);       
        }
        else                                                 // The user has sent this message to someone else, forward to them
        {
            if (shouldCache)
                submitMessageToCache(destination, message);  // Don't want to submit to cache if the chat was converted to a message in case of deletion
            
            
                                                             // If the client is online, push the message using their worker, if not - let's just push to DB and leave a notification for their next login
            if (isOnline(destination))
            {
                                                             // No need to push to other users DB, their message cache will handle this itself
                RequestWorker destinationWorker = TarkovTraderServer.authenticatedUsers.get(destination);
                
                // Send the message to destination client
                destinationWorker.processMessage(messageform, true);
                
                // Close destination worker
                destinationWorker = null;
            }
            else
            {
                // Just push to the other users DB and set notification
                if (dbWorker.insertNewMessage(destination, clientUsername, message))     // If method returns true, the message was inserted into an existing chat send a 'Message' notification
                    sendNotification(NotificationType.MESSAGE, destination, clientUsername);
                else                                                                     // If method returns false, a new chat had to be created before insertion, create a 'Chat' notification
                    sendNotification(NotificationType.CHAT, destination, clientUsername);
            }
        }
        
        return true;
    }
    
    
    private void convertMessageToChat(Message messageToConvert)
    {
        String origin = messageToConvert.getOrigin();
        String destination = messageToConvert.getDestination();
        String message = messageToConvert.getMessage();
        
        // Build the new chat using given message information
        Chat newChat = new Chat(origin, destination, null);          // Create the non-existant chat
        newChat.setOpened();                                         // This has been recognized by the server, set it opened
        newChat.appendMessage(message);                              // Append this processed message to the chat
        chatmap.put(origin, newChat);                                // Insert this new chat into the map
        dbWorker.insertChatMap(clientUsername, chatmap, false);      // Resinert to this users DB. Now we can sync
        syncChats();                                                 // Update client with newly inserted chat
        sendNotification(NotificationType.CHAT, clientUsername, origin);                // Send notification to this client for the new chat            
    }
    
    
    private void processChatDelete(String username)
    {
        // ChatMap = <Destination username, Chat object>
        if (chatmap.containsKey(username))
        {
            chatmap.remove(username);                                    // Remove requested user's chat from the current map
            dbWorker.insertChatMap(clientUsername, chatmap, false);      // Resinert to this users DB. Now we can sync    
            
            // Cache may contain messages related to this chat, let's delete those messages before syncing
            // messageCache = <Username, ArrayList of String messages for respective user>
            if (messageCache.containsKey(username))
                messageCache.remove(username);
            
            // Proceed to sync chats, will force list with removed chat to the client
            syncChats();
            
            // Verbose logging
            communicator.sendAlert("Chat with " + username + " successfully deleted.");
            TarkovTraderServer.broadcast("Request: Chat '" + username + "' successfully deleted for '" + clientUsername + "'.");
        }
        else
        {
            // For extra protection, should never really occur 
            communicator.sendAlert("Chat could not be deleted.");
            TarkovTraderServer.broadcast("Request: Chat '" + username + "' failed to delete for '" + clientUsername + "'.");
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
            // Create a temp arraylist to put into the message cache which can then be added to for future messages
            
            ArrayList<String> tempMessageCache = new ArrayList<>();
            tempMessageCache.add(message);
            messageCache.put(username, tempMessageCache);
            tempMessageCache = null;
        }
   
    }


    // Pulls the most recent chat map stored in the database, and iterates through all the chats to add into an arraylist. Then pushes the list to the client. The client Messenger is automatically populated
    public void syncChats()
    {
        // Before we sync, clear the message cache so these messages will be included in their respective chat
        if (!messageCache.isEmpty())
        {
            syncCache();
        }
        
        // Pull the latest map (this could've just been updated when syncing cache)
        this.chatmap = dbWorker.pullChatMap(clientUsername);  
        
        // Build the form to prepare to send to client for processing
        ChatListForm chatlistform = new ChatListForm();   // This will be sent to the client
        ArrayList<Chat> chatlist = new ArrayList<>();     // this will be added to by iterating through our recently pulled ChatMap, and then set into the new ChatListForm to be sent
        
        for (Map.Entry<String, Chat> entry : chatmap.entrySet())   // Iterate current chat map
        {
            chatlist.add(entry.getValue());   // Each entry will have a corresponding Chat to be packed for the client
        }
        
        chatlistform.setChatList(chatlist);   // Set the ArrayList<Chat> in the new Form we just created 
        
        this.sendForm(chatlistform);          // Finally, send completed form to client
    }
    
    
    private boolean syncCache()
    {
        // Need to update the current instance's 'chatmap' with the message cache, and then sync with DB   --  Message Cache; Hashmap<String, ArrayList<String>>
        // Hashmap with key being the other parties name, and the arraylist of messages
        
        if (messageCache.isEmpty())
        {
            TarkovTraderServer.broadcast("Client " + clientIp + " message cache empty. Skipping sync.");
            return false;
        }
        
                
        TarkovTraderServer.broadcast("Syncing cached messages for " + clientIp + "...");
        int counter = 0;
        
        // Need to push this sessions messages into the corresponding chat in the chatmap
        for (Map.Entry<String, ArrayList<String>> entry : messageCache.entrySet())    // The key will be the other users name, the value will contain this sessions messages
        {
            String destination = entry.getKey();
            ArrayList<String> cachedMessages = entry.getValue();
 
            Chat syncChat = chatmap.get(destination);
            ArrayList<String> currentMessages = syncChat.getMessages();
            
            if (currentMessages == null)
                currentMessages = new ArrayList<>();
            
            // Iterate the cached messages and append to the respective chat
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
        
        dbWorker.insertChatMap(clientUsername, chatmap, false);
        
        messageCache.clear();
        
        TarkovTraderServer.broadcast("Sync complete... " + counter + " messages pushed.");
        
        return true;
    }
   
    
 
    
    /* 
    // NOTIFICATION SECTION
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
        if (sendNotificationTo.equals(clientUsername))
        {
            // Send our client this notification
            sendForm(newNotification);
        }
        else if (isOnline(sendNotificationTo))
        {
            // The intended user is NOT this user
            // User is online, we'll send a notification now through their worker
            RequestWorker destinationWorker = TarkovTraderServer.authenticatedUsers.get(sendNotificationTo);
            destinationWorker.sendForm(newNotification);             
        }
        else
        {
            // User is offline, push to the intended user's database
            ArrayList<Notification> destinationNotifications = dbWorker.getNotifications(sendNotificationTo);                                // Get the current destination user's notifications list
            
            ArrayList<Notification> destinationUpdatedNotifications = addNotification(destinationNotifications, type_flag, displayOnNotify);    // Append our new chat notification 
            
            dbWorker.insertNotifications(sendNotificationTo, destinationUpdatedNotifications);                                               // Update the list for the user's DB            
        }
    }
 
    
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
    
   
    
    /*
    // PROFILES
    */
    
    public void updateCurrentProfile(Profile updatedProfile)
    {
        this.currentProfile = updatedProfile;
    }
    
    
    private void processProfileRequest(ProfileRequest request)
    {
        Profile matchingProfile = dbWorker.getProfile(request.getUsername());
        
        if (matchingProfile == null)
            return;
        
        request.setProfile(matchingProfile);
        
        if (sendForm(request))
            TarkovTraderServer.broadcast("Request Worker: Profile request -- returned " + request.getUsername() + "'s profile to " + clientUsername + ".");
    }
    
    
    private void syncProfileWithModFlag()
    {
        // Mod flag - force client to look for an open moderator window.
        // If it is open, the client request worker will populate the table
        
        // Create a 'ProfileRequest' for the client to process upon receiving
        // Set the profile in this request to our 'currentProfile'
        ProfileRequest updatedProfile = new ProfileRequest(clientUsername);
        updatedProfile.setProfile(currentProfile);
        ArrayList<String> flags = new ArrayList<>();
        // Add a moderator flag (client's request worker will look for an open moderator window and populate it)
        flags.add("moderator");
        updatedProfile.setFlags(flags);
        // Send the new profile request
        sendForm(updatedProfile);        
    }
    
    private boolean appendCurrentSale(Item newItem)
    {
        currentProfile.appendItem(newItem);
        
        return dbWorker.updateProfile(currentProfile, clientUsername);
    }
    
    
    private boolean appendBuyItem(Item newItem)
    {
        currentProfile.appendBuyItem(newItem);
        
        return dbWorker.updateProfile(currentProfile, clientUsername);
    }
    
    
    private boolean removeBuyItem(Item item)
    {
        currentProfile.removeBuyItem(item);
        
        return dbWorker.updateProfile(currentProfile, clientUsername);
    }
    
    
    private void resolveBrokenProfile()
    {
        TarkovTraderServer.broadcast("Request Worker: Resolving profile for " + clientUsername + ".");
        
        // Build a profile with the unique username, IGN, and set their timezone
        Profile profile = new Profile(clientUsername, clientInfo.get("ign"), clientInfo.get("timezone"));
        
        // Append generic new account flags
        profile.appendFlag(AccountFlag.NEW_ACCOUNT);
        profile.appendFlag(AccountFlag.NO_COMPLETED_PURCHASES);
        profile.appendFlag(AccountFlag.NO_COMPLETED_SALES);
        
        // The user may have items for sale, we need to include these in the profile
        ArrayList<Item> itemList = dbWorker.getUserItems(clientUsername);
        
        // Set the list in the profile itself
        profile.setCurrentSalesList(itemList);
        
        // Send updated profile to database
        dbWorker.updateProfile(profile, clientUsername);
    }
    
    // END Profiles
    
    
    
    /*
    // Item Status Modifications / Buying - Selling
    */
    
    private void processStatusModRequest(ItemStatusModRequest request)
    {
        Item item = request.getItem();
        
        switch(item.getItemStatus())
        {
            case AWAITING_RESPONSE:
                communicator.sendAlert("The other user has not yet replied. Modification not permitted.");
                TarkovTraderServer.broadcast("Request Worker: Item modification not permitted (" + clientUsername + ")");
                return;
                
            case REPORT_PENDING:
                communicator.sendAlert("Report status pending. Modification not permitted.");
                TarkovTraderServer.broadcast("Request Worker: Item modification not permitted (" + clientUsername + ")");
                return;
        }
        
        
        
        // Check what the user is asking to change the status to is
        // If there is no requested status, then we are only performing an action on the item not modifying its status
        if (request.getStatus() == null)
        {
            // We are just performing an action on the item, not changing the status
            // Continue
        }
        /*else if (item.getItemStatus() != request.getStatus())   OLD DEPRECATED
        {
            // TODO: Bug?
            item.setItemStatus(request.getStatus());
        }
        else
        {
            TarkovTraderServer.broadcast("Request Worker: This item's status has already been changed.");
            communicator.sendAlert("This item's status has already been changed.");
            return;
        }*/
        else if (item.getItemStatus() == request.getStatus()) {
            TarkovTraderServer.broadcast("Request Worker: This item's status has already been changed.");
            communicator.sendAlert("This item's status has already been changed.");
            return;
        }
        
        
        
        // Check if the requested action is to move to the buy list
        // We will check if the current buy list already contains the item - and if it does, return an error message to the user and exit the method
        // If it can be added,
        // We will add the item to the user's profile BUY LIST
        // And then we will add the item to the other affected users REQUESTED SALES list
        // The end user's profile will be updated in the database, 
        if (request.getAction() == ItemAction.MOVE_TO_BUY_LIST)
        {
            // Deny request if usernames match - can't add user's own item to buy list
            if (item.getUsername().equals(clientUsername)) {
                TarkovTraderServer.broadcast("Request Worker: Can't add item to own buy list. Item owned by " + item.getUsername() + "..Requested by: " + clientUsername + ".");
                return;
            }

            // This client requested an add to buy list, the seller will need to know this username so we will set the requested user as 'this'
            item.setRequestedUser(clientUsername);
            
            if (currentProfile.getBuyList().contains(item))
            {
                TarkovTraderServer.broadcast("Request Worker: Failed to add item to buy list for " + clientUsername + ". Already exists.");
                communicator.sendAlert("Your buy list already contains this listing.");
                return;
            }
            
            if (appendBuyItem(item))   // Method returns true if it successfully updates current profile after adding to buy list
            {
                // Successfully added to our current profile
                
                // Add to requested sales of end user's profile
                Profile endUserProfile = dbWorker.getProfile(item.getUsername());
                endUserProfile.appendRequestedSale(item);
                dbWorker.updateProfile(endUserProfile, item.getUsername());
                sendNotification(NotificationType.NEW_SALE_REQUEST, item.getUsername(), clientUsername);
                
                TarkovTraderServer.broadcast("Request Worker: Added item to buy list for " + clientUsername + ".");
                communicator.sendAlert("Added item to buy list. '" + item.getUsername() + "' was notified.");           
                return;
            }
            
            return;
        }
        
        if (request.getAction() == ItemAction.MODIFY_STATUS)
        {
            // Status has been already modified at start of method
            // We just need to process the new status accordingly
            if (item.getItemStatus() == ItemStatus.CONFIRMED)
            {
                Sale newSale;
                // The seller has confirmed the sale
                newSale = new Sale(clientUsername, item.getRequestedUser(), item, SaleType.SOLD, SaleStatus.BUYER_CONFIRMED);
                currentProfile.appendSale(newSale);
                dbWorker.updateProfile(currentProfile, clientUsername);
                
                Profile endUserProfile = dbWorker.getProfile(item.getRequestedUser());
                newSale = null;
                newSale = new Sale(clientUsername, item.getRequestedUser(), item, SaleType.BOUGHT, SaleStatus.SELLER_CONFIRMED);
                endUserProfile.appendSale(newSale);
                dbWorker.updateProfile(endUserProfile, item.getRequestedUser());
                sendNotification(NotificationType.SALE_REQUEST_UPDATE, item.getRequestedUser(), clientUsername);
                
                communicator.sendAlert("Accepted sale. '" + item.getRequestedUser() + "' was notified.");
                TarkovTraderServer.broadcast("Request Worker: Seller '" + clientUsername + "' accepted sale with '" + item.getRequestedUser() + "'.");
            }
            else if (item.getItemStatus() == ItemStatus.DECLINED)
            {
                // The seller has denied the sale
                for (Item matchingItem : currentProfile.getRequestedSales())
                {
                    if (matchingItem.equals(item))
                    {
                        matchingItem.setItemStatus(ItemStatus.DECLINED);
                        break;
                    }
                }
                dbWorker.updateProfile(currentProfile, clientUsername);
                
                Profile endUserProfile = dbWorker.getProfile(item.getRequestedUser());
                for (Item matchingItem : endUserProfile.getBuyList())
                {
                    if (matchingItem.equals(item))
                    {
                        matchingItem.setItemStatus(ItemStatus.DECLINED);
                        break;
                    }
                }
                dbWorker.updateProfile(endUserProfile, item.getRequestedUser());
                sendNotification(NotificationType.SALE_REQUEST_UPDATE, item.getRequestedUser(), clientUsername);
                
                communicator.sendAlert("Declined sale. '" + item.getRequestedUser() + "' was notified.");
                TarkovTraderServer.broadcast("Request Worker: Seller '" + clientUsername + "' declined sale with '" + item.getRequestedUser() + "'.");
            }
            syncProfileWithModFlag();
            
            return;
        }
        
        if (request.getAction() == ItemAction.REMOVE_FROM_REQUESTS)
        {            
            if (currentProfile.getRequestedSales().contains(item))
            {
                currentProfile.removeRequestedSale(item);
                dbWorker.updateProfile(currentProfile, clientUsername);
                communicator.sendAlert("Request removed.");
                TarkovTraderServer.broadcast("Request Worker: Requested sale from '" + item.getRequestedUser() + "' removed for '" + clientUsername + "'.");
            }
            else
            {
                communicator.sendAlert("Failed to remove request.");
                TarkovTraderServer.broadcast("Request Worker: Failed to find requested sale to remove for " + clientUsername + ".");
            }
            syncProfileWithModFlag();
            
            return;
        }
        
        if (request.getAction() == ItemAction.REMOVE_FROM_BUY_LIST)
        {   
            if (currentProfile.getBuyList().contains(item))
            {
                currentProfile.removeBuyItem(item);
                dbWorker.updateProfile(currentProfile, clientUsername);
                communicator.sendAlert("Item removed.");
                TarkovTraderServer.broadcast("Request Worker: Buy item removed for " + clientUsername + ".");
            }
            else
            {
                communicator.sendAlert("Failed to remove item.");
                TarkovTraderServer.broadcast("Request Worker: Failed to remove buy item for " + clientUsername + ".");
            }
            syncProfileWithModFlag();
            
            return;
        }
    }
    
    
    
    private boolean updateProfileItemStatus(String username, Profile profile, ItemLocation itemLocation, ItemStatus newStatus, boolean sync)
    {
        // Create an item object to set as the matching item found in the profile
        // We will modify the reportedItem, and then replace the unmodified item in the list with our newly modified item
        Item matchedItem;
        if (itemLocation.getListLocation() == ListLocation.BUY_LIST)
        {
            // The item is located in the profiles buy list, modify it accordingly
            matchedItem = profile.getBuyList().get(itemLocation.getIndex());
            matchedItem.setItemStatus(newStatus);
            profile.getBuyList().remove(itemLocation.getIndex());
            profile.appendBuyItem(matchedItem);
            // The profile is now updated
        }
        else if (itemLocation.getListLocation() == ListLocation.SALE_REQUEST_LIST)
        {
            // The item is located in the profiles buy list, modify it accordingly
            matchedItem = profile.getRequestedSales().get(itemLocation.getIndex());
            matchedItem.setItemStatus(newStatus);
            profile.getRequestedSales().remove(itemLocation.getIndex());
            profile.appendRequestedSale(matchedItem);
            // The profile is now updated                
        }

        // Update profile in database
        dbWorker.updateProfile(profile, username);
        
        // Force client profile sync - if the method was called for this user specifically
        // If not, update profile in the database
        if (sync)
            syncProfileWithModFlag();

        return true;
    }
        
    // End Item Status Modifications
    
    
    
    /*
    // ITEM MODIFICATION section
    */
    
    private void processItemModRequest(ItemModificationRequest itemModRequest)
    {
        // Handle the item modification
        dbWorker.processItemModification(itemModRequest, clientUsername);
        
        // Force moderator sync by sending updated profile with a 'moderator' flag
        ProfileRequest modProfile = new ProfileRequest(clientUsername);
        modProfile.setProfile(currentProfile);
        ArrayList<String> flags = new ArrayList<>();
        flags.add("moderator");
        modProfile.setFlags(flags);
        sendForm(modProfile);
    }
    
    // End Item Modifications
    
    
    
    /*
    // Reports 
    */
    
    private void processReport(Report report)
    {
        // Client submitted a report
        // We need to update the item status for this client and the affected destination client (the client connected to the report)
        
        TarkovTraderServer.broadcast("Request Worker: Processing report. User " + report.getUserToReport() + " reported by " + report.getByUsername() + ".");
        
        // Check the current status of the submitted item, if it is Report Pending, decline the report
        if (report.getReportedItem().getItemStatus() == ItemStatus.REPORT_PENDING)
        {
            communicator.sendAlert("A pending report already exists for this item.");
            TarkovTraderServer.broadcast("Request Worker: Duplicate report declined. Sent by '" + clientUsername + "' against '" + report.getUserToReport() + "'.");       
            return;
        }
        
        
        // Check if there is already a report pending for this item matching this username
        for (Report existingReport : TarkovTraderServer.reportLog.getReports())
        {
            if (existingReport.getByUsername().equals(clientUsername))
            {
                if (existingReport.getReportedItem().equals(report.getReportedItem()))
                {
                    communicator.sendAlert("A pending report already exists for this item.");
                    TarkovTraderServer.broadcast("Request Worker: Duplicate report declined. Sent by '" + clientUsername + "' against '" + report.getUserToReport() + "'.");
                    return;
                }
            }
        }
        
        // Verify the report was successfully added to our report log server side
        if (!TarkovTraderServer.reportLog.appendReport(report))
        {
            communicator.sendAlert("Failed to accept report. Report log access failure. Message 'tarkovtrader' through messenger.");
            TarkovTraderServer.broadcast("Request Worker: Failed to append report to report log.");
            return;
        }
        
        // The report log was successfully appended to, continue
        
        
        // Get the reported user's profile
        // We don't need to get our own profile because we can just use 'currentProfile' for this user
        Profile reportedUserProfile = dbWorker.getProfile(report.getUserToReport());
        if (reportedUserProfile == null)
        {
            communicator.sendAlert("Failed to accept report. Profile error. Message 'tarkovtrader' through messenger.");
            TarkovTraderServer.broadcast("Request Worker: Failed to process report. Reported user profile null.");
            return;
        }
            
        
        // Get the item locations for each user
        ItemLocation currentUserItemLocation = currentProfile.getItemLocation(report.getReportedItem());
        ItemLocation reportedUserItemLocation = reportedUserProfile.getItemLocation(report.getReportedItem());
        
        // Verify that the item exists in both profiles before attempting to update the item status
        if (currentUserItemLocation == null)
        {
            TarkovTraderServer.broadcast("Request Worker: Failed to locate item in profile (" + clientUsername + ").");
            return;
        }        
        else if (reportedUserItemLocation == null)
        {
            TarkovTraderServer.broadcast("Request Worker: Failed to locate item in profile (" + report.getUserToReport() + ").");
            return;
        }        
        
        
        // At this point we have ready profiles with proof the item exists in both profiles
        updateProfileItemStatus(clientUsername, currentProfile, currentUserItemLocation, ItemStatus.REPORT_PENDING, true);
        updateProfileItemStatus(report.getUserToReport(), reportedUserProfile, reportedUserItemLocation, ItemStatus.REPORT_PENDING, false);
        
        
        // We aren't going to force a sync for the destination worker because we don't know exactly what is happening on their end
        // We'll check if they are online, if so, we can send them an announcement to let them know it was reported
        sendNotification(NotificationType.REPORT, report.getUserToReport(), clientUsername);
        
        
        // Complete
        communicator.sendAlert("Report accepted. Pending approval.");
        TarkovTraderServer.broadcast("*****New report request from " + clientUsername + ".*****");
    }
    
    // End Reports
    
    
    
    /*
    // Client Connection Handling Section
    */
    
    private boolean isOnline(String username)
    {
        if (TarkovTraderServer.authenticatedUsers.containsKey(username))
            return true;
        else
            return false;
    }
    

    public void disconnect()
    {
        TarkovTraderServer.broadcast("Client " + clientIp + " has disconnected.");
            
        if (TarkovTraderServer.authenticatedUsers.containsKey(clientUsername))
        {
            syncCache();
            TarkovTraderServer.authenticatedUsers.remove(clientUsername);
            TarkovTraderServer.syncOnlineList();  // User disconnected, push latest online user list to all available clients
        }
        // TODO: HANDLE A CLOSED CONNECTION    
    }    
}


