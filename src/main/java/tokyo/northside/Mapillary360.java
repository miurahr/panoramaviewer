package tokyo.northside;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.image.DataBufferInt;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Mapillary360 {
    private static final List<ImageDetection> detections = Collections.synchronizedList(new ArrayList<>());
    private static BufferedImage sphereImage;
    private final BufferedImage offscreenImage;
    private int[] sphereImageBuffer;
    private final int[] offscreenImageBuffer;
    private static final double FOV = Math.toRadians(110);
    private final double cameraPlaneDistance;
    private double rayVecs[][][];
    private static final double ACCURACY_FACTOR = 2048;
    private static final int REQUIRED_SIZE = (int) (2 * ACCURACY_FACTOR);
    private double[] asinTable;
    private double[] atan2Table;
    private static final double INV_PI = 1 / Math.PI;
    private static final double INV_2PI = 1 / (2 * Math.PI);
    private double targetRotationX, targetRotationY;
    private double currentRotationX, currentRotationY;
    private int mouseX, mouseY;

    public Mapillary360() {
        offscreenImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        offscreenImageBuffer = ((DataBufferInt) offscreenImage.getRaster().getDataBuffer()).getData();
        cameraPlaneDistance = (offscreenImage.getWidth() / 2) / Math.tan(FOV / 2);
        createRayVecs();
        precalculateAsinAtan2();

        JFrame frame = new JFrame();
        frame.setTitle("Java 360 Sphere Image Viewer");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        try {
            MapillaryImageDisplay mapillaryImageDisplay = new MapillaryImageDisplay();
            sphereImage = ImageIO.read(getClass().getResourceAsStream("360photo.jpg"));
            if (sphereImage == null) {
                return;
            }
            frame.getContentPane().add(mapillaryImageDisplay);
            frame.setResizable(false);
            frame.setVisible(true);
            mapillaryImageDisplay.requestFocus();
            mapillaryImageDisplay.setImage(sphereImage, detections);
        } catch (IOException e){

        }
    }

    private void createRayVecs() {
        rayVecs = new double[offscreenImage.getWidth()][offscreenImage.getHeight()][3]; // x, y, z
        for (int y = 0; y < offscreenImage.getHeight(); y++) {
            for (int x = 0; x < offscreenImage.getWidth(); x++) {
                double vecX = x - offscreenImage.getWidth() / 2;
                double vecY = y - offscreenImage.getHeight() / 2;
                double vecZ = cameraPlaneDistance;
                double invVecLength = 1 / Math.sqrt(vecX * vecX + vecY * vecY + vecZ * vecZ);
                rayVecs[x][y][0] = vecX * invVecLength;
                rayVecs[x][y][1] = vecY * invVecLength;
                rayVecs[x][y][2] = vecZ * invVecLength;
            }
        }
    }

    private void precalculateAsinAtan2() {
        asinTable = new double[REQUIRED_SIZE];
        atan2Table = new double[REQUIRED_SIZE * REQUIRED_SIZE];
        for (int i = 0; i < 2 * ACCURACY_FACTOR; i++) {
            asinTable[i] = Math.asin((i - ACCURACY_FACTOR) * 1 / ACCURACY_FACTOR);
            for (int j = 0; j < 2 * ACCURACY_FACTOR; j++) {
                double y = (i - ACCURACY_FACTOR) / ACCURACY_FACTOR;
                double x = (j - ACCURACY_FACTOR) / ACCURACY_FACTOR;
                atan2Table[i + j * REQUIRED_SIZE] = Math.atan2(y, x);
            }
        }
    }

    public void start() {
        new Thread(() -> {
            boolean running = true;
            while (running) {
            }
        }).start();
    }

     private void draw(Graphics2D g) {
        targetRotationX = (mouseY - (offscreenImage.getHeight() / 2)) * 0.025;
        targetRotationY = (mouseX - (offscreenImage.getWidth() / 2)) * 0.025;
        currentRotationX += (targetRotationX - currentRotationX) * 0.25;
        currentRotationY += (targetRotationY - currentRotationY) * 0.25;
        double sinRotationX = Math.sin(currentRotationX);
        double cosRotationX = Math.cos(currentRotationX);
        double sinRotationY = Math.sin(currentRotationY);
        double cosRotationY = Math.cos(currentRotationY);
        double tmpVecX, tmpVecY, tmpVecZ;
        for (int y = 0; y < offscreenImage.getHeight(); y++) {
            for (int x = 0; x < offscreenImage.getWidth(); x++) {
                double vecX = rayVecs[x][y][0];
                double vecY = rayVecs[x][y][1];
                double vecZ = rayVecs[x][y][2];
                // rotate x
                tmpVecZ = vecZ * cosRotationX - vecY * sinRotationX;
                tmpVecY = vecZ * sinRotationX + vecY * cosRotationX;
                vecZ = tmpVecZ;
                vecY = tmpVecY;
                // rotate y
                tmpVecZ = vecZ * cosRotationY - vecX * sinRotationY;
                tmpVecX = vecZ * sinRotationY + vecX * cosRotationY;
                vecZ = tmpVecZ;
                vecX = tmpVecX;
                int iX = (int) ((vecX + 1) * ACCURACY_FACTOR);
                int iY = (int) ((vecY + 1) * ACCURACY_FACTOR);
                int iZ = (int) ((vecZ + 1) * ACCURACY_FACTOR);
                // https://en.wikipedia.org/wiki/UV_mapping
                double u = 0.5 + (atan2Table[iZ + iX * REQUIRED_SIZE] * INV_2PI);
                double v = 0.5 - (asinTable[iY] * INV_PI);
                int tx = (int) (sphereImage.getWidth() * u);
                int ty = (int) (sphereImage.getHeight() * (1 - v));
                int color = sphereImageBuffer[ty * sphereImage.getWidth() + tx];
                offscreenImageBuffer[y * offscreenImage.getWidth() + x] = color;
            }
        }
        g.drawImage(offscreenImage, 0, 0, sphereImage.getWidth(), sphereImage.getHeight(), null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
               new Mapillary360();
            }
        });
    }
}
