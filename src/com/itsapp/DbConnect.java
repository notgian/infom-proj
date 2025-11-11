package com.itsapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class DbConnect {

    private final String HOST; 
    private final String PORT; 
    private final String DATABASE; 
    private final String USERNAME; 
    private final String PASSWORD; 
    private final String DB_URL; 

    private Connection Conn;

    public DbConnect() {
        this.HOST = "localhost";
        this.PORT = "3306";
        this.DATABASE = "itsapp";
        this.USERNAME = "root";
        this.PASSWORD = "1234";
        this.DB_URL = "jdbc:mysql://" + this.HOST + ":" + this.PORT + "/" + this.DATABASE;
    }

    /**
     * attempts to initialize a connection with the database
     *
     *
     *
     * */
    public void connect() {
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

        if (conn != null) this.Conn = conn;
    }

    /**
     * Creates the tables 
     *
     *
     */
    public void createTables() {

    }
}
