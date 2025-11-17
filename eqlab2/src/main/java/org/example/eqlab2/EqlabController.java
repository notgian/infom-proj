package org.example.eqlab2;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class EqlabController {

    private Stage getStage(ActionEvent event) {
        return (Stage) ((Node) event.getSource()).getScene().getWindow();
    }
    //IOException is used so the app doesn't do other shit when it bugs
    //basically it's there to terminate the app incase there's something wrong
    // i wanted to try it too hehe

    //load new scene or page better... i added it cuz i wanted to try it out cuz i saw it somewhere
    private void loadScene(ActionEvent event, String fxml) throws IOException {
        Parent root = FXMLLoader.load(
                getClass().getResource("/org/example/eqlab2/" + fxml)
        );
        getStage(event).setScene(new Scene(root, 400, 650));
    }

    // the method used when u click the login button on the home page
    // it'll obv take u to the login page but i js wanted to write this comment
    @FXML
    public void goToLogin(ActionEvent event) throws IOException {
        loadScene(event, "login-view.fxml");
    }

    // the method used when u click the register button on the home page
    @FXML
    public void goToRegister(ActionEvent event) throws IOException {
        loadScene(event, "register-view.fxml");
    }

    // this is js the back btn
    @FXML
    public void goBackToHome(ActionEvent event) throws IOException {
        loadScene(event, "homepg-view.fxml");
    }

    //we don't hv a database yet so this button will direct us to the dashboard
    //can be deleted later
    @FXML
    public void goDirectDashboard(ActionEvent event) throws IOException {
        loadScene(event, "dashboard-view.fxml");
    }
}
