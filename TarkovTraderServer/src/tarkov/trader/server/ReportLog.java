
package tarkov.trader.server;

import java.io.Serializable;
import java.util.ArrayList;
import tarkov.trader.objects.Report;

/**
 *
 * @author austin
 */

public class ReportLog implements Serializable {
    
    private ArrayList<Report> reports;
    private ArrayList<Report> history;
    
    public ReportLog()
    {
        reports = new ArrayList<>();
        history = new ArrayList<>();
    }
    
    public ArrayList<Report> getReports()
    {
        return reports;
    }
    
    public void setReports(ArrayList<Report> reports)
    {
        this.reports = reports;
    }
    
    public boolean appendReport(Report report)
    {
        if (!reports.contains(report))
        {
            reports.add(report);
            history.add(report);
            return true;
        }
        else
        {
            TarkovTraderServer.broadcast("Report Log: Failed to append report - report already exists.");
            return false;
        }
    }
    
    public boolean removeReport(Report report)
    {
        if (reports.contains(report))
        {
            reports.remove(report);
            return true;
        }
        else
        {
            TarkovTraderServer.broadcast("Report Log: Failed to remove report - report doesn't exist.");
            return false;
        }
    }
    
}
