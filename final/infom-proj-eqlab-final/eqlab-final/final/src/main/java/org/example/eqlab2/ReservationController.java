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
import javafx.scene.control.*;
import java.time.format.DateTimeFormatter;

public class ReservationController {

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> labCombo;
    @FXML private ComboBox<String> startTimeCombo;
    @FXML private ComboBox<String> endTimeCombo;
    @FXML private ComboBox<String> studentOrgDropdown;
    @FXML private TextField studentIdField;

    public void initialize() {
        String[] orgs = DbConnection.getOrgs();
        String[] labs = DbConnection.getLabs();

        for (String org: orgs) {
            studentOrgDropdown.getItems().add(org);
        }

        for (String lab: labs) {
            labCombo.getItems().add(lab);
        }

        // time slots, and it's only until 5 since labs close 5-ish
        for (int hour = 7; hour <= 16; hour++) {
            String timeStart = String.format("%02d:00", hour);
            startTimeCombo.getItems().add(timeStart);

            String timeEnd = String.format("%02d:00", hour+1);
            endTimeCombo.getItems().add(timeEnd);
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
        
        try {

            int org_id = Integer.parseInt(studentOrgDropdown.getValue().substring(1,9));
            int lab_id = Integer.parseInt(labCombo.getValue().substring(1,9));
            int student_id = Integer.parseInt(studentIdField.getText());

            LocalDate reservation_date = datePicker.getValue();

            DateTimeFormatter datefmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            DbReturn res = DbConnection.reserveLab(student_id, org_id, lab_id, DbConnection.getLabTechID(), reservation_date.format(datefmt), end_time, remarks)
            showAlert(Alert.AlertType.INFORMATION, res.getTitle(), res.getMessage());
        clearForm();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
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
