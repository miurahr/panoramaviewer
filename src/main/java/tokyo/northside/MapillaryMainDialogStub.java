package tokyo.northside;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


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
    this.getContentPane().add(mapillaryImageDisplay);
    this.setResizable(false);
    this.setVisible(true);
    mapillary360ImageDetector = new Mapillary360ImageDetector();
    mapillary360ImageDisplay = new Mapillary360ImageDisplay(mapillaryImageDisplay);
    mapillaryImageDisplay.requestFocus();
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
    String imageFileName = "360photo.jpg";
    boolean res = mapillary360ImageDetector.check(getClass().getResourceAsStream(imageFileName));
    if (res) {
      try {
        BufferedImage img = ImageIO.read(getClass().getResourceAsStream(imageFileName));
        if (img == null) {
          return;
        }
        if (
          mapillary360ImageDisplay.getImage() == null
                        || img.getHeight() > this.mapillary360ImageDisplay.getImage().getHeight()
        ) {
          this.mapillary360ImageDisplay.setImage(img);
        }
      } catch(IOException e){

      }
      mapillary360ImageDisplay.setViewPoint(30, 160);
      mapillary360ImageDisplay.draw();
    }
  }

  public static void main(String[] args) {
      SwingUtilities.invokeLater(() -> MapillaryMainDialogStub.getInstance().loadingFinished());
  }
}
