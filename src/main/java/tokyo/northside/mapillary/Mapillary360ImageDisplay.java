package tokyo.northside.mapillary;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPIterator;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.properties.XMPPropertyInfo;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.xmp.XmpDirectory;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class Mapillary360ImageDisplay extends MapillaryAbstractImageDisplay {
    private final BufferedImage offscreenImage;
    private static final double FOV = Math.toRadians(110);
    private final double cameraPlaneDistance;
    private double rayVecs[][][];
    private static final double ACCURACY_FACTOR = 2048;
    private static final int REQUIRED_SIZE = (int) (2 * ACCURACY_FACTOR);
    private double[] asinTable;
    private double[] atan2Table;
    private static final double INV_PI = 1 / Math.PI;
    private static final double INV_2PI = 1 / (2 * Math.PI);
    private double currentRotationX, currentRotationY;

    public static boolean is360Image(InputStream imageStream) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(imageStream);
            XmpDirectory xmpDirectory = metadata.getFirstDirectoryOfType(XmpDirectory.class);
            XMPMeta xmpMeta = xmpDirectory.getXMPMeta();
            XMPIterator itr = xmpMeta.iterator();
            while (itr.hasNext()) {
                XMPPropertyInfo pi = (XMPPropertyInfo) itr.next();
                if (pi != null && pi.getPath() != null) {
                    if ((pi.getPath().endsWith("ProjectionType"))) {
                        String proj = pi.getValue();
                        if (proj.equals("equirectangular")) {
                            return true;
                        }
                    }
                }
            }
        } catch (NullPointerException | ImageProcessingException | IOException | XMPException e) {
            // ignore
        }
        return false;
    }

    public Mapillary360ImageDisplay() {
        offscreenImage = new BufferedImage(800, 600, BufferedImage.TYPE_3BYTE_BGR);
        cameraPlaneDistance = (offscreenImage.getWidth() / 2) / Math.tan(FOV / 2);
        createRayVecs();
        precalculateAsinAtan2();
    }

    private void createRayVecs() {
        rayVecs = new double[offscreenImage.getWidth()][offscreenImage.getHeight()][3]; // x, y, z
        for (int y = 0; y < offscreenImage.getHeight(); y++) {
            for (int x = 0; x < offscreenImage.getWidth(); x++) {
                double vecX = x - offscreenImage.getWidth() / 2;
                double vecY = y - offscreenImage.getHeight() / 2;
                double vecZ = cameraPlaneDistance;
                double invVecLength = 1 / Math.sqrt(vecX * vecX + vecY * vecY + vecZ * vecZ);
                rayVecs[x][y][0] = vecX * invVecLength;
                rayVecs[x][y][1] = vecY * invVecLength;
                rayVecs[x][y][2] = vecZ * invVecLength;
            }
        }
    }

    private void precalculateAsinAtan2() {
        asinTable = new double[REQUIRED_SIZE];
        atan2Table = new double[REQUIRED_SIZE * REQUIRED_SIZE];
        for (int i = 0; i < 2 * ACCURACY_FACTOR; i++) {
            asinTable[i] = Math.asin((i - ACCURACY_FACTOR) * 1 / ACCURACY_FACTOR);
            for (int j = 0; j < 2 * ACCURACY_FACTOR; j++) {
                double y = (i - ACCURACY_FACTOR) / ACCURACY_FACTOR;
                double x = (j - ACCURACY_FACTOR) / ACCURACY_FACTOR;
                atan2Table[i + j * REQUIRED_SIZE] = Math.atan2(y, x);
            }
        }
    }

    public void setViewPoint(int vx, int vy) {
        double targetRotationX = (vy - (offscreenImage.getHeight() / 2)) * 0.025;
        double targetRotationY = (vx - (offscreenImage.getWidth() / 2)) * 0.025;
        currentRotationX += (targetRotationX - currentRotationX) * 0.25;
        currentRotationY += (targetRotationY - currentRotationY) * 0.25;
    }

    public void redrawOffscreenImage() {
        double sinRotationX = Math.sin(currentRotationX);
        double cosRotationX = Math.cos(currentRotationX);
        double sinRotationY = Math.sin(currentRotationY);
        double cosRotationY = Math.cos(currentRotationY);
        double tmpVecX, tmpVecY, tmpVecZ;
        for (int y = 0; y < offscreenImage.getHeight(); y++) {
            for (int x = 0; x < offscreenImage.getWidth(); x++) {
                double vecX = rayVecs[x][y][0];
                double vecY = rayVecs[x][y][1];
                double vecZ = rayVecs[x][y][2];
                // rotate x
                tmpVecZ = vecZ * cosRotationX - vecY * sinRotationX;
                tmpVecY = vecZ * sinRotationX + vecY * cosRotationX;
                vecZ = tmpVecZ;
                vecY = tmpVecY;
                // rotate y
                tmpVecZ = vecZ * cosRotationY - vecX * sinRotationY;
                tmpVecX = vecZ * sinRotationY + vecX * cosRotationY;
                vecZ = tmpVecZ;
                vecX = tmpVecX;
                int iX = (int) ((vecX + 1) * ACCURACY_FACTOR);
                int iY = (int) ((vecY + 1) * ACCURACY_FACTOR);
                int iZ = (int) ((vecZ + 1) * ACCURACY_FACTOR);
                // https://en.wikipedia.org/wiki/UV_mapping
                double u = 0.5 + (atan2Table[iZ + iX * REQUIRED_SIZE] * INV_2PI);
                double v = 0.5 - (asinTable[iY] * INV_PI);
                int tx = (int) (image.getWidth() * u);
                int ty = (int) (image.getHeight() * (1 - v));

                if (tx >= image.getWidth()) {
                    tx = image.getWidth() - 1;
                }
                if (ty >= image.getHeight()) {
                    ty = image.getHeight() - 1;
                }

                int color = image.getRGB(tx, ty);
                offscreenImage.setRGB(x, y, color);
            }
        }
    }

    /**
     * Paints the visible part of the picture.
     */
    @Override
    public void paintComponent(Graphics g) {
        Image image;
        synchronized (this) {
            redrawOffscreenImage();
            image = this.offscreenImage;
        }
        if (image == null) {
            g.setColor(Color.black);
            String noImageStr = "No image selected";
            Rectangle2D noImageSize = g.getFontMetrics(g.getFont()).getStringBounds(
                    noImageStr, g);
            Dimension size = getSize();
            g.drawString(noImageStr,
                    (int) ((size.width - noImageSize.getWidth()) / 2),
                    (int) ((size.height - noImageSize.getHeight()) / 2));
        } else {
            g.drawImage(this.offscreenImage, 0, 0, null);
        }
    }

}
