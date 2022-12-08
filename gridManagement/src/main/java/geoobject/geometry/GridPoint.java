package geoobject.geometry;

import org.locationtech.geomesa.curve.Z2SFC;
import org.locationtech.jts.geom.Point;

public class GridPoint extends GridGeometry {

    public GridPoint(String id, Point point, int level) {
        super(id,point);
//        splitGeoms.add(new SplitGeometry(createGrid(point, level), false, id));
        splitGeoms.add(new SplitGeometry(createGrid(point, level), point, id));
    }

    private Grid createGrid(Point point, int level){
        Z2SFC z2 = new Z2SFC(level);
        long z2index = z2.index(point.getX(), point.getY(), false);
        return new Grid(level, z2index);
    }

}
