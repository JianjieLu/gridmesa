package util;

import com.alibaba.fastjson.JSONObject;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.geotools.data.DataUtilities;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geojson.geom.GeometryJSON;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import java.io.*;
import org.geotools.geojson.feature.FeatureJSON;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class JsonUtil {

    /**
     * Read text file from local disk. Only for small file.
     *
     * @param path file path on local disk.
     * @return string of file content.
     */
    public static JSONObject readLocalJSONFile(String path) {
        File file = new File(path);
        StringBuilder sb = new StringBuilder();
        try {
            Reader reader = new InputStreamReader(new FileInputStream(file));
            BufferedReader br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return JSONObject.parseObject(sb.toString());
    }

    /**
     * 读取HDFS文件
     *
     * @param path 文件路径
     * @param conf HDFS配置
     */
    public static String readHDFSFile(String path, Configuration conf) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        Path file = new Path(path);
        FSDataInputStream inStream = fs.open(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(inStream));

        StringBuilder text = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            text.append(line);
        }
        inStream.close();
        br.close();

        return text.toString();
    }

    /**
     * 写文件到HDFS
     *
     * @param text 写入的文件内容
     * @param path 文件路径
     * @param conf HDFS配置
     */
    public static void writeHDFSText(String text, String path, Configuration conf) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        Path file = new Path(path);

        FSDataOutputStream outStream = fs.create(file);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outStream));
        bw.write(text);
        bw.flush();
        bw.close();
        outStream.close();
    }

    public static JSONObject wkt2Json(String wktStr){
        JSONObject geometry=null;
        try{
            WKTReader reader=new WKTReader();
            Geometry geom=reader.read(wktStr);
            StringWriter writer=new StringWriter();
            GeometryJSON gjson=new GeometryJSON();
            gjson.write(geom,writer);
            geometry=JSONObject.parseObject(writer.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
        return geometry;
    }

    /**
     * WKT 转 Feature
     * @param wktStr WKT 字符串
     * @return Feature JSON 对象
     */
    public static JSONObject wkt2Feature(String wktStr) {
        JSONObject featureJson = new JSONObject();
        try {
            // wkt2Geometry
            WKTReader reader=new WKTReader();
            Geometry geom=reader.read(wktStr);
            // set geometry
            SimpleFeatureType type = DataUtilities.createType("Link", "geometry:"+geom.getGeometryType());
            SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(type);
            featureBuilder.add(geom);
            // set id
            String id = NanoIdUtils.randomNanoId();
            SimpleFeature feature = featureBuilder.buildFeature(id);
            // write
            StringWriter writer = new StringWriter();
            FeatureJSON fJson = new FeatureJSON();
            fJson.writeFeature(feature, writer);
            // trans2json
            featureJson = JSONObject.parseObject(writer.toString());
        }catch (Exception e) {
            System.out.println("WKT 转 Feature 出现异常："+ e);
            e.printStackTrace();
        }
        return featureJson;
    }
}
