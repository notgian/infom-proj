package com.itsapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Main {
    
    public static void main(String[] args) {
        DbConnection dbconn = new DbConnection(); dbconn.connect();

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

        try {

            dateFormatter.parse("0000-00-00");
            Report tables[] = {
                // dbconn.reportEquipmentBorrowingHistory(),
                // dbconn.reportLabReservationHistory(),
                // dbconn.reportStudentEquipmentBorrowHistory(null),
                // dbconn.reportOrgLabReservationHistory(null),
                dbconn.reportLabTechApprovalHistory(null),
                dbconn.reportLabTechApprovalHistory(10001002),
            };

            for (int k=0; k<tables.length; k++) {
                String[][] table = tables[k].getTable();

                System.out.println(table.length);

                for (int i = 0; i < table.length; i++) {
                    for (int j = 0; j < table[i].length; j++) {
                        System.out.print("%s, ".formatted(table[i][j]));
                    }
                    System.out.print("\n");
                }

                System.out.println(); 
            }

        }
        catch (InvalidFields e) {
            e.printStackTrace();
        }
        catch (ParseException e) {
            e.printStackTrace();
        }


    }

}

