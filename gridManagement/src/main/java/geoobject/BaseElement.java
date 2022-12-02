package geoobject;

import index.util.VincentyGeodesy;

import javax.annotation.Nonnull;

/**
 * This class is mainly used for the convenience kNN query.
 */
public abstract class BaseElement implements Comparable<BaseElement> {
    /**
     * Get the distance from the query point to element,
     * mainly in the scene of kNN query
     *
     * @return distance from query point to element
     */
    public abstract double getDistanceToPoint();

    protected abstract void calDistanceToPoint(STPoint point);

    public abstract String getID();

    @Override
    public int compareTo(@Nonnull geoobject.BaseElement element) {
        if (this.getDistanceToPoint() > element.getDistanceToPoint()) {
            return 1;
        } else if (this.getDistanceToPoint() < element.getDistanceToPoint()) {
            return -1;
        }
        return 0;
    }

    /**
     * Get the distance between two points under the WGS-84 coordinate.
     */
    protected double getEucDistance(STPoint p1, STPoint p2) {
        return VincentyGeodesy.distanceInMeters(p1, p2);
    }
}
