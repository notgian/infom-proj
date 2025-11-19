package org.example.eqlab2;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Random;

public class CancellationController {

    @FXML private TextField studentIdField;
    @FXML private DatePicker cancellationDate;
    @FXML private ComboBox<String> labCombo;
    @FXML private ComboBox<String> startTimeCombo;
    @FXML private ComboBox<String> endTimeCombo;

    @FXML
    public void initialize() {
       //date of cancellation starts on the date today since u can't
        //cancel tomorrows' appointment
        cancellationDate.setValue(LocalDate.now());

        // lab options like sa reservation but can be changed when thje db is available
        labCombo.getItems().addAll(
                "Laboratory 101",
                "Laboratory 202",
                "Laboratory 303"
        );

        // time slots 7-5PM only
        for (int hour = 7; hour <= 17; hour++) {
            String time = String.format("%02d:00", hour);
            startTimeCombo.getItems().add(time);
            endTimeCombo.getItems().add(time);
        }
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        String studentId = studentIdField.getText().trim();
        String selectedLab = labCombo.getValue();
        LocalDate selectedDate = cancellationDate.getValue();

        // id number must be 8 digits (got it from eulan)
        if (!studentId.matches("\\d{8}")) {
            showAlert(Alert.AlertType.ERROR, "Invalid Student ID",
                    "The Student ID must be exactly 8 digits.");
            return;
        }

        // field validation
        if (selectedLab == null || selectedDate == null) {
            showAlert(Alert.AlertType.ERROR, "Missing Required Fields",
                    "Please select the Lab and the Date of the reservation to proceed with cancellation.");
            return;
        }

        // cancellation checker
        if (isReservationFound(studentId, selectedLab)) {
            // successful
            String successMessage = String.format(
                    "Reservation successfully cancelled.\n\n" +
                            "Lab Room: %s\n" +
                            "Date: %s\n" +
                            "User ID: %s\n\n" +
                            "The lab slot is now open.",
                    selectedLab, selectedDate.toString(), studentId
            );
            showAlert(Alert.AlertType.INFORMATION, "Cancellation Successful (Receipt)", successMessage);
            clearForm();
        } else {
            //failed
            String failureMessage = String.format(
                    "We could not find an active reservation under Student ID %s for Lab %s on %s.\n\n" +
                            "Please verify the details (ID, Lab, Date) and try again.",
                    studentId, selectedLab, selectedDate.toString()
            );
            showAlert(Alert.AlertType.WARNING, "Reservation Not Found", failureMessage);
        }
    }

    //kunwari may db na
    private boolean isReservationFound(String studentId, String lab) {
        Random random = new Random();
        return random.nextDouble() > 0.3;
    }

    private void clearForm() {
        studentIdField.clear();
        labCombo.setValue(null);
        startTimeCombo.setValue(null);
        endTimeCombo.setValue(null);
        cancellationDate.setValue(LocalDate.now());
    }

    //same popup template
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void goBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/eqlab2/dashboard-view.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 400, 650));
    }


}