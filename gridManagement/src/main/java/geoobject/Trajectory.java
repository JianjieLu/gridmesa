package geoobject;


import serialize.SerializeTrajectory;

/**
 * This class is the abstraction of trajectory.
 *
 * @author yangxiangyang
 * Create on 2022-07-20.
 */
public class Trajectory extends BaseElement{
    private SerializeTrajectory trajectory;
    private String trajID;

    public Trajectory(SerializeTrajectory trajectory) {
        this.trajectory = trajectory;
        this.trajID = trajectory.getId();
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
        return this.trajID;
    }

    @Override
    public int hashCode() {
        return this.trajID.hashCode();
    }

    public SerializeTrajectory getFeatures() {
        return this.trajectory;
    }
}
