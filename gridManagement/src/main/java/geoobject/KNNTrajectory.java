package geoobject;


import serialize.SerializeTrajectory;

/**
 * This class is the abstraction of trajectory, mainly
 * for the convenience of kNN query.
 *
 * @author Yu Liebing
 * Create on 2019-07-06.
 */
public class KNNTrajectory extends BaseElement{
    private double distanceToPoint = -1.0;
    private SerializeTrajectory trajectory;
    private String trjID;

    public KNNTrajectory(SerializeTrajectory trajectory) {
        this.trajectory = trajectory;
        this.trjID = trajectory.getId();
    }

    public KNNTrajectory(SerializeTrajectory trajectory,
                         STPoint kNNQueryPoint) {
        this.trajectory = trajectory;
        this.trjID = trajectory.getId();
        calDistanceToPoint(kNNQueryPoint);
    }

    @Override
    public void calDistanceToPoint(STPoint p) {
        double distance = Double.MAX_VALUE;
        for (STPoint stPoint : this.trajectory) {
            double pDistance = getEucDistance(p,
                    new STPoint(stPoint.getLongitude(), stPoint.getLatitude()));
            if (pDistance < distance) {
                distance = pDistance;
            }
        }
        this.distanceToPoint = distance;
    }

    @Override
    public double getDistanceToPoint() {
        return distanceToPoint;
    }

    @Override
    public String getID() {
        return this.trjID;
    }

    @Override
    public int hashCode() {
        return this.trjID.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof KNNTrajectory) {
            KNNTrajectory trajectory = (KNNTrajectory) obj;
            if (!this.trjID.equals(trajectory.trjID)) {
                return false;
            }
            if (Math.abs(distanceToPoint - trajectory.distanceToPoint) > 1e-8) {
                return false;
            }
            return true;
        }
        return false;
    }

    public SerializeTrajectory getFeatures() {
        return this.trajectory;
    }
}
