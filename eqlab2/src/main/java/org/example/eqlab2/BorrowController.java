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
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;

public class BorrowController {

    @FXML
    private DatePicker borrowDate;

    @FXML
    private ComboBox<String> equipmentDropdown;

    @FXML
    private ComboBox<String> startTime;

    @FXML
    private ComboBox<String> endTime;


    @FXML
    public void initialize() {

        // Disable selecting past dates
        borrowDate.setDayCellFactory(picker -> new DatePickerCell(picker));

        // Equipment list
        equipmentDropdown.getItems().addAll(
                "Keyboard",
                "Mouse",
                "Headset",
                "USB Cable",
                "HDMI Cable",
                "Projector Remote",
                "Extension Cord",
                "Ethernet Cable",
                "Web Camera",
                "Microphone",
                "Laptop",
                "Tablet"
        );

        // Time options
        for (int hour = 7; hour <= 20; hour++) {
            String time = String.format("%02d:00", hour);
            startTime.getItems().add(time);
            endTime.getItems().add(time);
        }
    }


    @FXML
    public void submitBorrowRequest(ActionEvent event) {

        // Validation ----------------------------------------------------------
        if (borrowDate.getValue() == null ||
            equipmentDropdown.getValue() == null ||
            startTime.getValue() == null ||
            endTime.getValue() == null) {

            showAlert(Alert.AlertType.ERROR, "Missing Fields",
                    "Please complete all fields before submitting.");
            return;
        }

        // Time validation
        int start = Integer.parseInt(startTime.getValue().substring(0, 2));
        int end = Integer.parseInt(endTime.getValue().substring(0, 2));

        if (end <= start) {
            showAlert(Alert.AlertType.ERROR,
                    "Invalid Time", "End time must be later than start time.");
            return;
        }

        // Success popup -------------------------------------------------------
        showAlert(Alert.AlertType.INFORMATION, "Success!",
                "Your equipment borrowing request has been recorded.");

        // Clear the form
        clearForm();
    }


    private void clearForm() {
        borrowDate.setValue(null);
        equipmentDropdown.setValue(null);
        startTime.setValue(null);
        endTime.setValue(null);
    }


    // Reusable popup helper
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }


    // Back button
    @FXML
    public void goBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/eqlab2/dashboard-view.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 400, 650));
    }


    // Disable past dates ------------------------------------------------------
    private class DatePickerCell extends javafx.scene.control.DateCell {
        private final DatePicker picker;

        public DatePickerCell(DatePicker dp) {
            this.picker = dp;
        }

        @Override
        public void updateItem(LocalDate item, boolean empty) {
            super.updateItem(item, empty);

            LocalDate today = LocalDate.now();

            if (item.isBefore(today)) {
                setDisable(true);
                setStyle("-fx-background-color: #eee;");
            }
        }
    }
}
