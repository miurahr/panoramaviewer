// License: GPL. For details, see LICENSE file.
package tokyo.northside.imageviewer.panorama;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.stream.IntStream;

import org.joml.Math;
import org.joml.Vector2d;
import org.joml.Vector3d;


public class CameraPlane {

  private Vector3d[][] vectors;
  private double theta;
  private double sinTheta;
  private double cosTheta;
  private double phi;
  private double sinPhi;
  private double cosPhi;

  public CameraPlane(int width, int height, double distance) {
    setRotation(0.0, 0.0);
    vectors = new Vector3d[width][height];
    IntStream.range(0, height).parallel().forEach(y -> {
      IntStream.range(0, width).parallel().forEach(x -> {
        vectors[x][y] = new Vector3d(x - width / 2.0d, y - height / 2.0d, distance).normalize();
      });
    });
  }

  Vector3d getVector3d(final Point p) {
    Vector3d res;
    try {
      res = rotate(vectors[p.x][p.y], 1);
    } catch (Exception e) {
      res = new Vector3d(0, 0, 1);
    }
    return res;
  }

  /**
   * Set camera plane rotation by current plane position.
   * @param p Point within current plane.
   */
  public void setRotation(final Point p) {
    setRotation(getVector3d(p));
  }

  public void setRotationFromDelta(final Point from, final Point to) {
    Vector3d f1 = vectors[from.x][from.y];
    Vector3d t1 = vectors[to.x][to.y];
    double deltaTheta = Math.atan2(f1.x, f1.z) - Math.atan2(t1.x, t1.z);
    double deltaPhi = Math.atan2(f1.y, Math.sqrt(f1.x * f1.x + f1.z * f1.z))
        - Math.atan2(t1.y, Math.sqrt(t1.x * t1.x + t1.z * t1.z));
    double newTheta = theta + deltaTheta;
    double newPhi = phi + deltaPhi;
    setRotation(newTheta, newPhi);
  }

  /**
   * Set camera plane rotation by spherical vector.
   * @param vec vector pointing new view position.
   */
  public void setRotation(Vector3d vec) {
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

  Vector3d getRotation() {
    return new Vector3d(sinTheta, sinPhi, cosPhi * cosTheta);
  }

  synchronized void setRotation(double theta, double phi) {
    this.theta = theta;
    this.sinTheta = Math.sin(theta);
    this.cosTheta = Math.cos(theta);
    this.phi = phi;
    this.sinPhi = Math.sin(phi);
    this.cosPhi = Math.cos(phi);
  }

  private Vector3d rotate(final Vector3d vec, final int rotationFactor) {
    double vecX, vecY, vecZ;
    vecZ = vec.z * cosPhi - vec.y * sinPhi;
    vecY = vec.z * sinPhi + vec.y * cosPhi;
    vecX = vecZ * sinTheta * rotationFactor + vec.x * cosTheta;
    vecZ = vecZ * cosTheta - vec.x * sinTheta * rotationFactor;
    return new Vector3d(vecX, vecY, vecZ);
  }

  public void mapping(BufferedImage sourceImage, BufferedImage targetImage) {
    IntStream.range(0, targetImage.getHeight()).parallel().forEach(y -> {
      IntStream.range(0, targetImage.getWidth()).forEach(x -> {
        final Vector3d vec = getVector3d(new Point(x, y));
        final Vector2d p = UVMapping.getTextureCoordinate(vec);
        targetImage.setRGB(x, y,
            sourceImage.getRGB((int) (p.x * (sourceImage.getWidth() - 1)), (int) (p.y * (sourceImage.getHeight() - 1)))
        );
      });
    });
  }
}
