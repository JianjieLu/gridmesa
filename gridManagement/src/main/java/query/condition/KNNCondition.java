package query.condition;

import geoobject.BoundingBox;
import geoobject.STGeometryTypeEnum;
import geoobject.STPoint;
import index.data.ByteArray;

import java.util.Date;
import java.util.Map;

/**
 * KNN query condition
 */

public class KNNCondition extends QueryCondition {

    private final STPoint queryPoint;
    private final int k;
    private final Date startTime;
    private final Date endTime;
    private Map<ByteArray, BoundingBox> preIndex;
    /**
     * Only use for trajectory.
     * In order to determine the required type of k , contain trajectory and point.
     * POINT for trajectory point query.
     * TRAJECTORY for trajectory object query.
     * Default is POINT.
     */
    private STGeometryTypeEnum kType = STGeometryTypeEnum.POINT;
    /**
     * In order to avoid infinite .query, the max search distance is required.
     * The max search distance is 100000m(i.e. 100km) by default.
     */
    private double maxSearchDistance = 10000.0;

    public KNNCondition(STPoint queryPoint, int k, Date startTime, Date endTime) {
        this.queryPoint = queryPoint;
        this.k = k;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public KNNCondition(STPoint queryPoint, int k, Date startTime, Date endTime, Map<ByteArray, BoundingBox> preIndex) {
        this.queryPoint = queryPoint;
        this.k = k;
        this.startTime = startTime;
        this.endTime = endTime;
        this.preIndex = preIndex;
    }

    public KNNCondition(STPoint queryPoint, int k, Date startTime, Date endTime,
                        STGeometryTypeEnum stGeometryTypeEnum) {
        this.queryPoint = queryPoint;
        this.k = k;
        this.startTime = startTime;
        this.endTime = endTime;
        this.kType = stGeometryTypeEnum;
    }

    public KNNCondition(STPoint queryPoint, int k, Date startTime, Date endTime,
                        STGeometryTypeEnum stGeometryTypeEnum, Map<ByteArray, BoundingBox> preIndex) {
        this.queryPoint = queryPoint;
        this.k = k;
        this.startTime = startTime;
        this.endTime = endTime;
        this.kType = stGeometryTypeEnum;
        this.preIndex = preIndex;
    }

    public KNNCondition(STPoint queryPoint, int k, Date startTime, Date endTime,
                        double maxSearchDistance) {
        this.queryPoint = queryPoint;
        this.k = k;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxSearchDistance = maxSearchDistance;
    }

    public KNNCondition(STPoint queryPoint, int k, Date startTime, Date endTime,
                        double maxSearchDistance, STGeometryTypeEnum stGeometryTypeEnum) {
        this.queryPoint = queryPoint;
        this.k = k;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxSearchDistance = maxSearchDistance;
        this.kType = stGeometryTypeEnum;
    }

    public KNNCondition(STPoint queryPoint, int k, Date startTime, Date endTime,
                        double maxSearchDistance, STGeometryTypeEnum stGeometryTypeEnum, Map<ByteArray, BoundingBox> preIndex) {
        this.queryPoint = queryPoint;
        this.k = k;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxSearchDistance = maxSearchDistance;
        this.kType = stGeometryTypeEnum;
        this.preIndex = preIndex;
    }

    public double getMaxSearchDistance() {
        return this.maxSearchDistance;
    }

    public void setMaxSearchDistance(double distance) {
        this.maxSearchDistance = distance;
    }

    public int getK() {
        return this.k;
    }

    public Date getStartTime() {
        return this.startTime;
    }

    public Date getEndTime() {
        return this.endTime;
    }

    public STPoint getQueryPoint() {
        return this.queryPoint;
    }

    public void setKType(STGeometryTypeEnum kTypeEnum) {
        this.kType = kTypeEnum;
    }

    public STGeometryTypeEnum getkType() {
        return kType;
    }

    public Map<ByteArray, BoundingBox> getPreIndex() {
        return preIndex;
    }

    public void setPreIndex(Map<ByteArray, BoundingBox> preIndex) {
        this.preIndex = preIndex;
    }

}