
package tarkov.trader.objects;

import java.util.ArrayList;

/**
 *
 * @author austin
 */

public class Profile {
    
    // Need confirmed/completed sales
    // Need reported sales
    // Flags for second accounts under same IGN's, re-registrations, reputation, new account warnings
    
    private String username;
    private String ign;
    private String timezone;
    private ArrayList<Sale> sales;
    private ArrayList<AccountFlag> accountFlags;
    
    public Profile(String username, String ign, String timezone)
    {
        this.username = username;
        this.ign = ign;
        this.timezone = timezone;
        sales = new ArrayList<>();
        accountFlags = new ArrayList<>();
    }
    
    public String getUsername()
    {
        return username;
    }
    
    public String getIgn()
    {
        return ign;
    }
    
    public String getTimezone()
    {
        return timezone;
    }
    
    public ArrayList<Sale> getSales()
    {
        return sales;
    }
    
    public ArrayList<AccountFlag> getFlags()
    {
        return accountFlags;
    }
    
    
    // Modifiers:
    public void appendSale(Sale sale)
    {
        if (!sales.contains(sale))
            sales.add(sale);
        else
            System.out.println("Profile: Sale already exists for profile " + username + ".");
    }
    
    public void removeSale(Sale sale)
    {
        if (sales.contains(sale))
            sales.remove(sale);
        else
            System.out.println("Profile: Sale does not exist for profile " + username + ".");
    }
    
    public void appendFlag(AccountFlag flag)
    {
        if (!accountFlags.contains(flag))
            accountFlags.add(flag);
        else
            System.out.println("Profile: Account flag '" + flag + "' already exists for " + username + ".");
    }
    
    public void removeFlag(AccountFlag flag)
    {
        if (accountFlags.contains(flag))
            accountFlags.remove(flag);
        else
            System.out.println("Profile: Account flag '" + flag + "' does not exist for " + username + ".");
    }
}
