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
import java.util.concurrent.atomic.AtomicReference;

public class Controller_signin {
    Logger LOGGER = Logger.getLogger(Controller_signin.class);
    private Client client = Client.getInstance();
    ClientCommands clientCommands = new ClientCommands();


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

        String sendToServer = "authorization\n" + userName + "\n" + pass;

        clientCommands.sendCommand(client.getCurrentChannel(), sendToServer);

        client.getHandler().getCallback(callback -> {
            System.out.println("CLIENT: " + callback);
            if (callback.equals("notOk")) {
                Platform.runLater(() -> {
                            label_error.setText("User not found");
                        }
                );
            } else {
                changeScheme();
            }
        });
    }

    private void changeScheme() {
        Platform.runLater(() -> {
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

                }
        );
    }

    private String getCryptPass(String pass) {
        return GFG.toHexString(GFG.getSHA(pass));
    }
}
