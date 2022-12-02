package query.condition;

import org.locationtech.jts.geom.Geometry;

public class QueryConditionOld {

    private Geometry queryGeometry;

    private boolean contain;
    private boolean refine;

    public QueryConditionOld(Geometry queryGeometry, boolean contain, boolean refine) {
        this.queryGeometry = queryGeometry;
        this.contain = contain;
        this.refine = refine;
    }

    public void setQueryGeometry(Geometry queryGeometry) {
        this.queryGeometry = queryGeometry;
    }

    public void setContains(boolean contain) {
        this.contain = contain;
    }

    public Geometry getQueryGeometry() {
        return queryGeometry;
    }

    public boolean isContain() {
        return contain;
    }
    public boolean isRefine() {
        return refine;
    }
}
