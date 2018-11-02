
package tarkov.trader.objects;

/**
 *
 * @author austin
 */

public enum ReportType {
    BUYER("Buyer"),
    SELLER("Seller");
    
    private String desc;
    
    private ReportType(String desc)
    {
        this.desc = desc;
    }
    
    public String getDesc()
    {
        return desc;
    }
}
