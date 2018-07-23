
package tarkov.trader.client;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import javafx.application.Platform;
import tarkov.trader.objects.Chat;
import tarkov.trader.objects.ChatListForm;
import tarkov.trader.objects.LoginForm;
import tarkov.trader.objects.Form;
import tarkov.trader.objects.Item;
import tarkov.trader.objects.ItemListForm;
import tarkov.trader.objects.ProcessedItem;


public class RequestWorker implements Runnable
{
    private TarkovTrader trader;
    private Browser browser;
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
            Platform.runLater(() -> Alert.display("Request Worker", "Failed to decipher received processed request."));
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
            connection = new Socket("70.93.96.32", 6550);
            serverComm = new Socket("70.93.96.32", 6550);
            
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
                LoginPrompt.acknowledged = true;
                break;
                
            case "itemlist":
                ItemListForm itemlistform = (ItemListForm)processedRequest;
                // Browser only really needs the arraylist of items to populate
                // Convert the 'Item's to 'ProcessedItem's and get an arraylist of ProcessedItems to send to browser
                
                trader.getBrowser().populate(getProcessedItemList(itemlistform));
                break;
                
            case "chat":
                // New chat received from server, check if messenger is active else just let messenger pull from server upon opening and display a notification
                Chat newchat = (Chat)processedRequest;
                
                if (Messenger.isOpen)
                {
                    trader.getMessenger().chatListView.getItems().add(newchat);
                    Platform.runLater(() -> Alert.display(null, "New chat from: " + newchat.getOrigin()));
                }
                else
                    Platform.runLater(() -> Alert.display(null, "New chat from: " + newchat.getOrigin()));
                
                break;
                
            case "chatlist":
                // Client requested a chat list, results were returned from the server, and now we need to populate the messenger list
                ChatListForm chatlistform = (ChatListForm)processedRequest;
                
                if (Messenger.isOpen)
                {
                    trader.getMessenger().populate(chatlistform.getChatList());
                }
                
                break;
                
            default:
                Platform.runLater(() -> Alert.display(null, "Received an unknown type of form."));
                break;
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
