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
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

public class BorrowController {

    private String studentId; 

    @FXML
    private TextField studentIdDisplay;

    @FXML
    private ComboBox<String> equipmentDropdown;

    @FXML
    public void initialize() {
        // Equipment list random things
        equipmentDropdown.getItems().addAll(
        ); // CHANGE ME

    }

        @FXML
        public void submitBorrowRequest(ActionEvent event) {

            // Student ID validation -------------------------------------------
            String studentId = studentIdDisplay.getText().trim();

            if (!studentId.matches("\\d{8}")) {
                showAlert(Alert.AlertType.ERROR, "Invalid Student ID",
                        "Student ID must be exactly 8 digits.");
                return;
            }

            // Field validation -------------------------------------------------
            if (equipmentDropdown.getValue() == null) {

                showAlert(Alert.AlertType.ERROR, "Missing Fields",
                        "Please complete all fields before submitting.");
                return;
            }

            // Success popup ----------------------------------------------------
            showAlert(Alert.AlertType.INFORMATION, "Success!",
                    "Equipment borrowed successfully!");

            clearForm();
        }



        private void clearForm() {
            equipmentDropdown.setValue(null);
            studentIdDisplay.clear();
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
