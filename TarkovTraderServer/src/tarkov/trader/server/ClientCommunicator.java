
package tarkov.trader.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ClientCommunicator {
    
    private Socket client;
    private BufferedWriter writer;
    
    public ClientCommunicator(Socket client)
    {
        this.client = client;
        openStreams();
    }
    
    
    private void openStreams()
    {
        try 
        {
            writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        }
        catch (IOException e)
        {
            System.out.println("Client communicator: Failed to open output stream. Error: " + e.getMessage());
        }
    }
    
    
    public void sendAlert(String message)
    {
        try
        {
            writer.write(message);
            writer.newLine();
            writer.flush();
        }
        catch (IOException e)
        {
            System.out.println("Client communicator: Failed to write error message to client. Lost connection? Error: " + e.getMessage());
        }
    }
}
