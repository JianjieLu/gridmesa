package geoobject.geometry;

import org.locationtech.jts.geom.Geometry;

/**
 * we only consider the polygon without hole
 **/
public class GridNonPoint extends GridGeometry {

    public GridNonPoint(String id, Geometry geometry, int recursiveTimes){
        super(id,geometry);
        geomDecompose(geometry, recursiveTimes);
    }

    public GridNonPoint(Geometry geometry, int level){
        super("queryBox",geometry);
        computeGrids(geometry, level);
    }

}
