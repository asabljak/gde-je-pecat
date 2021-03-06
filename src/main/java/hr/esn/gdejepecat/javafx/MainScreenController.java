package hr.esn.gdejepecat.javafx;

import hr.esn.gdejepecat.GdeJePecatApp;
import hr.esn.gdejepecat.exception.GdeJePecatException;
import hr.esn.gdejepecat.helper.FileFormats;
import hr.esn.gdejepecat.model.UIData;
import hr.esn.gdejepecat.service.ImageService;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.UnaryOperator;

import static hr.esn.gdejepecat.helper.AlertHelper.showWarning;

public class MainScreenController implements Initializable{
    private ImageService imageService;
    private File imagesDirectory = null;
    private File logo = null;

    @FXML
    Label lblFolderPath;

    @FXML
    GridPane gridWidth;

    @FXML
    GridPane gridHeight;

    @FXML
    CheckBox checkBoxResize;

    @FXML
    TitledPane photosPane;

    @FXML
    TitledPane logoPane;

    @FXML
    TextField txtWidth;

    @FXML
    TextField txtHeight;

    @FXML
    TextField txtXOffset;

    @FXML
    TextField txtYOffset;

    @FXML
    Label lblLogoPath;

    @FXML
    Slider sliderSize;

    @FXML
    Label lblSize;

    @FXML
    Slider sliderOpacity;

    @FXML
    Label lblOpacity;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gridWidth.setDisable(true);
        gridHeight.setDisable(true);
        photosPane.setCollapsible(false);
        logoPane.setCollapsible(false);

        TextFormatter<String> textFormatterHeight = new TextFormatter<>(getNumberFilter());
        TextFormatter<String> textFormatterWidth = new TextFormatter<>(getNumberFilter());
        TextFormatter<String> textFormatterXOffset = new TextFormatter<>(getNumberFilter());
        TextFormatter<String> textFormatterYOffset = new TextFormatter<>(getNumberFilter());

        txtHeight.setTextFormatter(textFormatterHeight);
        txtWidth.setTextFormatter(textFormatterWidth);
        txtXOffset.setTextFormatter(textFormatterXOffset);
        txtYOffset.setTextFormatter(textFormatterYOffset);

        lblOpacity.setText("Opacity: " + (int)(sliderOpacity.getValue() * 100) + "%");
        sliderOpacity.valueProperty().addListener(
                (observable, oldValue, newValue) -> lblOpacity.setText("Opacity: " + (int)(sliderOpacity.getValue() * 100) + "%")
        );

        lblSize.setText("Size: " + (int)(sliderSize.getValue() * 100) + "%");
        sliderSize.valueProperty().addListener(
                (observable, oldValue, newValue) -> lblSize.setText("Size: " + (int)(sliderSize.getValue() * 100) + "%")
        );

        imageService = new ImageService();
    }

    @FXML
    public void pickFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Pick images folder");
        File directoryPath = chooser.showDialog(GdeJePecatApp.getPrimaryStage());

        if(directoryPath != null && !directoryPath.getPath().equals("")) {
            imagesDirectory = directoryPath;
            lblFolderPath.setText(imagesDirectory.getAbsolutePath());
        }
    }

    @FXML
    public void pickLogo() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Pick logo");

        FileChooser.ExtensionFilter fileExtensions =
                new FileChooser.ExtensionFilter(
                        "Images", FileFormats.getSupportedFormatsForFileChooser());
        chooser.getExtensionFilters().add(fileExtensions);

        File pathToLogo = chooser.showOpenDialog(GdeJePecatApp.getPrimaryStage());

        if(pathToLogo != null && !pathToLogo.getPath().equals("")) {
            logo = pathToLogo;
            lblLogoPath.setText(logo.getAbsolutePath());
        }
    }

    @FXML
    public void checkboxResizeClicked() {
        if(checkBoxResize.isSelected()) {
            gridWidth.setDisable(false);
            gridHeight.setDisable(false);
        }
        else  {
            gridWidth.setDisable(true);
            gridHeight.setDisable(true);
        }
    }

    @FXML
    public void putLogos() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        if (imagesDirectory != null && logo != null) {
            executorService.submit(() -> imageService.putLogo(getUiData()));
        } else {
            showWarning("Path to images folder or logo is not properly set");
            return;
        }

        showProgress();
    }

    @FXML
    public void preview() {
        BufferedImage previewImage;
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/fxml/preview.fxml"));
        try {
            fxmlLoader.load();
            if (imagesDirectory != null && logo != null) {
                previewImage = imageService.getPreviewImage(getUiData());
            }  else {
                showWarning("Path to images folder or logo is not properly set");
                return;
            }

        }
        catch (IOException | GdeJePecatException e) {
                showWarning(e.getMessage());
                return;
        }

        PreviewController previewController = fxmlLoader.getController();
        previewController.setPreview(previewImage);

        Parent parent = fxmlLoader.getRoot();
        Stage stage = new Stage();
        stage.setTitle("Preview");
        stage.setScene(new Scene(parent, 1190, 870));
        stage.setResizable(false);
        stage.getIcons().add(new Image("/icons/icon.png"));
        stage.show();
    }

    private UIData getUiData() {
        int width = 0;
        int height = 0;
        int xOffset = 0;
        int yOffset = 0;
        float opacity = (float)sliderOpacity.getValue();
        float scaleFactor = (float)sliderSize.getValue();

        try {
            if(checkBoxResize.isSelected()) {
                width = Integer.parseInt(txtWidth.getText());
                height = Integer.parseInt(txtHeight.getText());
            }
            xOffset = Integer.parseInt(txtXOffset.getText());
            yOffset = Integer.parseInt(txtYOffset.getText());
        } catch (NumberFormatException e) {
            System.out.println("Sve vrijednosti nisu ispravno popunjene");
            showWarning("All values are not properly set");
        }

        return new UIData(imagesDirectory, logo, width, height, xOffset, yOffset, opacity, scaleFactor);
    }

    private UnaryOperator<TextFormatter.Change> getNumberFilter() {
        return change -> {
            String text = change.getText();

            if (text.matches("[0-9]*")) {
                return change;
            }

            return null;
        };
    }

    private void showProgress() {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/fxml/progress.fxml"));
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ProgressController progressController = fxmlLoader.getController();
        progressController.setImageService(imageService);

        Parent parent = fxmlLoader.getRoot();
        Stage stage = new Stage();
        stage.setTitle("Progress");
        stage.getIcons().add(new Image("/icons/icon.png"));
        stage.setScene(new Scene(parent));
        stage.setResizable(false);
        stage.setOnHiding(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Platform.runLater(() -> imageService.terminate());
            }
        });
        stage.show();
    }
}
