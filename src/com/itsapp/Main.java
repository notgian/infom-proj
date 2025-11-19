package com.itsapp;

import java.time.LocalDate;

public class Main {
    
    public static void main(String[] args) {
        DbConnection dbconn = new DbConnection();
        dbconn.connect();

        System.out.println("\n=== TESTING BORROW/RETURN/REPLACE EQUIPMENT ===");

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

        System.out.println("\n=== TESTING RESERVING LAB / CANCELLING RESERVATION ===");

        // Valid reservation: No Conflict, Student Rep is officer, >72 hours ahead
        String reservingLab = dbconn.reserveLab(
            12010001,
            30000001,
            40000001,
            10001001,
            "2025-12-16", // Tuesday — no class
            "10:00:00",
            "12:00:00",
            "This is a test reservation (valid)"
        );
        System.out.println(reservingLab);

        // Valid cancellation: >24 hours ahead
        String cancellingReservation = dbconn.cancelLabReservation(
            10000030, // previous reservation made above
            12010001, // student rep who made it
            "This is a test cancellation (valid)"
        );
        System.out.println(cancellingReservation);

        // Too late reservation: <72 hours ahead
        String reserveTooLate = dbconn.reserveLab(
            12010001, // Michael Scott
            30000001,
            40000001,
            10001001,
            LocalDate.now().plusDays(2).toString(),
            "10:00:00",
            "12:00:00",
            "This is a test reservation (too late)"
        );
        System.out.println(reserveTooLate);

        // Overlapping reservation: Goks 101 already reserved on 2025-12-01 from 10:00–12:00
        String overlappingReservation = dbconn.reserveLab(
            12010001,
            30000001,
            40000001,
            10001001,
            "2025-12-01",
            "11:00:00",
            "13:00:00",
            "This is a test reservation (overlap)"
        );
        System.out.println(overlappingReservation);

        String cancelDuringTimeslot = dbconn.cancelLabReservation(
            10000001,
            12010002,
            "This is a test cancellation (during timeslot)"
        );
        System.out.println(cancelDuringTimeslot);

        // <24 hours before reservation time
        String cancelTooLate = dbconn.cancelLabReservation(
            10000005,
            12310002,
            "This is a test cancellation (too late)"
        );
        System.out.println(cancelTooLate);

        // Invalid time range
        String invalidTimeRange = dbconn.reserveLab(
            12010001,
            30000001,
            40000001,
            10001001,
            "2025-12-10",
            "14:00:00",
            "10:00:00",
            "End time earlier than start time"
        );
        System.out.println(invalidTimeRange);

        String memberOnlyReservation = dbconn.reserveLab(
            12010002, // Member of CompSoc, not officer
            30000001,
            40000001,
            10001001,
            "2025-12-18",
            "10:00:00",
            "12:00:00",
            "Reservation by member (should fail)"
        );
        System.out.println(memberOnlyReservation);

        String duringClassSchedule = dbconn.reserveLab(
            12010001,
            30000001,
            40000001,
            10001001,
            "2025-12-08", // Goks 101 has class on Monday 08:00–11:00
            "08:30:00",
            "10:30:00",
            "Reservation during class schedule (should fail)"
        );
        System.out.println(duringClassSchedule);

        System.out.println("\n=== TESTING REGISTERING ===");

        // Test 1: Register a student (no org)
        String studentResult = dbconn.registerStudent(
            13010001,
            "Alyssa",
            "Reyes",
            "BSCS",
            "enrolled",
            "alyssa.reyes@dlsu.edu.ph",
            -1,
            null
        );
        System.out.println(studentResult);

        // Test 2: Register a student (with org)
        String studentWithOrg = dbconn.registerStudent(
            130100021,
            "Miguel",
            "Tan",
            "BSIS",
            "enrolled",
            "miguel.tan@dlsu.edu.ph",
            300200301,
            "President"
        );
        System.out.println(studentWithOrg);

        // Test 3: Register equipment
        String equipmentResult = dbconn.registerEquipment(
            50000001,
            "Oscilloscope",
            "Used for measuring electronic signals"
        );
        System.out.println(equipmentResult);

        // Test 4: Register laboratory
        String labResult = dbconn.registerLaboratory(
            40000035,
            "Goks 305",
            "Engineering Building, 2nd Floor",
            30
        );
        System.out.println(labResult);

        // Test 5: Register organization
        String orgResult = dbconn.registerOrganization(
            30000035,
            "Photonics Society",
            "testing.haha@dlsu.edu.ph"
        );
        System.out.println(orgResult);
    }
}

