// License: GPL. For details, see LICENSE file.
package tokyo.northside.imageviewer;

import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link ImageDisplay}
 */
public class ImageDisplayTest {

  private static final BufferedImage DUMMY_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

  @Test
  public void testImagePersistence() {
    ImageDisplay display = new ImageDisplay();
    display.setImage(DUMMY_IMAGE, false);
    assertEquals(DUMMY_IMAGE, display.getImage());
  }

  /**
   * This test does not check if the scroll events result in the correct changes in the {@link ImageDisplay},
   * it only checks if the tested method runs through.
   */
  @Test
  public void testMouseWheelMoved() {
    if (GraphicsEnvironment.isHeadless()) {
      return;
    }
    ImageDisplay display = new ImageDisplay();
    final MouseWheelEvent dummyScroll = new MouseWheelEvent(display, 42, System.currentTimeMillis(), 0, 0, 0, 0, false, MouseWheelEvent.WHEEL_UNIT_SCROLL, 1, 3);
    display.getMouseWheelListeners()[0].mouseWheelMoved(dummyScroll);

    display.setImage(DUMMY_IMAGE, false);

    display.getMouseWheelListeners()[0].mouseWheelMoved(dummyScroll);

    // This is necessary to make the size of the component > 0. If you know a more elegant solution, feel free to change it.
    JFrame frame = new JFrame();
    frame.setSize(42, 42);
    frame.getContentPane().add(display);
    frame.pack();

    display.getMouseWheelListeners()[0].mouseWheelMoved(dummyScroll);
  }

  /**
   * This test does not check if the scroll events result in the correct changes in the {@link ImageDisplay},
   * it only checks if the tested method runs through.
   */
  @Test
  public void testMouseClicked() {
    if (GraphicsEnvironment.isHeadless()) {
      return;
    }
    for (int button = 1; button <= 3; button++) {
      ImageDisplay display = new ImageDisplay();
      final MouseEvent dummyClick = new MouseEvent(display, 42, System.currentTimeMillis(), 0, 0, 0, 1, false, button);
      display.getMouseListeners()[0].mouseClicked(dummyClick);

      display.setImage(DUMMY_IMAGE, false);

      display.getMouseListeners()[0].mouseClicked(dummyClick);

      // This is necessary to make the size of the component > 0. If you know a more elegant solution, feel free to change it.
      JFrame frame = new JFrame();
      frame.setSize(42, 42);
      frame.getContentPane().add(display);
      frame.pack();

      display.getMouseListeners()[0].mouseClicked(dummyClick);
    }
  }
}
