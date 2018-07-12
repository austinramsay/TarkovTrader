
package tarkov.trader.server;

import tarkov.trader.objects.NewAccountForm;
import java.net.*;
import java.io.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import tarkov.trader.objects.LoginForm;


public class RequestWorker implements Runnable {
    
    private DatabaseWorker dbWorker;
    private ClientCommunicator communicator;
    
    private Socket client;
    private Socket clientComm;
    private String clientIp;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private LinkedHashMap<String, Object> form;
    
    
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
        dbWorker = new DatabaseWorker(clientIp, communicator);
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
            // TODO: HANDLE A CLOSED CONNECTION
        }
        catch (ClassNotFoundException e)
        {
            System.out.println("Request Worker: Failed to create form map. " + e.getMessage());
            communicator.sendAlert("Request Worker: Failed to create form map.");
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
        
        while((form = (LinkedHashMap)inputStream.readObject()) != null)
        {
            decipherForm(form);
        }
    }
    
    
    private void decipherForm(LinkedHashMap form)
    {
        // This method determines the type of form sent from the client and processes information accordingly
        
        if (form.containsKey("newaccount"))
        {
            NewAccountForm newAccountInfo = (NewAccountForm)form.get("newaccount");
            if (verifyClientIP())
                 processNewAccount(newAccountInfo);
            else
            {
                // User has more than 1 account associated with their IP address
                // TODO: SET FLAG FOR POTENTIAL FRAUD ACCOUNT
                communicator.sendAlert("Failed to verify user info. Please contact: tarkovtrader@gmail.com");
            }
        }
            
        
        if (form.containsKey("login"))
        {
            LoginForm packedLogin = (LoginForm)form.get("login");
            if (packedLogin == null)
                System.out.println("packedlogin null");
            System.out.println("username: " + packedLogin.getUsername());
            if (loginAuthenticated(packedLogin))
            {
                System.out.println("authenticated!");
                packedLogin.setAuthenticationState(true);
                LinkedHashMap<String, Object> loginForm = new LinkedHashMap();
                loginForm.put("login", packedLogin);
                if (sendForm(loginForm)) 
                {
                    TarkovTraderServer.authenticatedUsers.put(packedLogin.getUsername(), clientIp);
                    System.out.println("authenticated login sent back");
                }
            }
        }
    }
    
    
    private void processNewAccount(NewAccountForm newAccountInfo)
    {
        LinkedHashMap newAccountMap = new LinkedHashMap();
        newAccountMap.put("username", newAccountInfo.getUsername());
        newAccountMap.put("password", newAccountInfo.getPassword());
        newAccountMap.put("firstname", newAccountInfo.getFirst());
        newAccountMap.put("lastname", newAccountInfo.getLast());
        newAccountMap.put("ign", newAccountInfo.getIgn());
        newAccountMap.put("ipaddr", clientIp);
        dbWorker.insertAccountInfo(newAccountMap);
    }
    
    
    public boolean sendForm(Map<String, Object> form)
    {
        try
        {
            outputStream.writeObject(form);
            return true;
        }
        catch (IOException e)
        {
            communicator.sendAlert("Server failed to return login authentication.");
            return false;
        }
    }
    
    
    private boolean verifyClientIP()
    {
        // Prevents users from creating multiple accounts (scam accounts, etc.)
       try 
       {
            String command = "SELECT CASE WHEN EXISTS (SELECT ipaddr FROM accounts WHERE ipaddr = \"" + clientIp + "\") THEN CAST(1 AS UNSIGNED) ELSE CAST(0 AS UNSIGNED) END;";
            ResultSet checkIp = dbWorker.query(command);
            checkIp.first(); // Move cursor to the first row
            return (checkIp.getInt(1) != 1); // Get value from first column (only one column for this query)
       }
       catch (SQLException e)
       {
           System.out.println("Request Worker: SQL Exception for " + clientIp + ". Error:" + e.getMessage());
           return false;
       }
    }
    
    private boolean loginAuthenticated(LoginForm packedLogin)
    {
        try
        {
            String command = "SELECT password FROM accounts WHERE username=\"" + packedLogin.getUsername() + "\""; 
            System.out.println("the string: " + command);
            ResultSet authResult = dbWorker.query(command);
            authResult.first();
            return (authResult.getString(1).equals(packedLogin.getPassword()));
        }
        catch (SQLException e)
        {
            System.out.println("Request Worker: SQL Exception for " + clientIp + " when attempting to authenticate. Error: " + e.getMessage());
            return false;
        }
    }
    
}