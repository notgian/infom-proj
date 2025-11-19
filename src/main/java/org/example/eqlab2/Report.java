
package org.example.eqlab2;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Arrays;

class Report {
    private String[][] table;
    public Report(ResultSet resultSet) {
        try {
            ResultSetMetaData rsmd = resultSet.getMetaData();
            int colCount = rsmd.getColumnCount();
            // Need to use array list bc we cannot determine row count beforehand
            ArrayList<String[]> tableTemp = new ArrayList<>();
            String[] colNames = new String[colCount];
            for (int i = 1; i <= colCount; i++) {
               colNames[i-1] = rsmd.getColumnLabel(i);
            }
            tableTemp.add(colNames);

            while(resultSet.next()) {
                String[] currentRow = new String[colCount];
                for (int i = 1; i <= colCount; i++) {
                    Object obj = resultSet.getObject(i);
                    currentRow[i-1] = (obj == null) ? null : obj.toString();
                }
                tableTemp.add(currentRow);
            }

            this.table = tableTemp.toArray(new String[tableTemp.size()][]);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** 
     * Gets the names of the columns 
     *  @return an array of the names of the columns in order
     *
     */
    public String[] getColNames() {
        return Arrays.copyOf(this.table[0], this.table[0].length);
    }

    /**
     * Gets the values as a "table" (2D array, matrix, whatever you
     * want to call it)
     *
     *  @return a 2D array of the values in order of the column labels
     */
    public String[][] getValuesTable() {
        return Arrays.copyOfRange(this.table, 1, this.table.length);
    }
   
    /** 
     * Gets the values of the full table
     *  @return a 2D array of the full tables including the column headers
     *  as the first row and the values in succeeding rows
     */
    public String[][] getTable() {
        return Arrays.copyOf(this.table, this.table.length);
    }
}
