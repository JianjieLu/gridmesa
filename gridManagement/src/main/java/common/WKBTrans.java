package common;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import java.io.Serializable;

public class WKBTrans implements Serializable {

    public Geometry getGeomByWKB(byte[] box) {
        try{
            WKBReader wkbReader = new WKBReader();
            return wkbReader.read(box);
        }catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
