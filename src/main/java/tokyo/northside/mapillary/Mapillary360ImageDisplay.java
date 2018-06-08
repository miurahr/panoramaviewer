package tokyo.northside.mapillary;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPIterator;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.properties.XMPPropertyInfo;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.xmp.XmpDirectory;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class Mapillary360ImageDisplay extends MapillaryAbstractImageDisplay {
    private final BufferedImage offscreenImage;
    private final int offscreenSizeX = 800;
    private final int offscreenSizeY = 600;
    private Rectangle viewRect;
    private static final double FOV = Math.toRadians(110);
    private final double cameraPlaneDistance;
    private double rayVecs[][][];
    private static final double ACCURACY_FACTOR = 2048;
    private static final int REQUIRED_SIZE = (int) (2 * ACCURACY_FACTOR);
    private double[] asinTable;
    private double[] atan2Table;
    private static final double INV_PI = 1 / Math.PI;
    private static final double INV_2PI = 1 / (2 * Math.PI);
    private double currentPsi, currentTheta;

    static boolean is360Image(InputStream imageStream) {
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

    private class ImageDisplayMouseListener implements MouseListener, MouseWheelListener, MouseMotionListener {
        private long lastTimeForMousePoint;

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            Image image;
            Rectangle visibleRect;

            synchronized (Mapillary360ImageDisplay.this) {
                image = getImage();
            }
            if (image != null) {
                if (e.getWhen() - this.lastTimeForMousePoint > 1500) {
                    this.lastTimeForMousePoint = e.getWhen();
                }

                // Set the zoom to the visible rectangle in image coordinates
                // FIXME: implement me
                if (e.getWheelRotation() > 0) {
                    // increment zoom, should keep xy ratio
                    visibleRect = new Rectangle(200,150,600,450);
                } else {
                    // decrement zoom
                    visibleRect = new Rectangle(0, 0, 800,600);
                }
                synchronized (Mapillary360ImageDisplay.this) {
                    Mapillary360ImageDisplay.this.viewRect = visibleRect;
                }
                Mapillary360ImageDisplay.this.repaint();
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            // Move the center to the clicked point.
            Image image;
            Rectangle visibleRect;
            synchronized (Mapillary360ImageDisplay.this) {
                image = getImage();
                visibleRect = Mapillary360ImageDisplay.this.viewRect;
            }
            if (image != null) {
                // Calculate the translation to set the clicked point the center of
                // the view.
                Point click = comp2imgCoord(visibleRect, e.getX(), e.getY());
                Point center = new Point(offscreenImage.getWidth() / 2, offscreenImage.getHeight() / 2);

                // FIXME: should convert clicked point to rotationXY correctly.
                double deltaTheta = (double)(click.x - center.x) * (1.0 / (offscreenSizeX / 2) * 10);
                double deltaPsi = (double)(click.y - center.y) * ( 1.0 / (offscreenSizeY / 2) * 10);
                currentTheta += (deltaTheta - currentTheta) * 0.25;
                currentPsi += (deltaPsi - currentPsi)  * 0.25;
                Mapillary360ImageDisplay.this.repaint();
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseDragged(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mouseMoved(MouseEvent e) {
        }

    }

    public Mapillary360ImageDisplay() {
        ImageDisplayMouseListener mouseListener = new ImageDisplayMouseListener();
        addMouseListener(mouseListener);
        addMouseWheelListener(mouseListener);
        addMouseMotionListener(mouseListener);

        offscreenImage = new BufferedImage(offscreenSizeX, offscreenSizeY, BufferedImage.TYPE_3BYTE_BGR);

        viewRect = new Rectangle(0, 0, offscreenImage.getWidth(), offscreenImage.getHeight());
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

    /**
     * Sets a new picture to be displayed.
     *
     * @param image The picture to be displayed.
     * @param detections image detections
     */
    @Override
    public void setImage(BufferedImage image, Collection<ImageDetection> detections) {
        synchronized (this) {
            this.image = image;
            this.detections.clear();
            if (detections != null) {
                this.detections.addAll(detections);
            }
            if (image != null) {
                // reset view size
                this.viewRect = new Rectangle(0, 0, offscreenImage.getWidth(), offscreenImage.getHeight());
            }
        }
        repaint();
    }

    private void redrawOffscreenImage(BufferedImage image) {
        double sinRotationX = Math.sin(currentPsi);
        double cosRotationX = Math.cos(currentPsi);
        double sinRotationY = Math.sin(currentTheta);
        double cosRotationY = Math.cos(currentTheta);
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
        BufferedImage image;
        synchronized (this) {
            image = this.image;
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
            synchronized (this) {
                redrawOffscreenImage(image);
            }
            g.drawImage(offscreenImage, 0, 0, offscreenImage.getWidth(null),
                    offscreenImage.getHeight(null),
                    viewRect.x, viewRect.y, viewRect.x
                    + viewRect.width, viewRect.y + viewRect.height, null);
        }
    }

}
