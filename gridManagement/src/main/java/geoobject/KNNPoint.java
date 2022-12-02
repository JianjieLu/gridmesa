package geoobject;

import serialize.SerializeGeometry;
import serialize.SerializePoint;

import javax.annotation.Nonnull;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class is the abstraction of spatial-temporal point, mainly
 * for the convenience of kNN query.
 *
 * @author Guan Hongli
 * Create on 2019-3-9.
 * Edit by Yu Liebing on 2019-07-06.
 */
public class KNNPoint extends BaseElement {
    private SerializeGeometry geometry;
    private double distanceToPoint = -1.0;
    private String id;
    private double longitude, latitude;
    private long timestamp;

    public KNNPoint(SerializePoint geometry) {
        this.geometry = geometry;
        this.id = geometry.getId();
        this.latitude =  geometry.getLat();
        this.longitude = geometry.getLng();
        this.timestamp = geometry.getTime();
    }

    public KNNPoint(SerializePoint geometry, STPoint kNNQueryPoint) {
        this.geometry = geometry;
        this.id = geometry.getId();
        this.latitude =  geometry.getLat();
        this.longitude = geometry.getLng();
        this.timestamp = geometry.getTime();
        calDistanceToPoint(kNNQueryPoint);
    }

    @Override
    public void calDistanceToPoint(STPoint p) {
        distanceToPoint = getEucDistance(new STPoint(longitude,latitude), p);
    }

    @Override
    public double getDistanceToPoint() {
        return this.distanceToPoint;
    }

    @Override
    public String getID() {
        return this.id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof KNNPoint) {
            KNNPoint point = (KNNPoint) obj;
            if (id.equals(point.id) && Math.abs(latitude - point.latitude) < 1e-8
            && Math.abs(longitude - point.longitude) < 1e-8 && timestamp == point.timestamp) {
                return true;
            }
            return false;
        }
        return false;

    }

    @Override
    public int hashCode() {
        StringBuilder sb = new StringBuilder(this.id);
        sb.append(this.longitude);
        sb.append(this.latitude);

        sb.append(this.timestamp);
        return sb.hashCode();
    }

    /**
     * Need to override it to ensure the timestamp is different.
     * */

    @Override
    public int compareTo(@Nonnull BaseElement element) {
        if (this.getDistanceToPoint() > element.getDistanceToPoint()) {
            return 1;
        }
        else if (this.getDistanceToPoint() < element.getDistanceToPoint()) {
            return -1;
        }

        KNNPoint point = (KNNPoint) element;
        if (this.timestamp > point.timestamp) {
            return 1;
        } else if (this.timestamp < point.timestamp) {
            return -1;
        }
        return 0;
    }


    public SerializeGeometry getStGeometry() {
        return geometry;
    }

    @Override
    public String toString() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return "lng: " + longitude + ", lat: " + latitude + ", time: "
                + df.format(new Date(timestamp)) + ", distance: " + distanceToPoint;
    }

}