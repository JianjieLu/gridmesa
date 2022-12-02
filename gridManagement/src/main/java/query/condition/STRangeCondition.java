package query.condition;

import geoobject.BoundingBox;
import geoobject.STPoint;

import java.util.Date;

/**
 * Spatial temporal range query condition.
 **/
public class STRangeCondition extends QueryCondition {

    /**
     * 左下角点
     */
    private STPoint bottomLeft;

    /**
     * 右上角点
     */
    private STPoint upperRight;

    /**
     * 起始时间
     */
    private Date startDate;

    /**
     * 终止时间
     */
    private Date endDate;

    /**
     * 查询框划分层级
     */
    private int maxRecursive;

    private STRangeCondition(STPoint bottomLeft, STPoint upperRight, Date startDate, Date endDate) {
        this.bottomLeft = bottomLeft;
        this.upperRight = upperRight;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static STRangeCondition fromContinuousTime(STPoint bottomLeft,
                                                      STPoint upperRight,
                                                      Date startDate,
                                                      Date endDate) {
        return new STRangeCondition(bottomLeft, upperRight, startDate, endDate);
    }

    public STPoint getBottomLeft() {
        return bottomLeft;
    }

    public STPoint getUpperRight() {
        return upperRight;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public boolean hasStartDate() {
        return startDate != null;
    }

    public boolean hasEndDate() {
        return endDate != null;
    }

    public BoundingBox getQueryBBox() {
        if (this.hasStartDate() && this.hasEndDate()) {
            return new BoundingBox(bottomLeft, upperRight, new Date[]{startDate, endDate});
        } else {
            return new BoundingBox(bottomLeft, upperRight);
        }
    }

    public int getMaxRecursive(){
        return maxRecursive;
    }

    public void setMaxRecursive(int maxRecursive){
        this.maxRecursive = maxRecursive;
    }

}
