package common;

import com.google.common.collect.Iterators;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.io.Serializable;
import java.util.Iterator;

public class WKTTransformation<T extends Geometry> implements Serializable, FlatMapFunction<Iterator<String>, T> {

    @Override
    public Iterator<T> call(Iterator<String> iterator) throws Exception {
        WKTReader wktReader = new WKTReader();
        return Iterators.transform(iterator, s -> {
            try {
                return (T)wktReader.read(s);
            }catch (ParseException e){
                throw new RuntimeException(e);
            }
        });
    }

}
