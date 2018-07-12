
package tarkov.trader.client;

import java.net.*;
import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.application.Platform;
import tarkov.trader.objects.LoginForm;


public class RequestWorker implements Runnable
{
    
    private Socket connection;
    public Socket serverComm;
    private ObjectOutputStream objOutput;
    private ObjectInputStream objInput;
    private Thread commThread;
    
    private LinkedHashMap<String, Object> form;
    
    
    @Override
    public void run()
    {
        sendInitialConnectionRequest();
        
        try 
        {
            while (true)
            {
                if (connection != null)
                    receiveForms();
            }
        }
        catch (ClassNotFoundException e)
        {
            Platform.runLater(() -> Alert.display("Request Worker", "Failed to decipher received form object."));
        }
        catch (IOException e)
        {
            Platform.runLater(() -> Alert.display("Request Worker", "Failed to communicate with server."));
        }
    }
    
    
    public void sendInitialConnectionRequest()
    {
        try 
        {
            connection = new Socket("192.168.1.107", 6550);
            serverComm = new Socket("192.168.1.107", 6550);
            
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
        while ((form = (LinkedHashMap)objInput.readObject()) != null)
        {
            decipherForm(form);
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
    
    
    public boolean sendForm(LinkedHashMap<String, Object> form)
    {
        try
        {
            objOutput.writeObject(form);
            return true;
        }
        catch (IOException e)
        {
            Platform.runLater(() -> Alert.display("Request Worker", "Failed to send form to server."));
            return false;
        }
    }
    
    
    private void decipherForm(LinkedHashMap form)
    {
        if (form.containsKey("login"))
        {
            LoginForm packedLogin = (LoginForm)form.get("login");
            TarkovTrader.authenticated = packedLogin.isAuthenticated();
        }
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
