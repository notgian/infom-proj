package com.itsapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represents an object to hold a connection to the database. 
 * It does not automatically connect to the database and the
 * connect method must be called for the connection to be established.
 *
 */
class DbConnection {

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
    public DbConnection() {
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

    public boolean isValidStudent(int id) {
        try {
            PreparedStatement stmt = this.Conn.prepareStatement("SELECT * FROM `student` WHERE student_id = " + id);
            ResultSet res = stmt.executeQuery();
            // returns true if there is a result in the query
            // i.e. there is a student with the id number provided
            return res.next();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        // a "default" return statement
        return false;
    }

    public boolean isValidEquipment(int id) {
        try {
            PreparedStatement stmt = this.Conn.prepareStatement("SELECT * FROM `equipment` WHERE equipment_code = " + id);
            ResultSet res = stmt.executeQuery();
            return res.next();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isValidLaboratory(int id) {
        try {
            PreparedStatement stmt = this.Conn.prepareStatement("SELECT * FROM `laboratory` WHERE lab_code = " + id);
            ResultSet res = stmt.executeQuery();
            return res.next();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isValidLabTech(int id) {
        try {
            PreparedStatement stmt = this.Conn.prepareStatement("SELECT * FROM `lab_technician` WHERE lab_tech_id = " + id);
            ResultSet res = stmt.executeQuery();
            return res.next();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isValidOrg(int id) {
        try {
            PreparedStatement stmt = this.Conn.prepareStatement("SELECT * FROM `organization` WHERE org_code = " + id);
            ResultSet res = stmt.executeQuery();
            return res.next();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Checks whether a student with the provided id is elligible to borrow 
     * equipment or not. 
     *
     * Assumes that the id is a valid id. Entering an invalid id number will
     * cause an unhandled error to be thrown.
     *
     * A student is elligible to borrow equipment if and only if the following
     * criteria are satisfied:
     * (1) They are not currently borrowing any equipment
     * (2) They do not have a previously broken equipment that has not been
     * replaced
     * (3) They are enrolled
     *
     *  @return whether the student is elligible to borrow equipment or not
     *  based on the mentioned criteria.
     */
    public boolean studentCanBorrow(int id) {
        try {
            // checking if student is enrolled
            String queryA = """
                SELECT * FROM `student` WHERE student_id = %d;
                """.formatted(id);
            PreparedStatement stmtA = this.Conn.prepareStatement(queryA.strip());
            ResultSet resA = stmtA.executeQuery();
            resA.next();
            if (resA.getString("enrollment_status").equals("not enrolled"))
                return false;

            // checking borrow log for eligibility
            String queryB = """
                SELECT * FROM `equipment_transaction_log` 
                    WHERE student_id = %d
                    ORDER BY transaction_date DESC 
                    LIMIT 1;
                """.formatted(id);
    
            PreparedStatement stmtB = this.Conn.prepareStatement(queryB);
            ResultSet resB = stmtB.executeQuery();
            // if student has no previous transactions: they can borrow
            if (!resB.next())
                return true;

            String brwStatus = resB.getString("status");
            if (brwStatus.equals("borrowed") || brwStatus.equals("broken"))
                return false;

            return true;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }



    /**
     * Implementation of the borrow equipment transaction.
     *
     *  @param student_id the student id
     *  @param equipment_code the equipment code
     *  @param lab_tech_id the attending lab_technician's id
     *  @param remarks remarks to add to the transaction.
     *
     *  @return a message to display in the application. Regardless of whether
     *  the message is an error message or not, it will be returned the same.
     */
    public String borrowEquipment(int student_id, int equipment_code, int lab_tech_id, String remarks) {
        // Viewing the student’s record to verify that there is no pending borrowed equipment .
        // Viewing the record of the equipment to verify its availability. 

        try {
            if (!this.isValidStudent(student_id))
                return "Operation failed! Given student_id is not a valid student.";

            if (!this.isValidEquipment(equipment_code))
                return "Operation failed! The given equipment code does not correspond to a valid piece of equipment.";

            if (!this.studentCanBorrow(student_id))
                return "Operation failed! Given student is not elligible to borrow equipment at the moment.";

            // Check availability  
            // Checking if equipment is broken
            String availabilityQueryA = """
                SELECT * FROM equipment WHERE equipment_code = %d;
                """.formatted(equipment_code);
            PreparedStatement availStmtA = this.Conn.prepareStatement(availabilityQueryA);
            ResultSet availResA = availStmtA.executeQuery();
            availResA.next();

            if (availResA.getString("status") == "broken")
                return "Operation failed! The specified equipment is broken and cannot be borrowed at the moment";

            // Checking if equipment is in use
            String availabilityQueryB = """
                SELECT * FROM equipment_transaction_log 
                    WHERE equipment_id = %d
                    ORDER BY transaction_date DESC
                    LIMIT 1;
                """.formatted(equipment_code);
            PreparedStatement availStmtB = this.Conn.prepareStatement(availabilityQueryB);
            ResultSet availResB = availStmtB.executeQuery();
            if (availResB.next()) {
                String availResBStatus = availResB.getString("status");
                if (availResBStatus.equals("borrowed"))
                    return "Operation failed! Equipment is currently being borrowed!";
            }

            // Actual borrowing 
            // Recording the transaction into the Equipment Borrowing Log.
            
            String borrowUpdateQuery = """ 
                INSERT INTO `equipment_transaction_log` 
                    (student_id, equipment_id, labtech_id, transaction_date, remarks, status) VALUES
                    (%d, %d, %d, CURRENT_DATE(), \"%s\", 'borrowed');
                """.formatted(student_id, equipment_code, lab_tech_id, remarks);
            System.out.println(borrowUpdateQuery);
            PreparedStatement brwUpdStmt = this.Conn.prepareStatement(borrowUpdateQuery);
            int affected = brwUpdStmt.executeUpdate();

            // Get the transaction_id to display
            String getTIDQuery = """ 
                SELECT transaction_id FROM equipment_transaction_log
                    ORDER BY transaction_date DESC
                    LIMIT 1
                """; 
            ResultSet TIDres = this.Conn.prepareStatement(getTIDQuery)
                .executeQuery();
            TIDres.next();
            int transactionID = TIDres.getInt("transaction_id");
            return "Borrowing operation completed! (Transaction ID: %d) (%d rows affected)".formatted(transactionID, affected);
            
        }
        catch (SQLException e) {
            e.printStackTrace();
        } 
        
        // A default return message
        return "Something went wrong...";
    }

    /**
     * For doing the borrow equipment operation with no remarks.
     *
     *  @param student_id the student id
     *  @param equipment_code the equipment code
     *  @param lab_tech_id the attending lab_technician's id
     *
     *  @return a message to display in the application. Regardless of whether
     *  the message is an error message or not, it will be returned the same.
     */
    public String borrowEquipment(int student_id, int equipment_code, int lab_tech_id) {
        return borrowEquipment(student_id, equipment_code, lab_tech_id, "");
    }
}
