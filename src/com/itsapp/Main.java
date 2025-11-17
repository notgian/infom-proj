package com.itsapp;

public class Main {
    
    public static void main(String[] args) {
        DbConnection dbconn = new DbConnection();
        dbconn.connect();
        String borrow = dbconn.borrowEquipment(
                   12110002,
                   10400500,
                   10003003,
                   "This is a test borrow operation"
                   );
        System.out.println(borrow);

        String returnEq = dbconn.returnEquipment(
                   12110002,
                   10400500,
                   10003003,
                   "This is a test borrow operation",
                   false
                   );
        System.out.println(returnEq);
    }

}
