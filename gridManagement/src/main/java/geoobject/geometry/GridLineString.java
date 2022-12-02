package geoobject.geometry;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class GridLineString extends GridGeometry {


    public GridLineString(String id,LineString lineString, int recursiveTimes) {
        super(id,lineString);
        geomDecompose(lineString, recursiveTimes);
    }
}
