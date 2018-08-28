package tarkov.trader.server;

import java.awt.BorderLayout;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import tarkov.trader.objects.Report;

/**
 *
 * @author austin
 */

public class ReportsTable extends JPanel {
    
    JTable table;
    ReportsTableModel model;
    
    public ReportsTable() 
    {
        model = new ReportsTableModel();
        table = new JTable(model);
        
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        setLayout(new BorderLayout());
        
        add(new JScrollPane(table), BorderLayout.CENTER);
    }
    
    public void setReports(ArrayList<Report> reports)
    {
        model.setReports(reports);
    }
    
    public Report getSelectedReport()
    {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1)
            return null;
        
        Report selectedReport = model.getReports().get(selectedRow);
        return selectedReport;
    }
    
    public void refresh()
    {
        model.fireTableDataChanged();
    }
    
}
