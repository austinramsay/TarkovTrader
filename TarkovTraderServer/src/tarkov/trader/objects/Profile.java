
package tarkov.trader.objects;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author austin
 */

public class Profile implements Serializable {
    
    // Need confirmed/completed sales
    // Need reported sales
    // Flags for second accounts under same IGN's, re-registrations, reputation, new account warnings
    
    private String username;
    private String ign;
    private String timezone;
    private String registrationDate;
    private ArrayList<Sale> completedSales;
    private ArrayList<Item> currentSales;
    private ArrayList<AccountFlag> accountFlags;
    private int repPoints;
    
    public Profile(String username, String ign, String timezone)
    {
        this.username = username;
        this.ign = ign;
        this.timezone = timezone;
        this.repPoints = 0;
        this.completedSales = new ArrayList<>();
        this.currentSales = new ArrayList<>();
        this.accountFlags = new ArrayList<>();
        setDate();
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
    
    public ArrayList<Sale> getCompletedSales()
    {
        return completedSales;
    }
    
    public ArrayList<Item> getCurrentSales()
    {
        return currentSales;
    }
    
    public ArrayList<AccountFlag> getAccountFlags()
    {
        return accountFlags;
    }
    
    public String getRepPoints()
    {
        return NumberFormat.getNumberInstance().format(repPoints);
    }
    
    public String getRegistrationDate()
    {
        return registrationDate;
    }
    
    
    // Modifiers:
    private void setDate()
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        this.registrationDate = dateFormat.format(new Date());
    }
    
    public void appendSale(Sale sale)
    {
        if (!completedSales.contains(sale))
        {
            completedSales.add(sale);
            resolvePoints();
        }
        else
            System.out.println("Profile: Sale already exists for profile " + username + ".");
    }
    
    public void removeSale(Sale sale)
    {
        if (completedSales.contains(sale))
        {
            completedSales.remove(sale);
            resolvePoints();
        }
        else
            System.out.println("Profile: Sale does not exist for profile " + username + ".");
    }
    
    public void appendItem(Item item)
    {
        if (!currentSales.contains(item))
            currentSales.add(item);
        else
            System.out.println("Profile: Item already exists in current sales for " + username + ".");
    }
    
    public void removeItem(Item item)
    {
        if (currentSales.contains(item))
            currentSales.remove(item);
        else
            System.out.println("Profile: Item does not exist for profile " + username + ".");
    }
    
    public void appendFlag(AccountFlag flag)
    {
        if (!accountFlags.contains(flag))
        {
            accountFlags.add(flag);
            resolvePoints();
        }
        else
            System.out.println("Profile: Account flag '" + flag + "' already exists for " + username + ".");
    }
    
    public void removeFlag(AccountFlag flag)
    {
        if (accountFlags.contains(flag))
        {
            accountFlags.remove(flag);
            resolvePoints();
        }
        else
            System.out.println("Profile: Account flag '" + flag + "' does not exist for " + username + ".");
    }
    
    
    public void resolvePoints()
    {
        repPoints = 0;
        
        for (AccountFlag flag : accountFlags)
        {
            switch (flag) {
                case CONFIRMED_BUY_REP:
                    repPoints += 100;
                    break; 
                    
                case CONFIRMED_HIGH_BUY_REP:
                    repPoints += 200;
                    break;
                    
                case CONFIRMED_SELL_REP:
                    repPoints += 100;
                    break;
                    
                case CONFIRMED_HIGH_SELL_REP:
                    repPoints += 200;
                    break;
                    
                case VERIFIED_SUB:
                    repPoints += 50;
                    break;
                    
                case CONFIRMED_MULTIPLE_SCAM:
                    repPoints -= 1000;
                    break;
                    
                case CONFIRMED_SCAM:
                    repPoints -= 500;
                    break;
                    
                case MULTIPLE_REG:
                    repPoints -= 150;
                    break; 
                    
                default:
                    break;
            }
        }
        
        for (Sale completedSale : completedSales)
        {
            switch (completedSale.getSaleStatus())
            {
                case BUYER_CONFIRMED:
                    repPoints += 10;
                    break;
                    
                case SELLER_CONFIRMED:
                    repPoints += 10;
                    break;
                    
                case FAILED:
                    repPoints -= 30;
                    break;
                
                default:
                    // Points will be handled by account flags for any other sale status'
                    break;
            }
        }
    }
}
