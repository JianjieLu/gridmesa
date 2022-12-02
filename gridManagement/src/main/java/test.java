//import geoobject.BoundingBox;
//import geoobject.STPoint;
//import geoobject.geometry.GridGeometry;
//import geoobject.geometry.GridPolygon;
//import geoobject.geometry.SplitGeometry;
//import index.data.ByteArrayRange;
//import index.util.ByteArrayUtils;
//import com.alibaba.fastjson.JSONObject;
//import org.locationtech.jts.geom.Polygon;
//
//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//public class test {
//
//    public List<ByteArrayRange> getRanges(BoundingBox queryBox, int queryBoxSplitTimes){
//        List<ByteArrayRange> ranges = new ArrayList<>();
//        Polygon polygon = queryBox.toJTSPolygon();
//        System.out.println("polygon:"+polygon.getExteriorRing());
//        System.out.println("queryBoxSplitTimes:"+queryBoxSplitTimes);
//        GridGeometry gridPolygon = new GridPolygon("test", polygon, Integer.MAX_VALUE, queryBoxSplitTimes, Integer.MAX_VALUE);
//        for (SplitGeometry grid:gridPolygon.getSplitGeoms()){
//            // index转换10->01,01->10,与本项目中的geohash编码顺序一致;
//            // todo:怎么让这两个在生成时保持一致？
//            String index = grid.getGrid().getBinaryStrIndex();
//            int indexLength = index.length();
//            StringBuilder indexNew = new StringBuilder();
//            for (int i=0;i<indexLength;i+=2){
//                String subStr = index.substring(i, i + 2);
//                if (subStr.equals("10")){indexNew.append("01");}
//                else if (subStr.equals("01")){indexNew.append("10");}
//                else {indexNew.append(subStr);}
//            }
//
//            String binary = indexNew.toString();
//            System.out.println("binary:"+binary);
//            ByteArrayRange range = ByteArrayUtils.binaryStringToIndexRange(binary);
//            ranges.add(range);
//        }
//        return ranges;
//    }
//
//    public List<BoundingBox> parseQueryWindow(String queryFile) {
//
//        List<BoundingBox> queryBoxes = new ArrayList<>();
//        try {
//            // create a reader instance
//            BufferedReader br = new BufferedReader(new FileReader(queryFile));
//            // read until end of file
//            String line;
//            while ((line = br.readLine()) != null) {
//                JSONObject jsonObj = JSONObject.parseObject(line);
//                if (jsonObj.containsKey("minLng")) {
//                    double minLng = jsonObj.getDoubleValue("minLng");
//                    double minLat = jsonObj.getDoubleValue("minLat");
//                    double maxLng = jsonObj.getDoubleValue("maxLng");
//                    double maxLat = jsonObj.getDoubleValue("maxLat");
//                    STPoint bottomLeft = new STPoint(minLng, minLat);
//                    STPoint upperRight = new STPoint(maxLng, maxLat);
//                    BoundingBox queryBox = new BoundingBox(bottomLeft, upperRight);
//                    queryBoxes.add(queryBox);
//                }
//            }
//
//            // close the reader
//            br.close();
//
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//        return queryBoxes;
//    }
//
//    public static void main(String[] args) {
//        test a = new test();
//
//        List<BoundingBox> queryBoxes = a.parseQueryWindow("D:\\data\\SRQLine20_5_0.txt");
//
//        for (BoundingBox queryBox:queryBoxes){
//            a.getRanges(queryBox,1);
//        }
//
//
////        for (int i=0;i<10;i++){
////            System.out.println("NanoId:"+NanoIdUtils.randomNanoId());
////            System.out.println("UUID:"+UUID.randomUUID().toString().replace("-",""));
////        }
////        System.out.println(JsonUtil.wkt2Feature("LineString (13.74 51.05,13.75 51.06,13.78 51.00,13.74 51.05)"));
//
////        JSONObject jsonObject = JsonUtil.readLocalJSONFile("./ingest.json");
////        String inputPath = jsonObject.getString("inputPath");//输入文件
////        String tableName = jsonObject.getString("tableNamePrefix");//表名前缀
////        Object maxNumTemp = jsonObject.get("maxNum");//表名前缀
////        int maxNum = maxNumTemp == null ?Integer.MAX_VALUE:(int) maxNumTemp;
//    }
//}
