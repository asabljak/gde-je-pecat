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

public class ImageService {
    public void putLogo(UIData uiData) throws GdeJePecatException {
        File destination;
        BufferedImage sourceImage;

        destination = new File(uiData.getImagesDirectory().getAbsolutePath() + "/gde_je_pecat/");
        destination.mkdirs();

        for(File photo : uiData.getImagesDirectory().listFiles(getImageFilter())) {
            System.out.println(photo.getAbsolutePath());
            sourceImage = mergeImages(photo, uiData.getLogo(), uiData.getxOffset(), uiData.getyOffset(), uiData.getOpacity(),
                    uiData.getScaleFactor(), uiData.getWidth(), uiData.getHeight());

            destination = new File(destination.getAbsolutePath() + "/" + photo.getName());

            try {
                ImageIO.write(sourceImage, "jpeg", destination);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public BufferedImage getPreviewImage(UIData uiData) throws GdeJePecatException{
        File photo = uiData.getImagesDirectory().listFiles(getImageFilter())[0];
        return mergeImages(photo, uiData.getLogo(), uiData.getxOffset(), uiData.getyOffset(), uiData.getOpacity(),
                uiData.getScaleFactor(), uiData.getWidth(), uiData.getHeight());

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
                BufferedImage tmpImage = Thumbnails.of(sourceImage)
                        .size(width, height)
                        .asBufferedImage();
                sourceImage = tmpImage;
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
                throw new GdeJePecatException("Logo position is outside of source image.");
            }

            // paints the image watermark
            g2d.drawImage(watermarkImage, topLeftX, topLeftY, null);
            g2d.dispose();

            System.out.println("The image watermark is added to the image.");

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
