package geoobject.geometry;

import org.locationtech.jts.geom.Polygon;

/**
 * we only consider the polygon without hole
 **/
public class GridPolygon extends GridGeometry {

    public GridPolygon(String id,Polygon polygon, int recursiveTimes){
        super(id,polygon);
        geomDecompose(polygon, recursiveTimes);
    }

    public GridPolygon(Polygon polygon, int level){
        super("queryBox",polygon);
        computeGrids(polygon, level);
    }

}
