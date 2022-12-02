package query.condition;

import constant.CommonConstants;
import constant.ConfConstants;
import geoobject.STPoint;

import com.alibaba.fastjson.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for parsing cn.edu.whu.storage.query condition file.
 */
public class QueryConditionParser {

    public static QueryCondition parseQueryJSONFile(JSONObject jsonObject) {
        String queryType = jsonObject.getString(ConfConstants.QUERY_TYPE);
        JSONObject queryConditionObject = jsonObject.getJSONObject(ConfConstants.QUERY_CONDITION);
        QueryCondition queryCondition;
        switch (queryType) {
            //最近点位查询
            case ConfConstants.POINT_QUERY:
                queryCondition = parsePointCondition(queryConditionObject);
                break;
            case ConfConstants.ST_QUERY:
                queryCondition = parseContinuousSTRangeCondition(queryConditionObject);
                break;
            case ConfConstants.S_QUERY:
                queryCondition = parseSpatialCondition(queryConditionObject);
                break;

            default:
                throw new IllegalArgumentException("Unsupported cn.edu.whu.storage.query type");
        }
        if (jsonObject.containsKey(ConfConstants.QUERY_MODE)) {
            queryCondition.setMode(jsonObject.getString(ConfConstants.QUERY_MODE));
        }
        return queryCondition;
    }



    public static PointCondition parsePointCondition(JSONObject jsonObject) {
        double lng = jsonObject.getDoubleValue(ConfConstants.LONGITUDE);
        double lat = jsonObject.getDoubleValue(ConfConstants.LATITUDE);
        double maxDistance = jsonObject.getDoubleValue(ConfConstants.MAX_DISTANCE);
        STPoint queryPoint = new STPoint(lng, lat);

        PointCondition condition = new PointCondition(queryPoint, maxDistance);
        return condition;
    }

    public static SRangeCondition parseSpatialCondition(JSONObject jsonObject) {
        double minLng = jsonObject.getDoubleValue(ConfConstants.MIN_LONGITUDE);
        double minLat = jsonObject.getDoubleValue(ConfConstants.MIN_LATITUDE);
        STPoint bottomLeft = new STPoint(minLng, minLat);
        double maxLng = jsonObject.getDoubleValue(ConfConstants.MAX_LONGITUDE);
        double maxLat = jsonObject.getDoubleValue(ConfConstants.MAX_LATITUDE);
        STPoint upperRight = new STPoint(maxLng, maxLat);
        int maxRecursive = jsonObject.getIntValue(ConfConstants.MAX_RECURSIVE);
        return SRangeCondition.fromPoints(bottomLeft, upperRight, maxRecursive);
    }

    public static STRangeCondition parseContinuousSTRangeCondition(JSONObject jsonObject) {
        double minLng = jsonObject.getDoubleValue(ConfConstants.MIN_LONGITUDE);
        double minLat = jsonObject.getDoubleValue(ConfConstants.MIN_LATITUDE);
        STPoint bottomLeft = new STPoint(minLng, minLat);
        double maxLng = jsonObject.getDoubleValue(ConfConstants.MAX_LONGITUDE);
        double maxLat = jsonObject.getDoubleValue(ConfConstants.MAX_LATITUDE);
        STPoint upperRight = new STPoint(maxLng, maxLat);


        DateFormat dateFormat = new SimpleDateFormat(CommonConstants.TIME_FORMAT);
        Date startDateTime = null;
        Date endDateTime = null;
        try {
            startDateTime = dateFormat.parse(jsonObject.getString(ConfConstants.START_TIME));
            endDateTime = dateFormat.parse(jsonObject.getString(ConfConstants.END_TIME));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return STRangeCondition.fromContinuousTime(bottomLeft, upperRight, startDateTime, endDateTime);
    }

    public static KNNCondition parseKNNCondition(JSONObject jsonObject) {
        double lng = jsonObject.getDoubleValue(ConfConstants.LONGITUDE);
        double lat = jsonObject.getDoubleValue(ConfConstants.LATITUDE);
        STPoint knnPoint = new STPoint(lng, lat);
        int k = jsonObject.getIntValue(ConfConstants.K);
        DateFormat dateFormat = new SimpleDateFormat(CommonConstants.TIME_FORMAT);
        Date startDateTime = null;
        Date endDateTime = null;
        try {
            startDateTime = dateFormat.parse(jsonObject.getString(ConfConstants.START_TIME));
            endDateTime = dateFormat.parse(jsonObject.getString(ConfConstants.END_TIME));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        KNNCondition knnCondition = new KNNCondition(knnPoint, k, startDateTime, endDateTime);
//        if(jsonObject.getString("k_type")!=null){
//            if(jsonObject.getString("k_type").equals("point")){
//                knnCondition.setKType(STGeometryTypeEnum.POINT);
//            }else if(jsonObject.getString("k_type").equals("trajectory")){
//                knnCondition.setKType(STGeometryTypeEnum.TRAJECTORY);
//            }
//        }
        if (jsonObject.getDouble("radius") != null) {
            double radius = jsonObject.getDoubleValue("radius");
            knnCondition.setMaxSearchDistance(radius);
        }
        return knnCondition;
    }
}
