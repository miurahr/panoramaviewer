package tokyo.northside;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static java.lang.Thread.sleep;

public class MapillaryMainDialogStub {
  private static MapillaryMainDialogStub instance;
  /**
   * Object containing the shown image and that handles zoom and drag
   */
  public final Mapillary360ImageDisplay mapillary360ImageDisplay;
  public final MapillaryImageDisplay mapillaryImageDisplay;

  private MapillaryMainDialogStub() {
    JFrame frame = new JFrame();
    frame.setTitle("Java 360 Sphere Image Viewer");
    frame.setSize(800, 600);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLocationRelativeTo(null);
    mapillaryImageDisplay = new MapillaryImageDisplay();
    frame.getContentPane().add(mapillaryImageDisplay);
    frame.setResizable(false);
    frame.setVisible(true);
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
    try {
      BufferedImage img = ImageIO.read(getClass().getResourceAsStream("360photo.jpg"));
      if (img == null) {
        return;
      }
      if (
        mapillary360ImageDisplay.getImage() == null
        || img.getHeight() > this.mapillary360ImageDisplay.getImage().getHeight()
      ) {
        this.mapillary360ImageDisplay.setImage(img);
      }
    } catch (IOException e) {

    }
    mapillary360ImageDisplay.setMouse(100, 160);
    mapillary360ImageDisplay.draw();
  }

  public static void main(String[] args) {
      SwingUtilities.invokeLater(() -> MapillaryMainDialogStub.getInstance().loadingFinished());
  }
}
