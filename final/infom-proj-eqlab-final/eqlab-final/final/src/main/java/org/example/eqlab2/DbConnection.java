package org.example.eqlab2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.ArrayList;

/**
 * Represents an object to hold a connection to the database. 
 * It does not automatically connect to the database and the
 * connect method must be called for the connection to be established.
 *
 */
class DbConnection {

    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DATABASE = "itsapp";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "1234";
    private static final String DB_URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE;

    private static Connection Conn = null;
    private static Integer lab_tech_id = null;

    /**
     * attempts to initialize a connection with the database
     *  @return a boolean representing whether the connection was successful or not
     *
     * */
    public static boolean connect() {
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
            Conn = conn;
            return true;
        }
        return false;
    }

    /** LABTECH STUFF **/

    public static void setLabTechID(int id) {
        lab_tech_id = id;
    }

    public static Integer getLabTechID() {
        return lab_tech_id;
    }

    public static void clearLabTechID() {
        lab_tech_id = null;
    }

    /** GET FUNCTIONS START **/

    public static String[] getEquipment() {
        try {
            ResultSet equipRes = Conn.prepareStatement("""
                        SELECT equipment_code, equipment_name FROM equipment
                    """).executeQuery();
           ArrayList<String> equipmentList = new ArrayList<>();
           while (equipRes.next()) {
                String equipment = "[%d] %s".formatted(equipRes.getInt("equipment_code"), equipRes.getString("equipment_name"));
                equipmentList.add(equipment);
           }
            return equipmentList.toArray(new String[equipmentList.size()]);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String[] getLabs() {
        try {
            ResultSet labRes = Conn.prepareStatement("""
                        SELECT lab_code, lab_location FROM laboratory
                    """).executeQuery();
           ArrayList<String> labList = new ArrayList<>();
           while (labRes.next()) {
                String lab = "[%d] %s".formatted(labRes.getInt("lab_code"), labRes.getString("lab_location"));
                labList.add(lab);
           }
            return labList.toArray(new String[labList.size()]);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String[] getOrgs() {
        try {
            ResultSet orgRes = Conn.prepareStatement("""
                        SELECT org_id, org_name FROM organization
                    """).executeQuery();
           ArrayList<String> orgList = new ArrayList<>();
           while (orgRes.next()) {
                String org = "[%d] %s".formatted(orgRes.getInt("org_id"), orgRes.getString("org_name"));
                orgList.add(org);
           }
            return orgList.toArray(new String[orgList.size()]);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    /** UTIL FUNCTIONS START **/

    public static boolean isValidStudent(int id) {
        try {
            PreparedStatement stmt = Conn.prepareStatement("SELECT * FROM student WHERE student_id = " + id);
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

    public static boolean isValidEquipment(int id) {
        try {
            PreparedStatement stmt = Conn.prepareStatement("SELECT * FROM equipment WHERE equipment_code = " + id);
            ResultSet res = stmt.executeQuery();
            return res.next();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isValidLaboratory(int id) {
        try {
            PreparedStatement stmt = Conn.prepareStatement("SELECT * FROM laboratory WHERE lab_code = " + id);
            ResultSet res = stmt.executeQuery();
            return res.next();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isValidLabTech(int id) {
        try {
            PreparedStatement stmt = Conn.prepareStatement("SELECT * FROM lab_technician WHERE lab_tech_id = " + id);
            ResultSet res = stmt.executeQuery();
            return res.next();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isValidOrg(int id) {
        try {
            PreparedStatement stmt = Conn.prepareStatement("SELECT * FROM organization WHERE org_id = " + id);
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
    public static boolean studentCanBorrow(int id) {
        try {
            // checking if student is enrolled
            String queryA = """
                SELECT * FROM `student` WHERE student_id = %d;
            """.formatted(id);
            PreparedStatement stmtA = Conn.prepareStatement(queryA.strip());
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

            PreparedStatement stmtB = Conn.prepareStatement(queryB);
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

    /** TRANSACTIONS START HERE **/

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
    public static DbReturn borrowEquipment(int student_id, int equipment_code, int lab_tech_id, String remarks) {
        // Viewing the student’s record to verify that there is no pending borrowed equipment .
        // Viewing the record of the equipment to verify its availability. 

        try {
            if (!isValidStudent(student_id))
                return new DbReturn("ERROR", "Invalid Fields.", "The given student ID does not correspond to a valid student.");

            if (!isValidEquipment(equipment_code))
                return new DbReturn("ERROR", "Invalid Fields.", "The given equipment code does not correspond to a valid piece of equipment.");
            // Checking student borrowing eligibility

            Boolean canBorrow = null;
            String queryA = """
                SELECT * FROM `student` WHERE student_id = %d;
            """.formatted(student_id);
            PreparedStatement stmtA = Conn.prepareStatement(queryA.strip());
            ResultSet resA = stmtA.executeQuery();
            resA.next();
            if (resA.getString("enrollment_status").equals("not enrolled"))
                canBorrow = false;

            String queryB = """
                SELECT * FROM `equipment_transaction_log` 
                WHERE student_id = %d
                ORDER BY transaction_date DESC, transaction_id DESC
                """.formatted(student_id);
            PreparedStatement stmtB = Conn.prepareStatement(queryB);
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
                return new DbReturn("WARN", "Operation failed.", "Given student is not elligible to borrow equipment at the moment.");

            // Check availability  
            // Checking if equipment is broken
            String availabilityQueryA = """
                SELECT * FROM equipment WHERE equipment_code = %d;
            """.formatted(equipment_code);
            PreparedStatement availStmtA = Conn.prepareStatement(availabilityQueryA);
            ResultSet availResA = availStmtA.executeQuery();
            availResA.next();

            if (availResA.getString("status").equals("broken"))
                return new DbReturn("WARN", "Operation failed.", "The specified equipment is broken and cannot be borrowed at the moment.");

            // Checking if equipment is in use
            String availabilityQueryB = """
                SELECT * FROM equipment_transaction_log 
                WHERE equipment_id = %d
                ORDER BY transaction_date DESC, transaction_id DESC
                """.formatted(equipment_code);
            PreparedStatement availStmtB = Conn.prepareStatement(availabilityQueryB);
            ResultSet availResB = availStmtB.executeQuery();
            if (availResB.next()) {
                String availResBStatus = availResB.getString("status");
                if (availResBStatus.equals("borrowed"))
                    return new DbReturn("WARN", "Operation failed.", "Equipment is currently being borrowed.");
            }

            // Actual borrowing 
            // Recording the transaction into the Equipment Borrowing Log.

            String borrowUpdateQuery = """ 
                INSERT INTO `equipment_transaction_log` 
                (student_id, equipment_id, labtech_id, transaction_date, remarks, status) VALUES
                (%d, %d, %d, NOW(), \"%s\", 'borrowed');
            """.formatted(student_id, equipment_code, lab_tech_id, remarks);
            PreparedStatement brwUpdStmt = Conn.prepareStatement(borrowUpdateQuery);
            int affected = brwUpdStmt.executeUpdate();

            // Get the transaction_id to display
            String getTIDQuery = """ 
                SELECT etl.transaction_id, e.equipment_name 
                FROM equipment_transaction_log etl
                JOIN equipment e ON e.equipment_code = etl.equipment_id
                ORDER BY etl.transaction_date DESC, etl.transaction_id DESC
                """; 
                ResultSet TIDres = Conn.prepareStatement(getTIDQuery)
                .executeQuery();
            TIDres.next();
            int transactionID = TIDres.getInt("transaction_id");
            String equipmentName = TIDres.getString("equipment_name");
            return new DbReturn("INFO", 
                    "Borrowing successful.",
                    "\n\nTransaction ID: %d\nStudent ID: %d\nEquipment Name: %s".formatted(transactionID, student_id, equipmentName)
                    );

        }
        catch (SQLException e) {
            e.printStackTrace();
        } 

        // A default return message
        return new DbReturn("WARN", "???", "Something went wrong... please try again");
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
    public static DbReturn borrowEquipment(int student_id, int equipment_code, int lab_tech_id) {
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
    public static DbReturn returnEquipment(int student_id, int equipment_code, int lab_tech_id, String remarks, boolean broken) {
        try {

            if (!isValidStudent(student_id))
                return new DbReturn("ERROR", "Invalid Fields.", "The given student ID does not correspond to a valid student.");

            if (!isValidEquipment(equipment_code))
                return new DbReturn("ERROR", "Invalid Fields.", "The given equipment code does not correspond to a valid piece of equipment.");

            // Checking if student can return an equipment 
            String queryA = """
                SELECT * FROM `equipment_transaction_log` 
                WHERE student_id = %d
                ORDER BY transaction_date DESC, transaction_id DESC
                """.formatted(student_id);

            PreparedStatement stmtA = Conn.prepareStatement(queryA);
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
                return new DbReturn("WARN", "Operation failed.", "Given student does not have the specified equipment to return.");

            // Actual 'returning' part of the operation
            String returnStatus = "returned";
            if (broken) {
                returnStatus = "broken";

                String brokenStmntStr = """ 
                    UPDATE `equipment` SET status = "broken" 
                    WHERE equipment_code = %d
                    """.formatted(equipment_code);
                PreparedStatement brokenStmt = Conn.prepareStatement(brokenStmntStr);
                brokenStmt.executeUpdate();

                // Update equipment record -> broken
                // Update equipment_transaction_log -> broken
            } 

            String returnUpdateQuery = """ 
                INSERT INTO `equipment_transaction_log` 
                (student_id, equipment_id, labtech_id, transaction_date, remarks, status) VALUES
                (%d, %d, %d, NOW(), "%s", "%s");
            """.formatted(student_id, equipment_code, lab_tech_id, remarks, returnStatus);
            PreparedStatement rtrnUpdStmt = Conn.prepareStatement(returnUpdateQuery);
            int affected = rtrnUpdStmt.executeUpdate();
            String getTIDQuery = """ 
                SELECT etl.transaction_id, e.equipment_name 
                FROM equipment_transaction_log etl
                JOIN equipment e ON e.equipment_code = etl.equipment_id
                ORDER BY etl.transaction_date DESC, etl.transaction_id DESC
                """; 
                ResultSet TIDres = Conn.prepareStatement(getTIDQuery)
                .executeQuery();
            TIDres.next();
            int transactionID = TIDres.getInt("transaction_id");
            String equipmentName = TIDres.getString("equipment_name");

            if (broken) {
                return new DbReturn(
                        "INFO", 
                        "Broken equipment.", 
                        "Equipment has been marked as broken! The student now cannot borrow until equipment has been replaced.\n\nTransaction ID: %d\nStudent_ID: %d\nEquipment: %s"
                        .formatted(transactionID, student_id, equipmentName)
                        );
            }

            // Get the transaction_id to display
            return new DbReturn(
                    "INFO",
                    "Return successful.",
                    "Return operation completed!\n\nTransaction ID: %d\nStudent ID: \nEquipment: %s"
                    .formatted(transactionID, student_id, equipmentName)
                    );
        }

        catch (SQLException e) {
            e.printStackTrace();
        }

        return new DbReturn("WARN", "???", "Something went wrong... please try again");
    }

    /**
     * Implementation of the return equipment transaction with no
     * remarks.
     *
     *  @param student_id the student id
     *  @param equipment_code the equipment code
     *  @param lab_tech_id the attending lab_technician's id
     *  @param broken whether this equipment in this return is being marked
     *
     */
    public static DbReturn returnEquipment(int student_id, int equipment_code, int lab_tech_id, boolean broken) {
        return returnEquipment(student_id, equipment_code, lab_tech_id, "", broken);
    }

    public static DbReturn replaceEquipment(int student_id, int lab_tech_id, String new_equipment_name, String new_equipment_desc) {
        try {
            // Viewing the student record to verify that they are a student enrolled in the system.
            if (!isValidStudent(student_id))
                return new DbReturn("ERROR", "Invalid Fields.", "The given student ID does not correspond to a valid student.");

            // Checking for previous incident
            // Viewing the equipment record to verify that the equipment to be replaced was indeed broken.
            // Viewing the borrowing log to verify that a previous incident with the student and the equipment had occurred. 
            String hasPrevIncdtQry = """ 
                SELECT equipment_id, status FROM equipment_transaction_log 
                WHERE student_id = %d
                ORDER BY transaction_date DESC, transaction_id DESC
                """.formatted(student_id);
            PreparedStatement hasPrevIncdtStmt = Conn.prepareStatement(hasPrevIncdtQry);
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
                return new DbReturn("WARN", "Operation failed.", "Student has no incident to resolve. There is no equipment that needs to be replaced.");

            int brokenEquipmentId = hasPrevIncdtRes.getInt("equipment_id");

            // Getting new equipment id
            PreparedStatement equipmentCodeStmt = Conn.prepareStatement("SELECT equipment_code FROM equipment ORDER BY equipment_code DESC");
            ResultSet equipmentCodeRes = equipmentCodeStmt.executeQuery();

            // Handling an edge case
            if (!equipmentCodeRes.next())
                return new DbReturn("WARN", "Something went wrong.", "It seems that there are no equipment in the equipment record");

            int nextEquipmentCode = equipmentCodeRes.getInt("equipment_code") + 1;

            // Updating the equipment record to add the replacement equipment provided by the student.

            String updNewEquipmentQry = """ 
                INSERT INTO `equipment` 
                (equipment_code, equipment_name, description, status) VALUES
                (%d, "%s", "%s", "usable")
                """.formatted(nextEquipmentCode, 
                new_equipment_name.substring(0, Math.min(20, new_equipment_name.length())), 
                new_equipment_desc.substring(0, Math.min(100, new_equipment_desc.length())) );

            PreparedStatement updNewEquipmentStmt = Conn.prepareStatement(updNewEquipmentQry);
            int updNewEquipmentRes = updNewEquipmentStmt.executeUpdate();

            if (updNewEquipmentRes < 1)
                return new DbReturn("WARN", "Something went wrong.", "New equipment could not be added.");

            // Updating the borrowing log to resolve the previous incident.
            String replacementUpdQuery = """ 
                INSERT INTO `equipment_transaction_log` 
                (student_id, equipment_id, labtech_id, transaction_date, remarks, status) VALUES
                (%d, %d, %d, NOW(), "%s", "%s");
            """.formatted(student_id, nextEquipmentCode, lab_tech_id, "Replacement for " + brokenEquipmentId, "replaced");
            PreparedStatement rplUpdStmt = Conn.prepareStatement(replacementUpdQuery);
            int affected = rplUpdStmt.executeUpdate();

            // Get the transaction_id to display
            String getTIDQuery = """ 
                SELECT transaction_id FROM equipment_transaction_log
                ORDER BY transaction_date DESC, transaction_id DESC
                """; 
                ResultSet TIDres = Conn.prepareStatement(getTIDQuery)
                .executeQuery();
            TIDres.next();
            int transactionID = TIDres.getInt("transaction_id");

            return new DbReturn("INFO",
                    "Replacement success.",
                    "Equipment replacement operation completed!\n\nTransaction ID: %d\nStudent ID: %d\nNew Equipment ID: %d\nNew Equipment Name: %s"
                    .formatted(transactionID, student_id, nextEquipmentCode, new_equipment_name)
                    );
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        // A default return
        return new DbReturn("WARN", "???", "Something went wrong... please try again");
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
    public static String orgCanReserve(int student_rep_id, int org_id, int lab_code, String reservation_date, String start_time, String end_time) {
        // Validate organization and laboratory IDs
        if (!isValidOrg(org_id))
            return "Invalid organization ID.";
        if (!isValidLaboratory(lab_code))
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

            PreparedStatement membershipStmt = Conn.prepareStatement(membershipQuery);
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
    public static String isLabAvailable(int laboratory_id, String reservation_date, String start_time, String end_time) {
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

            PreparedStatement reservationStmt = Conn.prepareStatement(reservationConflictQuery);
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

            PreparedStatement scheduleStmt = Conn.prepareStatement(scheduleConflictQuery);
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
    public static DbReturn reserveLab(int student_rep_id, int org_id, int lab_code, int lab_tech_id, String reservation_date, String start_time, String end_time, String remarks) {
        try {
            // Step 1: Check reservation eligibility using orgCanReserve()
            String eligibility = orgCanReserve(student_rep_id, org_id, lab_code, reservation_date, start_time, end_time);

            if (!eligibility.equals("Eligible to reserve."))
                return new DbReturn("WARN", "Operation failed.", eligibility);

            // Step 2: Record the reservation transaction
            String reserveQuery = """
                INSERT INTO lab_reservation_log
                (student_rep_id, organization_id, laboratory_id, labtech_id, transaction_date, reservation_date, start_time, end_time, remarks, status)
                VALUES (%d, %d, %d, %d, NOW(), '%s', '%s', '%s', '%s', 'reserved')
                """.formatted(student_rep_id, org_id, lab_code, lab_tech_id, reservation_date, start_time, end_time, remarks);

            PreparedStatement reserveStmt = Conn.prepareStatement(reserveQuery);
            int affected = reserveStmt.executeUpdate();

            // Step 3: Retrieve the reservation ID for confirmation
            String getRIDQuery = """
                SELECT 
                lrl.reservation_id, 
                o.org_name, 
                l.lab_location, 
                lrl.reservation_date, 
                lrl.start_time,
                lrl.end_time
                    FROM 
                    lab_reservation_log lrl
                    LEFT JOIN 
                    organization o ON o.org_id = lrl.organization_id
                    LEFT JOIN 
                    laboratory l ON l.lab_code=lrl.laboratory_id
                    ORDER BY lrl.transaction_date DESC, lrl.reservation_id DESC
                    """;
            ResultSet RIDres = Conn.prepareStatement(getRIDQuery).executeQuery();
            RIDres.next();
            int reservationID = RIDres.getInt("reservation_id");
            String orgName = RIDres.getString("org_name");
            String labName = RIDres.getString("lab_location");
            String date = RIDres.getString("reservation_date");
            String timeStart = RIDres.getString("start_time");
            String timeEnd = RIDres.getString("end_time");

            SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat timeFmt = new SimpleDateFormat("kk:ss");

            String message = "Reservation successful.\n\nReservation ID: %d\nOrganization: %s\nLaboratory: %s\nDate: %s\nTime Start: %s\nTime End: %s".
                formatted(reservationID, orgName, labName, date, timeStart, timeEnd);

            return new DbReturn(
                    "INFO",
                    "Reserve success.",
                    message
                    );
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new DbReturn("WARN", "???", "Something went wrong... please try again");

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
    public static DbReturn reserveLab(int student_rep_id, int org_id, int lab_code, int lab_tech_id, String reservation_date, String start_time, String end_time) {
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
    public static DbReturn cancelLabReservation(int reservation_id, int lab_tech_id, String remarks) {
        try {
            // Retrieve reservation details
            String checkQuery = """
                SELECT reservation_date, start_time, end_time
                FROM lab_reservation_log
                WHERE reservation_id = %d AND status = 'reserved'
                """.formatted(reservation_id);
            PreparedStatement checkStmt = Conn.prepareStatement(checkQuery);
            ResultSet res = checkStmt.executeQuery();

            if (!res.next())
                return new DbReturn("WARN", "Operation failed.", "No active reservation found with the given ID");

            // Parse reservation timing
            LocalDate reservationDate = LocalDate.parse(res.getString("reservation_date"));
            LocalTime startTime = LocalTime.parse(res.getString("start_time"));
            LocalTime endTime = LocalTime.parse(res.getString("end_time"));
            LocalDateTime reservationStart = LocalDateTime.of(reservationDate, startTime);
            LocalDateTime reservationEnd = LocalDateTime.of(reservationDate, endTime);
            LocalDateTime now = LocalDateTime.now();

            // Enforce cancellation rules
            if (!now.isBefore(reservationStart))
                return new DbReturn("WARN", "Operation failed!", "Cannot cancel during the reserved timeslot.");

            long minutesUntilStart = Duration.between(now, reservationStart).toMinutes();
            if (minutesUntilStart < 1440)
                return new DbReturn("WARN", "Operation failed!", "Cancellations must be made at least 24 hours in advance.");

            // Record the cancellation if rules are satisfied
            String cancelQuery = """
                UPDATE lab_reservation_log
                SET status = 'cancelled',
                    remarks = CONCAT(remarks, " | Cancelled: %s")
                        WHERE reservation_id = %d
                        """.formatted(remarks, reservation_id);
            PreparedStatement cancelStmt = Conn.prepareStatement(cancelQuery);
            int affected = cancelStmt.executeUpdate();

            return new DbReturn("INFO", "Cancel success", "Cancellation completed!");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new DbReturn("WARN", "???", "Something went wrong... please try again");
    }

    /**
     * Implementation of the cancel laboratory reservation transaction with no remarks.
     *
     * @param reservation_id the reservation ID to cancel
     * @param lab_tech_id the attending lab technician's ID
     *
     * @return a confirmation message if successful, or an error message if the cancellation fails
     */
    public static DbReturn cancelLabReservation(int reservation_id, int lab_tech_id) {
        return cancelLabReservation(reservation_id, lab_tech_id, "");
    }


    /* REPORT GENERATION */

    /** 
     * Generates a report object for report 1:
     * equipment borrowing history. This method implementation
     * allows to filter by equipment_id, start_date, and end_date.
     *  @return a Report object containing the generated report.
     */
    public static Report reportEquipmentBorrowingHistory(Integer equipment_id, Date start_date, Date end_date) throws InvalidFields {
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

            PreparedStatement stmt = Conn.prepareStatement(query);
            ResultSet res = stmt.executeQuery();
            return new Report(res);
        }            

        catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Report reportEquipmentBorrowingHistory(Integer equipment_id) throws InvalidFields {
        return reportEquipmentBorrowingHistory(equipment_id, new Date(0), new Date(0));
    }

    public static Report reportEquipmentBorrowingHistory() throws InvalidFields {
        return reportEquipmentBorrowingHistory(null, new Date(0), new Date(0));
    }


    /** 
     * Generates a report object for report 2:
     * lab reservation history. This method implementation
     * allows to filter by lab_code, start_date, and end_date.
     *  @return a Report object containing the generated report.
     */
    public static Report reportLabReservationHistory(Integer lab_code, Date start_date, Date end_date) throws InvalidFields {
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

            PreparedStatement stmt = Conn.prepareStatement(query);
            ResultSet res = stmt.executeQuery();
            return new Report(res);
        }            

        catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Report reportLabReservationHistory(Integer lab_code) throws InvalidFields {
        return reportLabReservationHistory(lab_code, new Date(0), new Date(0));
    }
    public static Report reportLabReservationHistory() throws InvalidFields {
        return reportLabReservationHistory(null, new Date(0), new Date(0));
    }


    /** 
     * Generates a report object for report 3:
     * student equipment borrowing history. This implementation allows
     * to set a null student ID if the report needs to generate for
     * all students.
     *  @return a Report object containing the generated report.
     */
    public static Report reportStudentEquipmentBorrowHistory(Integer student_id) throws InvalidFields {
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

            PreparedStatement stmt = Conn.prepareStatement(query);
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
    public static Report reportOrgLabReservationHistory(Integer org_id) throws InvalidFields {
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

            PreparedStatement stmt = Conn.prepareStatement(query);
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
    public static Report reportLabTechApprovalHistory(Integer labtech_id) throws InvalidFields {
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


            PreparedStatement stmt = Conn.prepareStatement(query);
            ResultSet res = stmt.executeQuery();
            return new Report(res);
        }            

        catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * Registers a new student in the system.
     * 
     * @param student_id the student ID
     * @param first_name the student's first name
     * @param last_name the student's last name
     * @param course the student's course
     * @param enrollment_status the student's enrollment status
     * @param email the student's email
     * @param org_id the organization ID if the student is part of an organization, -1 otherwise
     * @param org_position the student's position in the organization if applicable, null otherwise
     * @return a confirmation message if successful, or an error message if registration fails
     */
    public static String registerStudent(int student_id, String first_name, String last_name, String course, int yearLevel,
                              String enrollment_status, String email, int org_id, String org_position) {
        StringBuilder validationErrors = new StringBuilder();

        // Step 1: Validate field lengths and values
        if (String.valueOf(student_id).length() > 8)
            validationErrors.append("Student ID must be at most 8 digits.\n");

        if (first_name.length() > 20)
            validationErrors.append("First name must be at most 20 characters.\n");

        if (last_name.length() > 20)
            validationErrors.append("Last name must be at most 20 characters.\n");

        if (course.length() > 10)
            validationErrors.append("Course must be at most 10 characters.\n");

        String normalizedStatus = enrollment_status.trim().toLowerCase();
        if (enrollment_status.length() > 12)
            validationErrors.append("Enrollment status must be at most 12 characters.\n");

        if (!normalizedStatus.equals("enrolled") && !normalizedStatus.equals("not enrolled"))
            validationErrors.append("Enrollment status must be either 'enrolled' or 'not enrolled'.\n");

        if (email.length() > 40)
            validationErrors.append("Email must be at most 40 characters.\n");

        if (org_position != null && org_position.length() > 20)
            validationErrors.append("Organization position must be at most 20 characters.\n");

        // Step 2: If org_id is provided, check if it exists
        if (org_id != -1 && org_position != null) {
            try {
                String orgCheckQuery = "SELECT COUNT(*) FROM organization WHERE org_id = ?";
                PreparedStatement orgCheckStmt = Conn.prepareStatement(orgCheckQuery);
                orgCheckStmt.setInt(1, org_id);
                ResultSet rs = orgCheckStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    validationErrors.append("Organization ID " + org_id + " does not exist.\n");
                }
            } catch (SQLException e) {
                return "Student registration failed while checking organization: " + e.getMessage();
            }
        }

        // Step 3: If any validation failed, return all messages
        if (validationErrors.length() > 0) {
            return "Student registration failed:\n" + validationErrors.toString().trim();
        }

        // Step 4: Proceed with insertion
        try {
            String registerQuery = """
                INSERT INTO student 
                    (student_id, first_name, last_name, course, year_level, enrollment_status, email)
                VALUES (?, ?, ?, ?, ?, ?, ?);
            """;
            PreparedStatement registerStmt = Conn.prepareStatement(registerQuery);
            registerStmt.setInt(1, student_id);
            registerStmt.setString(2, first_name);
            registerStmt.setString(3, last_name);
            registerStmt.setString(4, course);
            registerStmt.setInt(5, yearLevel);
            registerStmt.setString(6, enrollment_status);
            registerStmt.setString(7, email);
            int affected = registerStmt.executeUpdate();

            if (org_id != -1 && org_position != null) {
                String orgStudentQuery = """
                    INSERT INTO org_students (student_id, org_id, position)
                    VALUES (?, ?, ?);
                """;
                PreparedStatement orgStmt = Conn.prepareStatement(orgStudentQuery);
                orgStmt.setInt(1, student_id);
                orgStmt.setInt(2, org_id);
                orgStmt.setString(3, org_position);
                orgStmt.executeUpdate();
            }

            return "Student registration completed! (%d rows affected)".formatted(affected);
        } catch (SQLException e) {
            return "Student registration failed due to a database error: " + e.getMessage();
        }
    }

    /**
     * Registers a new equipment in the system.
     * 
     * @param equipment_code the equipment code
     * @param equipment_name the equipment name
     * @param description the equipment description
     * @return a confirmation message if successful, or an error message if registration fails
     */
    public static String registerEquipment(int equipment_code, String equipment_name, String description) {
        StringBuilder validationErrors = new StringBuilder();

        // Step 1: Validate input lengths
        if (String.valueOf(equipment_code).length() > 8)
            validationErrors.append("Equipment code must be at most 8 digits.\n");

        if (equipment_name.length() > 20)
            validationErrors.append("Equipment name must be at most 20 characters.\n");

        if (description.length() > 100)
            validationErrors.append("Description must be at most 100 characters.\n");

        // Step 2: If any validation failed, return all messages
        if (validationErrors.length() > 0) {
            return "Equipment registration failed:\n" + validationErrors.toString().trim();
        }

        // Step 3: Proceed with insertion
        try {
            String registerQuery = """ 
                INSERT INTO equipment 
                    (equipment_code, equipment_name, description, status)
                VALUES (?, ?, ?, "usable");
            """;

            PreparedStatement registerStmt = Conn.prepareStatement(registerQuery);
            registerStmt.setInt(1, equipment_code);
            registerStmt.setString(2, equipment_name);
            registerStmt.setString(3, description);

            int affected = registerStmt.executeUpdate();
            return "Equipment registration completed! (%d rows affected)".formatted(affected);

        } catch (SQLException e) {
            return "Equipment registration failed due to a database error: " + e.getMessage();
        }
    }

    /**
     * Registers a new laboratory in the system.
     * 
     * @param lab_code the laboratory code
     * @param lab_location the laboratory location
     * @param description the laboratory description
     * @param capacity the laboratory capacity
     * @return a confirmation message if successful, or an error message if registration fails
     */
    public static String registerLaboratory(int lab_code, String lab_location, String description, int capacity) {
        StringBuilder validationErrors = new StringBuilder();

        // Step 1: Validate input lengths and values
        if (String.valueOf(lab_code).length() > 8)
            validationErrors.append("Lab code must be at most 8 digits.\n");

        if (lab_location.length() > 20)
            validationErrors.append("Lab location must be at most 20 characters.\n");

        if (description.length() > 100)
            validationErrors.append("Description must be at most 100 characters.\n");

        if (capacity > 999)
            validationErrors.append("Capacity must be at most 3 digits.\n");

        if (capacity <= 0)
            validationErrors.append("Capacity must be a positive number.\n");

        // Step 2: If any validation failed, return all messages
        if (validationErrors.length() > 0) {
            return "Laboratory registration failed:\n" + validationErrors.toString().trim();
        }

        // Step 3: Proceed with insertion
        try {
            String registerQuery = """
                INSERT INTO laboratory (lab_code, lab_location, description, capacity)
                VALUES (?, ?, ?, ?);
            """;

            PreparedStatement registerStmt = Conn.prepareStatement(registerQuery);
            registerStmt.setInt(1, lab_code);
            registerStmt.setString(2, lab_location);
            registerStmt.setString(3, description);
            registerStmt.setInt(4, capacity);

            int affected = registerStmt.executeUpdate();
            return "Laboratory registration completed! (%d rows affected)".formatted(affected);

        } catch (SQLException e) {
            return "Laboratory registration failed due to a database error: " + e.getMessage();
        }
    }

    /**
     * Registers a new organization in the system.
     * 
     * @param org_id the organization ID
     * @param org_name the organization name
     * @param org_email the organization email
     * @return a confirmation message if successful, or an error message if registration fails
     */
    public static String registerOrganization(int org_id, String org_name, String org_email) {
        StringBuilder validationErrors = new StringBuilder();

        // Step 1: Validate input lengths
        if (String.valueOf(org_id).length() > 8)
            validationErrors.append("Organization ID must be at most 8 digits.\n");

        if (org_name.length() > 20)
            validationErrors.append("Organization name must be at most 20 characters.\n");

        if (org_email.length() > 40)
            validationErrors.append("Organization email must be at most 40 characters.\n");

        // Optional: Basic email format check
        if (!org_email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"))
            validationErrors.append("Organization email must be a valid email address.\n");

        // Step 2: If any validation failed, return all messages
        if (validationErrors.length() > 0) {
            return "Organization registration failed:\n" + validationErrors.toString().trim();
        }

        // Step 3: Proceed with insertion
        try {
            String registerQuery = """
                INSERT INTO organization (org_id, org_name, org_email)
                VALUES (?, ?, ?);
            """;

            PreparedStatement registerStmt = Conn.prepareStatement(registerQuery);
            registerStmt.setInt(1, org_id);
            registerStmt.setString(2, org_name);
            registerStmt.setString(3, org_email);

            int affected = registerStmt.executeUpdate();
            return "Organization registration completed! (%d rows affected)".formatted(affected);

        } catch (SQLException e) {
            return "Organization registration failed due to a database error: " + e.getMessage();
        }
    }
}
