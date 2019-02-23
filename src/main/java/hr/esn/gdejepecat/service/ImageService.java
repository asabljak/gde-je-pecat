package hr.esn.gdejepecat.service;

import hr.esn.gdejepecat.model.UIData;
import hr.esn.gdejepecat.exception.GdeJePecatException;
import hr.esn.gdejepecat.helper.FileFormats;
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
    private AtomicInteger finished;
    private AtomicInteger failed;
    private ExecutorService executorService;
    private int imagesTotal;
    private BufferedImage bufferedImage;
    private UIData uiData;

    public void putLogo(UIData uiData) throws GdeJePecatException {
        this.uiData = uiData;
        File destination;
        File[] allPhotos = uiData.getImagesDirectory().listFiles(getImageFilter());
        imagesTotal = allPhotos.length;
        finished = new AtomicInteger();
        failed = new AtomicInteger();

        destination = new File(uiData.getImagesDirectory().getAbsolutePath() + "/gde_je_pecat/");
        destination.mkdirs();

        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for(File photo : allPhotos) {
            executorService.submit(() -> mergeImages(photo, false));
        }
    }

    public BufferedImage getPreviewImage(UIData uiData) throws GdeJePecatException{
        this.uiData = uiData;
        failed = new AtomicInteger();
        File[] photos = uiData.getImagesDirectory().listFiles(getImageFilter());

        if (photos != null && photos.length > 0) {
            mergeImages(photos[0], true);
        } else {
            throw new GdeJePecatException("Path to images folder or logo is not properly set");
        }

        return bufferedImage;
    }

    public boolean isWorkInProgress() {
        return !executorService.isTerminated();
    }

    public int getTotalImageNumber() {
        return this.imagesTotal;
    }

    public int getFinishedImagesNumber() {
        return  finished.get();
    }

    public int getFailed() {
        return failed.get();
    }

    public void terminate() {
        executorService.shutdownNow();
    }

    private void mergeImages(File photo, boolean preview) {
        BufferedImage sourceImage = null;
        int width = uiData.getWidth();
        int height = uiData.getHeight();
        double logoScale = uiData.getScaleFactor();
        float logoOpacity = uiData.getOpacity();
        int logoHorizontalOffset = uiData.getxOffset();
        int logoVerticalOffset = uiData.getyOffset();

        try {
            sourceImage =  ImageIO.read(photo);
            BufferedImage watermarkImage = ImageIO.read(uiData.getLogo());

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
                 failed.getAndIncrement();
                 throw new GdeJePecatException("Logo position is outside of source image.");
            }

            // paints the image watermark
            g2d.drawImage(watermarkImage, topLeftX, topLeftY, null);
            g2d.dispose();

            System.out.println("The image watermark is added to the image " + photo.getAbsolutePath());

        } catch (IOException ex) {
            System.err.println(photo.getAbsolutePath() + " - " +  ex);
        }

        bufferedImage = sourceImage;

        if (!preview) {
            File destination = new File(uiData.getImagesDirectory().getAbsolutePath() + "/gde_je_pecat/");
            File resultImage = new File(destination.getAbsolutePath() + "/" + photo.getName());
            finished.getAndIncrement();
            try {
                ImageIO.write(sourceImage, "jpeg", resultImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
