package tokyo.northside.imageviewer;

import tokyo.northside.imageviewer.panorama.ImageProperty;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class MainDialog extends JFrame {
  private static MainDialog instance;
  /**
   * Object containing the shown image and that handles zoom and drag
   */
  public ImageDisplay imageDisplay;

  /**
   *  Initialize gui parts and prepare Mapillary360ImageDisplay instance
   */
  private MainDialog() {
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
  public static synchronized MainDialog getInstance() {
    if (instance == null)
      instance = new MainDialog();
    return instance;
  }

  /**
   * @return true, iff the singleton instance is present
   */
  public static boolean hasInstance() {
    return instance != null;
  }


  public void open(File file) {
    try {
        BufferedImage img = ImageIO.read(file);
        if (img == null)
          return;
        imageDisplay = new ImageDisplay();
        boolean pano = ImageProperty.is360Image(getClass().getResourceAsStream(file.getPath()));
        this.getContentPane().add(imageDisplay);
        this.setResizable(false);
        this.setVisible(true);
        imageDisplay.requestFocus();
        if (imageDisplay.getImage() == null ||
            img.getHeight() > this.imageDisplay.getImage().getHeight()) {
          imageDisplay.setImage(img, pano);
        }
    } catch (IOException e) {
      // ignore
    }
  }

  public static void main(final String[] args) {
    if (args == null || args.length == 0 || args[0].trim().isEmpty()) {
        System.out.println("You need to specify an image path!");
        return;
    }
    File file = new File(args[0]);
    SwingUtilities.invokeLater(() -> MainDialog.getInstance().open(file));
  }
}
