package com.itsapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;

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

    /**
     * Checks whether an organization is eligible to reserve a lab at the specified date and time.
     *
     * Rules enforced:
     * - Student representative must be a member of the organization and hold an officer position
     * - Reservation must be made at least 72 hours before the scheduled start time
     * - End time must be later than start time
     * - Laboratory must not already be reserved during the specified timeslot
     *
     * @param org_id the organization ID
     * @param lab_code the lab to be reserved
     * @param reservation_date the date of reservation (YYYY-MM-DD)
     * @param start_time the start time (HH:MM:SS)
     * @param end_time the end time (HH:MM:SS)
     *
     * @return "Eligible to reserve." if eligible, or a specific failure reason string
     */
    public String orgCanReserve(int student_rep_id, int org_id, int lab_code, String reservation_date, String start_time, String end_time) {
        // Validate organization and laboratory IDs
        if (!this.isValidOrg(org_id))
            return "Invalid organization ID.";
        if (!this.isValidLaboratory(lab_code))
            return "Invalid laboratory ID.";

        // Check reservation timing
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reservationStart = LocalDateTime.parse(reservation_date + "T" + start_time);
        LocalDateTime reservationEnd = LocalDateTime.parse(reservation_date + "T" + end_time);

        if (Duration.between(now, reservationStart).toHours() < 72)
            return "Reservation must be made at least 72 hours in advance.";

        if (!reservationStart.isBefore(reservationEnd))
            return "End time must be later than start time.";

        // Check laboratory availability
        String availability = isLabAvailable(lab_code, reservation_date, start_time, end_time);
        if (!availability.equals("Available for reservation."))
            return availability;

        // Check student representative's membership and position
        try {
            String membershipQuery = """
                SELECT position FROM org_students
                WHERE student_id = %d AND org_id = %d
                """.formatted(student_rep_id, org_id);

            PreparedStatement membershipStmt = this.Conn.prepareStatement(membershipQuery);
            ResultSet membershipRes = membershipStmt.executeQuery();
            if (!membershipRes.next())
                return "Student representative is not a member of the organization.";

            String position = membershipRes.getString("position");
            if (position.equalsIgnoreCase("member"))
                return "Student representative must be an officer to reserve the lab.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error while checking membership.";
        }

        return "Eligible to reserve."; // All checks passed
    }

    /**
     * Checks whether the specified lab is available during the given date and time.
     * 
     * Conflicts are checked against:
     * - Existing reservations in lab_reservation_log with status = 'reserved'
     * - Scheduled classes or activities in lab_schedule
     *
     * @param lab_code the laboratory code
     * @param reservation_date the reservation date (format: YYYY-MM-DD)
     * @param start_time the start time (format: HH:MM:SS)
     * @param end_time the end time (format: HH:MM:SS)
     * @return "Available for reservation." if available, or a specific conflict reason string
     */
    public String isLabAvailable(int laboratory_id, String reservation_date, String start_time, String end_time) {
        try {
            // Check reservation log
            String reservationConflictQuery = """
                SELECT * FROM lab_reservation_log
                WHERE laboratory_id = %d AND reservation_date = '%s'
                AND (
                    (start_time <= '%s' AND end_time > '%s') OR
                    (start_time < '%s' AND end_time >= '%s') OR
                    (start_time >= '%s' AND end_time <= '%s')
                ) AND status = 'reserved'
                """.formatted(laboratory_id, reservation_date, start_time, start_time, end_time, end_time, start_time, end_time);

            PreparedStatement reservationStmt = this.Conn.prepareStatement(reservationConflictQuery);
            ResultSet reservationRes = reservationStmt.executeQuery();
            if (reservationRes.next())
                return "This laboratory is already reserved during the specified timeslot.";

            // Check class schedule
            String dayCode = switch (LocalDate.parse(reservation_date).getDayOfWeek()) {
            case MONDAY -> "M";
            case TUESDAY -> "T";
            case WEDNESDAY -> "W";
            case THURSDAY -> "H";
            case FRIDAY -> "F";
            case SATURDAY -> "S";
            case SUNDAY -> "U";
            };

            String scheduleConflictQuery = """
                SELECT * FROM lab_class_schedule
                WHERE laboratory_id = %d AND day = '%s'
                AND (
                    (start_time <= '%s' AND end_time > '%s') OR
                    (start_time < '%s' AND end_time >= '%s') OR
                    (start_time >= '%s' AND end_time <= '%s')
                )
                """.formatted(laboratory_id, dayCode, start_time, start_time, end_time, end_time, start_time, end_time);

            PreparedStatement scheduleStmt = this.Conn.prepareStatement(scheduleConflictQuery);
            ResultSet scheduleRes = scheduleStmt.executeQuery();
            if (scheduleRes.next())
                return "This laboratory is scheduled for a class during the specified timeslot.";

            return "Available for reservation."; // No conflicts found
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error while checking laboratory availability.";
        }
    }

    /**
     * Implementation of the reserve laboratory transaction.
     *
     * @param org_id the ID of the requesting organization
     * @param lab_code the code of the laboratory to be reserved
     * @param lab_tech_id the ID of the lab technician processing the reservation
     * @param reservation_date the date of the reservation (format: YYYY-MM-DD)
     * @param start_time the start time of the reservation (format: HH:MM:SS)
     * @param end_time the end time of the reservation (format: HH:MM:SS)
     * @param remarks optional remarks or purpose for the reservation
     * 
     * @return a confirmation message if successful, or an error message if the reservation fails
     */
    public String reserveLab(int student_rep_id, int org_id, int lab_code, int lab_tech_id, String reservation_date, String start_time, String end_time, String remarks) {
        try {
            // Step 1: Check reservation eligibility using orgCanReserve()
            String eligibility = orgCanReserve(student_rep_id, org_id, lab_code, reservation_date, start_time, end_time);

            if (!eligibility.equals("Eligible to reserve."))
                return "Operation failed! " + eligibility;

            // Step 2: Record the reservation transaction
            String reserveQuery = """
                INSERT INTO lab_reservation_log
                (student_rep_id, organization_id, laboratory_id, labtech_id, transaction_date, reservation_date, start_time, end_time, remarks, status)
                VALUES (%d, %d, %d, %d, NOW(), '%s', '%s', '%s', '%s', 'reserved')
                """.formatted(student_rep_id, org_id, lab_code, lab_tech_id, reservation_date, start_time, end_time, remarks);

            PreparedStatement reserveStmt = this.Conn.prepareStatement(reserveQuery);
            int affected = reserveStmt.executeUpdate();

            // Step 3: Retrieve the reservation ID for confirmation
            String getRIDQuery = """
                SELECT reservation_id FROM lab_reservation_log
                ORDER BY transaction_date DESC, reservation_id DESC
                """;
            ResultSet RIDres = this.Conn.prepareStatement(getRIDQuery).executeQuery();
            RIDres.next();
            int reservationID = RIDres.getInt("reservation_id");

            return "Reservation completed! (Reservation ID: %d) (%d rows affected)".formatted(reservationID, affected);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "Something went wrong... please try again.";
    }

    /**
     * Implementation of the reserve laboratory transaction with no remarks.
     *
     * @param org_id the organization ID
     * @param lab_code the laboratory code
     * @param lab_tech_id the attending lab technician's ID
     * @param reservation_date the date of the reservation (format: YYYY-MM-DD)
     * @param start_time the start time of the reservation (format: HH:MM:SS)
     * @param end_time the end time of the reservation (format: HH:MM:SS)
     *
     * @return a confirmation message if successful, or an error message if the reservation fails
     */
    public String reserveLab(int student_rep_id, int org_id, int lab_code, int lab_tech_id, String reservation_date, String start_time, String end_time) {
        return reserveLab(student_rep_id, org_id, lab_code, lab_tech_id, reservation_date, start_time, end_time, "");
    }

    /**
     * Implementation of the cancel laboratory reservation transaction.
     *
     * @param reservation_id the ID of the reservation to cancel
     * @param lab_tech_id the ID of the lab technician processing the cancellation
     * @param remarks optional remarks explaining the reason for cancellation
     * 
     * @return a confirmation message if successful, or an error message if the cancellation fails
     */
    public String cancelLabReservation(int reservation_id, int lab_tech_id, String remarks) {
        try {
            // Retrieve reservation details
            String checkQuery = """
                SELECT reservation_date, start_time, end_time
                FROM lab_reservation_log
                WHERE reservation_id = %d AND status = 'reserved'
                """.formatted(reservation_id);
            PreparedStatement checkStmt = this.Conn.prepareStatement(checkQuery);
            ResultSet res = checkStmt.executeQuery();

            if (!res.next())
                return "Operation failed! No active reservation found with the given ID.";

            // Parse reservation timing
            LocalDate reservationDate = LocalDate.parse(res.getString("reservation_date"));
            LocalTime startTime = LocalTime.parse(res.getString("start_time"));
            LocalTime endTime = LocalTime.parse(res.getString("end_time"));
            LocalDateTime reservationStart = LocalDateTime.of(reservationDate, startTime);
            LocalDateTime reservationEnd = LocalDateTime.of(reservationDate, endTime);
            LocalDateTime now = LocalDateTime.now();

            // Enforce cancellation rules
            if (!now.isBefore(reservationStart))
                return "Operation failed! Cannot cancel during the reserved timeslot.";

            long minutesUntilStart = Duration.between(now, reservationStart).toMinutes();
            if (minutesUntilStart < 1440)
                return "Operation failed! Cancellations must be made at least 24 hours in advance.";

            // Record the cancellation if rules are satisfied
            String cancelQuery = """
                UPDATE lab_reservation_log
                SET status = 'cancelled',
                    remarks = CONCAT(remarks, " | Cancelled: %s")
                WHERE reservation_id = %d
                """.formatted(remarks, reservation_id);
            PreparedStatement cancelStmt = this.Conn.prepareStatement(cancelQuery);
            int affected = cancelStmt.executeUpdate();

            return "Cancellation completed! (%d rows affected)".formatted(affected);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "Something went wrong... please try again.";
    }

    /**
     * Implementation of the cancel laboratory reservation transaction with no remarks.
     *
     * @param reservation_id the reservation ID to cancel
     * @param lab_tech_id the attending lab technician's ID
     *
     * @return a confirmation message if successful, or an error message if the cancellation fails
     */
    public String cancelLabReservation(int reservation_id, int lab_tech_id) {
        return cancelLabReservation(reservation_id, lab_tech_id, "");
    }
}
