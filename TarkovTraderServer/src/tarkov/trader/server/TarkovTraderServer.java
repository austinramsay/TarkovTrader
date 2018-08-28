
package tarkov.trader.server;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import tarkov.trader.objects.HeartbeatForm;
import tarkov.trader.objects.SyncForm;

public class TarkovTraderServer {

    private Network networking;
    private ServerWorker serverWorker;
    private static Moderator mod;
      
    // Database variables
    public static String dbIpAddr;
    public static String dbName;
    public static String dbUsername;
    public static String dbPassword;
    public final static String dbDriver = "com.mysql.cj.jdbc.Driver";
    
    // File usage variables
    private static boolean shouldSave = false;
    public static final String SERVER_DIR = System.getProperty("user.home");
    private static final File REPORT_LOG_FILE = new File(SERVER_DIR + "/.tarkovtrader_reportlog");
    public static ReportLog reportLog;
    
    // Current user variables
    private static volatile ArrayList<String> userList;
    public static volatile HashMap<String, RequestWorker> authenticatedUsers;   // < Username, User's respective worker >
    
    
    public static void main(String[] args) 
    {
        System.out.println("Tarkov Trader Server");
        
        TarkovTraderServer server = new TarkovTraderServer();
         
        mod = new Moderator(server);
        
        // Upon exit, ensure that we save logs to their respective locations
        Thread shutdown = new Thread(() -> { 
            server.saveLogs();
        });
        Runtime.getRuntime().addShutdownHook(shutdown);
    }
    
    
    private void loadSavedLogs()
    {
        mod.broadcast(String.format("Checking %s for existing server files...", SERVER_DIR));
        
        
        // Check if 'Report Logs' exist to set the static 'reportLog' variable
        StringBuilder reportLogExistsStatus = new StringBuilder();
        reportLogExistsStatus.append("Reports: ");
        if (REPORT_LOG_FILE.exists())
        {
            reportLogExistsStatus.append("Found!");
        }
        else
        {
            reportLogExistsStatus.append("Not found.");
            if (createReportLogFile(REPORT_LOG_FILE))
                reportLogExistsStatus.append("\nNew report log file created.");
        }
        
        
        // Attempt to read the report log file and place into the static report log variable
        StringBuilder reportLogLoadStatus = new StringBuilder();
        ObjectInputStream ois = null;
        try 
        {
            ois = new ObjectInputStream(new FileInputStream(REPORT_LOG_FILE));
            TarkovTraderServer.reportLog = (ReportLog)ois.readObject();
            reportLogLoadStatus.append("Report log ready.");
            shouldSave = true;
        }
        catch (FileNotFoundException e)
        {
            reportLogLoadStatus.append("Attempted to load report log file, but file was not found.");
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            reportLogLoadStatus.append("\nAttempted to load report log file, but failed to cast as a 'ReportLog'.");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            reportLogLoadStatus.append("\nAttempted to load report log file. IOException occured.");
            e.printStackTrace();
        }
        finally 
        {
            try {
                if (ois != null)
                    ois.close();
            } catch (IOException e) {  System.out.println("Failed to close resources.");  e.printStackTrace();  }
        }
        
        
        // Broadcast our status messages to the GUI
        mod.broadcast(reportLogExistsStatus.toString());
        mod.broadcast(reportLogLoadStatus.toString());
    }
    
    
    private boolean createReportLogFile(File newFile)
    {
        ObjectOutputStream oos = null;
        
        try 
        {  
            newFile.createNewFile();
            ReportLog newReportLog = new ReportLog();
            oos = new ObjectOutputStream(new FileOutputStream(newFile));
            oos.writeObject(newReportLog);
            return true;
        } 
        catch (IOException e) 
        {
            mod.broadcast("Failed to create new file: " + newFile.getName());
            e.printStackTrace();
            return false;
        }
        finally 
        {
            try {
                if (oos != null)
                    oos.close();
            } catch (IOException e) {  System.out.println("Failed to close resources.");  e.printStackTrace();  }
        }
    }
    
    
    private void saveLogs()
    {
        if (!shouldSave)
            return;
        
        ObjectOutputStream oos = null;
        
        try
        {
            oos = new ObjectOutputStream(new FileOutputStream(REPORT_LOG_FILE));
            oos.writeObject(TarkovTraderServer.reportLog);
            System.out.println("Log files saved.");
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Failed to save log files. File not found.");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            System.out.println("Failed to save log files. IOException occured.");
            e.printStackTrace();
        }
        finally 
        {
            try {
                if (oos != null)
                    oos.close();
            } catch(IOException e) {  System.out.println("Failed to close resources.");  e.printStackTrace();  }
        }
    }
    
    
    public void start()
    {
        loadSavedLogs();
        
        TarkovTraderServer.authenticatedUsers = new HashMap();
        networking = new Network();
        TarkovTraderServer.syncUserList();
        Thread networker = new Thread(networking);
        networker.start();
        serverWorker = new ServerWorker();
    }
    
    
    public static void broadcast(String logMessage)
    {
        mod.broadcast(logMessage);
    }
    
    
    public boolean verifyDBconnection()
    {
        try 
        {
            Class.forName(TarkovTraderServer.dbDriver);
            
            Connection dbConnection = DriverManager.getConnection("jdbc:mysql://" + TarkovTraderServer.dbIpAddr + ":3306/" + TarkovTraderServer.dbName + "?verifyServerCertificate=false&useSSL=true", TarkovTraderServer.dbUsername, TarkovTraderServer.dbPassword);
            dbConnection.close();
            System.out.println("Database test connection successful. Starting server...");
            return true;
        }
        catch (ClassNotFoundException e)
        {
            String error = "DBConnection: " + e.getMessage() + "\n";
            System.out.println(error);
            e.printStackTrace();
            return false;
        }
        catch (SQLException e)
        {
            String error = "Failed database connection.\n";
            System.out.println(error);
            return false;
        }
    }
    
    
    public ServerWorker getServerWorker()
    {
        return serverWorker;
    }
    
    
    public synchronized static void syncUserList()
    {
        // When a new account is created, or the server is started
        // The ArrayList containing Strings of all usernames should be updated
        // This will be sent to users on login
        // Or pushed to all online clients when a new account is created for the most updated list client-side
        
        // Sync in progress lock
        // Because a new account has been added into the database, get the latest info
        DatabaseWorker dbWorker = new DatabaseWorker();
        ArrayList<String> pulledUserList = dbWorker.pullUserList();
        
        // Set the static arraylist equal to the latest pulled list
        TarkovTraderServer.userList = pulledUserList;
        
        // Build the SyncForm to push to all online clients
        ArrayList<String> syncFlags = new ArrayList<>();
        syncFlags.add("fulluserlist");
        
        SyncForm syncinfo = new SyncForm(syncFlags);
        syncinfo.setFullUserList(TarkovTraderServer.userList);
        
        // Get all online users workers, send the SyncForm
        for (Map.Entry<String, RequestWorker> client : TarkovTraderServer.authenticatedUsers.entrySet())
        {
            RequestWorker tempWorker = client.getValue();
            tempWorker.sendForm(syncinfo);
            tempWorker = null;
        }
    }
    
    
    @SuppressWarnings("empty-statement")
    public synchronized static ArrayList<String> getUserList()
    {
        return TarkovTraderServer.userList;
    }
    
    
    public synchronized static void syncOnlineList()
    {
        // This method ONLY pushes the latest online list to all available clients

        // Prepare the SyncForm
        ArrayList<String> syncFlags = new ArrayList<>();
        syncFlags.add("onlineuserlist");
        SyncForm syncinfo = new SyncForm(syncFlags);        
        
        // Prepare to pack arraylist to be set into SyncForm
        ArrayList<String> onlineUsers = TarkovTraderServer.getOnlineList();   
        syncinfo.setOnlineUserList(onlineUsers);
        
        // Push to available clients
        for (Map.Entry<String, RequestWorker> client : TarkovTraderServer.authenticatedUsers.entrySet())
        {
            RequestWorker tempWorker = client.getValue();
            tempWorker.sendForm(syncinfo);
            tempWorker = null;
        }
    }
    
    
    public synchronized static ArrayList<String> getOnlineList()
    {
        ArrayList<String> onlineUsers = new ArrayList<>();
        
        for (Map.Entry<String, RequestWorker> entry : TarkovTraderServer.authenticatedUsers.entrySet())
        {
            onlineUsers.add(entry.getKey());
        }        
        
        return onlineUsers;
    }
    
    
    private void listOnlineUsers()
    {
        System.out.println("Online User List:");
        for (Map.Entry<String, RequestWorker> entry : TarkovTraderServer.authenticatedUsers.entrySet())
        {
            System.out.println(" - " + entry.getValue().clientUsername);
        }
    }
    
    
    private void checkClientConnectivity()
    {
        System.out.println("Sending heartbeat checks:");
        for (Map.Entry<String, RequestWorker> entry : TarkovTraderServer.authenticatedUsers.entrySet())
        {
            if (entry.getValue().sendForm(new HeartbeatForm()))
                System.out.println(" - Requested: " + entry.getValue().clientUsername);
            else
            {
                TarkovTraderServer.authenticatedUsers.remove(entry.getValue().clientUsername);
                System.out.println(" - Removed: " + entry.getValue().clientUsername);
            }
        }        
    }
    
    
}
