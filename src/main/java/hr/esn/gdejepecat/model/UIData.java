package hr.esn.gdejepecat.model;


import java.io.File;

public class UIData {
    private File imagesDirectory;
    private File logo;
    private int width;
    private int height;
    private int xOffset;
    private int yOffset;
    private float opacity;
    private float scaleFactor;

    public UIData(File imagesDirectory, File logo, int width, int height, int xOffset, int yOffset, float opacity, float scaleFactor) {
        this.imagesDirectory = imagesDirectory;
        this.logo = logo;
        this.width = width;
        this.height = height;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.opacity = opacity;
        this.scaleFactor = scaleFactor;
    }

    public File getImagesDirectory() {
        return imagesDirectory;
    }

    public File getLogo() {
        return logo;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getxOffset() {
        return xOffset;
    }

    public int getyOffset() {
        return yOffset;
    }

    public float getOpacity() {
        return opacity;
    }

    public float getScaleFactor() {
        return scaleFactor;
    }
}
