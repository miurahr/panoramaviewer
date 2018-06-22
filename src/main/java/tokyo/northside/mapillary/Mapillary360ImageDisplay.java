package tokyo.northside.mapillary;

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

import java.util.Collection;
import java.util.stream.IntStream;

public class Mapillary360ImageDisplay extends MapillaryAbstractImageDisplay {
    private final BufferedImage offscreenImage;
    private Rectangle viewRect;
    private CameraPlane cameraPlane;

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
                Point click = comp2imgCoord(visibleRect, e.getX(), e.getY());
                Vector3D vec = cameraPlane.getVector3D(click.x, click.y);
                cameraPlane.setRotation(vec);
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

        double FOV = Math.toRadians(110);
        double cameraPlaneDistance = (offscreenImage.getWidth() / 2) / Math.tan(FOV / 2);
        cameraPlane = new CameraPlane(offscreenImage.getWidth(), offscreenImage.getHeight(), cameraPlaneDistance, 0, 0);
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
                Vector3D vec = cameraPlane.getVector3D(x, y);
                Point p = cameraPlane.mapping(vec, image.getWidth(),image.getHeight());
                int color = image.getRGB(p.x, p.y);
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
