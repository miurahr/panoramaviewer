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
import java.util.stream.IntStream;

public class Mapillary360ImageDisplay extends MapillaryAbstractImageDisplay {
    private final BufferedImage offscreenImage;
    private Rectangle viewRect;
    private VectorUtil vectorUtil;

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

                // FIXME: should convert clicked point correctly.
                double deltaTheta = (double)(click.x - center.x)  / (offscreenImage.getWidth() / 2);
                double deltaPsi = (double)(click.y - center.y) / (offscreenImage.getHeight() / 2);
                vectorUtil.setRotationDelta(deltaTheta ,deltaPsi);
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

    public Mapillary360ImageDisplay(int width, int height) {
        ImageDisplayMouseListener mouseListener = new ImageDisplayMouseListener();
        addMouseListener(mouseListener);
        addMouseWheelListener(mouseListener);
        addMouseMotionListener(mouseListener);

        offscreenImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

        viewRect = new Rectangle(0, 0, offscreenImage.getWidth(), offscreenImage.getHeight());

        vectorUtil = new VectorUtil();
        double FOV = Math.toRadians(110);
        double cameraPlaneDistance = (offscreenImage.getWidth() / 2) / Math.tan(FOV / 2);
        vectorUtil.setCameraScreen(offscreenImage.getWidth(), offscreenImage.getHeight(), cameraPlaneDistance);
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
        int height = offscreenImage.getHeight();
        int width = offscreenImage.getWidth();
        IntStream.range(0, height).forEach(y -> {
            IntStream.range(0, width).forEach(x -> {
                Vector3D vec = vectorUtil.getVector3D(x, y);
                // https://en.wikipedia.org/wiki/UV_mapping
                double u = 0.5 - (vectorUtil.atan2(vec.getX(), vec.getZ()) * VectorUtil.INV_2PI);
                double v = 0.5 + (vectorUtil.asin(vec.getY()) * VectorUtil.INV_PI);
                int tx = (int) ((image.getWidth() - 1) * u);
                int ty = (int) ((image.getHeight() - 1) * v);
                int color = image.getRGB(tx, ty);
                offscreenImage.setRGB(x, y, color);
            });
        });
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
