
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
    
    private String messageAlert = "%d new messages from %s.";
    private String chatAlert = "New chat from %s.";
    
    public NotificationManager(TarkovTrader trader)
    {
        this.trader = trader;
        
        notificationQueue = new ArrayList<>();
    }
    
    
    public void processNotificationsList(ArrayList<Notification> notificationsList)
    {
        for (Notification notification : notificationsList)
        {
            processNotification(notification);
        }
        
        handleQueue();
    }
    
    
    public void processNotification(Notification notification)
    {
        String type = notification.getNotificationType();
        
        switch (type)
        {
            case "message":
                notificationQueue.add(String.format(messageAlert, notification.getCount(), notification.getOriginUsername()));
                break;
                
            case "chat":
                notificationQueue.add(String.format(chatAlert, notification.getOriginUsername()));
                break;
        }
    }
    
    
    private boolean handleQueue()
    {
        if (notificationQueue.isEmpty())
            return false;
        
        Thread timedNotifications = new Thread(new timedNotificationHandler(notificationQueue));
        timedNotifications.start();
        
        return true;
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
        }
        
    }
}

