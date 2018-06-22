package tokyo.northside.mapillary;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPIterator;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.properties.XMPPropertyInfo;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.xmp.XmpDirectory;

import java.io.IOException;
import java.io.InputStream;

public class ImageProperty {

  static boolean is360Image(InputStream imageStream) {
    try {
      Metadata metadata = ImageMetadataReader.readMetadata(imageStream);
      XmpDirectory xmpDirectory = metadata.getFirstDirectoryOfType(XmpDirectory.class);
      XMPMeta xmpMeta = xmpDirectory.getXMPMeta();
      XMPIterator itr = xmpMeta.iterator();
      while (itr.hasNext()) {
        XMPPropertyInfo pi = (XMPPropertyInfo) itr.next();
        if (pi != null && pi.getPath() != null) {
          if ((pi.getPath().endsWith("ProjectionType"))) {
            String proj = pi.getValue();
            if (proj.equals("equirectangular")) {
              return true;
            } else {
              return false;
            }
          }
        }
      }
      JpegDirectory jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory.class);
      int imageHeight = jpegDirectory.getImageHeight();
      int imageWidth = jpegDirectory.getImageWidth();
      if ((imageWidth >= 2048) && (imageWidth == imageHeight * 2)) {
        return true;
      }
    } catch (NullPointerException | ImageProcessingException | IOException | XMPException | MetadataException e) {
      // ignore
    }
    return false;
  }
}
