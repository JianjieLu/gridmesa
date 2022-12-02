package query.condition;

import geoobject.BoundingBox;
import geoobject.STPoint;
import org.apache.hadoop.hbase.client.Get;

import java.util.List;

/**
 * Spatial range query condition.
 *
 * @author Shendannan
 * Create on 2021-04-27
 **/
public class SRangeCondition extends QueryCondition {
    private final BoundingBox boundingBox;
    private List<Get> getList;
    protected STPoint bottomLeft;
    protected STPoint upperRight;
    private int maxRecursive;
    private boolean isPolyline;
    private boolean isMultiThread;
    private int maxLevel;
    private int signatureSize;

    private SRangeCondition(STPoint bottomLeft, STPoint upperRight, int maxRecursive) {
        this.bottomLeft = bottomLeft;
        this.upperRight = upperRight;
        this.boundingBox = new BoundingBox(bottomLeft, upperRight);
        this.maxRecursive = maxRecursive;
    }

    private SRangeCondition(BoundingBox box, int maxRecursive) {
        this.bottomLeft = box.getLowerLeft();
        this.upperRight = box.getUpperRight();
        this.boundingBox = box;
        this.maxRecursive = maxRecursive;
    }

    private SRangeCondition(BoundingBox box, int signatureSize, boolean isPolyline) {
        this.bottomLeft = box.getLowerLeft();
        this.upperRight = box.getUpperRight();
        this.boundingBox = box;
        this.signatureSize = signatureSize;
        this.isPolyline = isPolyline;
    }


    public static SRangeCondition fromPoints(STPoint bottomLeft,
                                             STPoint upperRight,
                                             int maxRecursive) {
        return new SRangeCondition(bottomLeft, upperRight, maxRecursive);
    }

    public static SRangeCondition fromBox(BoundingBox box,
                                          boolean isPolyline) {
        return new SRangeCondition(box,4,isPolyline);
    }

    public STPoint getBottomLeft() {
        return bottomLeft;
    }

    public STPoint getUpperRight() {
        return upperRight;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public int getMaxRecursive() { return maxRecursive; }
    public int getMaxLevel() { return maxLevel; }
    public boolean getIsPolyline() {
        return isPolyline;
    }

    public int getSignatureSize() {
        return signatureSize;
    }
    public boolean getIsMultiThread() {
        return isMultiThread;
    }

    public List<Get> getGetList() {
        return getList;
    }

    public void setMaxRecursive(int maxRecursive) {
        this.maxRecursive = maxRecursive;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public void setSignatureSize(int signatureSize) {
        this.signatureSize = signatureSize;
    }

    public void setGetList(List<Get> getList) {
        this.getList = getList;
    }

}
