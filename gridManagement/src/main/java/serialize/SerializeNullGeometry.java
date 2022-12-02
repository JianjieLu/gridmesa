package serialize;

import geoobject.STPoint;

import java.util.Iterator;

/**
 * Represent a null object. Mainly use in concurrency queue.
 *
 * @author Yu Liebing
 * Create on 2019-05-19.
 */
public class SerializeNullGeometry extends SerializeGeometry {
    @Override
    public Iterator<STPoint> iterator() {
        throw new UnsupportedOperationException();
    }

}
