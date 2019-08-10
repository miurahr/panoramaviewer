// License: GPL. For details, see LICENSE file.
// SPDX-License-Identifier: GPL-2.0-or-later
package tokyo.northside.imageviewer.panorama;

import static org.junit.Assert.assertEquals;

import java.awt.Point;
import java.awt.geom.Point2D;

import org.joml.Vector2d;
import org.joml.Vector3d;
import org.junit.Test;


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
    Vector3d out = cameraPlane.getVector3d(new Point(400, 300));
    assertEquals(0.0, out.x, 1.0E-04);
    assertEquals(0.0, out.y, 1.0E-04);
    assertEquals(1.0, out.z, 1.0E-04);
  }

  @Test
  public void testMapping() {
    cameraPlane = new CameraPlane(800, 600, CAMERA_PLANE_DISTANCE);
    Vector3d vec = new Vector3d(0, 0, 1);
    cameraPlane.setRotation(vec);
    Vector3d out = cameraPlane.getVector3d(new Point(300, 200));
    Vector2d map = UVMapping.getTextureCoordinate(out);
    assertEquals(0.44542099, map.x, 1e-8);
    assertEquals(0.39674936, map.y, 1e-8);
  }
}

