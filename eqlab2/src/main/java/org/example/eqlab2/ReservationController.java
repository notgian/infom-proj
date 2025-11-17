package org.example.eqlab2;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import java.io.IOException;

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

        // start times but idk how this will work when we plan to js show available times
        //I added it here as a placeholder to see how the app will look like
        startTimeCombo.getItems().addAll(
                "07:00 AM", "08:00 AM", "09:00 AM",
                "10:00 AM", "11:00 AM", "01:00 PM"
        );

        //js populatiing the end times same rzn as above
        endTimeCombo.getItems().addAll(
                "08:00 AM", "09:00 AM", "10:00 AM",
                "11:00 AM", "12:00 PM", "02:00 PM"
        );
    }

    @FXML
    public void reserveSlot() {
        System.out.println("Reservation Submitted:");
        System.out.println("Date: " + datePicker.getValue());
        System.out.println("Lab: " + labCombo.getValue());
        System.out.println("Start: " + startTimeCombo.getValue());
        System.out.println("End: " + endTimeCombo.getValue());
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
