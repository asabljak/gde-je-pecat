package hr.esn.gdejepecat.javafx;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;

public class PreviewController {
    @FXML
    ImageView imgPreview;

    @FXML
    Button bttnExit;

    public void setPreview(BufferedImage bufferedImage) {
        Image image = SwingFXUtils.toFXImage(bufferedImage, null);
        imgPreview.setImage(image);
    }

    @FXML
    public void exitPreview() {
        Stage stage = (Stage) bttnExit.getScene().getWindow();
        stage.close();
    }
}
