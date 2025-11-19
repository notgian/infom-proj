package org.example.eqlab2;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Alert;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Random;

public class ReservationController {

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> labCombo;
    @FXML private ComboBox<String> startTimeCombo;
    @FXML private ComboBox<String> endTimeCombo;

    @FXML
    public void initialize() {
        labCombo.getItems().addAll(
                "Laboratory 101",
                "Laboratory 202",
                "Laboratory 303"
        );

        // time slots, and it's only until 5 since labs close 5-ish
        for (int hour = 7; hour <= 17; hour++) {
            String time = String.format("%02d:00", hour);
            startTimeCombo.getItems().add(time);
            endTimeCombo.getItems().add(time);
        }
    }

    @FXML
    public void reserveSlot() {
        String selectedLab = labCombo.getValue();
        String selectedStartTime = startTimeCombo.getValue();
        String selectedEndTime = endTimeCombo.getValue();
        LocalDate selectedDate = datePicker.getValue();

        if (selectedDate == null || selectedLab == null || selectedStartTime == null || selectedEndTime == null) {
            showAlert(Alert.AlertType.ERROR, "Missing Fields",
                    "Please select a date, lab, start time, and end time before submitting.");
            return;
        }

        //time validation
        int startHour = Integer.parseInt(selectedStartTime.substring(0, 2));
        int endHour = Integer.parseInt(selectedEndTime.substring(0, 2));

        if (startHour == endHour) {
            showAlert(Alert.AlertType.ERROR,
                    "Invalid Time Slot", "Start time and end time cannot be the same.");
            return;
        }

        if (endHour < startHour) {
            showAlert(Alert.AlertType.ERROR,
                    "Invalid Time Slot", "The end time must be later than the start time.");
            return;
        }

        //checking labroom availability
        if (isLabAvailable(selectedLab, selectedDate, startHour, endHour)) {
            String receiptMessage = String.format(
                    "Lab Room: %s\n" +
                            "Date: %s\n" +
                            "Time Slot: %s - %s\n\n" +
                            "Your reservation is confirmed. Click 'OK' to finalize.",
                    selectedLab, selectedDate.toString(), selectedStartTime, selectedEndTime
            );
            showAlert(Alert.AlertType.INFORMATION, "Reservation Confirmed", receiptMessage);
            clearForm();
        } else {
            String unavailableMessage = String.format(
                    "The requested lab (%s) is already reserved for the slot:\n" +
                            "Date: %s\n" +
                            "Time: %s - %s\n\n" +
                            "Please select a different time slot or another laboratory.",
                    selectedLab, selectedDate.toString(), selectedStartTime, selectedEndTime
            );
            showAlert(Alert.AlertType.WARNING, "Slot Not Available", unavailableMessage);
        }
    }

    // hi so i wanted to check if the checking is working so i added this method
    // totally deletable when we go over it in a while
    private boolean isLabAvailable(String lab, LocalDate date, int startHour, int endHour) {
        Random random = new Random();
        if (lab.equals("Laboratory 202")) {
            return random.nextDouble() > 0.8;
        }
        return random.nextDouble() > 0.2;
    }

    //Clears the input fields of the form if the user intendeds to reserve more than
    //one room (it js looks better)
    private void clearForm() {
        labCombo.setValue(null);
        startTimeCombo.setValue(null);
        endTimeCombo.setValue(null);
        datePicker.setValue(null);
    }

    //i made this so i can js use this method for all the prompts
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void goBackToDashboard(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(
                getClass().getResource("/org/example/eqlab2/dashboard-view.fxml")
        );
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 400, 650));
    }
}