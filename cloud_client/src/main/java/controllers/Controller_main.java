package controllers;

import Network.Client;
import Network.ClientCommands;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import utils.FileInfo;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Controller_main implements Initializable {
    @FXML
    TableView<FileInfo> clientFilesTable;
    @FXML
    TableView<FileInfo> serverFilesTable;

    @FXML
    ComboBox<String> diskBox;

    @FXML
    TextField clientPathField;

    @FXML
    Button clientUpAction;


    @FXML
    VBox clientPanel, serverPanel;

    private Client client = Client.getInstance();
    ClientCommands clientCommands = new ClientCommands();
    private List<FileInfo> serverFileList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Tables.prepareTable(clientFilesTable);
        Tables.comboBoxPrepare(diskBox);
        Tables.prepareTable(serverFilesTable);

        clientFilesTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    clientUpAction.setDisable(false);
                    Path path = Paths.get(clientPathField.getText())
                            .resolve(clientFilesTable.getSelectionModel().getSelectedItem().getFileName());
                    if (Files.isDirectory(path)) {
                        updateFileListClient(path);
                    }
                }
            }
        });
        updateFileListClient(Paths.get("."));
        clientCommands.sendCommand(client.getCurrentChannel(), "SERVER_FILE_LIST");

        client.getHandler().getCallback(MESSAGE ->{
            String[] command = MESSAGE.split("\n");
            if (command[0].equals("SERVER_FILE_LIST")) {
                if (MESSAGE.split("\n").length != 1){
                    serverFileList = clientCommands.createFileList(MESSAGE.split("\n", 2)[1]);
                    Platform.runLater(() -> {
                        serverFilesTable.getItems().clear();
                        serverFileList.forEach(o -> serverFilesTable.getItems().add(o));
                        serverFilesTable.sort();
                    });} else {
                    Platform.runLater(() -> {
                        serverFilesTable.getItems().clear();
                    });
                }
            }
        });

    }

    public void updateFileListClient(Path path) {
        clientFilesTable.getItems().clear();
        try {
            clientPathField.setText(path.normalize().toAbsolutePath().toString());
            clientFilesTable.getItems().addAll(Files.list(path).map(FileInfo::new).collect(Collectors.toList()));
            clientFilesTable.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "can not update file list", ButtonType.OK);
            alert.showAndWait();
        }
    }


    public void buttonClientPathUpAction(ActionEvent actionEvent) {
        Path upperPath = Paths.get(clientPathField.getText()).getParent();
        if (upperPath != null) {
            updateFileListClient(upperPath);
        }

        upperPath = Paths.get(clientPathField.getText()).getParent();
        if (upperPath.toString().equals("/")) {
            clientUpAction.setDisable(true);
        }
    }

    public void buttonClientFileListRefresh(ActionEvent actionEvent) {

    }

    public void selectClientDiskAction(ActionEvent actionEvent) {
        clientUpAction.setDisable(false);
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
        updateFileListClient(Paths.get(element.getSelectionModel().getSelectedItem()));
    }

    public void buttonServerPathUpAction(ActionEvent actionEvent) {
    }

    public void buttonServerFileListRefresh(ActionEvent actionEvent) {
    }

    public void buttonNewFolder(ActionEvent actionEvent) {
    }

    //TODO button copy. need to implement
    public void buttonCopy(ActionEvent actionEvent) {
        clientPanel.getProperties().get("ctrl");
    }

    public void buttonDelete(ActionEvent actionEvent) {
    }

    public void buttonExitAction(ActionEvent actionEvent) {
        Platform.exit();
    }

    public String getSelectedFileName() {
        if (!clientFilesTable.isFocused()) {
            return null;
        }
        return clientFilesTable.getSelectionModel().getSelectedItem().getFileName();
    }

    public String getCurrentPath() {
        return clientPathField.getText();
    }
}
