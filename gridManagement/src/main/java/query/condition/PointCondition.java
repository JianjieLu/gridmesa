package query.condition;

import geoobject.STPoint;

/**
 * @Author zhangjianhao
 * @Date 2022/1/10
 */


public class PointCondition extends QueryCondition {

    private STPoint queryPoint;

    /**
     * In order to avoid infinite .query, the max search distance is required.
     * The max search distance is 100m by default.
     */
    private double maxSearchDistance = 100.0;

    public PointCondition(STPoint queryPoint, double maxSearchDistance) {
        this.queryPoint = queryPoint;
        this.maxSearchDistance = maxSearchDistance;
    }

    public STPoint getQueryPoint() {
        return queryPoint;
    }

    public double getMaxSearchDistance() {
        return maxSearchDistance;
    }


}
