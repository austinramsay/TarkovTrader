
package tarkov.trader.client;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javafx.application.Platform;
import tarkov.trader.objects.Chat;
import tarkov.trader.objects.ChatListForm;
import tarkov.trader.objects.LoginForm;
import tarkov.trader.objects.Form;
import tarkov.trader.objects.HeartbeatForm;
import tarkov.trader.objects.Item;
import tarkov.trader.objects.ItemListForm;
import tarkov.trader.objects.Message;
import tarkov.trader.objects.Notification;
import tarkov.trader.objects.ProcessedItem;
import tarkov.trader.objects.Profile;
import tarkov.trader.objects.ProfileRequest;
import tarkov.trader.objects.SyncForm;


public class RequestWorker implements Runnable
{
    private TarkovTrader trader;
    private Form processedRequest;
    
    private Socket connection;
    public Socket serverComm;
    private ObjectOutputStream objOutput;
    private ObjectInputStream objInput;
    private Thread commThread;
    
    
    public RequestWorker(TarkovTrader trader)
    {
        this.trader = trader;
    }
    
    
    @Override
    public void run()
    {
        sendInitialConnectionRequest();
        
        try    // SHOULD MOVE THIS TO A NEW METHOD. IN CATCH SHOULD ADD AN OPTION TO RECALL FOR A RECONNECT?
        {
            while (true)
            {
                if (connection != null)
                    receiveForms();
            }
        }
        catch (ClassNotFoundException e)
        {
            Platform.runLater(() -> Alert.display("Request Worker", "Failed to decipher received request."));
            // TODO: NEED REQUEST WORKER TO REESTABLISH
        }
        catch (IOException e)
        {
            Platform.runLater(() -> Alert.display("Request Worker", "Failed to communicate with server."));
            // TODO: NEED REQUEST WORKER TO REESTABLISH
        }
    }
    
    
    public void sendInitialConnectionRequest()
    {
        try 
        {
            // Italia Server
            connection = new Socket("70.93.96.32", 6550);
            serverComm = new Socket("70.93.96.32", 6550);
            
            // Localhost
            //connection = new Socket("127.0.0.1", 6550);
            //serverComm = new Socket("127.0.0.1", 6550);
            
            if ((connection != null) && (serverComm != null))
            {
                openStreams();
                startCommunicator();
                TarkovTrader.connected = true;
            }

        }
        catch (UnknownHostException e)
        {
            Platform.runLater(() -> Alert.display("Network", "Host couldn't be located."));
        }
        catch (IOException e)
        {
            Platform.runLater(() -> Alert.display("Network", "Connection to server failed."));
        }
    }
    
    
    private void openStreams() throws IOException
    {
        objOutput = new ObjectOutputStream(connection.getOutputStream());
        objInput = new ObjectInputStream(connection.getInputStream());
    }
    
    
    private void receiveForms() throws IOException, ClassNotFoundException
    {
        while ((processedRequest = (Form)objInput.readObject()) != null)
        {
            unpack(processedRequest);
        }
    }
    
    
    private void startCommunicator()
    {
        if (serverComm != null)
        {
            ServerCommunicator communicator = new ServerCommunicator(serverComm);
            commThread = new Thread(communicator);
            commThread.start();
        }
    }
    
    
    public boolean sendForm(Form unprocessedRequest)
    {
        try
        {
            objOutput.writeObject(unprocessedRequest);
            return true;
        }
        catch (IOException e)
        {
            Platform.runLater(() -> Alert.display("Request Worker", "Failed to send request to server."));
            e.printStackTrace();
            // TODO: DISCONNECTING HERE
            return false;
        }
    }
    
    
    private void unpack(Form processedRequest)
    {
        String type = processedRequest.getType();
        
        switch (type)
        {
            case "login":
                LoginForm unpackedLogin = (LoginForm)processedRequest;
                TarkovTrader.authenticated = unpackedLogin.isAuthenticated();
                TarkovTrader.username = unpackedLogin.getUsername();
                TarkovTrader.ign = unpackedLogin.getIgn();
                TarkovTrader.timezone = unpackedLogin.getTimezone();
                TarkovTrader.userImageFile = unpackedLogin.getUserImageFile();
                
                TarkovTrader.userList = unpackedLogin.getUserList();
                TarkovTrader.onlineList = unpackedLogin.getOnlineList();
                TarkovTrader.notificationsList = unpackedLogin.getNotificationsList();
                LoginPrompt.acknowledged = true;
                
                break;
                
                
            case "itemlist":
                ItemListForm itemlistform = (ItemListForm)processedRequest;
                // Browser only really needs the arraylist of items to populate
                // Convert the 'Item's to 'ProcessedItem's and get an arraylist of ProcessedItems to send to browser
                
                Platform.runLater(() -> trader.getBrowser().populate(getProcessedItemList(itemlistform)));
                
                break;
                
                
            case "chatlist":
                // Client requested a chat list, results were returned from the server, and now we need to populate the messenger list
                ChatListForm chatlistform = (ChatListForm)processedRequest;
                populateMessenger(chatlistform);
                
                break;
                
                
            case "message":
                Message messageform = (Message)processedRequest;
                processMessage(messageform);
                
                break; 
                
                
            case "sync":
                SyncForm syncinfo = (SyncForm)processedRequest;
                processSync(syncinfo);
                
                break;
                
                
            case "profile":
                ProfileRequest profileRequest = (ProfileRequest)processedRequest;
                processProfile(profileRequest);
                
                break;
                
                
            case "notification":
                Notification notification = (Notification)processedRequest;
                
                TarkovTrader.notificationsList.add(notification);
                
                trader.getNotificationManager().processNotificationsList();
                
                break;
                
                
            case "heartbeat":
                // Send back a heartbeat
                HeartbeatForm heartbeat = (HeartbeatForm)processedRequest;
                sendForm(heartbeat);
                
                break;
                
                
            default:
                Platform.runLater(() -> Alert.display(null, "Received an unknown type of form: " + processedRequest.getType() + "..."));
                break;
        }
        
        if (trader.getLoadingPrompt().isRunning())
        {
            Platform.runLater(() -> { trader.getLoadingPrompt().close(); } );
        }
    }
    
    
    private ArrayList<ProcessedItem> getProcessedItemList(ItemListForm itemlistform)
    {
        // Get the list of items from the form to begin
        ArrayList<Item> itemList;
        itemList = itemlistform.getItemList();
        
        ArrayList<ProcessedItem> processedItemList = new ArrayList<>();
        
        for (Item item : itemList)
        {
            processedItemList.add(new ProcessedItem(item));
        }
        
        return processedItemList;
    }
    
    
    private void populateMessenger(ChatListForm chatlistform)
    {
        if (Messenger.isOpen)
        {   
            FutureTask<Void> updateChatList = new FutureTask(() -> {
                // Get the open Messenger
                Messenger tempMessenger = trader.getMessenger();
                   
                // If there is a chat selected, get the selected name of the chat to reselect after repopulating
                String currentChatName = null;
            
                if (tempMessenger.chatListView.getSelectionModel().getSelectedItem() != null)
                    currentChatName = tempMessenger.chatListView.getSelectionModel().getSelectedItem().getName(TarkovTrader.username);
                       
                // Set the chat list for the messenger
                tempMessenger.populate(chatlistform.getChatList());
                        
                // If we got a name to select, find in the list and select again
                if (currentChatName != null)
                {
                    for (Chat openChat : tempMessenger.chatListView.getItems())
                    {
                        if (openChat.getName(TarkovTrader.username).equals(currentChatName))
                        {
                            tempMessenger.chatListView.getSelectionModel().select(openChat);
                            break;
                        }
                    }
                }
            }, null);
                    
            // Run the task on the JavaFX application thread
            Platform.runLater(updateChatList);  // RequestWorker needs access to the JavaFX application thread
                   
            // Wait for task to complete, handle possible exceptions, and set syncInProgess to false
            try { 
                updateChatList.get();           // Wait until the ListView has been populated before setting 'syncInProgress' to false again
            }
            catch (InterruptedException | ExecutionException e) { 
                Platform.runLater(() -> Alert.display(null, "Sync failed."));
                e.printStackTrace();
            }
            finally {
                // Sync complete
                TarkovTrader.syncInProgress.compareAndSet(true, false);
            }
        }        
        else
        {
            // Sync complete
            TarkovTrader.syncInProgress.compareAndSet(true, false);
        }                  
    }
    
    
    private void processMessage(Message messageform)
    {
        if (Messenger.isOpen)
        {
            trader.getMessenger().processMessage(messageform);
        }
    }
    
    
    private void processSync(SyncForm syncinfo)
    {
        // Determine flags set
        ArrayList<String> flags = syncinfo.getFlags();
        
        String flag = flags.get(0);
        
        if (flag.equals("onlineuserlist"))
        {
            TarkovTrader.onlineList = syncinfo.getOnlineUserList();
        }
        
        if (flag.equals("fulluserlist"))
        {
            TarkovTrader.userList = syncinfo.getFullUserList();
        }
    }
    
    
    private void processProfile(ProfileRequest profileRequest)
    {   
        final Profile matchingProfile = profileRequest.getReturnedProfile();
        
        final boolean allowEdit = matchingProfile.getUsername().equals(TarkovTrader.username);   // Add fields to edit profile if this matches user's username
        
        String flag = null;
        
        if (profileRequest.flags != null && !profileRequest.flags.isEmpty())
            flag = profileRequest.flags.get(0);
        
        if (flag != null && flag.equals("moderator"))
            Platform.runLater(() -> trader.getModerator().populate(matchingProfile));
        else
            Platform.runLater(() -> trader.displayProfile(matchingProfile, allowEdit));
    }
    
    
    public void closeNetwork()
    {
        try 
        {
            if (connection != null)
                connection.close();
            if (serverComm != null)
                serverComm.close();
            if (commThread != null)
                commThread.stop();
        }
        catch (IOException e)
        {
            Platform.runLater(() -> Alert.display("Network", "Failed to close sockets."));
        }
    }
}