package tarkov.trader.server;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;
import tarkov.trader.objects.Report;

/**
 *
 * @author austin
 */

public class ReportsTableModel extends AbstractTableModel {

    private ArrayList<Report> reports;
    
    private String[] colNames = {"Reported By", "User Reported"};
    
    public ReportsTableModel()
    {
    }
    
    public void setReports(ArrayList<Report> reports)
    {
        this.reports = reports;
    }
    
    public void appendReport(Report newReport)
    {
        reports.add(newReport);
    }
    
    public ArrayList<Report> getReports()
    {
        return reports;
    }
    
    @Override
    public int getRowCount() 
    {
        return reports.size();
    }

    @Override
    public int getColumnCount() 
    {
        return 2;
    }

    @Override
    public String getColumnName(int column)
    {
        return colNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) 
    {
        Report currentReport = reports.get(rowIndex);
        
        switch(columnIndex)
        {
            case 0:
                return currentReport.getByUsername();
            case 1:
                return currentReport.getUserToReport();
        }
        
        return null;
    }
    
}
