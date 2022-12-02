package common;

import geoobject.BaseElement;
import geoobject.Polygon;
import geoobject.STPoint;
import com.google.common.collect.Iterators;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class readPolygon<T extends BaseElement> implements Serializable, FlatMapFunction<Iterator<String>, T> {

    @Override
    public Iterator<T> call(Iterator<String> iterator) throws Exception {
        WKTReader wktReader = new WKTReader();
        return Iterators.transform(iterator, s -> {
            try {
                String[] stringSplit = s.split("@");
                String objId = stringSplit[0];
                Geometry geometry = wktReader.read(stringSplit[1]);
                Coordinate[] coordinates = geometry.getCoordinates();
                List<STPoint> lngLatList = new ArrayList<>();
                for (Coordinate coordinate : coordinates) {
                    double lng = coordinate.getX();
                    double lat = coordinate.getY();
                    lngLatList.add(new STPoint(lng, lat));
                }
                Polygon polygon = new Polygon(lngLatList, objId);
                return (T) polygon;
            }catch (ParseException e){
                throw new RuntimeException(e);
            }
        });
    }

}
