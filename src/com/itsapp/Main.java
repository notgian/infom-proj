package com.itsapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {

    private static final String HOST = "localhost"; // e.g., "localhost" or "127.0.0.1"
    private static final String PORT = "3306";
    private static final String DATABASE = "mysql";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "1234";

    private static final String DB_URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE;


    public static Connection dbconnect() {
        System.out.println("Connecting to database: " + DB_URL);
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            System.out.println("✅ Connection successful!");
            System.out.println("Connected to: " + conn.getMetaData().getDatabaseProductName() + " " + conn.getMetaData().getDatabaseProductVersion());
        } catch (SQLException e) {
            System.err.println("❌ Connection Failed!");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
        }

        return conn;
    }

    
    public static void main(String[] args) {
        dbconnect();
    }

}
