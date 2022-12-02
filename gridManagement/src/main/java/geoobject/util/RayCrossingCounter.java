package geoobject.util;


import geoobject.STPoint;

/**
 * Ray method is used for determining the topological relationship between points and polygons.
 */
public class RayCrossingCounter {
    private STPoint p;
    private int crossingCount = 0;
    // true if the test point lies on an input segment
    private boolean isPointOnSegment = false;
    public RayCrossingCounter(STPoint p) {
        this.p = p;
    }

    public static boolean isPointInRing(STPoint p, STPoint[] ring) {
        RayCrossingCounter counter = new RayCrossingCounter(p);

        for (int i = 1; i < ring.length; i++) {
            STPoint p1 = ring[i];
            STPoint p2 = ring[i - 1];
            counter.countSegment(p1, p2);
            if (counter.isOnSegment()) {
                return counter.getLocation() != Location.EXTERIOR;
            }
        }
        return counter.getLocation() != Location.EXTERIOR;
    }

    /**
     * Counts a segment
     *
     * @param p1 an endpoint of the segment
     * @param p2 another endpoint of the segment
     */
    public void countSegment(STPoint p1, STPoint p2) {
        /**
         * For each segment, check if it crosses
         * a horizontal ray running from the test point in the positive x direction.
         */

        // check if the segment is strictly to the left of the test point
        if (p1.getLongitude() < p.getLongitude() && p2.getLongitude() < p.getLongitude()) {
            return;
        }

        // check if the point is equal to the current ring vertex
        if (p.getLongitude() == p2.getLongitude() && p.getLatitude() == p2.getLatitude()) {
            isPointOnSegment = true;
            return;
        }
        /**
         * For horizontal segments, check if the point is on the segment.
         * Otherwise, horizontal segments are not counted.
         */
        if (p1.getLatitude() == p.getLatitude() && p2.getLatitude() == p.getLatitude()) {
            double minLng = p1.getLongitude();
            double maxLng = p2.getLongitude();
            if (minLng > maxLng) {
                minLng = p2.getLongitude();
                maxLng = p1.getLongitude();
            }
            if (p.getLongitude() >= minLng && p.getLongitude() <= maxLng) {
                isPointOnSegment = true;
            }
            return;
        }
        /**
         * Evaluate all non-horizontal segments which cross a horizontal ray to the
         * right of the test pt. To avoid double-counting shared vertices, we use the
         * convention that
         * <ul>
         * <li>an upward edge includes its starting endpoint, and excludes its
         * final endpoint
         * <li>a downward edge excludes its starting endpoint, and includes its
         * final endpoint
         * </ul>
         */
        if (((p1.getLatitude() > p.getLatitude()) && (p2.getLatitude() <= p.getLatitude()))
                || ((p2.getLatitude() > p.getLatitude()) && (p1.getLatitude() <= p.getLatitude()))) {
            int orient = Orientation.orientationIndex(p1, p2, p);
            if (orient == 0) {
                isPointOnSegment = true;
                return;
            }
            // Re-orient the result if needed to ensure effective segment direction is upwards
            if (p2.getLatitude() < p1.getLatitude()) {
                orient = -orient;
            }
            // The upward segment crosses the ray if the test point lies to the left (CCW) of the segment.
            if (orient == 1) {
                crossingCount++;
            }
        }
    }

    /**
     * Reports whether the point lies exactly on one of the supplied segments.
     * This method may be called at any time as segments are processed.
     * If the result of this method is <tt>true</tt>,
     * no further segments need be supplied, since the result
     * will never change again.
     *
     * @return true if the point lies exactly on a segment
     */
    public boolean isOnSegment() {
        return isPointOnSegment;
    }

    private Location getLocation() {
        if (isPointOnSegment) {
            return Location.BOUNDARY;
        }

        // The point is in the interior of the ring if the number of X-crossings is
        // odd.
        if ((crossingCount % 2) == 1) {
            return Location.INTERIOR;
        }
        return Location.EXTERIOR;
    }

    enum Location {
        INTERIOR,
        EXTERIOR,
        BOUNDARY
    }

    static class Orientation {
        /**
         * A value which is safely greater than the
         * relative round-off error in double-precision numbers
         */
        private static final double DP_SAFE_EPSILON = 1e-15;

        /**
         * Returns the index of the direction of the point <code>q</code> relative to
         * a vector specified by <code>p1-p2</code>.
         *
         * @param p1 the origin point of the vector
         * @param p2 the final point of the vector
         * @param q  the point to compute the direction to
         * @return 1 if q is counter-clockwise (left) from p1-p2
         * @return -1 if q is clockwise (right) from p1-p2
         * @return 0 if q is collinear with p1-p2
         */
        public static int orientationIndex(STPoint p1, STPoint p2, STPoint q) {
            // fast filter for orientation index
            // avoids use of slow extended-precision arithmetic in many cases
            int index = orientationIndexFilter(p1, p2, q);
            if (index <= 1) {
                return index;
            }

            // normalize coordinates
            DD dx1 = DD.valueOf(p2.getLongitude()).selfAdd(-p1.getLongitude());
            DD dy1 = DD.valueOf(p2.getLatitude()).selfAdd(-p1.getLatitude());
            DD dx2 = DD.valueOf(q.getLongitude()).selfAdd(-p2.getLongitude());
            DD dy2 = DD.valueOf(q.getLatitude()).selfAdd(-p2.getLatitude());

            // sign of determinant - unrolled for performance
            return dx1.selfMultiply(dy2).selfSubtract(dy1.selfMultiply(dx2)).signum();
        }

        /**
         * A filter for computing the orientation index of three coordinates.
         * <p>
         * If the orientation can be computed safely using standard DP
         * arithmetic, this routine returns the orientation index.
         * Otherwise, a value i > 1 is returned.
         * In this case the orientation index must
         * be computed using some other more robust method.
         * The filter is fast to compute, so can be used to
         * avoid the use of slower robust methods except when they are really needed,
         * thus providing better average performance.
         * <p>
         * Uses an approach due to Jonathan Shewchuk, which is in the public domain.
         *
         * @param pa a coordinate
         * @param pb a coordinate
         * @param pc a coordinate
         * @return the orientation index if it can be computed safely
         * @return i > 1 if the orientation index cannot be computed safely
         */
        private static int orientationIndexFilter(STPoint pa, STPoint pb, STPoint pc) {
            double detsum;

            double detleft = (pa.getLongitude() - pc.getLongitude()) * (pb.getLatitude() - pc.getLatitude());
            double detright = (pa.getLatitude() - pc.getLatitude()) * (pb.getLongitude() - pc.getLongitude());
            double det = detleft - detright;

            if (detleft > 0.0) {
                if (detright <= 0.0) {
                    return signum(det);
                } else {
                    detsum = detleft + detright;
                }
            } else if (detleft < 0.0) {
                if (detright >= 0.0) {
                    return signum(det);
                } else {
                    detsum = -detleft - detright;
                }
            } else {
                return signum(det);
            }

            double errbound = DP_SAFE_EPSILON * detsum;
            if ((det >= errbound) || (-det >= errbound)) {
                return signum(det);
            }

            return 2;
        }

        private static int signum(double x) {
            if (x > 0) {
                return 1;
            }
            if (x < 0) {
                return -1;
            }
            return 0;
        }
    }
}