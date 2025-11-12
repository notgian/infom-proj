package com.itsapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Represents an object to hold a connection to the database. 
 * It does not automatically connect to the database and the
 * connect method must be called for the connection to be established.
 *
 */
class DbConnect {

    private final String HOST; 
    private final String PORT; 
    private final String DATABASE; 
    private final String USERNAME; 
    private final String PASSWORD; 
    private final String DB_URL; 

    private Connection Conn = null;
     
    /**
     * Default and only constructor for this connection object.
     * Connection information is hardcoded here.
     */
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
     *  @return a boolean representing whether the connection was successful or not
     *
     * */
    public boolean connect() {
        System.out.println("Attempting to connect to database: " + DB_URL);
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

        if (conn != null) {
            this.Conn = conn;
            return true;
        }
        return false;
    }


    /**
     * Creates the tables 
     *
     *
     */
    public void createTables() {

    }
}
