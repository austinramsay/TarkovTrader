
package tarkov.trader.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import javafx.application.Platform;


public class ServerCommunicator implements Runnable {
    
    private Socket serverComm;
    private BufferedReader reader;
    private String message;
    
    public ServerCommunicator(Socket serverComm)
    {
        this.serverComm = serverComm;
    }
    
    
    @Override
    public void run()
    {
        while (true)
        {
            listen();
        }
    }
    
    
    private void openStreams() throws IOException
    {
        reader = new BufferedReader(new InputStreamReader(serverComm.getInputStream()));
    }
    
    
    private void listen()
    {
        try 
        {
            openStreams();
            while ((message = reader.readLine()) != null)
            {
                Platform.runLater(() -> { Alert.display("Message from Server", message); });
            }
        }
        catch (IOException e)
        {
            Alert.display("Server Communicator", "Lost connection with server communicator.");
        }
    }
    
}
