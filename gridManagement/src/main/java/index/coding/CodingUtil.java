package index.coding;

import org.apache.hadoop.hbase.util.Bytes;


/**
 * @author Yu Liebing
 * create on 2019-3-2
 */
public class CodingUtil {

    /**
     * coding schema format
     *
     * {
     *   "id": 0,
     *   "structure": "T_S_T",
     *   "encoding": {
     *     "precision": 30,
     *     "order": "yyMMdd-HH",
     *     "spatialCoding": "z2"
     *   },
     *   "timeCoarseBin" : "d"
     * }
     * */
//    public static Coding createCoding(JSONObject codingSchema) {
//        String codingType = codingSchema.getString("structure");
//        JSONObject encoding = codingSchema.getJSONObject("encoding");
//        int precision = encoding.getInteger("precision");
//        if(codingType.equals("Z3")){
//            BinnedTime.TimePeriod timePeriod = BinnedTime.TimePeriod.valueOf(encoding.getString("timePeriod"));
//            return new Z3Coding(precision,timePeriod);
//        }
//        else if(codingType.equals("S")){
//            Coding.SpatialType spatialType = Coding.SpatialType.valueOf(encoding.getString("spatialCoding"));
//            switch (spatialType){
//                case Z2:
//                    return new Z2Coding(precision);
//                case Geohash:
//                    return new GeohashCoding(precision);
//                case Tile:
//                    return new TileCoding(precision/2);
//                default:
//                    return null;
//            }
//        }
//        else {
//            Coding.ConcateneteType concateneteType = Coding.ConcateneteType.valueOf(codingType);
//            Coding.SpatialType spatialType = Coding.SpatialType.valueOf(encoding.getString("spatialCoding"));
//
//            switch (concateneteType) {
//                case T_S_T:
//                    return new TSTCoding(spatialType, precision,
//                            encoding.getString("order"), codingSchema.getString(
//                            "timeCoarseBin"));
//                case S_T:
//                    return new STCoding(spatialType, precision,
//                            encoding.getString("order"), codingSchema.getString(
//                            "timeCoarseBin"));
//                case T_S:
//                    return new TSCoding(spatialType, precision,
//                            encoding.getString("order"), codingSchema.getString(
//                            "timeCoarseBin"));
//                default:
//                    return null;
//            }
//        }
//    }

//    public static String getCodingString(double lat, double lng, Date date,
//                                         JSONObject codingSchema) throws UnsupportedEncodingException {
//
//        JSONObject encoding = codingSchema.getJSONObject("encoding");
//        TimeCoding timeCoding = new TimeCoding(encoding.getString(
//                "order"));
//        List<String> times = timeCoding.index(date);
//
//        SpatialCoding spatialCoding;
//        int precision = encoding.getIntValue("precision");
//        String spatialCodingValue;
//        switch(encoding.getString("spatialCoding") ) {
//            case "cn.edu.whu.stdb.dao.sfc.geohash":
//                spatialCoding = new GeohashCoding( precision );
//                spatialCodingValue = Bytes.toString(spatialCoding.index(lat,
//                        lng));
//                break;
//            case "z2":
//                spatialCoding = new Z2Sfc( precision );
//                long tmp = Bytes.toLong(spatialCoding.index(lat, lng));
//                spatialCodingValue = String.valueOf(tmp);
//                break;
//            case "z3":
//                spatialCoding = new Z3Sfc(BinnedTime.TimePeriod.WEEK,precision);
//                spatialCodingValue = "";
//                break;
//            default:
//                spatialCodingValue = "";
//                break;
//        }
//
//        String res;
//        switch (Coding.SpatialTemporalStructure.valueOf(codingSchema.getString(
//                "structure"))) {
//            case T_S:
//                res = times.get(0) + spatialCodingValue;
//                break;
//            case S_T:
//                res = spatialCodingValue + times.get(0);
//                break;
//            case T_S_T:
//                res = times.get(0) + spatialCodingValue + times.get(1);
//                break;
//            default:
//                res = "";
//                break;
//        }
//
//        return res;
//    }
//
//
//    public static byte[] getCodingBytes(String codingString,
//                                        JSONObject codingSchema) {
//        JSONObject encoding = codingSchema.getJSONObject("encoding");
//
//        String timeFormat = encoding.getString("order");
//        String[] times = timeFormat.split("-");
//
//        byte[] res;
//        String t, s;
//        long tmp;
//        switch (Coding.SpatialTemporalStructure.valueOf(codingSchema.getString("structure"))) {
//            case T_S:
//                t = codingString.substring(0, times[0].length());
//                s = codingString.substring(times[0].length());
//                if (encoding.getString("spatialCoding").equals("z2")) {
//                    tmp = Long.parseLong(s);
//                    res = com.google.common.primitives.Bytes.concat(
//                            t.getBytes(),
//                            Bytes.toBytes(tmp));
//                } else if (encoding.getString("spatialCoding").equals("z3")) {
//                    res = null;
//                } else {
//                    res = com.google.common.primitives.Bytes.concat(
//                            t.getBytes(),
//                            s.getBytes());
//                }
//
//                break;
//            case S_T:
//                t = codingString.substring(codingString.length() - times[0].length());
//                s = codingString.substring(0,
//                        codingString.length() - times[0].length());
//                if (encoding.getString("spatialCoding").equals("z2")) {
//                    tmp = Long.parseLong(s);
//                    res = com.google.common.primitives.Bytes.concat(
//                            t.getBytes(),
//                            Bytes.toBytes(tmp));
//                } else if (encoding.getString("spatialCoding").equals("z3")) {
//                    res = null;
//                } else {
//                    res = com.google.common.primitives.Bytes.concat(
//                            t.getBytes(),
//                            s.getBytes());
//                }
//                break;
//            case T_S_T:
//                String t1 = codingString.substring(0, times[0].length());
//                s = codingString.substring(times[0].length(),
//                        codingString.length() - times[1].length());
//                String t2 =
//                        codingString.substring(codingString.length() - times[1].length());
//                if (encoding.getString("spatialCoding").equals("z2")) {
//                    tmp = Long.parseLong(s);
//                    res = com.google.common.primitives.Bytes.concat(
//                            t1.getBytes(),
//                            Bytes.toBytes(tmp),
//                            t2.getBytes());
//                } else if (encoding.getString("spatialCoding").equals("z3")) {
//                    res = null;
//                } else {
//                    res = com.google.common.primitives.Bytes.concat(
//                            t1.getBytes(),
//                            s.getBytes(),
//                            t2.getBytes());
//                }
//
//                break;
//            default:
//                res = null;
//                break;
//        }
//        return res;
//    }
//

    public static byte[][] bytesInterSect(byte[][] regionStartEnd, byte[][] range) {
        byte[] startRowKey = regionStartEnd[0];
        byte[] endRowKey = regionStartEnd[1];

        // if there's no start end key of this region, just return the range
        if ((startRowKey.length ==0) && endRowKey.length == 0) {
            return range;
        }

        byte[] startRangeKey = range[0];
        byte[] endRangeKey = range[1];

        byte[][] result = new byte[2][];

        if (startRowKey.length == 0) {
            if (Bytes.compareTo(startRangeKey, endRowKey) > 0) {
                return null;
            }
            if (Bytes.compareTo(endRangeKey, endRowKey) <= 0) {
                result[0] = startRangeKey;
                result[1] = endRangeKey;
            } else {
                result[0] = startRangeKey;
                result[1] = endRowKey;
            }
            return result;
        }

        if (endRowKey.length == 0) {
            if (Bytes.compareTo(endRangeKey, startRowKey) < 0) {
                return null;
            }
            if (Bytes.compareTo(startRangeKey, startRowKey) >= 0) {
                result[0] = startRangeKey;
                result[1] = endRangeKey;
            } else {
                result[0] = startRowKey;
                result[1] = endRangeKey;
            }
            return result;
        }

        // not intersect
        if (Bytes.compareTo(startRangeKey, endRowKey) > 0
                || Bytes.compareTo(endRangeKey, startRowKey) < 0) {
            return null;
        }

        // range contains region
        if (Bytes.compareTo(startRowKey, startRangeKey) >= 0
                && Bytes.compareTo(endRowKey, endRangeKey) <=0) {
            result[0] = startRowKey;
            result[1] = endRowKey;
        }
        // region contains range
        else if (Bytes.compareTo(startRangeKey, startRowKey) >= 0
                && Bytes.compareTo(endRangeKey, endRowKey) <= 0) {
            result[0] = startRangeKey;
            result[1] = endRangeKey;
        }
        // region bigger than range but intersect
        else if (Bytes.compareTo(startRangeKey, startRowKey) <= 0
                && Bytes.compareTo(endRangeKey, startRowKey) >= 0) {
            result[0] = startRowKey;
            result[1] = endRangeKey;
        }
        // range bigger than region but intersect
        else {
            result[0] = startRangeKey;
            result[1] = endRowKey;
        }

        return result;
    }
}
