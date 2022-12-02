package geoobject.geometry;

import org.locationtech.geomesa.curve.Z2SFC;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.sfcurve.zorder.Z2;
import scala.Tuple2;

import java.util.Objects;

public class Grid implements Comparable<Grid> {

    private int level; //level<=256

    private long index;

    private String binaryStrIndex;

    private Envelope envelope;

    private Polygon polygon;

    public Grid(int level, long index) {
        this.level = level;
        this.index = index;
    }

    public Grid() {
    }

    public Grid(String binaryStrIndex) {
        this.level = binaryStrIndex.length()/2;
        this.index = Long.parseLong(binaryStrIndex,2);
        this.binaryStrIndex = binaryStrIndex;
    }

    public Grid(int level, double x, double y) {
        this(level, new Z2SFC(level).index(x, y, false));
    }

    public static Envelope computeEnvelope(Grid grid){
        Z2SFC z2 = new Z2SFC(grid.level);
        Tuple2<Object, Object> centLonLat = z2.invert(grid.index);
        double minLon = (Double) centLonLat._1() - (180.0 / (1 << grid.level));
        double minLat = (Double) centLonLat._2() - (90.0 / (1 << grid.level));
        double maxLon = (Double) centLonLat._1() + (180.0 / (1 << grid.level));
        double maxLat = (Double) centLonLat._2() + (90.0 / (1 << grid.level));

        return new Envelope(minLon, maxLon, minLat, maxLat);
    }

    public static double[] computeGridSize(int gridLevel){
        double detaLng = (double) 2*(180.0 / (1 << gridLevel));
        double detaLat = (double) 2*(90.0 / (1 << gridLevel));
        return new double[]{detaLng, detaLat};
    }

    public static Grid[] getFourNeighbors(Grid grid){
        Grid[] neighbors = new Grid[4];
        Z2 z2 = new Z2(grid.index);
        Tuple2<Object, Object> XY = z2.decode();
        neighbors[0] = new Grid(grid.level, Z2.apply((Integer) XY._1(), (Integer) XY._2() + 1));
        neighbors[1] = new Grid(grid.level,Z2.apply((Integer) XY._1() + 1, (Integer) XY._2()));
        neighbors[2] = new Grid(grid.level,Z2.apply((Integer) XY._1(), (Integer) XY._2() - 1));
        neighbors[3] = new Grid(grid.level,Z2.apply((Integer) XY._1() - 1, (Integer) XY._2()));
        return neighbors;
    }


    public Envelope getEnvelope() {
        if (envelope == null){
            envelope = computeEnvelope(this);
        }
        return envelope;
    }

    public Polygon getPolygon() {
        if (polygon == null){
            polygon = (Polygon) new GeometryFactory().toGeometry(getEnvelope());
        }
        return polygon;
    }



    public static Grid[] getChildren(Grid grid) {
        return new Grid[]{new Grid(grid.level + 1, grid.index << 2),
                new Grid(grid.level + 1, (grid.index << 2) | 1),
                new Grid(grid.level + 1, (grid.index << 2) | 2),
                new Grid(grid.level + 1, (grid.index << 2) | 3)};
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Grid grid = (Grid) o;
        return level == grid.level && index == grid.index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, index);
    }

    @Override
    public int compareTo(Grid other) {
        if (other == null)
            return 1;
        return this.getBinaryStrIndex().compareTo(other.getBinaryStrIndex());
    }

    @Override
    public String toString() {
        return "Grid{" +
                "level=" + level +
                ", index=" + index +
                '}';
    }

    public int getLevel() {
        return level;
    }


    public long getIndex() {
        return index;
    }

    //将index转换为二进制字符串
    public String getBinaryStrIndex(){
        if (binaryStrIndex == null){
            binaryStrIndex = toBinaryStrIndex(index, level);
        }

        return binaryStrIndex;
    }

    public static String toBinaryStrIndex(long index, int level){
        char[] charArr = new char[level * 2];
        for(int i = 0; i < charArr.length; i += 2){
            int offset = level * 2 - i - 1;
            charArr[i] = (index & (1L << offset)) == 0 ? '0' : '1';
            charArr[i + 1] = (index & (1L << (offset - 1))) == 0 ? '0' : '1';
        }
        return new String(charArr);
    }

}
