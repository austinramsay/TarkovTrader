
package tarkov.trader.objects;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author austin
 */

public class Report extends Form {
    
    private String byUsername;
    private String userToReport;
    private String comments;
    private String reportDate;
    private ReportType report_type;
    private ReportReason reason;
    private Item reportedItem;
    
    public Report(String byUsername, String userToReport, String comments, Item reportedItem, ReportType report_type)
    {
        this.type = "report";
        this.byUsername = byUsername;
        this.userToReport = userToReport;
        this.comments = comments;
        this.report_type = report_type;
        this.reportedItem = reportedItem;
        setDate();
    }
    
    public String getByUsername()
    {
        return byUsername;
    }
    
    public String getUserToReport()
    {
        return userToReport;
    }
    
    public String getComments()
    {
        return comments;
    }
    
    public String getReportDate()
    {
        return reportDate;
    }
    
    public ReportType getReportType()
    {
        return report_type;
    }
    
    public ReportReason getReportReason()
    {
        return reason;
    }
    
    public String getReportTypeDesc()
    {
        return report_type.getDesc();
    }
    
    public String getReportReasonDesc()
    {
        return reason.getDesc();
    }
    
    public Item getReportedItem()
    {
        return reportedItem;
    }
    
    public void setDate()
    {
        DateFormat dateformat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        reportDate = dateformat.format(new Date());
    }
    
    public void setReason(ReportReason reason)
    {
        // Called by server once accepting a report before pushing to a profile
        this.reason = reason;
    }

    @Override
    public boolean equals(Object report)
    {
        if (!(report instanceof Report))
            return false;
        
        if (report == this)
            return true;
        
        Report compareReport = (Report)report;
        
        if (!compareReport.getByUsername().equals(this.getByUsername()))
            return false;
        if (!compareReport.getUserToReport().equals(this.getUserToReport()))
            return false;
        if (!compareReport.getComments().equals(this.getComments()))
            return false;
        
        return true;
    }
    
}
