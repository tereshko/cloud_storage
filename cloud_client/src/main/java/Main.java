import Network.Client;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    private static Client client;

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = null;

        try {
            root = FXMLLoader.load(getClass().getResource("/signin_modal.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        primaryStage.setTitle("Cloud Client");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        Thread t = new Thread(() -> {
            client = new Client("localhost", 8989);
        });
        t.start();

        launch(args);
    }

}