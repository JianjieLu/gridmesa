package query.filter;

import geoobject.BoundingBox;
import geoobject.Polygon;
import geoobject.STPoint;
import serialize.SerializePolygon;
import java.io.Serializable;
import java.util.BitSet;
import java.util.List;

public class commonFilter implements Serializable {

    public static int rowKeyFilter(BoundingBox extendBox,BoundingBox queryBox,BoundingBox rowBox){
        if (!rowBox.getExtendBox().intersects(queryBox) || !extendBox.intersects(rowBox)) { return 0; }
        if (queryBox.contains(rowBox)) {return 2;}
        return 1;
    }

    public static boolean rowKeyFilter1(BoundingBox extendBox,BoundingBox rowBox){
        return !extendBox.intersects(rowBox);
    }

    public static boolean mbrFilter(BoundingBox queryBox,BoundingBox mbrBox){
        return queryBox.intersects(mbrBox);
    }

    // 根据signature过滤；0:不相交;1:相交;2:至少包含一个网格;
    public static int bitSetFilter(BoundingBox rowBox, BitSet bs,int signatureSize){

        double MinLng = rowBox.getMinLng();
        double MinLat = rowBox.getMinLat();
        double MaxLng = rowBox.getMaxLng();
        double MaxLat = rowBox.getMaxLat();
        double detaLng = (MaxLng - MinLng)*2 / signatureSize;
        double detaLat = (MaxLat - MinLat)*2 / signatureSize;
        for (int k = bs.nextSetBit(0); k >= 0; k = bs.nextSetBit(k+1)) {
            int i = k / signatureSize;
            int j = k % signatureSize;
            double gridMinX = MinLng + detaLng * i;
            double gridMaxX = MinLng + detaLng * (i+1);
            double gridMinY = MinLat + detaLat*j;
            double gridMaxY = MinLat + detaLat*(j+1);
            BoundingBox signatureBox = new BoundingBox(gridMinX, gridMaxX, gridMinY, gridMaxY);
            if (rowBox.intersects(signatureBox)) {
                if (rowBox.contains(signatureBox)) { return 2; }
                else {return 1;}
            }
        }
        return 0;
    }

    public static boolean geomFilter(byte[] geomByte, BoundingBox queryBox, boolean isPolyline) {
        SerializePolygon serializeGeom = new SerializePolygon.Builder(geomByte).build();
        List<STPoint> lngLats = serializeGeom.getSTPointList();
        // 线面数据都使用polygon
        Polygon polygon = new Polygon(lngLats, isPolyline);
        return queryBox.intersects(polygon);
    }

}
