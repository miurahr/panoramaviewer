// License: GPL. For details, see LICENSE file.
package tokyo.northside.imageviewer.panorama;

import org.junit.Test;

import java.awt.Point;

import org.joml.Math;
import org.joml.Vector3d;

import static org.junit.Assert.assertEquals;

public class CameraPlaneTest {

  private CameraPlane cameraPlane;
  private static final double FOV = Math.toRadians(110);
  private static final double CAMERA_PLANE_DISTANCE = (800 / 2) / Math.tan(FOV / 2);

  @Test
  public void testSetRotation() {
    cameraPlane = new CameraPlane(800, 400, CAMERA_PLANE_DISTANCE);
    Vector3d vec = new Vector3d(0, 0, 1);
    cameraPlane.setRotation(vec);
    Vector3d out = cameraPlane.getRotation();
    assertEquals(0, out.x, 0.001);
    assertEquals(0, out.y, 0.001);
    assertEquals(1, out.z, 0.001);
  }

  @Test
  public void testGetVector3d() {
    cameraPlane = new CameraPlane(800, 600, CAMERA_PLANE_DISTANCE);
    Vector3d vec = new Vector3d(0, 0, 1);
    cameraPlane.setRotation(vec);
    Vector3d out = cameraPlane.getVector(400, 300);
    assertEquals(0.0, out.x(), 1.0E-04);
    assertEquals(0.0, out.y(), 1.0E-04);
    assertEquals(1.0, out.z(), 1.0E-04);
  }

  @Test
  public void testMapping() {
    cameraPlane = new CameraPlane(800, 600, CAMERA_PLANE_DISTANCE);
    Vector3d vec = new Vector3d(0, 0, 1);
    cameraPlane.setRotation(vec);
    Vector3d out = cameraPlane.getVector(300, 200);
    Point map = cameraPlane.getSourcePoint(out, 2048, 1024);
    assertEquals(911, map.getX(), 1);
    assertEquals(405, map.getY(), 1);
  }
}

