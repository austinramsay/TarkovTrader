
package tarkov.trader.objects;

import java.io.Serializable;
import java.text.NumberFormat;

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
    private SaleCondition condition;
    private SaleConfirmation confirmed;
    private Item soldItem;
    
    public Sale(String sellerName, String buyerName, Item soldItem, SaleCondition condition, SaleConfirmation confirmed)
    {
        this.sellerName = sellerName;
        this.buyerName = buyerName;
        this.soldItem = soldItem;
        this.condition = condition;
        this.confirmed = confirmed;
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
    
    public String getItemPrice()
    {
        if (soldItem.getPrice() == 0)
        {
            return null;
        }
        return NumberFormat.getNumberInstance().format(soldItem.getPrice()) + "₽";
    }
    
    public SaleCondition getSaleCondition()
    {
        return condition;
    }
    
    public SaleConfirmation getSaleConfirmation()
    {
        return confirmed;
    }
    
    
    // Modifiers
    public void setSaleCondition(SaleCondition condition)
    {
        this.condition = condition;
    }
    
    public void setSaleConfirmation(SaleConfirmation confirmed)
    {
        this.confirmed = confirmed;
    }
    
}
