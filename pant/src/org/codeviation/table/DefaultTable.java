
package org.codeviation.table;

import java.text.DecimalFormat;

/**
 *
 * @author pzajac
 */
public class DefaultTable implements Table {
    private int rows;
    private int cols;
    private String name;
    private String title;
    private String description;
    private Object data[][];

    private String rowsHeader[];
    private String columnsHeader[];
    
    /** Creates a new instance of DefaultTable */
    public DefaultTable(int rows,int cols,String name) {
        this.rows = rows;
        this.cols = cols;
        this.name = name;
        
        data = new Object[rows][cols];
        for (int r = 0 ; r < rows ; r++) {
            data[r] = new Object[cols];
        }
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public void setDescription(String desc) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return cols;
    }

    DecimalFormat decFormat = new DecimalFormat("0.###E0");

    public void setData(int row,int column,Object value) {
        if (value instanceof Double) {
            value = decFormat.format(((Double)value).doubleValue());     
        }
        data[row][column] = value;
    }
    public Object getData(int row, int column) {
        return data[row][column];
    }

    public void setRowsHeader(String header[]) {
        this.rowsHeader = header;
    }
    public String[] getRowsHeader() {
        return rowsHeader;
    }

    public void setColumnsHeader(String columns[]) {
        this.columnsHeader = columns;
    }
    public String[] getColumnsHeader() {
        return columnsHeader;
    }
}
