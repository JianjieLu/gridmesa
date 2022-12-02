package geoobject.util;

import geoobject.BoundingBox;
import geoobject.STPoint;
import java.util.Date;

/**
 * The simple polygon refers to the polygons without any holes. And only some
 * simple topology computations are supported.
 * @author Guan Hongli
 * create on 2019-4-27
 */
public class SimplePolygon {

    BoundingBox boundingBox;
    STPoint[] ring;
    private Date dateStart = null; //Coarse
    private Date dateEnd = null; //Coarse
    private Date timeStart = null; //precise
    private Date timeEnd = null; //precise
    static int num = 0;
    private boolean hasTime = false;
    private boolean hasPreciseTime = false;

    /**
     * The construction of simple polygon must be a ring, i.e. the points of the
     * shell must be closed.
     * @param shell
     */
    public SimplePolygon(STPoint[] shell) {

        if (shell[shell.length - 1].getLongitude()!=shell[0].getLongitude() ||
                shell[shell.length - 1].getLatitude()!=shell[0].getLatitude()) {
            throw new IllegalArgumentException("The shell coordinates of simple polygon must be closed!");
        }
        double minLng = Double.MAX_VALUE;
        double minLat = Double.MAX_VALUE;
        double maxLng = -Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        for (STPoint coor: shell) {
            minLng = Math.min(coor.getLongitude(), minLng);
            minLat = Math.min(coor.getLatitude(), minLat);
            maxLng = Math.max(coor.getLongitude(), maxLng);
            maxLat = Math.max(coor.getLatitude(), maxLat);
        }
        boundingBox = new BoundingBox(minLng, minLat, maxLng, maxLat);
        ring = shell;
    }
    /**
     * The construction of simple polygon must be a ring, i.e. the points of the
     * shell must be closed.
     * @param shell
     */
    public SimplePolygon(STPoint[] shell, boolean isPolyline) {
        if(!isPolyline){
            new SimplePolygon(shell);
        }
        double minLng = Double.MAX_VALUE;
        double minLat = Double.MAX_VALUE;
        double maxLng = -Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        for (STPoint coor: shell) {
            minLng = Math.min(coor.getLongitude(), minLng);
            minLat = Math.min(coor.getLatitude(), minLat);
            maxLng = Math.max(coor.getLongitude(), maxLng);
            maxLat = Math.max(coor.getLatitude(), maxLat);
        }
        boundingBox = new BoundingBox(minLng, minLat, maxLng, maxLat);
        ring = shell;
    }

    /**
     * The construction of simple polygon must be a ring, i.e. the points of the
     * shell must be closed.
     * @param shell
     */
    public SimplePolygon(STPoint[] shell, Date[] timeRange) {
        this(shell);
        boundingBox.setTime(timeRange);
        try{
            if(timeRange.length == 2){
                dateStart = timeRange[0];
                dateEnd = timeRange[1];
                hasTime = true;
            }
            else if(timeRange.length == 4) {
                dateStart = timeRange[0];
                dateEnd = timeRange[1];
                timeStart = timeRange[2];
                timeEnd = timeRange[3];
                hasTime = true;
                hasPreciseTime = true;
            }
            else{
                throw new IllegalArgumentException();
            }
        }catch (IllegalArgumentException e){
            System.out.println("The length of timeRange should be two or four! ");
        }
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public boolean isHasTime() {
        return hasTime;
    }

    public boolean isHasPreciseTime(){
        return hasPreciseTime;
    }

    public Date getDateStart() {
        return dateStart;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public Date getTimeStart(){
        return timeStart;
    }

    public Date getTimeEnd(){
        return timeEnd;
    }

    public void setTime(Date[] timeRange){
        try{
            if(timeRange.length == 2){
                dateStart = timeRange[0];
                dateEnd = timeRange[1];
                hasTime = true;
            }
            else if(timeRange.length == 4) {
                dateStart = timeRange[0];
                dateEnd = timeRange[1];
                timeStart = timeRange[2];
                timeEnd = timeRange[3];
                hasTime = true;
                hasPreciseTime = true;
            }
            else{
                throw new IllegalArgumentException();
            }
        }catch (IllegalArgumentException e){
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

        SimplePolygon polygon = new SimplePolygon(ring);
        return polygon.contains(rectangle.getLowerLeft())
                && polygon.contains(rectangle.getUpperLeft())
                && polygon.contains(rectangle.getUpperRight())
                && polygon.contains(rectangle.getLowerRight());
    }

    public boolean intersects(BoundingBox rectangle) {
        if (!boundingBox.intersects(rectangle)) {
            return false;
        }
        return !(rectangle.getMinLng() > boundingBox.getMaxLng() || rectangle.getMaxLng() < boundingBox.getMinLng() ||
                rectangle.getMinLat() > boundingBox.getMaxLat() || rectangle.getMaxLat() < boundingBox.getMinLat());
    }


    public STPoint[] getRing(){
        return ring;
    }


}