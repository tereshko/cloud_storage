package controllers;

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
import javafx.util.Callback;
import utils.FileInfo;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Controller_main implements Initializable {
    @FXML
    TableView<FileInfo> clientFilesTable;

    @FXML
    ComboBox<String> diskBox;

    @FXML
    TextField clientPathField;

    @FXML
    Button clientUpAction;

    @FXML
    VBox clientPanel, serverPanel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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


        clientFilesTable.getColumns().addAll(fileTypeColumn, fileNameColumn, fileSizeColumn, fileLastModifiedColumn);


        clientFilesTable.getSortOrder().add(fileTypeColumn);

        diskBox.getItems().clear();
        for (Path p : FileSystems.getDefault().getRootDirectories()) {
            diskBox.getItems().add(p.toString());
        }
        diskBox.getSelectionModel().select(0);

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

    public void buttonCopy(ActionEvent actionEvent) {
        clientPanel.getProperties().get("ctrl")
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
