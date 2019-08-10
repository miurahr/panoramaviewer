package tokyo.northside.imageviewer.panorama;

import static org.junit.Assert.assertEquals;

import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector2d;
import org.junit.Test;

public class UVMappingTest {
  private static final double DEFAULT_DELTA = 1e-5;

  @Test
  public void testMapping() {
    assertPointEquals(new Vector2d(.5, 1), UVMapping.getTextureCoordinate(new Vector3d(0, 1, 0)), DEFAULT_DELTA);
    assertPointEquals(new Vector2d(.5, 0), UVMapping.getTextureCoordinate(new Vector3d(0, -1, 0)), DEFAULT_DELTA);
    assertVectorEquals(new Vector3d(0, 1, 0), UVMapping.getVector(.5, 1), DEFAULT_DELTA);
    assertVectorEquals(new Vector3d(0, -1, 0), UVMapping.getVector(.5, 0), DEFAULT_DELTA);

    assertPointEquals(new Vector2d(.25, .5), UVMapping.getTextureCoordinate(new Vector3d(-1, 0, 0)), DEFAULT_DELTA);
    assertPointEquals(new Vector2d(.5, .5), UVMapping.getTextureCoordinate(new Vector3d(0, 0, 1)), DEFAULT_DELTA);
    assertPointEquals(new Vector2d(.75, .5), UVMapping.getTextureCoordinate(new Vector3d(1, 0, 0)), DEFAULT_DELTA);
    assertPointEquals(new Vector2d(1, .5), UVMapping.getTextureCoordinate(new Vector3d(0, 0, -1)), DEFAULT_DELTA);
    assertVectorEquals(new Vector3d(-1, 0, 0), UVMapping.getVector(.25, .5), DEFAULT_DELTA);
    assertVectorEquals(new Vector3d(0, 0, 1), UVMapping.getVector(.5, .5), DEFAULT_DELTA);
    assertVectorEquals(new Vector3d(1, 0, 0), UVMapping.getVector(.75, .5), DEFAULT_DELTA);
    assertVectorEquals(new Vector3d(0, 0, -1), UVMapping.getVector(1, .5), DEFAULT_DELTA);

    assertPointEquals(new Vector2d(.125, .25), UVMapping.getTextureCoordinate(new Vector3d(-.5, -1 / Math.sqrt(2), -.5)), DEFAULT_DELTA);
    assertPointEquals(new Vector2d(.625, .75), UVMapping.getTextureCoordinate(new Vector3d(.5, 1 / Math.sqrt(2), .5)), DEFAULT_DELTA);
    assertVectorEquals(new Vector3d(-.5, -1 / Math.sqrt(2), -.5), UVMapping.getVector(.125, .25), DEFAULT_DELTA);
    assertVectorEquals(new Vector3d(.5, 1 / Math.sqrt(2), .5), UVMapping.getVector(.625, .75), DEFAULT_DELTA);
  }

  private static void assertVectorEquals(final Vector3d expected, final Vector3d actual, final double delta) {
    final String message = String.format(
      "Expected (%f %f %f), but was (%f %f %f)",
      expected.x, expected.y, expected.z,
      actual.x, actual.y, actual.z
    );
    assertEquals(message, expected.x, actual.x, delta);
    assertEquals(message, expected.y, actual.y, delta);
    assertEquals(message, expected.z, actual.z, delta);
  }

  private static void assertPointEquals(final Vector2d expected, final Vector2d actual, final double delta) {
    final String message = String.format(
      "Expected (%f, %f), but was (%f, %f)",
      expected.x, expected.y,
      actual.x, actual.y
    );
    assertEquals(message, expected.x, actual.x, delta);
    assertEquals(message, expected.y, actual.y, delta);
  }
}
