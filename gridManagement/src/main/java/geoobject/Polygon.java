package geoobject;



import geoobject.util.RayCrossingCounter;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * The simple polygon refers to the polygons without any holes. And only some
 * simple topology computations are supported.
 *
 * @author Guan Hongli
 * create on 2019-4-27
 */

public class Polygon extends BaseElement{

    BoundingBox boundingBox;
    STPoint[] ring;
    private Date dateStart = null; //Coarse
    private Date dateEnd = null; //Coarse
    private Date timeStart = null; //precise
    private Date timeEnd = null; //precise
    private boolean hasTime = false;
    private boolean hasPreciseTime = false;
    private String id;

    /**
     * The construction of simple polygon must be a ring, i.e. the points of the
     * shell must be closed.
     *
     * @param shell points list
     */
    public Polygon(STPoint[] shell) {
        this(shell, false);
    }

    public Polygon(STPoint[] shell, boolean isPolyline) {
        this(Arrays.asList(shell), isPolyline);
    }

    public Polygon(List<STPoint> shell) {
        this(shell, false);
    }

    public Polygon(List<STPoint> shell, boolean isPolyline) {
        if (!isPolyline) {
            if (shell.get(shell.size() - 1).getLongitude() != shell.get(0).getLongitude() ||
                    shell.get(shell.size() - 1).getLatitude() != shell.get(0).getLatitude()) {
                throw new IllegalArgumentException("The shell coordinates of polygon must be closed!");
            }
        }
        double minLng = Double.MAX_VALUE;
        double minLat = Double.MAX_VALUE;
        double maxLng = -Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        for (STPoint coor : shell) {
            minLng = Math.min(coor.getLongitude(), minLng);
            minLat = Math.min(coor.getLatitude(), minLat);
            maxLng = Math.max(coor.getLongitude(), maxLng);
            maxLat = Math.max(coor.getLatitude(), maxLat);
        }
        boundingBox = new BoundingBox(minLng, minLat, maxLng, maxLat);
        ring = new STPoint[shell.size()];
        shell.toArray(ring);
    }

    public Polygon(List<STPoint> shell, String id) {
        this(shell, false);
        this.id = id;
    }

    public Polygon(List<STPoint> shell, String id, boolean isPolyline) {
        this(shell, isPolyline);
        this.id = id;
    }
    /**
     * The construction of simple polygon must be a ring, i.e. the points of the
     * shell must be closed.
     *
     * @param shell
     */
    public Polygon(STPoint[] shell, Date[] timeRange) {
        this(shell);
        boundingBox.setTime(timeRange);
        try {
            if (timeRange.length == 2) {
                dateStart = timeRange[0];
                dateEnd = timeRange[1];
                hasTime = true;
            } else if (timeRange.length == 4) {
                dateStart = timeRange[0];
                dateEnd = timeRange[1];
                timeStart = timeRange[2];
                timeEnd = timeRange[3];
                hasTime = true;
                hasPreciseTime = true;
            } else {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            System.out.println("The length of timeRange should be two or four! ");
        }
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public boolean isHasTime() {
        return hasTime;
    }

    public boolean isHasPreciseTime() {
        return hasPreciseTime;
    }

    public Date getDateStart() {
        return dateStart;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public Date getTimeStart() {
        return timeStart;
    }

    public Date getTimeEnd() {
        return timeEnd;
    }

    public void setTime(Date[] timeRange) {
        try {
            if (timeRange.length == 2) {
                dateStart = timeRange[0];
                dateEnd = timeRange[1];
                hasTime = true;
            } else if (timeRange.length == 4) {
                dateStart = timeRange[0];
                dateEnd = timeRange[1];
                timeStart = timeRange[2];
                timeEnd = timeRange[3];
                hasTime = true;
                hasPreciseTime = true;
            } else {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            System.out.println("The length of timeRange should be two or four! ");
        }
    }

    public boolean contains(STPoint point) {
        if (point.getLongitude() < boundingBox.getMinLng()
                || point.getLongitude() > boundingBox.getMaxLng()
                || point.getLatitude() < boundingBox.getMinLat()
                || point.getLatitude() > boundingBox.getMaxLat()) {
            return false;
        }
        return RayCrossingCounter.isPointInRing(point, ring);
    }

    public boolean contains(BoundingBox rectangle) {
        if (!boundingBox.contains(rectangle)) {
            return false;
        }

        Polygon polygon = new Polygon(ring);
        return polygon.contains(rectangle.getLowerLeft())
                && polygon.contains(rectangle.getUpperLeft())
                && polygon.contains(rectangle.getUpperRight())
                && polygon.contains(rectangle.getLowerRight());
    }

    public boolean intersects(BoundingBox rectangle) {
        return rectangle.intersects(this);
    }

    public STPoint[] getRing() {
        return ring;
    }

    @Override
    public double getDistanceToPoint() {
        return 0;
    }

    @Override
    protected void calDistanceToPoint(STPoint point) {

    }

    @Override
    public String getID() {
        return this.id;
    }
}
