package tokyo.northside.mapillary;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;


public class MapillaryMainDialogStub extends JFrame {
  private static MapillaryMainDialogStub instance;
  /**
   * Object containing the shown image and that handles zoom and drag
   */
  public MapillaryAbstractImageDisplay mapillaryImageDisplay;

  /* Test data */
  private String imageFileName = "360photo.jpg";

  /**
   *  Initialize gui parts and prepare Mapillary360ImageDisplay instance
   */
  private MapillaryMainDialogStub() {
    this.setTitle("Java 360 Sphere Image Viewer");
    this.setSize(800, 600);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setLocationRelativeTo(null);
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

  /**
   * @return true, iff the singleton instance is present
   */
  public static boolean hasInstance() {
    return instance != null;
  }


  public void loadingFinished() {
     // if (!SwingUtilities.isEventDispatchThread()) {
     //   SwingUtilities.invokeLater(() -> loadingFinished(data, attributes, result));
     // } else if (data != null && result == LoadResult.SUCCESS) {
    try {
        // InputStream ins = new ByteArrayInputStream(data.getContent());
        BufferedImage img = ImageIO.read(getClass().getResourceAsStream(imageFileName));
        /* end of Mock */
        if (img == null) {
          return;
        }
        if(!Mapillary360ImageDisplay.is360Image(getClass().getResourceAsStream(imageFileName))) {
          mapillaryImageDisplay = new Mapillary360ImageDisplay();
        } else {
          mapillaryImageDisplay = new MapillaryImageDisplay();
        }
        this.getContentPane().add(mapillaryImageDisplay);
        this.setResizable(false);
        this.setVisible(true);
        mapillaryImageDisplay.requestFocus();
        if (mapillaryImageDisplay.getImage() == null ||
            img.getHeight() > this.mapillaryImageDisplay.getImage().getHeight()) {
          //final MapillaryAbstractImage mai = getImage();
          mapillaryImageDisplay.setImage(img, null);
        }
    } catch (IOException e) {
      // ignore
    }
  }

  public static void main(String[] args) {
      SwingUtilities.invokeLater(() -> MapillaryMainDialogStub.getInstance().loadingFinished());
  }
}
