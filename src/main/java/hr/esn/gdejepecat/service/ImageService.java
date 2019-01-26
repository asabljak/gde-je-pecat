package hr.esn.gdejepecat.service;

import hr.esn.gdejepecat.model.UIData;
import hr.esn.gdejepecat.exception.GdeJePecatException;
import hr.esn.gdejepecat.filter.FileFormats;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageService {
    AtomicInteger finished;
    ExecutorService executorService;
    Future<Void> executionResult;
    int imagessTotal;

    public ImageService() {
        finished = new AtomicInteger();
    }

    public Future<Void> putLogo(UIData uiData) throws GdeJePecatException {
        File destination;
        File[] allPhotos = uiData.getImagesDirectory().listFiles(getImageFilter());
        imagessTotal = allPhotos.length;

        destination = new File(uiData.getImagesDirectory().getAbsolutePath() + "/gde_je_pecat/");
        destination.mkdirs();

        executionResult = null;
        long begin = System.currentTimeMillis();
        executorService = Executors.newFixedThreadPool(4);

        for(File photo : allPhotos) {
            ImageService imageService = new ImageService();

            File resultImage = new File(destination.getAbsolutePath() + "/" + photo.getName());

            executionResult = executorService.submit(() -> {
                BufferedImage sourceImage = imageService.mergeImages(photo, uiData.getLogo(), uiData.getxOffset(), uiData.getyOffset(), uiData.getOpacity(),
                        uiData.getScaleFactor(), uiData.getWidth(), uiData.getHeight());
                try {
                    ImageIO.write(sourceImage, "jpeg", resultImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finished.getAndIncrement();
                return null;
            });
        }

//        executorService.shutdown();
        try {
            executionResult.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new GdeJePecatException(e.getCause().getMessage());
        }

        long end = System.currentTimeMillis();
        long time = end - begin;
        System.out.println("Duration: " + time + " milis");

        return executionResult;
    }

    public BufferedImage getPreviewImage(UIData uiData) throws GdeJePecatException{
        File photo = uiData.getImagesDirectory().listFiles(getImageFilter())[0];
        return mergeImages(photo, uiData.getLogo(), uiData.getxOffset(), uiData.getyOffset(), uiData.getOpacity(),
                uiData.getScaleFactor(), uiData.getWidth(), uiData.getHeight());

    }

    public boolean isWorkInProgress() {
        return !executorService.isTerminated();
    }

    public int getTotalImageNumber() {
        return this.imagessTotal;
    }

    public int getNumberOfFinishedImages() {
        return  finished.get();
    }

    public void terminate() {
        //executorService.shutdownNow();
        if(executionResult != null) {
            executionResult.cancel(true);
        }
    }

    private BufferedImage mergeImages(File photo, File logo, int logoHorizontalOffset, int logoVerticalOffset,
                                      float logoOpacity, float logoScale, int width, int height) throws GdeJePecatException{
        BufferedImage sourceImage = null;
        try {
            sourceImage =  ImageIO.read(photo);
            BufferedImage watermarkImage = ImageIO.read(logo);

            // resize original image
            if((sourceImage.getWidth() != width || sourceImage.getHeight() != height)
                    && (width > 0 && height > 0)) {
                sourceImage = Thumbnails.of(sourceImage)
                        .size(width, height)
                        .asBufferedImage();
            }

            // shrink logo size
            if (logoScale < 1.0) {
                watermarkImage = Thumbnails.of(watermarkImage)
                        .size((int)(watermarkImage.getWidth() * logoScale),
                                (int)(watermarkImage.getHeight() * logoScale))
                        .asBufferedImage();
            }

            // initializes necessary graphic properties
            Graphics2D g2d = (Graphics2D) sourceImage.getGraphics();
            AlphaComposite alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, logoOpacity);
            g2d.setComposite(alphaChannel);

            // calculates the coordinate where the image is painted
            int topLeftX = (sourceImage.getWidth() - watermarkImage.getWidth()) - logoHorizontalOffset;
            int topLeftY = (sourceImage.getHeight() - watermarkImage.getHeight()) - logoVerticalOffset;

            if(topLeftX < 0 || topLeftY < 0) {
                 System.out.println(photo.getName() + " - Logo position is outside of source image");
                 throw new GdeJePecatException("Logo position is outside of source image.");
            }

            // paints the image watermark
            g2d.drawImage(watermarkImage, topLeftX, topLeftY, null);
            g2d.dispose();

            System.out.println("The image watermark is added to the image " + photo.getAbsolutePath());

        } catch (IOException ex) {
            System.err.println(photo.getAbsolutePath() + " - " +  ex);
        }

        return sourceImage;
    }

    private FilenameFilter getImageFilter() {
        return (dir, name) -> {
            for (final String ext : FileFormats.getSupportedFormats()) {
                if (name.endsWith("." + ext)) {
                    return (true);
                }
            }
            return (false);
        };
    }
}
