// License: GPL. For details, see LICENSE file.
// SPDX-License-Identifier: GPL-2.0-or-later
package tokyo.northside.imageviewer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class MainDialog extends JFrame {
  private static MainDialog instance;

  /**
   *  Initialize gui parts and prepare Mapillary360ImageDisplay instance
   */
  private MainDialog() {
    this.setTitle("Java 360-Degree panorama photo image viewer");
    this.setSize(800, 600);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setLocationRelativeTo(null);
  }

  /**
   * Returns the unique instance of the class.
   *
   * @return The unique instance of the class.
   */
  private static synchronized MainDialog getInstance() {
    if (instance == null)
      instance = new MainDialog();
    return instance;
  }

  private void open(File file) {
    try {
        BufferedImage img = ImageIO.read(file);
        if (img == null)
          return;
      /**
       * Object containing the shown image and that handles zoom and drag
       */
        ImageDisplay imageDisplay = new ImageDisplay();
        boolean pano = ImageMetaDataUtil.isPanorama(file);
        this.getContentPane().add(imageDisplay);
        this.setResizable(false);
        this.setVisible(true);
        imageDisplay.setImage(img, pano);
        imageDisplay.requestFocus();
    } catch (IOException e) {
      // ignore
    }
  }

  public static void main(final String[] args) {
    File file;
    if (args == null || args.length == 0 || args[0].trim().isEmpty()) {
      JFileChooser chooser =  new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
      FileNameExtensionFilter filter = new FileNameExtensionFilter(
        "JPG Images", "jpg");
      chooser.setFileFilter(filter);
      int returnVal = chooser.showOpenDialog(null);
      if(returnVal == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
      } else {
        return;
      }
    } else {
      file = new File(args[0]);
    }
    SwingUtilities.invokeLater(() -> MainDialog.getInstance().open(file));
  }
}
