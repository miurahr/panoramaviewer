package tokyo.northside;

import java.awt.image.BufferedImage;

public class Mapillary360ImageDisplay {
    private BufferedImage sphereImage;
    private final BufferedImage offscreenImage;
    private static final double FOV = Math.toRadians(110);
    private final double cameraPlaneDistance;
    private double rayVecs[][][];
    private static final double ACCURACY_FACTOR = 2048;
    private static final int REQUIRED_SIZE = (int) (2 * ACCURACY_FACTOR);
    private double[] asinTable;
    private double[] atan2Table;
    private static final double INV_PI = 1 / Math.PI;
    private static final double INV_2PI = 1 / (2 * Math.PI);
    private double currentRotationX, currentRotationY;

    private MapillaryImageDisplay mapillaryImageDisplay;

    public Mapillary360ImageDisplay(MapillaryImageDisplay display) {
        this.mapillaryImageDisplay = display;
        offscreenImage = new BufferedImage(800, 600, BufferedImage.TYPE_3BYTE_BGR);
        cameraPlaneDistance = (offscreenImage.getWidth() / 2) / Math.tan(FOV / 2);
        createRayVecs();
        precalculateAsinAtan2();
    }

    public BufferedImage getImage() {
        return this.offscreenImage;
    }

    public void setImage(BufferedImage img) {
        sphereImage = img;
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

     public void setMouse(int mouseX, int mouseY) {
         double targetRotationX = (mouseY - (offscreenImage.getHeight() / 2)) * 0.025;
         double targetRotationY = (mouseX - (offscreenImage.getWidth() / 2)) * 0.025;
         currentRotationX += (targetRotationX - currentRotationX) * 0.25;
         currentRotationY += (targetRotationY - currentRotationY) * 0.25;
     }

     public void draw() {
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

                if(tx >= sphereImage.getWidth()) {
                    tx = sphereImage.getWidth()-1;
                }
                if(ty >= sphereImage.getHeight()) {
                    ty = sphereImage.getHeight()-1;
                }

                int color = sphereImage.getRGB(tx, ty);
                offscreenImage.setRGB(x, y, color);
            }
        }
        mapillaryImageDisplay.setImage(offscreenImage, null);
    }
}
