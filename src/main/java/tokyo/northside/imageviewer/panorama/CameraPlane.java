package tokyo.northside.imageviewer.panorama;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.stream.IntStream;

import org.joml.Math;
import org.joml.Vector3d;

/**
 *
 */
public class CameraPlane {
    private final Vector3d[][] vectors;
    private final int maxX;
    private final int maxY;

    private double theta;
    private double sinTheta;
    private double cosTheta;
    private double phi;
    private double sinPhi;
    private double cosPhi;


    public CameraPlane(int width, int height, int fov) {
        double cameraPlaneDistance = (width / 2) / Math.tan(Math.toRadians(fov)/2);
        setRotation(0.0, 0.0);
        vectors = new Vector3d[height][width];
        initVectors(width, height, cameraPlaneDistance);
        maxX = width;
        maxY = height;
    }

    /**
     *
     * @param width
     * @param height
     * @param d
     */
    public CameraPlane(int width, int height, double d) {
        setRotation(0.0, 0.0);
        vectors = new Vector3d[height][width];
        initVectors(width, height, d);
        maxX = width;
        maxY = height;
    }

    private void initVectors(int width, int height, double d) {
        IntStream.range(0, height).forEach(y -> {
            IntStream.range(0, width).forEach(x -> {
              vectors[y][x] = new Vector3d(x - width /2, y - height/2, d).normalize();
            });
        });
    }

    /**
     * Set camera plane rotation by current plane position.
     * @param p Point within current plane.
     */
    public void setRotation(final Point p) {
        int y = Math.min(maxY, p.y);
        int x = Math.min(maxX, p.x);
        setRotation(rotate(vectors[y][x]));
    }

    /**
     *
     * @param from
     * @param to
     */
    public void setRotationFromDelta(final Point from, final Point to) {
        // check and limit index range.
        int fromY = Math.min(maxY, from.y);
        int fromX = Math.min(maxX, from.x);
        int toY = Math.min(maxY, to.y);
        int toX = Math.min(maxX, to.x);

        Vector3d f1 = vectors[fromY][fromX];
        Vector3d t1 = vectors[toY][toX];

        double newTheta = theta + Math.atan2(f1.x, f1.z) - Math.atan2(t1.x, t1.z);
        double newPhi = phi + Math.atan2(f1.y, Math.sqrt(f1.x * f1.x+ f1.z * f1.z))
               - Math.atan2(t1.y, Math.sqrt(t1.x * t1.x + t1.z * t1.z));
        setRotation(newTheta, newPhi);
    }

    /**
     * Mapping and set color from equirectangular projected source image to target BufferedImage.
     * @param sourceImage equirectangular projected source image
     * @param targetImage target buffer
     */
    public void mapping(BufferedImage sourceImage, BufferedImage targetImage) {
        // limit range
        int height = Math.min(maxY, targetImage.getHeight());
        int width = Math.min(maxX, targetImage.getWidth());

        IntStream.range(0, height).parallel().forEach(y -> {
            IntStream.range(0, width).forEach(x -> {
                Point p = getSourcePoint(rotate(vectors[y][x]),
                    sourceImage.getWidth(), sourceImage.getHeight());
                targetImage.setRGB(x, y, sourceImage.getRGB(p.x, p.y));
            });
        });
    }

    /**
     * Internal function to get point in sourceImage from plane's point.
     * @param vec view point vector
     * @param width source image width
     * @param height source image height
     * @return
     */
    Point getSourcePoint(Vector3d vec, int width, int height) {
        int tx = (int) ((width - 1) * (0.5 + (Math.atan2(vec.x, vec.z) / (Math.PI * 2))));
        int ty = (int) ((height - 1) * (0.5 + (Math.asin(vec.y) / Math.PI)));
        return new Point(tx, ty);
    }

    /**
     * Set camera plane rotation by spherical vector.
     * @param vec vector pointing new view position.
     */
    void setRotation(Vector3d vec) {
        double theta, phi;
        try {
            theta = Math.atan2(vec.x, vec.z);
            phi = Math.atan2(vec.y, Math.sqrt(vec.x * vec.x + vec.z * vec.z));
        } catch (Exception e) {
            theta = 0;
            phi = 0;
        }
        setRotation(theta, phi);
    }

    private void setRotation(double theta, double phi) {
        this.theta = theta;
        this.sinTheta = Math.sin(theta);
        this.cosTheta = Math.cos(theta);
        this.phi = phi;
        this.sinPhi = Math.sin(phi);
        this.cosPhi = Math.cos(phi);
    }

    private Vector3d rotate(Vector3d vec) {
        double vecX, vecY, vecZ;
        vecZ = vec.z * cosPhi - vec.y * sinPhi;
        vecY = vec.z * sinPhi + vec.y * cosPhi;
        vecX = vecZ * sinTheta + vec.x * cosTheta;
        vecZ = vecZ * cosTheta - vec.x * sinTheta;
        return new Vector3d(vecX, vecY, vecZ);
    }

    /**
     * For test: getRotated vector.
     * @param x
     * @param y
     * @return
     */
    Vector3d getVector(final int x, final int y) {
            return rotate(vectors[y][x]);
    }

    /**
     * For test: Get current view vector
     * @return current rotation as vector
     */
    Vector3d getRotation() {
        return new Vector3d(sinTheta, sinPhi, cosPhi * cosTheta);
    }

}
