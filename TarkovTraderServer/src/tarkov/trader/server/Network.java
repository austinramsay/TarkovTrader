
package tarkov.trader.server;

import java.io.IOException;
import java.net.*;

public class Network implements Runnable {
    
    private ServerSocket server;
    private Socket client;
    private Socket clientComm;
    private final int port = 6550;
    
    public Network()
    {
    }
    
    
    @Override
    public void run()
    {
        // Open server socket
        try 
        {
            server = new ServerSocket(port);
            beginListener(); // Starts while true loop for listening
        }
        catch (IOException e)
        {
            TarkovTraderServer.broadcast("Network: couldn't open server socket. Check port: " + port);
        }
    }
    
    
    private void beginListener()
    {
        TarkovTraderServer.broadcast("Network: Awaiting clients...");
        
        try
        {
            while (true)
            {
                listen();
            }
        }
        catch (IOException e)
        {
            TarkovTraderServer.broadcast("Network: Failed to accept client.");
        }
    }
    
    
    private void listen() throws IOException
    {
        // Listen method runs in a while true loop after opening server socket
        
        client = server.accept();
        clientComm = server.accept();
        String clientIp = client.getInetAddress().getHostAddress();
        TarkovTraderServer.broadcast("Client connection from: " + clientIp);
        
        
        // Client immediately needs a request handler. Once a login or new account is requested, the server must be ready to accept and process
        
        RequestWorker clientRW = new RequestWorker(client, clientComm);
        Thread rwClientThread = new Thread(clientRW);
        rwClientThread.start();
    }
    
}
