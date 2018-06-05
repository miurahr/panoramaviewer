package tokyo.northside;

import java.io.IOException;
import java.io.InputStream;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPIterator;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.properties.XMPPropertyInfo;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.xmp.XmpDirectory;


public class Mapillary360ImageDetector {
    public boolean check(InputStream imageStream) {
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
                        }
                    }
                }
            }
        } catch  (NullPointerException | ImageProcessingException |IOException |XMPException e) {
            // ignore
        }
        return false;
    }
}
