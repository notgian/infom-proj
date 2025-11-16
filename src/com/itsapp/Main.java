package com.itsapp;

public class Main {
    
    public static void main(String[] args) {
        DbConnection dbconn = new DbConnection();
        dbconn.connect();
        System.out.println(dbconn.borrowEquipment(
                   12110002,
                   10400500,
                   10003003,
                   "This is a test borrow operation"
                   ));
    }

}
