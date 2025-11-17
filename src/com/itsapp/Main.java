package com.itsapp;

public class Main {
    
    public static void main(String[] args) {
        DbConnection dbconn = new DbConnection();
        dbconn.connect();

        String borrow = dbconn.borrowEquipment(
                   12110002,
                   10200400,
                   10003003,
                   "This is a test borrow operation"
                   );
        System.out.println(borrow);

        String returnEq = dbconn.returnEquipment(
                   12110002,
                   10200400,
                   10003003,
                   "This is a test return operation",
                   true
                   );
        System.out.println(returnEq);

        String borrow2 = dbconn.borrowEquipment(
                   12110002,
                   10200400,
                   10003003,
                   "This is a test borrow operation"
                   );
        System.out.println(borrow2);

        String returnEq2 = dbconn.returnEquipment(
                   12110002,
                   10200400,
                   10003003,
                   "This is a test return operation",
                   true
                   );
        System.out.println(returnEq2);

        String replc = dbconn.replaceEquipment(
                12110002, 
                10003003,
                "New computer ig", 
                "This is a result of a test insert to replace some equipment");
        System.out.println(replc);

    }

}

