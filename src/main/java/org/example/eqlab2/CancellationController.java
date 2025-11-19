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

    @FXML private TextField reservationIdField;

    @FXML
    public void initialize() {

    }

    @FXML
    public void handleCancel(ActionEvent event) {
        String reservationId = reservationIdField.getText().trim();

        // field validation
        if (reservationId == null) {
            showAlert(Alert.AlertType.ERROR, "Missing Required Fields",
                    "Please enter the reservation ID number.");
            return;
        }

        // id number must be 8 digits (got it from eulan)
        if (!reservationId.matches("\\d{8}")) {
            showAlert(Alert.AlertType.ERROR, "Invalid Reservation ID",
                    "The Reservation ID must be exactly 8 digits.");
            return;
        }

        // Call the thing
        try {

            DbReturn res = DbConnection.cancelLabReservation(Integer.parseInt(reservationId), DbConnection.getLabTechID());
            showAlert(Alert.AlertType.WARNING, res.getTitle(), res.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();            
        }

        
        // DbResult res = DbConnection.cancelLabReservation(Integer.parseInt(reservationId) )
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
