package tokyo.northside.mapillary;

import javax.swing.JComponent;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;

/**
 * This object is a responsible JComponent which lets you zoom and drag. It is
 * included in a object.
 *
 * @author nokutu
 * @see MapillaryImageDisplay
 */
public abstract class MapillaryAbstractImageDisplay extends JComponent {

  final Collection<ImageDetection> detections = new ArrayList<>();

  /** The image currently displayed */
  volatile BufferedImage image;


  /**
   * Sets a new picture to be displayed.
   *
   * @param image The picture to be displayed.
   * @param detections image detections
   */
  public void setImage(BufferedImage image, Collection<ImageDetection> detections) {
    synchronized (this) {
      this.image = image;
      this.detections.clear();
      if (detections != null) {
        this.detections.addAll(detections);
      }
    }
    repaint();
  }

  /**
   * Returns the picture that is being displayed
   *
   * @return The picture that is being displayed.
   */
  public BufferedImage getImage() {
    return this.image;
  }

  public abstract void paintComponent(Graphics g);

  Point img2compCoord(Rectangle visibleRect, int xImg, int yImg) {
    Rectangle drawRect = calculateDrawImageRectangle(visibleRect);
    return new Point(drawRect.x + ((xImg - visibleRect.x) * drawRect.width)
            / visibleRect.width, drawRect.y
            + ((yImg - visibleRect.y) * drawRect.height) / visibleRect.height);
  }

  Point comp2imgCoord(Rectangle visibleRect, int xComp, int yComp) {
    Rectangle drawRect = calculateDrawImageRectangle(visibleRect);
    return new Point(
            visibleRect.x + ((xComp - drawRect.x) * visibleRect.width) / drawRect.width,
            visibleRect.y + ((yComp - drawRect.y) * visibleRect.height) / drawRect.height
    );
  }

  static Point getCenterImgCoord(Rectangle visibleRect) {
    return new Point(visibleRect.x + visibleRect.width / 2, visibleRect.y + visibleRect.height / 2);
  }

  Rectangle calculateDrawImageRectangle(Rectangle visibleRect) {
    return calculateDrawImageRectangle(visibleRect, new Rectangle(0, 0, getSize().width, getSize().height));
  }

  /**
   * calculateDrawImageRectangle
   *
   * @param imgRect
   *          the part of the image that should be drawn (in image coordinates)
   * @param compRect
   *          the part of the component where the image should be drawn (in
   *          component coordinates)
   * @return the part of compRect with the same width/height ratio as the image
   */
  static Rectangle calculateDrawImageRectangle(Rectangle imgRect, Rectangle compRect) {
    int x = 0;
    int y = 0;
    int w = compRect.width;
    int h = compRect.height;
    int wFact = w * imgRect.height;
    int hFact = h * imgRect.width;
    if (wFact != hFact) {
      if (wFact > hFact) {
        w = hFact / imgRect.height;
        x = (compRect.width - w) / 2;
      } else {
        h = wFact / imgRect.width;
        y = (compRect.height - h) / 2;
      }
    }
    return new Rectangle(x + compRect.x, y + compRect.y, w, h);
  }
}
