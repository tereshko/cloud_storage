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

public class Controller_registration {
    Logger LOGGER = Logger.getLogger(Controller_registration.class);

    @FXML
    Button button_back;

    public void back(ActionEvent actionEvent) {
        Stage stage = (Stage) button_back.getScene().getWindow();
        stage.close();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/signin_modal.fxml"));

        Parent sigin_scene = null;
        try {
            sigin_scene = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stage = new Stage();
        stage.setTitle("Cloud Client");
        stage.setScene(new Scene(sigin_scene));
        stage.show();
    }

    @FXML
    Button button_registration;
    @FXML
    TextField textField_username;
    @FXML
    PasswordField passwordField_password;
    @FXML
    PasswordField passwordField_repeat_password;
    @FXML
    Label label_error;

    public void buttonRegistration(ActionEvent actionEvent) {
        label_error.setText("");

        String userName = textField_username.getText();
        String pass = getCryptPass(passwordField_password.getText());
        String repeatPass = getCryptPass(passwordField_repeat_password.getText());

        boolean isPasswordEquals = pass.equals(repeatPass);

        if (isPasswordEquals) {
            LOGGER.info("username:" + userName + "password:" + pass);
            String sendToServer = "registration\n" + "\n" + userName + "\n:" + pass;
            LOGGER.info(sendToServer);

            LOGGER.info("registration. message to server: " + sendToServer);

            Connections connection = new Connections();
            String answerFromServer = connection.getSendToServer(sendToServer);
            LOGGER.info("buttonLogIn. answerFromServer: " + answerFromServer);

            if (answerFromServer.equals("regNotOK")) {
                label_error.setText("user not registered");
            } else {
                label_error.setText("user registered");
            }
        } else {
            label_error.setText("Passwords not equals");
        }

    }

    public void buttonExitAction(ActionEvent actionEvent) {
        Platform.exit();
    }

    private String getCryptPass(String pass) {
        return GFG.toHexString(GFG.getSHA(pass));
    }
}
