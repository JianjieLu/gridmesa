package index.coding;

import index.coding.concatenate.ConcatenateCoding;
import index.coding.concatenate.STCoding;
import index.coding.concatenate.TSCoding;
import index.coding.concatenate.time.TemporalCoding;
import index.coding.concatenate.time.TimeCoding;
import index.coding.spatial.SpatialCoding;
import index.coding.spatial.Z2Coding;
import index.coding.spatial.geohash.GeohashCoding;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * Factory used to generate an instance of a known Coding type
 *
 * @Author zhangjianhao
 * @Date 2021/10/27
 */
public class CodingFactory {

    /***
     * Generates a spatial coding based on the dimensions definition and the space filling curve type
     *
     * @param precision Meshing depth of the specific SFC
     * @param codingType specifies the type (Hilbert, ZOrder) of space filling curve to generate
     * @return a space filling curve instance generated based on the supplied parameters
     */
    public static SpatialCoding createSpatialCoding(Coding.SpatialType codingType, int precision) {
        switch (codingType) {
            case Z2:
                return new Z2Coding(precision);
            case Geohash:
                return new GeohashCoding(precision);
            default:
                throw new IllegalArgumentException("The codingType is not supported,only Z2 and Geohash are supported.");
        }
    }

    public static TemporalCoding createTemporalCoding(String pattern, String timeCoarseBin) {
        return new TimeCoding(pattern, timeCoarseBin);
    }

    public static Coding createCoding(JSONObject codingSchema) {
        String codingType = codingSchema.getString("structure");
        JSONObject encoding = codingSchema.getJSONObject("encoding");
        int precision = encoding.getInteger("precision");

        Coding.ConcatenateType concateneteType = Coding.ConcatenateType.valueOf(codingType);
        Coding.SpatialType spatialType = Coding.SpatialType.valueOf(encoding.getString("spatialCoding"));
        Coding coding = CodingFactory.createConcatenateCoding(concateneteType,spatialType,
                precision,encoding.getString("order"), codingSchema.getString("timeCoarseBin"));

        return coding;
    }

    public static Coding createConcatenateCoding(Coding.ConcatenateType concatenateType, Coding.SpatialType spatialType,
                                                 int precision, String pattern, String timeCoarseBin) {
        switch (concatenateType) {
            case S_T:
                if (spatialType.name().equals(Coding.SpatialType.Z2.name())) {
                    return new STCoding(Coding.SpatialType.Z2, precision, pattern, timeCoarseBin);
                } else if (spatialType.name().equals(Coding.SpatialType.Geohash.name())) {
                    return new STCoding(Coding.SpatialType.Geohash, precision, pattern, timeCoarseBin);
                } else {
                    throw new IllegalArgumentException("The spatialType is not supported,only Z2 and Geohash are supported.");
                }
            case T_S:
                if (spatialType.name().equals(Coding.SpatialType.Z2.name())) {
                    return new TSCoding(Coding.SpatialType.Z2, precision, pattern, timeCoarseBin);
                } else if (spatialType.name().equals(Coding.SpatialType.Geohash.name())) {
                    return new TSCoding(Coding.SpatialType.Geohash, precision, pattern, timeCoarseBin);
                } else {
                    throw new IllegalArgumentException("The spatialType is not supported,only Z2 and Geohash are supported.");
                }
            default:
                throw new IllegalArgumentException("The concatenateType is not supported,only S_T and T_S are supported.");
        }
    }

    public static JSONObject createCodingSchema(Coding coding) throws Exception {
        String codingText="";
        int precision = coding.getSpatialCoding().getPrecision();
        Coding.IndexType indexType = coding.getIndexType();
        SpatialCoding spatialCoding;
        switch (indexType){
            case STConcatenate:
                spatialCoding = coding.getSpatialCoding();
                ConcatenateCoding concatenateCoding = coding.getConcatenateCoding();
                TimeCoding timeCoding = concatenateCoding.getTimeCoding();
                codingText =
                        "{\n" +
                                "  \"structure\": \""+ concatenateCoding.getConcatenateType() +"\",\n" +
                                "  \"encoding\": {\n" +
                                "    \"precision\": "+ precision +",\n" +
                                "    \"order\": \"" + timeCoding.getDateFormat().toPattern() + "\",\n" +
                                "    \"spatialCoding\": \""+ spatialCoding.getSpatialType() + "\"\n" +
                                "  },\n" +
                                "  \"timeCoarseBin\" : \"" + timeCoding.getCoarseBin() +"\"\n" +
                                "}";
                break;
            case OnlySpatial:
                spatialCoding = coding.getSpatialCoding();
                codingText =
                        "{\n" +
                                "  \"structure\": \"S\",\n" +
                                "  \"encoding\": {\n" +
                                "    \"precision\": "+ precision +",\n" +
                                "    \"spatialCoding\": \""+ spatialCoding.getSpatialType() +"\"\n" +
                                "  }\n" +
                                "}";
                break;
            default:
                System.out.println("Index type wrong!");
        }
        return JSON.parseObject(codingText);
    }
}