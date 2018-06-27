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
    private Vector3d[][] vectors;
    private double theta;
    private double sinTheta;
    private double cosTheta;
    private double phi;
    private double sinPhi;
    private double cosPhi;


    public CameraPlane(int width, int height, int target_width, int fov) {
        double HALFFOV = Math.toRadians(110)/2.0d;
        double cameraPlaneDistance = (target_width / 2.0d) / Math.tan(HALFFOV);
        init(width, height, cameraPlaneDistance);
    }

    /**
     *
     * @param width
     * @param height
     * @param d
     */
    public CameraPlane(int width, int height, double d) {
      init(width, height, d);
    }

    private void init(int width, int height, double d) {
        setRotation(0.0, 0.0);
        vectors = new Vector3d[width][height];
        IntStream.range(0, height).forEach(y -> {
            IntStream.range(0, width).forEach(x -> {
              vectors[x][y] = new Vector3d(x - width /2, y - height/2, d).normalize();
            });
        });
    }

    /**
     * Set camera plane rotation by current plane position.
     * @param p Point within current plane.
     */
    public void setRotation(final Point p) {
        setRotation(getVector(p));
    }

    /**
     *
     * @param from
     * @param to
     */
    public void setRotationFromDelta(final Point from, final Point to) {
        Vector3d f1 = vectors[from.x][from.y];
        Vector3d t1 = vectors[to.x][to.y];
        double deltaTheta = Math.atan2(f1.x, f1.z) - Math.atan2(t1.x, t1.z);
        double deltaPhi = Math.atan2(f1.y, Math.sqrt(f1.x * f1.x+ f1.z * f1.z))
               - Math.atan2(t1.y, Math.sqrt(t1.x * t1.x + t1.z * t1.z));
        double newTheta =  theta + deltaTheta;
        double newPhi = phi + deltaPhi;
        setRotation(newTheta, newPhi);
    }

    /**
     *
     * @param sourceImage
     * @param targetImage
     */
    public void mapping(BufferedImage sourceImage, BufferedImage targetImage) {
        int height = targetImage.getHeight();
        int width = targetImage.getWidth();
        IntStream.range(0, height).parallel().forEach(y -> {
            IntStream.range(0, width).forEach(x -> {
                Vector3d vec = getVector(new Point(x, y));
                Point p = mapping(vec, sourceImage.getWidth(), sourceImage.getHeight());
                int color = sourceImage.getRGB(p.x, p.y);
                targetImage.setRGB(x, y, color);
            });
        });
    }

    /**
     *
     * @param vec
     * @param width
     * @param height
     * @return
     */
    Point mapping(Vector3d vec, int width, int height) {
        // https://en.wikipedia.org/wiki/UV_mapping
        double u = 0.5 + (Math.atan2(vec.x, vec.z)  / (Math.PI * 2));
        double v = 0.5 + (Math.asin(vec.y) / Math.PI);
        int tx = (int) ((width - 1) * u);
        int ty = (int) ((height - 1) * v);
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

    /**
     * Get current view vector
     * @return current rotation as vector
     */
    Vector3d getRotation() {
        return new Vector3d(sinTheta, sinPhi, cosPhi * cosTheta);
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

    private Vector3d getVector(final Point p) {
        return getVector(p.x, p.y);
    }

    Vector3d getVector(final int x, final int y) {
        Vector3d res;
        try {
            res = rotate(vectors[x][y]);
        } catch (Exception e) {
            res = new Vector3d(0, 0, 1);
        }
        return res;
    }

}
