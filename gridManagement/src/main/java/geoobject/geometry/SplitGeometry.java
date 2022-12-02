package geoobject.geometry;

import org.locationtech.jts.geom.Geometry;

/**
 * 格网划分后的位于网格中的小Geom
 *
 */
public class SplitGeometry {

    private Grid grid;

    private Geometry splitGeom = null;

    //splitGeom是否充满Grid
    private boolean full;

    private String id;

    public SplitGeometry(Grid grid, Geometry splitGeom, String id) {
        this.grid = grid;
        this.splitGeom = splitGeom;
        this.full = grid.getPolygon() == splitGeom;
        this.id = id;
    }


    public SplitGeometry(Grid grid, boolean full, String id) {
        this.grid = grid;
        this.full = full;
        this.id = id;
    }


    public Grid getGrid() {
        return grid;
    }

    public Geometry getSplitGeom() {
        return splitGeom;
    }

    public boolean isFull() {
        return full;
    }

    public String getId() {
        return id;
    }
}
