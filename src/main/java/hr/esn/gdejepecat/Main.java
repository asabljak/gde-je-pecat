package hr.esn.gdejepecat;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
    private static Stage pStage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/main-screen.fxml"));
        primaryStage.setTitle("Gde je pečat?");
        primaryStage.setScene(new Scene(root, 750, 500));
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(new Image("/icons/icon.png"));
        pStage = primaryStage;
        primaryStage.show();
    }

    public static Stage getPrimaryStage() {
        return pStage;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
