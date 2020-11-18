package controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import security.GFG;

import java.io.IOException;

public class Controller_signin {
    Logger LOGGER = Logger.getLogger(Controller_signin.class);


    public void buttonExitAction(ActionEvent actionEvent) {
        Platform.exit();
    }


    @FXML
    Button registration;

    public void buttonRegistration(ActionEvent actionEvent) {
        Stage stage = (Stage) registration.getScene().getWindow();
        stage.close();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/registration_modal.fxml"));

        Parent registration_scene = null;
        try {
            registration_scene = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stage = new Stage();
        stage.setTitle("Cloud Client");
        stage.setScene(new Scene(registration_scene));
        stage.show();
    }

    @FXML
    PasswordField passwordField_password;
    @FXML
    TextField textField_username;
    @FXML
    Label label_error;

    public void buttonLogIn(ActionEvent actionEvent) {
        String pass = getCryptPass(passwordField_password.getText());
        String userName = textField_username.getText();
        //TODO сделать проверку на то, что данные не пустые

        LOGGER.info("buttonLogIn." + " username: " + userName + " password: " + pass);

        String sendToServer = "authorization\n" + "\n" + userName + "\n" + pass;
        LOGGER.info("buttonLogIn. message to server: " + sendToServer);

        //Connections connection = new Connections();
        //connection.server_connection();
        //String answerFromServer = connection.getSendToServer(sendToServer);
        //LOGGER.info("buttonLogIn. answerFromServer: " + answerFromServer);
        String answerFromServer = "authOK";

        //-1 mean that user not found or some errors
        if (answerFromServer.equals("authOK")) {
            Stage stage = (Stage) registration.getScene().getWindow();
            stage.close();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main_modal.fxml"));

            Parent registration_scene = null;
            try {
                registration_scene = fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            stage = new Stage();
            stage.setTitle("Cloud Client");
            stage.setScene(new Scene(registration_scene));
            stage.show();
        } else {
            label_error.setText("User not found");
        }

    }

    private String getCryptPass(String pass) {
        return GFG.toHexString(GFG.getSHA(pass));
    }
}
