
package tarkov.trader.client;

import java.net.*;
import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.application.Platform;
import tarkov.trader.objects.LoginForm;
import tarkov.trader.objects.Form;


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
            Platform.runLater(() -> Alert.display("Request Worker", "Failed to decipher received processed request."));
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
