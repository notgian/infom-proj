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

        String reservingLab = dbconn.reserveLab(
            30000001, // CompSoc
            40000001, // Goks 101
            10001001, // Michael Scott
            "2025-12-15", // safely >72 hours ahead
            "10:00:00",
            "12:00:00",
            "This is a test reservation (valid)"
        );
        System.out.println(reservingLab);

        String cancellingReservation = dbconn.cancelLabReservation(
            10000030, // Reservation ID for CompSoc, Goks 101, 2025-12-15 10:00–12:00 (the previous transaction)
            10001001, // Michael Scott
            "This is a test cancellation (valid)"
        );
        System.out.println(cancellingReservation);

        String reserveTooLate = dbconn.reserveLab(
            30000001,
            40000001,
            10001001,
            LocalDate.now().plusDays(2).toString(), // <72 hours ahead
            "10:00:00",
            "12:00:00",
            "This is a test reservation (too late)"
        );
        System.out.println(reserveTooLate);

        String overlappingReservation = dbconn.reserveLab(
            30000001,
            40000001, // Goks 101 already reserved on 2025-12-01 from 10:00–12:00
            10001001,
            "2025-12-01",
            "11:00:00", // overlaps with 10:00–12:00
            "13:00:00",
            "This is a test reservation (overlap)"
        );
        System.out.println(overlappingReservation);

        String cancelDuringTimeslot = dbconn.cancelLabReservation(
            3015, // Reservation ID for CompSoc, Goks 101, 2025-12-01 10:00–12:00
            10001001,
            "This is a test cancellation (during timeslot)"
        );
        System.out.println(cancelDuringTimeslot);

        String cancelTooLate = dbconn.cancelLabReservation(
            3016, // Reservation ID for IEEE, Goks 102, 2025-12-01 14:00–16:00 (assumed tomorrow)
            10001003,
            "This is a test cancellation (too late)"
        );
        System.out.println(cancelTooLate);

        String invalidTimeRange = dbconn.reserveLab(
            30000001, // CompSoc
            40000001, // Goks 101
            10001001, // Michael Scott
            "2025-12-10", // valid future date
            "14:00:00",  // start time
            "10:00:00",  // end time (earlier than start)
            "This is a test reservation where end time is earlier than start time"
        );
        System.out.println(invalidTimeRange);
    }

}

