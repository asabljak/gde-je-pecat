package hr.esn.gdejepecat.javafx;

import hr.esn.gdejepecat.service.ImageService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ProgressController implements Initializable {
    @FXML
    ProgressBar pbFinished;

    @FXML
    Label lblFinished;

    @FXML
    Button bttnCancel;

    ImageService imageService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pbFinished.setProgress(0.0);
        showProgress();
    }

    class BgThread implements Runnable {
        @Override
        public void run() {
            double finished = -1;
            double total = 0;
            while (imageService.isWorkInProgress() && finished < total) {
                finished = imageService.getNumberOfFinishedImages();
                total = imageService.getTotalImageNumber();
                System.out.println("Finished: " + finished + " Total: " + total);
                if(finished != 0 && total != 0 ) {
                    pbFinished.setProgress(finished / total);
                    System.out.println("Progress: " + finished/total);
                }
                int finalFinished = (int)finished;
                int finalTotal = (int)total;
                Platform.runLater(() -> lblFinished.setText("Finished: " + finalFinished + " / " + finalTotal));
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            Thread.currentThread().interrupt();
            if (finished == total) {
                Platform.runLater(() -> showInfo("All images edited successfully!"));
            }
            Platform.runLater(() -> terminate()); //FIXME ovo nije ok
        }
    }

    @FXML
    public void showProgress() {
//        Thread thread = new Thread(new BgThread());
//        thread.start();
    }

    @FXML
    public void terminate() {
        imageService.terminate();
        Stage stage = (Stage) bttnCancel.getScene().getWindow();
        stage.close();
    }

    public void setImageService(ImageService imageService) {
        this.imageService = imageService;
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Done");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
