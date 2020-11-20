package controllers;

import Network.Client;
import Network.ClientCommands;

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

    private Client client = Client.getInstance();
    ClientCommands clientCommands = new ClientCommands();


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
            String sendToServer = "registration\n" + userName + "\n" + pass;

            LOGGER.info("registration. message to server: " + sendToServer);

            clientCommands.sendCommand(client.getCurrentChannel(), sendToServer);

            final String[] answerFromServer = new String[1];
            client.getHandler().getCallback(callback -> {
                answerFromServer[0] = callback;
            });

            label_error.setText("User Successfully Registered");
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
