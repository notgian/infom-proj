package org.example.eqlab2;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.beans.property.SimpleStringProperty;

import java.util.List;
import java.util.Arrays;


import java.io.IOException;

public class ReportsController {

    // ROLE
    @FXML private ComboBox<String> roleDropdown;

    //EQUIPMENT BORROWING HISTORY REPORT
    @FXML private VBox equipmentBorrowingSection; 
    @FXML private TableView<List<String>> equipmentTable;


    //LABORATORY RESERVATION HISTORY REPORT
    @FXML private VBox laboratoryReservationSection;
    @FXML private TableView<List<String>> labreserveTable;


    //STUDENT BORROWING HISTORY REPORT
    @FXML private VBox studentBorrowingSection;
    @FXML private TableView<List<String>> studentTable;


    //ORG LAB RESERVATION HISTORY REPORT
    @FXML private VBox organizationLabReservationSection;
    @FXML private TableView<List<String>> orgreserveTable;


    //LABTECH TRANSACTION HISTORY REPORT
    @FXML private VBox labtechTransactionSection; 
    @FXML private TableView<List<String>> labtechTable;




    @FXML
    public void initialize() {

        // ---- Fill dropdowns ----
        roleDropdown.getItems().addAll("Equipment Borrowing History", "Laboratory Reservation History", "Student Borrowing History", "Organization Reservation History", "Lab Technician Transaction History");
        // Hide all section groups initially
        hideAllSections();

        // When role changes → update what fields show
        roleDropdown.setOnAction(e -> updateSectionVisibility());
    }


    private void hideAllSections() {
        equipmentBorrowingSection.setVisible(false);
        equipmentBorrowingSection.setManaged(false);

        laboratoryReservationSection.setVisible(false);
        laboratoryReservationSection.setManaged(false);

        studentBorrowingSection.setVisible(false);
        studentBorrowingSection.setManaged(false);

        organizationLabReservationSection.setVisible(false);
        organizationLabReservationSection.setManaged(false);

        labtechTransactionSection.setVisible(false);
        labtechTransactionSection.setManaged(false);
    }


    private void updateSectionVisibility() {
        hideAllSections();

        String role = roleDropdown.getValue();
        if (role == null) return;

        try {
            switch (role) {
                case "Equipment Borrowing History":
                    load2DArrayToTable(DbConnection.reportEquipmentBorrowingHistory().getTable(), equipmentTable);
                    equipmentBorrowingSection.setVisible(true);
                    equipmentBorrowingSection.setManaged(true);
                    break;

                case "Laboratory Reservation History":
                    laboratoryReservationSection.setVisible(true);
                    laboratoryReservationSection.setManaged(true);
                    break;

                case "Student Borrowing History":
                    studentBorrowingSection.setVisible(true);
                    studentBorrowingSection.setManaged(true);
                    break;

                case "Organization Reservation History":
                    organizationLabReservationSection.setVisible(true);
                    organizationLabReservationSection.setManaged(true);
                    break;

                case "Lab Technician Transaction History":
                    labtechTransactionSection.setVisible(true);
                    labtechTransactionSection.setManaged(true);
                    break;    
            }
        }
        catch (InvalidFields e) {
            e.printStackTrace();
        }

    }

private void load2DArrayToTable(String[][] data, TableView<List<String>> table) {

    table.getColumns().clear();
    table.getItems().clear();

    if (data == null || data.length == 0) return;

    int columnCount = data[0].length;

    // === Create columns using FIRST ROW as header ===
    for (int c = 0; c < columnCount; c++) {
        final int colIndex = c;

        String header = data[0][c];  // <-- column name from first row

        TableColumn<List<String>, String> column = new TableColumn<>(header);

        column.setCellValueFactory(param ->
                new SimpleStringProperty(param.getValue().get(colIndex))
        );

        table.getColumns().add(column);
    }

    // === Add data rows (start from row 1, skip header row) ===
    for (int r = 1; r < data.length; r++) {
        table.getItems().add(Arrays.asList(data[r]));
    }
}

    // ============================
    //         NAVIGATION
    // ============================
    @FXML
    public void goBackToDashboard(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(
                getClass().getResource("/org/example/eqlab2/dashboard-view.fxml")
                );
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 400, 650));
    }


}
