
package org.codeviation.table;

/**
 * Table model for visualization. For visualization of data is used Table.toString(). 
 * @author pzajac
 */
public interface Table<T> {
    /** get table uinique name
     */
    String getName();
    /** get table title
     */
    String getTitle();
    /** get description for table 
     */
    String getDescription();
    int getRows();
    int getColumns();
    /** get data for paint.
     * @return null if no value is available
     */  
    T getData(int row,int column);
    /** Get titles of rows header
     * @return null if now header is available
     */
    String[] getRowsHeader();
    /** Get titles of columns header
     *  @return null if no header is available
     */
    String[] getColumnsHeader();    
}
