package controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller_main implements Initializable {
    @FXML
    TableView filesTable;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void buttonExitAction(ActionEvent actionEvent) {
        Platform.exit();
    }
}
