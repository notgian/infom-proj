package org.example.eqlab2;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class RegisterController {

    // ROLE
    @FXML private ComboBox<String> roleDropdown;

    // STUDENT FIELDS
    @FXML private VBox studentSection;
    @FXML private TextField studentIdField;
    @FXML private TextField studentFirstField;
    @FXML private TextField studentLastField;
    @FXML private TextField studentCourseField;
    @FXML private TextField studentYearField;
    @FXML private ComboBox<String> studentStatusDropdown;
    @FXML private ComboBox<String> studentOrgDropdown;
    @FXML private TextField studentEmailField;
    @FXML private TextField studentOrgPosition;

    // EQUIPMENT FIELDS
    @FXML private VBox equipmentSection;
    @FXML private TextField equipIdField;
    @FXML private TextField equipNameField;
    @FXML private TextField equipDescField;

    // LAB FIELDS
    @FXML private VBox labSection;
    @FXML private TextField labIdField;
    @FXML private TextField labLocationField;
    @FXML private TextField labDescField;
    @FXML private TextField labCapacityField;

    // ORGANIZATION FIELDS
    @FXML private VBox orgSection;
    @FXML private TextField orgIdField;
    @FXML private TextField orgNameField;
    @FXML private TextField orgEmailField;


    @FXML
    public void initialize() {

        // ---- Fill dropdowns ----
        roleDropdown.getItems().addAll("Student", "Equipment", "Lab", "Organization");
        
        String[] orgs = DbConnection.getOrgs();
        for (String org: orgs) {
            studentOrgDropdown.getItems().add(org);
        }

        studentStatusDropdown.getItems().addAll("enrolled", "not enrolled");

        // Hide all section groups initially
        hideAllSections();

        // When role changes → update what fields show
        roleDropdown.setOnAction(e -> updateSectionVisibility());
    }


    private void hideAllSections() {
        studentSection.setVisible(false);
        studentSection.setManaged(false);

        equipmentSection.setVisible(false);
        equipmentSection.setManaged(false);

        labSection.setVisible(false);
        labSection.setManaged(false);

        orgSection.setVisible(false);
        orgSection.setManaged(false);
    }


    private void updateSectionVisibility() {
        hideAllSections();

        String role = roleDropdown.getValue();
        if (role == null) return;

        switch (role) {
            case "Student":
                studentSection.setVisible(true);
                studentSection.setManaged(true);
                break;

            case "Equipment":
                equipmentSection.setVisible(true);
                equipmentSection.setManaged(true);
                break;

            case "Lab":
                labSection.setVisible(true);
                labSection.setManaged(true);
                break;

            case "Organization":
                orgSection.setVisible(true);
                orgSection.setManaged(true);
                break;
        }
    }


    @FXML
    private void handleRegister(ActionEvent event) {

        String role = roleDropdown.getValue();

        if (role == null) {
            showAlert(Alert.AlertType.ERROR, "Missing Role", "Please select a role.");
            return;
        }

        switch (role) {

            // ========================
            //      STUDENT
            // ========================
            case "Student":

                if (!studentIdField.getText().matches("\\d{8}")) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Student ID",
                            "Student ID must be exactly 8 digits.");
                    return;
                }

                if (studentFirstField.getText().isBlank() ||
                    studentLastField.getText().isBlank() ||
                    studentCourseField.getText().isBlank() ||
                    studentYearField.getText().isBlank() ||
                    studentEmailField.getText().isBlank()) {

                    showAlert(Alert.AlertType.ERROR, "Missing Data", "Please fill in all student fields.");
                    return;
                }


                int student_id = Integer.parseInt(studentIdField.getText());
                String first_name = studentFirstField.getText();
                String last_name = studentLastField.getText();
                String course = studentCourseField.getText();
                int year_level = Integer.parseInt(studentYearField.getText());
                String enrollment_status = studentStatusDropdown.getValue();
                String email = studentEmailField.getText();
                int org_id = studentOrgDropdown.getValue() == null ? -1 : Integer.parseInt(studentOrgDropdown.getValue().substring(1,9));
                String org_position = studentOrgPosition.getText();
                 
                

                String message = DbConnection.registerStudent(student_id, first_name, last_name, course, year_level, enrollment_status, email, org_id, org_position);




                showAlert(Alert.AlertType.INFORMATION, "INFO",
                        message);
                return;

            // ========================
            //      EQUIPMENT
            // ========================
            case "Equipment":

                if (equipNameField.getText().isBlank() ||
                    equipDescField.getText().isBlank()) {

                    showAlert(Alert.AlertType.ERROR, "Missing Data", "Please complete all equipment fields.");
                    return;
                }

                    int equipment_code = Integer.parseInt(equipIdField.getText());
                    String equipment_name = equipNameField.getText();
                    String description = equipDescField.getText();

                    String msg = DbConnection.registerEquipment(equipment_code, equipment_name, description);

                showAlert(Alert.AlertType.INFORMATION, "INFO",
                        msg);
                return;

            // ========================
            //      LABORATORY
            // ========================
            case "Lab":

                if (labLocationField.getText().isBlank() ||
                    labDescField.getText().isBlank() ||
                    labCapacityField.getText().isBlank()) {

                    showAlert(Alert.AlertType.ERROR, "Missing Data", "Please fill all laboratory fields.");
                    return;
                }

                int lab_code = Integer.parseInt(labIdField.getText());
                String lab_location = labLocationField.getText();
                String lab_description = labDescField.getText();
                int capacity = Integer.parseInt(labCapacityField.getText());

                String msg_lab = DbConnection.registerLaboratory(lab_code, lab_location, lab_description, capacity);

                showAlert(Alert.AlertType.INFORMATION, "INFO",
                        msg_lab);
                return;

            // ========================
            //      ORGANIZATION
            // ========================
            case "Organization":

                if (orgNameField.getText().isBlank() || orgEmailField.getText().isBlank()) {
                    showAlert(Alert.AlertType.ERROR, "Missing Data", "Please complete all organization fields.");
                    return;
                }

                int org_id2 = Integer.parseInt(orgIdField.getText()); 
                String org_name = orgNameField.getText();
                String org_email = orgEmailField.getText();


                String msg_org = DbConnection.registerOrganization(org_id2, org_name, org_email) ;

                showAlert(Alert.AlertType.INFORMATION, "INFO",
                        msg_org);
                return;
        }
    }


    // ============================
    //         NAVIGATION
    // ============================
    @FXML
    private void goBackToHome(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/eqlab2/homepg-view.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 400, 650));
    }


    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setHeaderText(title);
        a.setContentText(msg);
        a.showAndWait();
    }

}
