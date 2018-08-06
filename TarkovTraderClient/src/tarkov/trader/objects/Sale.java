
package tarkov.trader.objects;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author austin
 */

public class Sale implements Serializable {
    
    // Information containing sale condition (confirmed, pending scam sale, etc)
    // Contains sold item itself
    // Contains buyer and seller names
    
    private String sellerName;
    private String buyerName;
    private String saleDate;
    private SaleType saleType;
    private SaleStatus saleStatus;
    private Item soldItem;
    
    public Sale(String sellerName, String buyerName, Item soldItem, SaleType saleType, SaleStatus saleStatus)
    {
        this.sellerName = sellerName;
        this.buyerName = buyerName;
        this.soldItem = soldItem;
        this.saleType = saleType;
        this.saleStatus = saleStatus;
        setDate();
    }
    
    
    // Getters
    public String getSellerName()
    {
        return sellerName;
    }
    
    public String getBuyerName()
    {
        return buyerName;
    }
    
    public Item getSoldItem()
    {
        return soldItem;
    }
    
    public String getItemName()
    {
        return soldItem.getName();
    }
    
    public String getSaleDate()
    {
        return saleDate;
    }
    
    public SaleType getSaleType()
    {
        return saleType;
    }
    
    public String getSaleTypeDesc()
    {
        return saleType.getDescription();
    }
    
    public SaleStatus getSaleStatus()
    {
        return saleStatus;
    }
    
    public String getSaleStatusDesc()
    {
        return saleStatus.getDescription();
    }
    
    
    // Modifiers
    public void setSaleType(SaleType saleType)
    {
        this.saleType = saleType;
    }
    
    public void setSaleStatus(SaleStatus saleStatus)
    {
        this.saleStatus = saleStatus;
    }
    
    private void setDate()
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        this.saleDate = dateFormat.format(new Date());        
    }
    
}
