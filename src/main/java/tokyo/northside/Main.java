package tokyo.northside;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
        public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
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
                mapillaryImageDisplay.paintComponent();
            }
        });
    }
}
