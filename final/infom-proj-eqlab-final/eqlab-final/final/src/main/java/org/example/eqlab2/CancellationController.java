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

    @FXML
    public void initialize() {

    }

    @FXML
    public void handleCancel(ActionEvent event) {
        String studentId = studentIdField.getText().trim();

        // id number must be 8 digits (got it from eulan)
        if (!studentId.matches("\\d{8}")) {
            showAlert(Alert.AlertType.ERROR, "Invalid Student ID",
                    "The Student ID must be exactly 8 digits.");
            return;
        }

        // field validation
        if (studentId == null) {
            showAlert(Alert.AlertType.ERROR, "Missing Required Fields",
                    "Please select the Lab and the Date of the reservation to proceed with cancellation.");
            return;
        }

        // cancellation checker
        // Call the thing
        showAlert(Alert.AlertType.WARNING, "Reservation Not Found", failureMessage);
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
