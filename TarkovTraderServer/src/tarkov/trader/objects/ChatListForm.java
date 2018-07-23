
package tarkov.trader.objects;

import java.util.ArrayList;

/**
 *
 * @author austin
 */

public class ChatListForm extends Form {
    
    private ArrayList<Chat> chatlist;
    
    
    public ChatListForm()
    {
        this.type = "chatlist";
    }
    
    
    public ArrayList<Chat> getChatList()
    {
        return chatlist;
    }
    
    
    public void setChatList(ArrayList<Chat> chatlist)
    {
        this.chatlist = chatlist;
    }
    
    
}
