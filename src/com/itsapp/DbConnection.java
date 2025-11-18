package com.itsapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
            PreparedStatement stmt = this.Conn.prepareStatement("SELECT * FROM `organization` WHERE org_id = " + id);
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
                    ORDER BY transaction_date DESC, transaction_id DESC
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
            
            // Checking student borrowing eligibility

            Boolean canBorrow = null;
            String queryA = """
                SELECT * FROM `student` WHERE student_id = %d;
                """.formatted(student_id);
            PreparedStatement stmtA = this.Conn.prepareStatement(queryA.strip());
            ResultSet resA = stmtA.executeQuery();
            resA.next();
            if (resA.getString("enrollment_status").equals("not enrolled"))
                canBorrow = false;

            String queryB = """
                SELECT * FROM `equipment_transaction_log` 
                    WHERE student_id = %d
                    ORDER BY transaction_date DESC, transaction_id DESC
                """.formatted(student_id);
            PreparedStatement stmtB = this.Conn.prepareStatement(queryB);
            ResultSet resB = stmtB.executeQuery();

            // checking borrow log for eligibility
            if (canBorrow == null) {
                // if student has no previous transactions: they can borrow
                if (!resB.next()) {
                    canBorrow=true;
                }
            } // separete this check in case this part becomes redundant due to the condition above...
            if (canBorrow == null) {
                // if student has last borrowed or broken something: they cannot borrow
                String brwStatus = resB.getString("status");
                if (brwStatus.equals("borrowed") || brwStatus.equals("broken"))
                    canBorrow=false;
                // inversely, if not borrowed or broken: they can borrow
                else
                    canBorrow=true;
            }

            if (!canBorrow)
                return "Operation failed! Given student is not elligible to borrow equipment at the moment.";

            // Check availability  
            // Checking if equipment is broken
            String availabilityQueryA = """
                SELECT * FROM equipment WHERE equipment_code = %d;
                """.formatted(equipment_code);
            PreparedStatement availStmtA = this.Conn.prepareStatement(availabilityQueryA);
            ResultSet availResA = availStmtA.executeQuery();
            availResA.next();

            if (availResA.getString("status").equals("broken"))
                return "Operation failed! The specified equipment is broken and cannot be borrowed at the moment.";

            // Checking if equipment is in use
            String availabilityQueryB = """
                SELECT * FROM equipment_transaction_log 
                    WHERE equipment_id = %d
                    ORDER BY transaction_date DESC, transaction_id DESC
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
                    (%d, %d, %d, NOW(), \"%s\", 'borrowed');
                """.formatted(student_id, equipment_code, lab_tech_id, remarks);
            PreparedStatement brwUpdStmt = this.Conn.prepareStatement(borrowUpdateQuery);
            int affected = brwUpdStmt.executeUpdate();

            // Get the transaction_id to display
            String getTIDQuery = """ 
                SELECT transaction_id FROM equipment_transaction_log
                    ORDER BY transaction_date DESC, transaction_id DESC
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
        return "Something went wrong... please try again.";
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

    /**
     * Implements the equipment return transaction.
     *
     *  @param student_id the student id
     *  @param equipment_code the equipment code
     *  @param lab_tech_id the attending lab_technician's id
     *  @param remarks remarks to add to the transaction.
     *  @param broken whether this equipment in this return is being marked
     *  as broken
     *
     *  @return a message to display in the application. Regardless of whether
     *  the message is an error message or not, it will be returned the same.
     */
    public String returnEquipment(int student_id, int equipment_code, int lab_tech_id, String remarks, boolean broken) {
        try {
            if (!this.isValidStudent(student_id))
                return "Operation failed! Given student_id is not a valid student.";

            if (!this.isValidEquipment(equipment_code))
                return "Operation failed! The given equipment code does not correspond to a valid piece of equipment.";

            // Checking if student can return an equipment 
            String queryA = """
                SELECT * FROM `equipment_transaction_log` 
                    WHERE student_id = %d
                    ORDER BY transaction_date DESC, transaction_id DESC
                """.formatted(student_id);
    
            PreparedStatement stmtA = this.Conn.prepareStatement(queryA);
            ResultSet resA = stmtA.executeQuery();
            Boolean canReturn = null;
            // if student has no previous transactions: they cannot return anything!
            if (!resA.next())
                canReturn = false;
            if (canReturn == null) {
                String brwStatus = resA.getString("status");
                if (brwStatus.equals("borrowed"))
                    canReturn = true;
                else    
                    canReturn = false;
            }
            else
                canReturn = false;

            
            if (!canReturn)
                return "Operation failed! Given student does not have the specified equipment to return.";
            
            // Actual 'returning' part of the operation
            String returnStatus = "returned";
            if (broken) {
                returnStatus = "broken";

                String brokenStmntStr = """ 
                        UPDATE `equipment` SET status = "broken" 
                            WHERE equipment_code = %d
                    """.formatted(equipment_code);
                PreparedStatement brokenStmt = this.Conn.prepareStatement(brokenStmntStr);
                brokenStmt.executeUpdate();

                // Update equipment record -> broken
                // Update equipment_transaction_log -> broken
            } 

            String returnUpdateQuery = """ 
                INSERT INTO `equipment_transaction_log` 
                    (student_id, equipment_id, labtech_id, transaction_date, remarks, status) VALUES
                    (%d, %d, %d, NOW(), "%s", "%s");
                """.formatted(student_id, equipment_code, lab_tech_id, remarks, returnStatus);
            PreparedStatement rtrnUpdStmt = this.Conn.prepareStatement(returnUpdateQuery);
            int affected = rtrnUpdStmt.executeUpdate();
            String getTIDQuery = """ 
                SELECT transaction_id FROM equipment_transaction_log
                    ORDER BY transaction_date DESC, transaction_id DESC
                """; 
            ResultSet TIDres = this.Conn.prepareStatement(getTIDQuery)
                .executeQuery();
            TIDres.next();
            int transactionID = TIDres.getInt("transaction_id");

            if (broken)
                return "Equipment has been marked as broken! The Student now cannot borrow until equipment has been replaced. (Transaction ID: %d)".formatted(transactionID);
            // Get the transaction_id to display
            return "Return operation completed! (Transaction ID: %d) (%d rows affected)".formatted(transactionID, affected);
            
        }

        catch (SQLException e) {
            e.printStackTrace();
        }
        return "Something went wrong... please try again.";
    }
    
    /**
     * Implementation of the return equipment transaction with no
     * remarks.
     *
     *  @param student_id the student id
     *  @param equipment_code the equipment code
     *  @param lab_tech_id the attending lab_technician's id
     *  @param remarks remarks to add to the transaction.
     *  @param broken whether this equipment in this return is being marked
     *
     */
    public String returnEquipment(int student_id, int equipment_code, int lab_tech_id, boolean broken) {
        return returnEquipment(student_id, equipment_code, lab_tech_id, "", broken);
    }

    public String replaceEquipment(int student_id, int lab_tech_id, String new_equipment_name, String new_equipment_desc) {
        try {
            // Viewing the student record to verify that they are a student enrolled in the system.
            if (!this.isValidStudent(student_id))
                return "Operation failed! Given student_id is not a valid student.";

            // Checking for previous incident
            // Viewing the equipment record to verify that the equipment to be replaced was indeed broken.
            // Viewing the borrowing log to verify that a previous incident with the student and the equipment had occurred. 
            String hasPrevIncdtQry = """ 
                SELECT equipment_id, status FROM equipment_transaction_log 
                    WHERE student_id = %d
                    ORDER BY transaction_date DESC, transaction_id DESC
                """.formatted(student_id);
            PreparedStatement hasPrevIncdtStmt = this.Conn.prepareStatement(hasPrevIncdtQry);
            ResultSet hasPrevIncdtRes = hasPrevIncdtStmt.executeQuery();
            Boolean hasPreviousIncident = null;

            // No entries
            if (!hasPrevIncdtRes.next())
                hasPreviousIncident = false;

            if (hasPreviousIncident == null) {
                // Checking if there is an incident (status is "broken") 
                String status = hasPrevIncdtRes.getString("status");
                if (status.equals("broken"))
                    hasPreviousIncident = true;
                else 
                    hasPreviousIncident = false;
            }

            if (!hasPreviousIncident)
                return "Operation failed! Student has no incident to resolve. There is no equipment that needs to be returned.";

            int brokenEquipmentId = hasPrevIncdtRes.getInt("equipment_id");
            
            // Getting new equipment id
            PreparedStatement equipmentCodeStmt = this.Conn.prepareStatement("SELECT equipment_code FROM equipment ORDER BY equipment_code DESC");
            ResultSet equipmentCodeRes = equipmentCodeStmt.executeQuery();

            // Handling an edge case
            if (!equipmentCodeRes.next())
                return "Something went wrong... it seems that there are no equipment in the equipment record.";

            int nextEquipmentCode = equipmentCodeRes.getInt("equipment_code") + 1;

            // Updating the equipment record to add the replacement equipment provided by the student.

            String updNewEquipmentQry = """ 
                INSERT INTO `equipment` 
                    (equipment_code, equipment_name, description, status) VALUES
                    (%d, "%s", "%s", "usable")
                """.formatted(nextEquipmentCode, 
                                new_equipment_name.substring(0, Math.min(20, new_equipment_name.length())), 
                                new_equipment_desc.substring(0, Math.min(100, new_equipment_desc.length())) );
           
            PreparedStatement updNewEquipmentStmt = this.Conn.prepareStatement(updNewEquipmentQry);
            int updNewEquipmentRes = updNewEquipmentStmt.executeUpdate();

            if (updNewEquipmentRes < 1)
                return "Something went wrong... new equipment could not be added.";

            // Updating the borrowing log to resolve the previous incident.
            String replacementUpdQuery = """ 
                INSERT INTO `equipment_transaction_log` 
                    (student_id, equipment_id, labtech_id, transaction_date, remarks, status) VALUES
                    (%d, %d, %d, NOW(), "%s", "%s");
                """.formatted(student_id, nextEquipmentCode, lab_tech_id, "Replacement for " + brokenEquipmentId, "replaced");
            PreparedStatement rplUpdStmt = this.Conn.prepareStatement(replacementUpdQuery);
            int affected = rplUpdStmt.executeUpdate();

            // Get the transaction_id to display
            String getTIDQuery = """ 
                SELECT transaction_id FROM equipment_transaction_log
                    ORDER BY transaction_date DESC, transaction_id DESC
                """; 
            ResultSet TIDres = this.Conn.prepareStatement(getTIDQuery)
                .executeQuery();
            TIDres.next();
            int transactionID = TIDres.getInt("transaction_id");
                return "Equipment replacement operation completed! (Transaction ID: %d) (%d rows affected)".formatted(transactionID, affected);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        // A default return
        return "Something went wrong...";
    }

    /* REPORT GENERATION */
   
    /** 
     * Generates a report object for report 1:
     * equipment borrowing history. This method implementation
     * allows to filter by equipment_id, start_date, and end_date.
     *  @return a Report object containing the generated report.
     */
    public Report reportEquipmentBorrowingHistory(Integer equipment_id, Date start_date, Date end_date) throws InvalidFields {
        try {
            Date date0 = new Date(0);
            if (equipment_id != null && !isValidEquipment(equipment_id))
                throw new InvalidFields("Provided equipment_id does not correspond to a valid equipment id.");
            if (!start_date.equals(date0) && !end_date.equals(date0) && start_date.getTime() - end_date.getTime() >= 0)
                throw new InvalidFields("Start time must be strictly before the end time.");

            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

            String query = """
                    SELECT 
                        e.equipment_code, 
                        e.equipment_name, 
                        s.student_id AS "borrower_id",
                        CONCAT(s.last_name, ", ", s.first_name) AS "borrower_name",
                        t.lab_tech_id AS "approver_id",
                        CONCAT(t.last_name, ", ", t.first_name) AS "approver_name",
                        etl.transaction_date AS "borrowed_on",
                        MIN(etl2.transaction_date) AS "returned_on"
                    FROM 
                        equipment_transaction_log etl
                    JOIN 
                        equipment e ON etl.equipment_id = e.equipment_code
                    JOIN 
                        student s ON etl.student_id = s.student_id 
                    JOIN 
                        lab_technician t ON etl.labtech_id =  t.lab_tech_id
                    LEFT JOIN 
                        equipment_transaction_log etl2
                        ON etl.equipment_id = etl2.equipment_id 
                        AND etl.student_id = etl2.student_id 
                        AND ( etl2.status = 'returned' OR etl2.status = "replaced")
                        AND etl2.transaction_date >= etl.transaction_date
                    WHERE 
                        etl.status = "borrowed" 
                        %s 
                        %s
                        %s
                    GROUP BY 
                        e.equipment_code, s.student_id
                    ORDER BY
                        etl.transaction_date DESC
                """;


            query = query.formatted(
                (equipment_id == null) ? "" : "AND etl.equipment_id=" + equipment_id,
                (start_date.equals(date0)) ? ""   : "AND etl.transaction_date >= '%s 00:00:00'".formatted(dateFormatter.format(start_date)),
                (end_date.equals(date0)) ? ""     : "AND etl.transaction_date <= '%s 23:59:59'".formatted(dateFormatter.format(end_date))
            );

            PreparedStatement stmt = this.Conn.prepareStatement(query);
            ResultSet res = stmt.executeQuery();
            return new Report(res);
        }            

        catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Report reportEquipmentBorrowingHistory(Integer equipment_id) throws InvalidFields {
        return reportEquipmentBorrowingHistory(equipment_id, new Date(0), new Date(0));
    }

    public Report reportEquipmentBorrowingHistory() throws InvalidFields {
        return reportEquipmentBorrowingHistory(null, new Date(0), new Date(0));
    }


    /** 
     * Generates a report object for report 2:
     * lab reservation history. This method implementation
     * allows to filter by lab_code, start_date, and end_date.
     *  @return a Report object containing the generated report.
     */
    public Report reportLabReservationHistory(Integer lab_code, Date start_date, Date end_date) throws InvalidFields {
        try {
            Date date0 = new Date(0);
            if (lab_code != null && !isValidLaboratory(lab_code))
                throw new InvalidFields("Provided equipment_id does not correspond to a valid equipment id.");
            if (!start_date.equals(date0) && !end_date.equals(date0) && start_date.getTime() - end_date.getTime() >= 0)
                throw new InvalidFields("Start time must be strictly before the end time.");

            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

            String query = """
                    SELECT 
                        l.lab_code,
                        l.lab_location,
                        lrl.reservation_date,
                        lrl.start_time,
                        lrl.end_time,
                        o.org_name AS "reserved_by",
                        s.student_id AS "org_rep_id",
                        CONCAT(s.last_name, ", ", s.first_name) AS "org_rep_name"
                    FROM
                        lab_reservation_log lrl
                    JOIN
                        organization o ON o.org_id = lrl.organization_id
                    JOIN 
                        student s ON s.student_id = lrl.student_rep_id
                    JOIN
                        laboratory l ON l.lab_code = lrl.laboratory_id
                    LEFT JOIN
                        ( SELECT * FROM lab_reservation_log WHERE status = "cancelled" ) lrl2
                        ON lrl.student_rep_id = lrl2.student_rep_id
                        AND lrl.organization_id = lrl2.organization_id
                        AND lrl.laboratory_id = lrl2.laboratory_id
                        AND lrl.reservation_date = lrl2.reservation_date
                        AND lrl.start_time = lrl2.start_time
                        AND lrl.end_time = lrl2.end_time
                    WHERE
                        lrl.status = "reserved"
                        AND lrl2.reservation_id IS NULL
                        %s
                        %s
                        %s
                    GROUP BY 
                        lrl.student_rep_id, lrl.organization_id, lrl.laboratory_id, lrl.reservation_date, lrl.start_time, lrl.end_time
                    ORDER BY
                        lrl.reservation_date 
                """;


            query = query.formatted(
                (lab_code == null) ? ""           : "AND l.lab_code=" + lab_code,
                (start_date.equals(date0)) ? ""   : "AND lrl.reservation_date >= '%s'".formatted(dateFormatter.format(start_date)),
                (end_date.equals(date0)) ? ""     : "AND lrl.reservation_date <= '%s'".formatted(dateFormatter.format(end_date))
            );

            PreparedStatement stmt = this.Conn.prepareStatement(query);
            ResultSet res = stmt.executeQuery();
            return new Report(res);
        }            

        catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Report reportLabReservationHistory(Integer lab_code) throws InvalidFields {
        return reportLabReservationHistory(lab_code, new Date(0), new Date(0));
    }
    public Report reportLabReservationHistory() throws InvalidFields {
        return reportLabReservationHistory(null, new Date(0), new Date(0));
    }


    /** 
     * Generates a report object for report 3:
     * student equipment borrowing history. This implementation allows
     * to set a null student ID if the report needs to generate for
     * all students.
     *  @return a Report object containing the generated report.
     */
    public Report reportStudentEquipmentBorrowHistory(Integer student_id) throws InvalidFields {
        if (student_id != null && !isValidStudent(student_id))
            throw new InvalidFields("Provided student_id does not correspond to a valid student_id.");

        try {
            String query = """
                    SELECT 
                        s.student_id,
                        CONCAT(s.last_name, ", ", s.first_name),
                        etl.transaction_id,
                        e.equipment_code, 
                        e.equipment_name, 
                        etl.transaction_date AS "borrowed_on",
                        MIN(etl2.transaction_date) AS "returned_on"
                    FROM 
                        equipment_transaction_log etl
                    JOIN 
                        equipment e ON etl.equipment_id = e.equipment_code
                    JOIN 
                        student s ON etl.student_id = s.student_id 
                    LEFT JOIN 
                        equipment_transaction_log etl2
                        ON etl.equipment_id = etl2.equipment_id 
                        AND etl.student_id = etl2.student_id 
                        AND ( etl2.status = 'returned' OR etl2.status = "replaced")
                        AND etl2.transaction_date >= etl.transaction_date
                    WHERE 
                        etl.status = "borrowed" 
                        %s
                    GROUP BY 
                        e.equipment_code, s.student_id
                    ORDER BY
                        etl.transaction_date DESC
                """;


            query = query.formatted(
                (student_id == null) ? "" : "AND etl.student_id=" + student_id
            );

            PreparedStatement stmt = this.Conn.prepareStatement(query);
            ResultSet res = stmt.executeQuery();
            return new Report(res);
        }            

        catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    /** 
     * Generates a report object for report 4:
     * organization lab reservation history This implementation allows
     * to set a null organization ID if the report needs to generate
     * for all organizations.
     *  @return a Report object containing the generated report.
     */
    public Report reportOrgLabReservationHistory(Integer org_id) throws InvalidFields {
        if (org_id != null && !isValidOrg(org_id))
            throw new InvalidFields("Provided org_id does not correspond to a valid org_id.");

        try {

            String query = """
                    SELECT 
                        o.org_name,
                        o.org_id,
                        s.student_id AS "org_rep_id",
                        CONCAT(s.last_name, ", ", s.first_name) AS "org_rep_name",
                        lrl.reservation_id,
                        l.lab_code,
                        l.lab_location,
                        lrl.reservation_date,
                        lrl.start_time,
                        lrl.end_time
                    FROM
                        lab_reservation_log lrl
                    JOIN
                        organization o ON o.org_id = lrl.organization_id
                    JOIN 
                        student s ON s.student_id = lrl.student_rep_id
                    JOIN
                        laboratory l ON l.lab_code = lrl.laboratory_id
                    LEFT JOIN
                        ( SELECT * FROM lab_reservation_log WHERE status = "cancelled" ) lrl2
                        ON lrl.student_rep_id = lrl2.student_rep_id
                        AND lrl.organization_id = lrl2.organization_id
                        AND lrl.laboratory_id = lrl2.laboratory_id
                        AND lrl.reservation_date = lrl2.reservation_date
                        AND lrl.start_time = lrl2.start_time
                        AND lrl.end_time = lrl2.end_time
                    WHERE
                        lrl.status = "reserved"
                        AND lrl2.reservation_id IS NULL
                        %s
                    GROUP BY 
                        lrl.student_rep_id, lrl.organization_id, lrl.laboratory_id, lrl.reservation_date, lrl.start_time, lrl.end_time
                    ORDER BY
                        lrl.reservation_id   
                """;

            query = query.formatted(
                (org_id == null) ? "" : "AND o.org_id=" + org_id
            );

            PreparedStatement stmt = this.Conn.prepareStatement(query);
            ResultSet res = stmt.executeQuery();
            return new Report(res);
        }            

        catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    /** 
     * Generates a report object for report 5:
     * lab technician transaction approval history. This implementation
     * allows to set a null labtech id if the report needs to generate
     * for all lab technicians.
     *  @return a Report object containing the generated report.
     */
    public Report reportLabTechApprovalHistory(Integer labtech_id) throws InvalidFields {
        if (labtech_id != null && !isValidLabTech(labtech_id))
            throw new InvalidFields("Provided labtech_id does not correspond to a valid labtech_id.");

        try {

            String query = """
                    WITH transactions AS ( 
                        SELECT 
                            etl.labtech_id, 
                            etl.transaction_id, 
                            etl.transaction_date,
                            "equipment_borrowing" AS "transaction_type",
                            IF(etl.status = "broken", CONCAT( CONCAT(s.first_name, " ", s.last_name) , " returned BROKEN ", e.equipment_name) ,
                            IF(etl.status = "replaced", CONCAT( CONCAT(s.first_name, " ", s.last_name) , " provided a REPLACEMENT ", e.equipment_name) ,
                            CONCAT( CONCAT(s.first_name, " ", s.last_name) , " ", UPPER(etl.status), " ", e.equipment_name) 
                            )) AS "summary"
                        FROM 
                            equipment_transaction_log etl
                        LEFT JOIN 
                            student s ON s.student_id = etl.student_id
                        LEFT JOIN
                            equipment e ON e.equipment_code = etl.equipment_id
                            
                        UNION

                        SELECT 
                            lrl.labtech_id,
                            lrl.reservation_id,
                            lrl.transaction_date,
                            "lab_reservation" AS "transaction_type",
                            IF (lrl.status = "reserved",
                                CONCAT(s.first_name, " ", s.last_name, " of ", o.org_name, " RESERVED ", l.lab_location, " (", lrl.start_time, " to ", lrl.end_time, ")"),
                                CONCAT(s.first_name, " ", s.last_name, " of ", o.org_name, " CANCELLED reservation of ", l.lab_location, " (", lrl.start_time, " to ", lrl.end_time, ")") 
                                ) AS SUMMARY
                        FROM
                            lab_reservation_log lrl
                        LEFT JOIN 
                            student s ON s.student_id = lrl.student_rep_id
                        LEFT JOIN
                            organization o ON o.org_id = lrl.organization_id
                        LEFT JOIN
                            laboratory l ON l.lab_code = lrl.laboratory_id 
                        )
                        
                    SELECT * FROM transactions
                        %s
                """;

            query = query.formatted(
                (labtech_id == null) ? "" : "WHERE labtech_id=" + labtech_id
            );

            System.out.println(query);

            PreparedStatement stmt = this.Conn.prepareStatement(query);
            ResultSet res = stmt.executeQuery();
            return new Report(res);
        }            

        catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
