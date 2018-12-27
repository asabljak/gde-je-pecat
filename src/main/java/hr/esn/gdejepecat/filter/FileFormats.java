package hr.esn.gdejepecat.filter;

public class FileFormats {
    private static final String[] supportedFormats = new String[]{
            "gif", "png", "bmp", "jpg", "jpeg", "GIF", "PNG", "BMP", "JPG", "JPEG"
    };

    public static String[] getSupportedFormats() {
        return supportedFormats;
    }

    public static String[] getSupportedFormatsForFileChooser() {
        String[] extensions = new String[supportedFormats.length];
        for (int i = 0; i < supportedFormats.length; i++) {
            extensions[i] = "*." + supportedFormats[i];
        }
        return extensions;
    }
}
