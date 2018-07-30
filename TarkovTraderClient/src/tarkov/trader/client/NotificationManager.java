
package tarkov.trader.client;

import java.util.ArrayList;
import javafx.application.Platform;
import tarkov.trader.objects.Notification;

/**
 *
 * @author austin
 */

public class NotificationManager {
    
    
    private TarkovTrader trader;
    
    private ArrayList<String> notificationQueue;
    private ArrayList<Notification> toRemove;
    private ArrayList<Notification> toReplace;
    
    private final String messageAlert = "%d new messages from %s.";
    private final String chatAlert = "New chat from %s.";
    
    private boolean toFlagMessages;
    
    
    public NotificationManager(TarkovTrader trader)
    {
        this.trader = trader;
        
        notificationQueue = new ArrayList<>();
        toRemove = new ArrayList<>();
        toReplace = new ArrayList<>();
    }
    
    
    public synchronized void processNotificationsList()
    {
        toFlagMessages = false;
        
        ArrayList<Notification> notificationsList = TarkovTrader.notificationsList;
        
        for (Notification notification : notificationsList)
        {
            processNotification(notification);
        }
        
        for (Notification notification : toRemove)
        {
            TarkovTrader.notificationsList.remove(notification);
        }
        
        for (Notification notification : toReplace)
        {
            TarkovTrader.notificationsList.add(notification);
        }
        
        if (toFlagMessages)
        {
            flagMessages(true);
        }
        
        handleQueue();
    }
    
    
    public void processNotification(Notification notification)
    {      
        toRemove.add(notification);
        
        String type = notification.getNotificationType();
        
        boolean shouldNotify = !notification.wasNotified();
        
        switch (type)
        {
            case "message":
                if (shouldNotify)
                {
                    processMessageNotification(notification);
                }
                toFlagMessages = true;
                break;
                
            case "chat":
                if (shouldNotify)
                    notificationQueue.add(String.format(chatAlert, notification.getOriginUsername()));
                toFlagMessages = true;
                break;
        }
        
        notification.setNotified(true);
        
        toReplace.add(notification);
    }
    
    
    private void processMessageNotification(Notification notification)
    {
        if (Messenger.isOpen)
        {
            // If the messenger is open, and the open current chat is NOT the same chat where this message is coming from, display a notification
            // Otherwise, leave alone
            if (trader.getMessenger().chatListView.getSelectionModel().isEmpty() || 
                !trader.getMessenger().chatListView.getSelectionModel().getSelectedItem().getName(TarkovTrader.username).equals(notification.getOriginUsername()))        
            {
                // Create a message for the queue if the chat isn't open
                notificationQueue.add(String.format(messageAlert, notification.getCount(), notification.getOriginUsername()));
            }
        }
        else
            notificationQueue.add(String.format(messageAlert, notification.getCount(), notification.getOriginUsername()));
    }
    
    
    public void clearMessageNotifications()
    {
        ArrayList<Notification> notificationsList = TarkovTrader.notificationsList;
        ArrayList<Notification> removeList = new ArrayList<>();
        
        for (Notification notification : notificationsList)
        {
            if (notification.getNotificationType().equals("chat") || notification.getNotificationType().equals("message"))
                removeList.add(notification);
        }
        
        notificationsList.removeAll(removeList);
        
        flagMessages(false);
    }
    
    
    private boolean handleQueue()
    {
        if (notificationQueue.isEmpty())
            return false;
        
        Thread timedNotifications = new Thread(new timedNotificationHandler(notificationQueue));
        timedNotifications.start();
        
        return true;
    }
    
    
    private void flagMessages(boolean toFlag)
    {
        trader.setNewMessagesButton(toFlag);
    }
    
    
    private class timedNotificationHandler implements Runnable {
        
        private ArrayList<String> notificationQueue;
        
        public timedNotificationHandler(ArrayList<String> notificationQueue)
        {
            this.notificationQueue = notificationQueue;
        }
        
        @Override
        public void run()
        {
            for (String message : notificationQueue)
            {
                Platform.runLater(() -> Alert.display(null, message));
                
                try {
                    Thread.sleep(5000);
               } catch (InterruptedException ex) {
                    Alert.display(null, "Notification manager interrupted."); }        
                
            }
            notificationQueue.clear();
        }
        
    }
}

