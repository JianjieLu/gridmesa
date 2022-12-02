package geoobject.geometry;

import com.alibaba.fastjson.JSONObject;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

public class GridGeometryFactory {

    public static GridGeometry createGridGeometry(String id,Geometry geometry, JSONObject jsonObject){

        int maxLevel = jsonObject.get("maxLevel") == null ?Integer.MAX_VALUE:(int) jsonObject.get("maxLevel");//最深等级
        int recursiveTimes = jsonObject.get("recursiveTimes") == null ?Integer.MAX_VALUE:(int) jsonObject.get("recursiveTimes");//最大分割次数

        if (geometry instanceof Point)
            return new GridPoint(id,(Point)geometry,maxLevel);
        else if(geometry instanceof LineString)
            return new GridLineString(id,(LineString)geometry,recursiveTimes);
        else if (geometry instanceof Polygon)
            return new GridPolygon(id,(Polygon)geometry,recursiveTimes);
        else
            throw new RuntimeException("unsupport geometry type!");
    }
}

