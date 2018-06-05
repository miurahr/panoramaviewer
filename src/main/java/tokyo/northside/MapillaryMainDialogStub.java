package tokyo.northside;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;


public class MapillaryMainDialogStub extends JFrame {
  private static MapillaryMainDialogStub instance;
  /**
   * Object containing the shown image and that handles zoom and drag
   */
  public final MapillaryImageDisplay mapillaryImageDisplay;
  /**
   * Object containing whole sphere image and handles view port
   */
  public final Mapillary360ImageDisplay mapillary360ImageDisplay;

  public final Mapillary360ImageDetector mapillary360ImageDetector;
  /**
   *  Initialize gui parts and prepare Mapillary360ImageDisplay instance
   */
  private MapillaryMainDialogStub() {
    this.setTitle("Java 360 Sphere Image Viewer");
    this.setSize(800, 600);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setLocationRelativeTo(null);
    mapillaryImageDisplay = new MapillaryImageDisplay();
    mapillary360ImageDetector = new Mapillary360ImageDetector();
    mapillary360ImageDisplay = new Mapillary360ImageDisplay();
  }

  /**
   * Returns the unique instance of the class.
   *
   * @return The unique instance of the class.
   */
  public static synchronized MapillaryMainDialogStub getInstance() {
    if (instance == null)
      instance = new MapillaryMainDialogStub();
    return instance;
  }

  public void loadingFinished() {
    BufferedImage img;

    String imageFileName = "360photo.jpg";

    boolean res = mapillary360ImageDetector.check(getClass().getResourceAsStream(imageFileName));
      try {
        img = ImageIO.read(getClass().getResourceAsStream(imageFileName));
      } catch (IOException e) {
        img = null;
      }
      if (img == null) {
        return;
      }
    if (res) {
      this.getContentPane().add(mapillary360ImageDisplay);
      this.setResizable(false);
      this.setVisible(true);
      mapillary360ImageDisplay.requestFocus();
      if (
              mapillary360ImageDisplay.getImage() == null
                      || img.getHeight() > this.mapillary360ImageDisplay.getImage().getHeight()
              ) {
        mapillary360ImageDisplay.setImage(img, null);
        mapillary360ImageDisplay.setViewPoint(30, 160);
      }
    } else {
      this.getContentPane().add(mapillaryImageDisplay);
      this.setResizable(false);
      this.setVisible(true);
      mapillaryImageDisplay.setImage(img, null);
      mapillaryImageDisplay.requestFocus();
    }
  }

  public static void main(String[] args) {
      SwingUtilities.invokeLater(() -> MapillaryMainDialogStub.getInstance().loadingFinished());
  }
}
