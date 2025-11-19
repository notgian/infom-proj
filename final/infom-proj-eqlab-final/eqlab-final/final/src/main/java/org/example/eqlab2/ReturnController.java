package org.example.eqlab2;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

public class ReturnController {
    private String studentId; 

    @FXML
    private TextField studentIdDisplay;

    @FXML
    private ComboBox<String> equipmentDropdown;

    @FXML
    private ComboBox<String> conditionDropdown;

    @FXML
    private TextArea notesField;

    @FXML
    public void initialize() {
        // Equipment list (same as Borrow page)
        String[] equipment = DbConnection.getEquipment();
        for (String e: equipment) {
            equipmentDropdown.getItems().add(e);
        }

        // Condition options
        conditionDropdown.getItems().addAll(
                "ok", "broken"
        );
    }

    @FXML
    public void submitReturn(ActionEvent event) {

            // Student ID validation -------------------------------------------
            String studentId = studentIdDisplay.getText().trim();

            if (!studentId.matches("\\d{8}")) {
                showAlert(Alert.AlertType.ERROR, "Invalid Student ID",
                        "Student ID must be exactly 8 digits.");
                return;
            }

        // Validation ---------------------------------------------------------
        if (equipmentDropdown.getValue() == null ||
            conditionDropdown.getValue() == null) {

            showAlert(Alert.AlertType.ERROR, "Missing Fields",
                    "Please complete all required fields.");
            return;
        }

        // Success popup -------------------------------------------------------
       
        try {
            int student_id = Integer.parseInt(studentIdDisplay.getText());
            int equipment_id = Integer.parseInt(equipmentDropdown.getValue().substring(1,9));
            
            boolean broken = conditionDropdown.getValue().equals("ok") ? false : true;

            String remarks = notesField.getText();

            DbReturn res = DbConnection.returnEquipment(student_id, equipment_id, DbConnection.getLabTechID(), remarks, broken);
            showAlert(Alert.AlertType.INFORMATION, res.getTitle(), res.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // Clear form
        clearForm();
    }

    private void clearForm() {
        equipmentDropdown.setValue(null);
        conditionDropdown.setValue(null);
        notesField.clear();
    }

    @FXML
    public void goBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/eqlab2/dashboard-view.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 400, 650));
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setHeaderText(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
