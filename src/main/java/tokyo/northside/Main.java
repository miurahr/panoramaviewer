package tokyo.northside;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {
    private static final List<ImageDetection> detections = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedImage img = ImageIO.read(getClass().getResourceAsStream("360photo.jpg"));
                    if (img == null) {
                        return;
                    }
                    MapillaryImageDisplay mapillaryImageDisplay = new MapillaryImageDisplay();
                    JFrame frame = new JFrame();
                    frame.setTitle("Java 360 Sphere Image Viewer");
                    frame.setSize(800, 600);
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setLocationRelativeTo(null);
                    frame.getContentPane().add(mapillaryImageDisplay);
                    frame.setResizable(false);
                    frame.setVisible(true);
                    mapillaryImageDisplay.requestFocus();
                    mapillaryImageDisplay.setImage(img, detections);
                } catch (IOException e){

                }
            }
        });
    }
}
