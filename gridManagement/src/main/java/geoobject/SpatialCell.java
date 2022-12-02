package geoobject;

import index.coding.spatial.SpatialCoding;
import index.coding.spatial.Z2Coding;
import index.coding.spatial.geohash.GeohashCoding;
import index.data.ByteArray;
import index.data.ByteArrayRange;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is an abstraction of spatial unit of SFC.
 *
 * @author Guan Hongli
 * Create on 2019-3-8.
 * Edit by Yu Liebing on 2019-07-06.
 */
public class SpatialCell extends BaseElement {
    private STPoint lowerLeft;
    private STPoint upperRight;
    private double distanceToPoint = -1.0;
    private ByteArray spatialCode;
    private SpatialCoding spatialCoding;
    // level is coding.precision / 2.
    private int level;
    private int childrenNum;
    private STPoint kNNQueryPoint;

    // default to 0
    private int hash;

    public SpatialCell(ByteArray spatialCode, int level,
                       SpatialCoding spatialCoding, STPoint kNNQueryPoint)
            throws UnsupportedEncodingException, ParseException {
        this.spatialCode = spatialCode;
        this.spatialCoding = spatialCoding;
        this.level = level;
        BoundingBox cornerCoors = spatialCoding.getBoundingBox(spatialCode);

        this.lowerLeft = new STPoint(
                cornerCoors.getLowerLeft().getLongitude(),
                cornerCoors.getLowerLeft().getLatitude());
        this.upperRight = new STPoint(
                cornerCoors.getUpperRight().getLongitude(),
                cornerCoors.getUpperRight().getLatitude());

        this.kNNQueryPoint = kNNQueryPoint;
        calDistanceToPoint(kNNQueryPoint);
    }

    /**
     * Calculate the distance from a spatial cell to a point.
     * (note the point is not in the spatial cell)
     * */
    @Override
    public void calDistanceToPoint(STPoint p) {
        // case 1:
        //    .p
        //  --------------
        //   |          |
        //   |          |
        //  --------------
        //        .p
        if (p.getLongitude() >= lowerLeft.getLongitude()
                && p.getLongitude() <= upperRight.getLongitude()) {
            STPoint p1 = new STPoint(p.getLongitude(), lowerLeft.getLatitude());
            STPoint p2 = new STPoint(p.getLongitude(), upperRight.getLatitude());
            this.distanceToPoint = Math.min(getEucDistance(p, p1), getEucDistance(p, p2));
        }
        // case 2:
        //
        //  --------------
        //   |          | .p
        // .p|          |
        //  --------------
        //
        else if (p.getLatitude() >= lowerLeft.getLatitude()
                && p.getLatitude() <= upperRight.getLatitude()) {
            STPoint p1 = new STPoint(lowerLeft.getLongitude(), p.getLatitude());
            STPoint p2 = new STPoint(upperRight.getLongitude(), p.getLatitude());
            this.distanceToPoint = Math.min(getEucDistance(p, p1), getEucDistance(p, p2));
        }
        // case 3:
        // .p|          | .p
        //  --------------
        //   |          |
        //   |          |
        //  --------------
        // .p|          | .p
        else {
            STPoint upperLeft = new STPoint(lowerLeft.getLongitude(), upperRight.getLatitude());
            STPoint lowerRight = new STPoint(upperRight.getLongitude(), lowerLeft.getLatitude());
            this.distanceToPoint = Math.min(
                    Math.min(getEucDistance(p, this.lowerLeft), getEucDistance(p, upperLeft)),
                    Math.min(getEucDistance(p, this.upperRight), getEucDistance(p, lowerRight))
            );
        }
    }

    @Override
    public double getDistanceToPoint() {
        return distanceToPoint;
    }

    @Override
    public String getID() {
        return spatialCoding.indexToString(spatialCode);
    }

    public ByteArray getSpatialCode() {
        return this.spatialCode;
    }

    public int getLevel() {
        return this.level;
    }

    public int getChildrenNum() {
        return this.childrenNum;
    }

    public SpatialCoding getSpatialCoding() {
        return this.spatialCoding;
    }

    /**
     * Get the father spatial cell of the current cell.
     * */
    public SpatialCell getFather() throws Exception {
        ByteArray father = spatialCoding.getFather(spatialCode);
        if(spatialCoding instanceof Z2Coding) {
            SpatialCoding spatialCoding = new Z2Coding((level - 1) * 2);
            return new SpatialCell(father, level - 1, spatialCoding, kNNQueryPoint);
        }
        else if(spatialCoding instanceof GeohashCoding){
            SpatialCoding spatialCoding = new GeohashCoding((level - 1) * 5);
            return new SpatialCell(father, level - 1, spatialCoding, kNNQueryPoint);
        }
        else {
            throw new Exception("The spatial Coding should be GeohashCoding , Z2Coding or HilbertCoding.");
        }
    }

    /**
     * Get 8 neighbour cells if the current cell.
     * */
    public List<SpatialCell> getNeighbourCells() throws UnsupportedEncodingException, ParseException {
        List<ByteArray> neighbours = spatialCoding.getNeighbours(spatialCode);
        List<SpatialCell> neighbourCells = new ArrayList<>(8);
        for (ByteArray neighbour : neighbours) {
            neighbourCells.add(new SpatialCell(neighbour, level, spatialCoding, kNNQueryPoint));
        }
        return neighbourCells;
    }

    public BoundingBox getBoundingBox() throws UnsupportedEncodingException, ParseException {
        return spatialCoding.getBoundingBox(spatialCode);
    }

    /**
     * Get range of this spatial cell on a specify level.
     * */
    public ByteArrayRange getRanges(int level) {
        int n = level - this.level;
        return spatialCoding.getChildrenRange(spatialCode, n);
    }

    @Override
    public String toString() {
        return this.spatialCoding.indexToString(spatialCode);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof SpatialCell) {
            SpatialCell anotherSpatialCell = (SpatialCell) obj;
            int n = spatialCode.getBytes().length;
            if (n == anotherSpatialCell.spatialCode.getBytes().length) {
                byte[] v1 = spatialCode.getBytes();
                byte[] v2 = anotherSpatialCell.spatialCode.getBytes();
                int i = 0;
                while (n-- != 0) {
                    if (v1[i] != v2[i])
                        return false;
                    i++;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0 && spatialCode.getBytes().length > 0) {

            for (int i = 0; i < spatialCode.getBytes().length; i++) {
                h = 31 * h + spatialCode.getBytes()[i];
            }
            hash = h;
        }

        return h;
    }
}
