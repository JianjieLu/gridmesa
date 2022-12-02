import geoobject.geometry.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.tukaani.xz.XZ;
import serialize.SerializeRowKey;
import serialize.SerializeSketch;
import util.BitSetUtil;
import util.ByteArrayUtils;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;

public class codingTest {



    public static void main(String[] args) throws ParseException, UnsupportedEncodingException, org.locationtech.jts.io.ParseException {

        WKTReader wktReader = new WKTReader();
        String geometryWKT = "POLYGON ((13.3890342 52.5167309, 13.3890734 52.5164921, 13.3891163 52.5162306, 13.3894315 52.5162497, 13.3897060 52.5162664, 13.3900578 52.5162878, 13.3904777 52.5163133, 13.3906579 52.5163243, 13.3906017 52.5166665, 13.3905758 52.5168246, 13.3904208 52.5168152, 13.3901880 52.5168010, 13.3898523 52.5167806, 13.3894488 52.5167561, 13.3891471 52.5167378, 13.3890342 52.5167309))";
//        String polygon="LINESTRING(-85.6482568 39.00405699999999,-85.648502 39.003826000000004,-85.648594 39.003759,-85.64874700000001 39.00373400000001,-85.649032 39.00375199999999,-85.649211 39.003725,-85.649633 39.003548999999964,-85.651228 39.00291300000001,-85.651326 39.00283400000001,-85.651462 39.00258600000001,-85.65157100000002 39.002463000000006,-85.651793 39.002329,-85.65211699999999 39.00218600000002,-85.652315 39.00213700000003,-85.652572 39.00213099999999,-85.65264499999999 39.002139)";
//        String polygon="POLYGON ((-77.00448697253832 38.65929319700553, -76.95448697253832 38.65929319700553, -76.95448697253832 38.70929319700553, -77.00448697253832 38.70929319700553, -77.00448697253832 38.65929319700553))";
//        String polygon="POLYGON((-75.0611686706543 39.75702659362287,-75.06116759776658 39.75699607625043,-75.06111314891541 39.756996282449705,-75.06111449001992 39.75702638741973,-75.0611686706543 39.75702659362287))";
//        String polygon="POLYGON((-75.07309806346892 39.76094832423922,-75.07315921780173 39.76084605557688,-75.07305192944112 39.760803993432944,-75.0729854106903 39.760910385893595,-75.07309806346892 39.76094832423922))";
//        String polygon="POLYGON ((13.7404253 51.0531853, 13.7401960 51.0532229, 13.7399074 51.0527668, 13.7398939 51.0527447, 13.7398689 51.0526946, 13.7398334 51.0525837, 13.7400300 51.0525574, 13.7401001 51.0525480, 13.7401783 51.0525353, 13.7403924 51.0525004, 13.7404538 51.0524907, 13.7405474 51.0524687, 13.7406320 51.0524486, 13.7408519 51.0524013, 13.7411055 51.0523423, 13.7411504 51.0524391, 13.7411585 51.0524570, 13.7411905 51.0526127, 13.7411993 51.0526632, 13.7412118 51.0527555, 13.7412266 51.0528641, 13.7412350 51.0530762, 13.7411485 51.0530888, 13.7407233 51.0531452, 13.7406739 51.0531517, 13.7406666 51.0531528, 13.7404253 51.0531853))";
        int recursiveTimes = 4;
        Geometry geometry = wktReader.read(geometryWKT);
        GridGeometry gridGeometry = new GridNonPoint("test",geometry,recursiveTimes);
//        Grid grid;
//        grid = gridPolygon.getSplitGrids().get(0);
//        System.out.println(grid.getBinaryStrIndex());
//        String XZ2Index = gridPolygon.getXZ2Index();
//        String objectID="123456";
//        System.out.println("XZ2Index:"+XZ2Index+"\tlevel:"+ XZ2Index.length());
//        SerializeRowKey serializeData = new SerializeRowKey.Builder().setXZ2Index(XZ2Index).setId(objectID).build();
//        byte[] serializeRes = serializeData.getData();
//        SerializeRowKey serializeData1 = new SerializeRowKey.Builder(serializeRes).build();
//
//        System.out.println((int)serializeData1.getLevel()[0]);
//        System.out.println(Bytes.toString(serializeData1.getId()));
//        System.out.println(ByteArrayUtils.indexToBinaryString(serializeData1.getXZ2Index(), (int)serializeData1.getLevel()[0]));
//        System.out.println("getSignature0:"+gridPolygon.getSignature());
//        System.out.println("MBR0:"+geometry.getEnvelopeInternal().toString());
//        SerializeSketch serializeRes = new SerializeSketch.Builder()
//                .MBR(geometry.getEnvelopeInternal())
//                .signature(gridPolygon.getSignature())
//                .build();
//
//        System.out.println("MBR1:"+serializeRes.getMBR().toString());
//        System.out.println("getSignature1:"+serializeRes.getSignature());
//
//
//
//        BitSet bitSet;
//        //将BitSet对象转成byte数组
//        byte[] bytes = BitSetUtil.bitSet2ByteArray(gridPolygon.getSignature());
//        System.out.println("bytes:"+Arrays.toString(bytes));
//        //在将byte数组转回来
//        bitSet = BitSetUtil.byteArray2BitSet(bytes);
//        System.out.println("bitSet:"+bitSet);
        Collection<Grid> grids = gridGeometry.getSplitGrids();
        StringBuilder printRes = new StringBuilder("GEOMETRYCOLLECTION(");
        printRes.append(geometryWKT);
        for (Grid grid:grids){
            printRes.append(",").append(grid.getPolygon());
        }
        printRes.append(")");
        System.out.println(printRes.toString());
//        for (SplitGeometry grid:gridLine.getSplitGeoms()){
//            System.out.println(grid.getGrid().getBinaryStrIndex()+" isFull:"+grid.isFull()+" Envelope:"+grid.getGrid().getEnvelope());
//        }
//        System.out.println(gridLine.getSplitGeoms().size());
//        List<STPoint> lngLatList = new ArrayList<>();
//        for(int j = 0;j< coordinates.length;j++) {
//            Coordinate coordinate = coordinates[j];
//            double lng = coordinate.getX();
//            double lat = coordinate.getY();
//            lngLatList.add(new STPoint(lng, lat));
//        }
//
//        Polygon polygon = new Polygon(lngLatList,"123",false);
//        BoundingBox bbox = new BoundingBox(lngLatList.iterator());
//        List<ByteArrayRange> ranges = new ArrayList<>();
//        org.locationtech.jts.geom.Polygon polygon2 = bbox.toJTSPolygon();
//        GridGeometry gridPolygon = new GridPolygon("test", polygon2, Integer.MAX_VALUE, 1, Integer.MAX_VALUE);
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
//            ByteArrayRange range = ByteArrayUtils.binaryStringToIndexRange(binary);
//            ranges.add(range);
//        }

//        Coding coding = CodingFactory.createConcatenateCoding(Coding.ConcatenateType.T_S, Coding.SpatialType.Z2,
//                4, TIME_CODING_PATTERN, "h");
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date pointData = sdf.parse("2010-01-01 00:54:00");
//        STPoint stPoint = new STPoint(-76.0500,39.4200,pointData);
//        ByteArray stPointCode = coding.getIndex(stPoint);
//        // 改成这样跑不通，报错：排序有问题
//        System.out.println(coding.indexToString(stPointCode));

//        STPoint upperRight = new STPoint(-76.0400,39.4300);
//        Date startDate = sdf.parse("2010-01-01 00:01:00");
//        Date endDate = sdf.parse("2010-01-01 00:59:00");
//        GeohashCoding spatialCoding =(GeohashCoding) coding.getSpatialCoding();
//        System.out.println(polygon.getID());
//        GeohashCoding geohashCoding = new GeohashCoding(60);
//        List<GeoHash> grids = geohashCoding.decomposeRange1(polygon,0);
////        List<ByteArrayRange> ranges = geohashCoding.decomposeRange(polygon, 0);
////        for (ByteArrayRange range:ranges) {
////            System.out.println("range:"+geohashCoding.indexToString(range.getStart()));
////        }
////        System.out.println(ranges.size());
//        for (GeoHash grid : grids) {
//            boolean isContained = grid.getContained();
//            String index = grid.toBinaryString();
//            double lowLeftLon = grid.getBoundingBox().getLowerLeft().getLongitude();
//            double lowLeftLat = grid.getBoundingBox().getLowerLeft().getLatitude();
//            double upRightLon = grid.getBoundingBox().getUpperRight().getLongitude();
//            double upRightLat = grid.getBoundingBox().getUpperRight().getLatitude();
//            System.out.println("index:"+index+"\tisContained:"+isContained
//                    +"\tPOINT ("+lowLeftLon+" "+lowLeftLat+")"
//                    +",POINT ("+upRightLon+" "+upRightLat+")");
//        }
//        Z2Coding z2code = new Z2Coding(20);
//        STPoint stPoint = new STPoint(-76.0431,39.4272);
//        ByteArray index = z2code.getIndex(stPoint);
//        ByteArray indexByteArray = z2code.binaryStringToIndex("1100");
//        String longStringIndex = z2code.indexToString(indexByteArray);
//        System.out.println("longStringIndex:"+index.getLevel());


//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//        Date pointData = sdf.parse("2010/8/1 00:54:00");
//        SimpleDateFormat newSdf = new SimpleDateFormat("yyMMddHH");
//        System.out.println("pointData:"+newSdf.format(pointData));

//        String tempTime = "2022-05-21 13:14:00";
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        LocalDateTime dateTime = LocalDateTime.parse(tempTime, formatter);
//        long timestamp = dateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
//        System.out.println("dateTime:"+dateTime);
//
//        System.out.println("timestamp:"+timestamp);

//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date pointData = sdf.parse("2010-01-01 00:54:00");
//        STPoint bottomLeft = new STPoint(-76.0500,39.4200);
//        STPoint upperRight = new STPoint(-76.0400,39.4300);
//        Date startDate = sdf.parse("2010-01-01 00:01:00");
//        Date endDate = sdf.parse("2010-01-01 00:59:00");
//        STPoint stPoint = new STPoint(-76.043118,39.427243,pointData);

//        STPoint stPoint = new STPoint(114.35499000523124,30.529060014666783,"12345678");
//        ByteArray rowkey = spatialCoding.getIndex(stPoint);
//        System.out.println(spatialCoding.indexToString(rowkey));
//
//        SerializePoint serializeSTPoint = SerializePoint.fromSPoint(stPoint);
//        System.out.println(serializeSTPoint.getId());
//        System.out.println(serializeSTPoint.getLat());
//        System.out.println(serializeSTPoint.getBoundingBox());


//        SpatialCoding spatialCoding = coding.getSpatialCoding();

//        ByteArray indexByteArray = z2code.getIndex(stPoint);
//        String longStringIndex = z2code.indexToString(indexByteArray);
//        SpatialCell queryPointCell = new SpatialCell(queryPointSpatialCode,
//                z2code.getPrecision() / 2, z2code, stPoint);
////        System.out.println("queryPointCell:"+queryPointCell.getID());
//        List<SpatialCell> neighbourCells = queryPointCell.getNeighbourCells();
//        for (SpatialCell neighbourCell:neighbourCells) {
//            System.out.println("neighbourCell:"+neighbourCell.getID());
//        }

//        int level = indexByte[indexByte.length - 1] & 0xff;
//        System.out.println(level);

//
//        int level = queryPointCell.getLevel();
//        System.out.println("level:"+level);
////        Z2SFC z2Curve = new Z2SFC(level);

//        long stPointIndex = z2Curve.index(stPoint.getLongitude(), stPoint.getLatitude(), false);
//        System.out.println("stPointIndex:"+stPointIndex);
        // 根据查询点和半径获取bbox
//        List<STPoint> bbox = coding.getBBoxByCircle(stPoint, 10000);
//        STPoint LowerLeftPoint = bbox.get(0);
//        STPoint UpperRightPoint = bbox.get(1);
//        ByteArray LLPointSpatialCode = spatialCoding.getIndex(LowerLeftPoint);
//        ByteArray URPointSpatialCode = spatialCoding.getIndex(UpperRightPoint);
//        String LLIndex = spatialCoding.indexToString(LLPointSpatialCode);
//        String URIndex = spatialCoding.indexToString(URPointSpatialCode);
//        System.out.println("LLIndex:"+LLIndex);
//        System.out.println("URIndex:"+URIndex);
//
//        Z2 LLz2 = new Z2(Long.parseLong(LLIndex));
//        Tuple2<Object, Object> LLXY = LLz2.decode();
//        Z2 URz2 = new Z2(Long.parseLong(URIndex));
//        Tuple2<Object, Object> URXY = URz2.decode();
//
//        int minX = (int) LLXY._1();
//        int minY = (int) LLXY._2();
//        int maxX = (int) URXY._1();
//        int maxY = (int) URXY._2();
//
//        for (int x = minX; x<=maxX;x++) {
//            for (int y = minY; y<=maxY;y++) {
//                long code = Z2.apply(x,y);
//                System.out.println("cellCode:"+code);
//            }
//        }


//        System.out.println("LowerLeftPointLon:"+LowerLeftPoint.getLongitude());
//        System.out.println("LowerLeftPointLat:"+LowerLeftPoint.getLatitude());
//        System.out.println("UpperRightPointLon:"+UpperRightPoint.getLongitude());
//        System.out.println("UpperRightPointLat:"+UpperRightPoint.getLatitude());
//        // 获取bbox的行列号范围
//        long LLIndex = z2Curve.index(LowerLeftPoint.getLongitude(), LowerLeftPoint.getLatitude(), false);
//        Z2 LLz2 = new Z2(LLIndex);
//        Tuple2<Object, Object> LLXY = LLz2.decode();
//        long URIndex = z2Curve.index(UpperRightPoint.getLongitude(), UpperRightPoint.getLatitude(), false);
//        Z2 URz2 = new Z2(URIndex);
//        Tuple2<Object, Object> URXY = URz2.decode();
//
//        int minX = (int) LLXY._1();
//        int minY = (int) LLXY._2();
//        int maxX = (int) URXY._1();
//        int maxY = (int) URXY._2();
//
//        for (int x = minX; x<=maxX;x++) {
//            for (int y = minY; y<=maxY;y++) {
//                long code = Z2.apply(x,y);
//                System.out.println("cellCode:"+code);
//            }
//        }

//        System.out.println("LowerLeftPoint.getLongitude:"+LowerLeftPoint.getLongitude());
//        System.out.println("LLIndex:"+LLIndex);
//
//        ByteArray index = coding.getSpatialCoding().stringToIndex(Long.toString(LLIndex));
//
//        System.out.println("LLIndex1:"+coding.getSpatialCoding().indexToString(index));
//
//        System.out.println("LLXY_X:"+(int) LLXY._1());
//        System.out.println("LLXY_Y:"+(int) LLXY._2());
//        System.out.println("URXY_X:"+(int) URXY._1());
//        System.out.println("URXY_Y:"+(int) URXY._2());

//        SpatialCoding spatialCoding = coding.getSpatialCoding();
//
//        ByteArray queryPointSpatialCode = spatialCoding.getIndex(stPoint);
//
//        SpatialCell queryPointCell = new SpatialCell(queryPointSpatialCode,
//                coding.getSpatialCoding().getPrecision() / 2, spatialCoding, stPoint);
//        System.out.println("queryPointSpatialCode:"+spatialCoding.indexToString(queryPointSpatialCode));
//        System.out.println("queryPointCell:"+queryPointCell.getID());
//        ByteArrayRange centerCell = queryPointCell.getRanges(coding.getPrecision() / 2);
//        System.out.println("centerCellStart:"+spatialCoding.indexToString(centerCell.getStart()));
//        System.out.println("centerCellEnd:"+spatialCoding.indexToString(centerCell.getEnd()));
//        STRangeCondition condition =
//                STRangeCondition.fromContinuousTime(bottomLeft, upperRight, startDate, endDate);
//        condition.setMaxRecursive(1);
//
//        List<ByteArrayRange> ranges = coding.decomposeRange(condition.getQueryBBox(), condition.getMaxRecursive());
//        for(ByteArrayRange range:ranges){
//            System.out.println("----------------------");
//            System.out.println("rangeStart:"+coding.indexToString(range.getStart())
//                    +"\nrangeEnd:"+coding.indexToString(range.getEnd())
//                    +"\nisContained:"+range.isContained());
//        }

        //string（时间）
//        String rowkey = coding.indexToString(coding.getIndex(stPoint));
//        System.out.println("rowkey:"+rowkey);

//        ByteArray index = coding.stringToIndex("10010100437057594784");
//        BoundingBox rowbbox = coding.getBoundingBox(index);
//        System.out.println("rowbbox"+rowbbox.getDateEnd());
//        byte[] b = new byte[0];
//        System.out.println("b:"+ b.length);

    }
}
