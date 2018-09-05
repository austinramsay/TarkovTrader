
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
    private ArrayList<Item> buyList;
    private ArrayList<Item> requestedSales;
    private ArrayList<AccountFlag> accountFlags;
    private ArrayList<Report> reports;
    private int repPoints;
    
    public Profile(String username, String ign, String timezone)
    {
        this.username = username;
        this.ign = ign;
        this.timezone = timezone;
        this.repPoints = 0;
        this.completedSales = new ArrayList<>();
        this.currentSales = new ArrayList<>();
        this.buyList = new ArrayList<>();
        this.requestedSales = new ArrayList<>();
        this.accountFlags = new ArrayList<>();
        this.reports = new ArrayList<>();
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
    
    public ArrayList<Item> getBuyList()
    {
        return buyList;
    }
    
    public ArrayList<Item> getRequestedSales()
    {
        return requestedSales;
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
    
    public ArrayList<Report> getReports()
    {
        return reports;
    }
    
    
    // Modifiers:
    private void setDate()
    {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        this.registrationDate = dateFormat.format(new Date());
    }
    
    public void setBuyList(ArrayList<Item> buyList)
    {
        this.buyList = buyList;
    }
    
    public void setRequestedSalesList(ArrayList<Item> requestedSales)
    {
        this.requestedSales = requestedSales;
    }
    
    public void setCompletedSalesList(ArrayList<Sale> completedSales)
    {
        this.completedSales = completedSales;
    }
    
    public void setCurrentSalesList(ArrayList<Item> currentSales)
    {
        this.currentSales = currentSales;
    }
    
    public void setReportsList(ArrayList<Report> reports)
    {
        this.reports = reports;
    }
    
    public boolean appendItem(Item item)
    {
        if (!currentSales.contains(item))
        {
            currentSales.add(item);
            return true;
        }
        else
            System.out.println("Profile: Item already exists in current sales for " + username + ".");
        
        return false;
    }
    
    public boolean removeItem(Item item)
    {
        if (currentSales.contains(item))
        {
            currentSales.remove(item);
            return true;
        }
        else
            System.out.println("Profile: Item does not exist for profile " + username + ".");
        
        return false;
    }
    
    public boolean appendSale(Sale sale)
    {
        if (!completedSales.contains(sale))
        {
            completedSales.add(sale);
            resolvePoints();
            return true;
        }
        else
            System.out.println("Profile: Sale already exists for profile " + username + ".");
        
        return false;
    }
    
    public boolean removeSale(Sale sale)
    {
        if (completedSales.contains(sale))
        {
            completedSales.remove(sale);
            resolvePoints();
            return true;
        }
        else
            System.out.println("Profile: Sale does not exist for profile " + username + ".");
        
        return false;
    }
    
    public boolean appendReport(Report report)
    {
        if (!reports.contains(report))
        {
            reports.add(report);
            resolvePoints();
            return true;
        }
        else
            System.out.println("Profile: Report already exists for profile " + username + ".");
        
        return false;
    }
    
    public boolean removeReport(Report report)
    {
        if (reports.contains(report))
        {
            reports.remove(report);
            resolvePoints();
            return true;
        }
        else
            System.out.println("Profile: Report does not exist for profile " + username + ".");
        
        return false;
    }    
    
    public boolean appendBuyItem(Item item)
    {
        if (!buyList.contains(item))
        {
            buyList.add(item);
            return true;
        }
        else
            System.out.println("Profile: Item already exists for profile " + username + ".");
            
        return false;
    }
    
    public boolean removeBuyItem(Item item)
    {
        if (buyList.contains(item))
        {
            buyList.remove(item);
            return true;
        }
        else
            System.out.println("Profile: Item does not exist for profile " + username + ".");
        
        return false;
    }
    
    public boolean appendRequestedSale(Item item)
    {
        if (!requestedSales.contains(item))
        {
            requestedSales.add(item);
            return true;
        }
        else 
            System.out.println("Profile: Requested sale already exists for profile " + username + ".");
        
        return false;
    }
    
    public boolean removeRequestedSale(Item item)
    {
        if (requestedSales.contains(item))
        {
            requestedSales.remove(item);
            return true;
        }
        else
            System.out.println("Profile: Requested sale does not exist for profile " + username + ".");
        
        return false;
    }
    
    public boolean appendFlag(AccountFlag flag)
    {
        if (!accountFlags.contains(flag))
        {
            accountFlags.add(flag);
            resolvePoints();
            return true;
        }
        else
            System.out.println("Profile: Account flag '" + flag + "' already exists for " + username + ".");
        
        return false;
    }
    
    public boolean removeFlag(AccountFlag flag)
    {
        if (accountFlags.contains(flag))
        {
            accountFlags.remove(flag);
            resolvePoints();
            return true;
        }
        else
            System.out.println("Profile: Account flag '" + flag + "' does not exist for " + username + ".");
        
        return false;
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
        
        for (Report report : reports)
        {
            switch (report.getReportReason())
            {
                case MOD_REMOVED:
                    repPoints -= 30;
                    break;
                    
                case SELLER_FAILED:
                    repPoints -= 30;
                    break;
                    
                case BUYER_FAILED:
                    repPoints -= 30;
                    break;
                    
                case BUYER_SCAM:
                    repPoints -= 200;
                    break;
                    
                case SELLER_SCAM:
                    repPoints -= 200;
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
                
                default:
                    // Points will be handled by account flags for any other sale status'
                    break;
            }
        }
    }
    
    
    public ItemLocation getItemLocation(Item item)
    {
        ItemLocation location = null;
        
        for (Item buyItem : buyList)
        {
            if (buyItem.equals(item))
            {
                location = new ItemLocation(ListLocation.BUY_LIST, buyList.indexOf(buyItem));
                return location;
            }
        }
        
        for (Item requestItem : requestedSales)
        {
            if (requestItem.equals(item))
            {
                location = new ItemLocation(ListLocation.SALE_REQUEST_LIST, requestedSales.indexOf(requestItem));
                return location;
            }
        }
        
        // Failed to find the item
        return null;
    }
}
