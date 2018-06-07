package tokyo.northside.mapillary;

import javax.swing.*;
import java.awt.*;
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
   * The rectangle (in image coordinates) of the image that is visible. This
   * rectangle is calculated each time the zoom is modified
   */
  volatile Rectangle visibleRect;

  /**
   * When a selection is done, the rectangle of the selection (in image
   * coordinates)
   */
  Rectangle selectedRect;

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
      this.selectedRect = null;
      if (image != null)
        this.visibleRect = new Rectangle(0, 0, image.getWidth(null),
            image.getHeight(null));
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
}
