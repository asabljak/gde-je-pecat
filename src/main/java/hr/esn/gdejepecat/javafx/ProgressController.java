package hr.esn.gdejepecat.javafx;

import hr.esn.gdejepecat.service.ImageService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

import static hr.esn.gdejepecat.helper.AlertHelper.showInfo;
import static hr.esn.gdejepecat.helper.AlertHelper.showWarning;

public class ProgressController implements Initializable {
    @FXML
    ProgressBar pbFinished;

    @FXML
    Label lblFinished;

    @FXML
    Button bttnCancel;

    private ImageService imageService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pbFinished.setProgress(0.0);
        showProgress();
    }

    class BgThread implements Runnable {
        @Override
        public void run() {
            int finished = -1;
            int total = 0;
            while (imageService.isWorkInProgress() && finished < total) {
                finished = imageService.getFinishedImagesNumber() + imageService.getFailed();
                total = imageService.getTotalImageNumber();
                int finalFinished = finished; //this must be effectively final
                int finalTotal = total; //this must be effectively final

                System.out.println("Finished: " + finished + " Total: " + total);
                if(finished != 0 && total != 0 ) {
                    Platform.runLater(() -> pbFinished.setProgress((double) finalFinished / finalTotal));
                    System.out.println("Progress: " + (double) finished/total);
                }
                Platform.runLater(() -> lblFinished.setText("Finished: " + finalFinished + " / " + finalTotal));
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            Thread.currentThread().interrupt();
            if (imageService.getFailed() > 0 && finished == total) {
                Platform.runLater(() -> showWarning("Done. " + imageService.getFailed() + " image(s) editing failed!"));
            }
            else if (finished == total) {
                Platform.runLater(() -> showInfo("All images edited successfully!"));
            }
            Platform.runLater(() -> terminate());
        }
    }

    @FXML
    private void showProgress() {
        Thread thread = new Thread(new BgThread());
        thread.start();
    }

    @FXML
    private void terminate() {
        imageService.terminate();
        Stage stage = (Stage) bttnCancel.getScene().getWindow();
        stage.close();
    }

    public void setImageService(ImageService imageService) {
        this.imageService = imageService;
    }
}
