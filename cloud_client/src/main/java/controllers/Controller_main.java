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
import utils.FileType;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
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
    TextField clientPathField, serverPathFiled;

    @FXML
    Button clientUpAction;

    @FXML
    VBox clientPanel, serverPanel;

    private Client client = Client.getInstance();
    ClientCommands clientCommands = new ClientCommands();
    private List<FileInfo> serverFileList;

    private String serverPathName = "user/";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        prepareTable(clientFilesTable);
        comboBoxPrepare(diskBox);
        prepareTable(serverFilesTable);

        serverPathFiled.setText(serverPathName + client.getHandler().getCurrentPath().toString());


        updateFileListClient(Paths.get("."));
        clientCommands.sendCommand(client.getCurrentChannel(), "SERVER_FILE_LIST");

        client.getHandler().getCallback(MESSAGE -> {
            System.out.println("!!!MESSAGE: " + MESSAGE);
            String[] command = MESSAGE.split("\n");
            if (command[0].equals("SERVER_FILE_LIST")) {
                if (MESSAGE.split("\n").length != 1) {
                    serverFileList = clientCommands.createFileList(MESSAGE.split("\n", 2)[1]);
                    Platform.runLater(() -> {
                        serverFilesTable.getItems().clear();
                        serverFileList.forEach(o -> serverFilesTable.getItems().add(o));
                        serverFilesTable.sort();
                    });
                } else {
                    Platform.runLater(() -> {
                        serverFilesTable.getItems().clear();
                    });
                }
            } else if (command[0].equals("serverPath")){
                if (MESSAGE.split("\n").length != 1) {
                    serverPathFiled.setText(command[1]);
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
        clientCommands.sendCommand(client.getCurrentChannel(), "upDirectory");
    }

    public void buttonServerFileListRefresh(ActionEvent actionEvent) {
        clientCommands.sendCommand(client.getCurrentChannel(), "updateServerFilesList");

    }

    public void buttonNewFolder(ActionEvent actionEvent) {
        TextInputDialog textInputDialog = new TextInputDialog();
        textInputDialog.setTitle("New folder");
        String folderName = String.valueOf(textInputDialog.showAndWait());
        int startNum = 9;
        int endNum = folderName.length() - 1;
        folderName = folderName.substring(startNum, endNum);
        System.out.println("FOLDER NAME: " + folderName);

        if (!folderName.isEmpty()) {
            if (clientFilesTable.isFocused()) {
                clientCommands.createDirectory(Paths.get(getCurrentPath()), folderName);
                updateFileListClient(Paths.get(getCurrentPath()));
            }

            if (serverFilesTable.isFocused()) {
                clientCommands.sendCommand(client.getCurrentChannel(), "mkdir\n" + folderName);
            }
        }

    }

    public void buttonCopy(ActionEvent actionEvent) {
        System.out.println("current path: " + client.getHandler().getCurrentPath());
        if (clientFilesTable.isFocused()) {
            System.out.println("CLIENT PANEL");
            clientCommands.uploadFile(client.getCurrentChannel(), null, getSelectedFile());
        }

        if (serverFilesTable.isFocused()) {
            client.getHandler().setCurrentPath(Paths.get(getCurrentPath()));
            clientCommands.sendCommand(client.getCurrentChannel(), "download\n" + getSelectedFileName());
            updateFileListClient(Paths.get(getCurrentPath()));
        }

    }

    public void buttonDelete(ActionEvent actionEvent) {
        if (serverFilesTable.isFocused()) {
            String fileName = getSelectedFileName();
            clientCommands.sendCommand(client.getCurrentChannel(), "deleteFile\n" + fileName);
        }

        if (clientFilesTable.isFocused()) {
            Path path = getSelectedFile();
            clientCommands.deleteFile(path);
            updateFileListClient(Paths.get(getCurrentPath()));
        }
    }

    public void buttonExitAction(ActionEvent actionEvent) {
        Platform.exit();
    }

    public String getSelectedFileName() {
        String fileName = null;

        if (clientFilesTable.isFocused()) {
            fileName = clientFilesTable.getSelectionModel().getSelectedItem().getFileName();
        } else if (serverFilesTable.isFocused()) {
            fileName = serverFilesTable.getSelectionModel().getSelectedItem().getFileName();
        }

        if (fileName == null) {
            Alert alertError = new Alert(Alert.AlertType.ERROR, "File not selected", ButtonType.OK);
            alertError.showAndWait();
        }
        return fileName;
    }

    public Path getSelectedFile() {
        return Paths.get(getCurrentPath(), getSelectedFileName());
    }

    public String getCurrentPath() {
        return clientPathField.getText();
    }

    private void prepareTable(TableView<FileInfo> tableView) {
        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>();
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        fileTypeColumn.setPrefWidth(24);

        TableColumn<FileInfo, String> fileNameColumn = new TableColumn<>("Name");
        fileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        fileNameColumn.setPrefWidth(240);

        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Size");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeColumn.setCellFactory(column -> {
            return new TableCell<FileInfo, Long>() {
                @Override
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        String text = String.format("%,d bytes", item);
                        if (item == -1L) {
                            text = "[DIR]";
                        }
                        setText(text);
                    }
                }
            };
        });
        fileNameColumn.setPrefWidth(120);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        TableColumn<FileInfo, String> fileLastModifiedColumn = new TableColumn<>("Updated");
        fileLastModifiedColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified()
                .format(dateTimeFormatter)));
        fileLastModifiedColumn.setPrefWidth(120);


        tableView.getColumns().addAll(fileTypeColumn, fileNameColumn, fileSizeColumn, fileLastModifiedColumn);
        tableView.getSortOrder().add(fileTypeColumn);

        serverFilesTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    clientUpAction.setDisable(false);
                    if (tableView.getSelectionModel().getSelectedItem().getType() == FileType.DIRECTORY) {
                        clientCommands.sendCommand(client.getCurrentChannel(), "openDirectory\n" + getSelectedFileName());
                    }
                }
            }
        });

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
    }

    private void comboBoxPrepare(ComboBox<String> diskBox) {
        diskBox.getItems().clear();
        for (Path p : FileSystems.getDefault().getRootDirectories()) {
            diskBox.getItems().add(p.toString());
        }
        diskBox.getSelectionModel().select(0);
    }
}
