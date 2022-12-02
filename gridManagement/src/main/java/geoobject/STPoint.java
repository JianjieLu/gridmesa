package geoobject;

import constant.CommonConstants;
import serialize.SerializeGeometry;
import serialize.SerializePoint;
import util.DateUtil;

import javax.annotation.Nonnull;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is the define of spatial-temporal point object.
 *
 * @author Shendannan
 * Create on 2019-06-06.
 */
public class STPoint extends BaseElement {

    private double longitude;
    private double latitude;
    private SerializeGeometry geometry;
    private double distanceToPoint = -1.0;
    private Date time;
    private String id;
    private String segId;
    private String pid;
    private String dataSetName;
    private boolean corePoint;
    private String streetName;

    private long quantity = 1;
    private boolean hasTime = false;

    private Map<String, String> attrs = new HashMap<>();

    public STPoint() {
    }

    public STPoint(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
        time = null;
        id = null;
    }

    public STPoint(double longitude, double latitude, String id) {
        this.longitude = longitude;
        this.latitude = latitude;
        time = null;
        this.id = id;
    }

    public STPoint(double longitude, double latitude, Date time) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.time = time;
        id = null;
        hasTime = true;
    }

    public STPoint(double longitude, double latitude, Date time, long quantity) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.time = time;
        id = null;
        this.quantity = quantity;
        hasTime = true;
    }

    public STPoint(double longitude, double latitude, Long timestamp) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.time = new Date(timestamp);
        hasTime = true;
    }

    public STPoint(double longitude, double latitude, Long timestamp, String id) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.time = new Date(timestamp);
        this.id = id;
        hasTime = true;
    }

    public STPoint(double longitude, double latitude, Date time, String id) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.time = time;
        this.id = id;
        hasTime = true;
    }

    public STPoint(double longitude, double latitude, Date time, String id, long quantity) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.time = time;
        this.id = id;
        this.quantity = quantity;
        hasTime = true;
    }

    public STPoint(SerializePoint geometry) {
        this.geometry = geometry;
        this.id = geometry.getId();
        this.latitude = geometry.getLat();
        this.longitude = geometry.getLng();
        this.time = new Date(geometry.getTime());
        hasTime = true;
    }

    public STPoint(SerializePoint geometry, geoobject.STPoint kNNQueryPoint) {
        this.geometry = geometry;
        this.id = geometry.getId();
        this.latitude = geometry.getLat();
        this.longitude = geometry.getLng();
        this.time = new Date(geometry.getTime());
        hasTime = true;
        calDistanceToPoint(kNNQueryPoint);
    }

    public static geoobject.STPoint fromString(String str) throws ParseException {
        String temp[] = str.split(",");
        String lon = temp[0].split(":")[1];
        String lat = temp[1].split(":")[1];
        String time = temp[2].split("e:")[1];
        DateFormat df = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        Date date = df.parse(time);
        return new geoobject.STPoint(Double.parseDouble(lon), Double.parseDouble(lat), date);
    }

    /**
     * 从HBase单元格中解析对象
     *
     * @param str    核心属性字符串
     * @param schema 时间格式
     * @return
     */
    public static geoobject.STPoint initFormCell(String str, String schema) {
        //lat lng time id
        if (str == null || str.equals("")) {
            return null;
        }
        String[] values = str.split(",");
        double lat = Double.parseDouble(values[0]);
        double lng = Double.parseDouble(values[1]);
        Date date = DateUtil.convertStringToDate(values[2], schema);
        String id = values[3];
        return new geoobject.STPoint(lng, lat, date, id);
    }

    /**
     * 将核心属性组织成字符串
     *
     * @param connector 属性之间的连接符
     * @return 属性字符串
     */
    public String buildCoreAttrs(String connector) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getLatitude()).append(connector)
                .append(this.getLongitude()).append(connector)
                .append(this.getStringTime()).append(connector)
                .append(this.getID());
        return sb.toString();
    }

    //为了省去在posdata表里的查询步骤，直接在对象索引表中存储核心信息
    public String buildObjectIndexAttrs(String connector) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getDataSetName()).append(connector)
                .append(this.getLatitude()).append(connector)
                .append(this.getLongitude()).append(connector)
                .append(this.getStringTime()).append(connector)
                .append(this.getID());
        return sb.toString();
    }

    public String buildOtherAttrs(String connector) {
        StringBuilder sb = new StringBuilder();
        Map<String, String> attrs = this.getAttrs();
        for (Map.Entry<String, String> entry : attrs.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append(":").append(value).append(connector);
        }
        return sb.toString();
    }

    /**
     * 填充时空点对象的扩展属性
     *
     * @param dataformat 字段名数组
     * @param values     字段值数组
     * @param value      额外添加的字段值
     */
    public void enrichAttrs(String[] dataformat, String[] values, String value) {
        if (dataformat == null || values == null) {
            return;
        }
        if (dataformat.length - 1 != values.length) {
            throw new IllegalArgumentException("dataformat size must be equals to values size + 1.");
        }
        for (int i = 0; i < values.length; i++) {
            //原始的速度单位是m/s，这里转成km/h，方面后续查询 只转了向前的速度
            if (dataformat[i].equals("vf")) {
                values[i] = String.valueOf(Double.parseDouble(values[i]) * 3.6);
            }
            this.attrs.put(dataformat[i], values[i]);
        }
        //针对兴趣路段进行填充
        this.attrs.put(dataformat[dataformat.length - 1], value);
    }

    @Override
    public String toString() {
        DateFormat df = new SimpleDateFormat(CommonConstants.TIME_FORMAT);
        if (!hasTime()) {
            return "lng:" + longitude + ",lat:" + latitude + ",id:" + id + ",attrs:" + attrs;
        } else {
            return "lng:" + longitude + ",lat:" + latitude + ",id:" + id + ",time:" + df.format(time) + ",attrs:" + attrs;
        }
    }

    public String print() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return id + "," + df.format(time) + "," + longitude + "," + latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
        hasTime = true;
    }

    public Long getLongTime() {
        return time.getTime();
    }

    public String getStringTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(CommonConstants.TIME_FORMAT);
        return sdf.format(this.time);
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public boolean hasTime() {
        return hasTime;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSegId() {
        return segId;
    }

    public void setSegId(String segId) {
        this.segId = segId;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getDataSetName() {
        return dataSetName;
    }

    public void setDataSetName(String dataSetName) {
        this.dataSetName = dataSetName;
    }

    public boolean isCorePoint() {
        return corePoint;
    }

    public void setCorePoint(boolean corePoint) {
        this.corePoint = corePoint;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    @Override
    public void calDistanceToPoint(geoobject.STPoint p) {
        distanceToPoint = getEucDistance(this, p);
    }

    @Override
    public double getDistanceToPoint() {
        return this.distanceToPoint;
    }

    public void setDistanceToPoint(double distanceToPoint) {
        this.distanceToPoint = distanceToPoint;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof geoobject.STPoint) {
            geoobject.STPoint point = (geoobject.STPoint) obj;
            return id.equals(point.id)
                    && Math.abs(latitude - point.latitude) < 1e-8
                    && Math.abs(longitude - point.longitude) < 1e-8
                    && time.getTime() == point.time.getTime();
        }
        return false;
    }

    @Override
    public int hashCode() {
        StringBuilder sb = new StringBuilder(this.id);
        sb.append(this.longitude);
        sb.append(this.latitude);
        sb.append(this.time.getTime());
        return sb.hashCode();
    }

    /**
     * Need to override it to ensure the timestamp is different.
     **/
    @Override
    public int compareTo(@Nonnull geoobject.BaseElement element) {
        if (this.getDistanceToPoint() > element.getDistanceToPoint()) {
            return 1;
        } else if (this.getDistanceToPoint() < element.getDistanceToPoint()) {
            return -1;
        }
        geoobject.STPoint point = (geoobject.STPoint) element;
        if (this.time.getTime() > point.time.getTime()) {
            return 1;
        } else if (this.time.getTime() < point.time.getTime()) {
            return -1;
        }
        return 0;
    }

    public SerializeGeometry getStGeometry() {
        return geometry;
    }

    public Map<String, String> getAttrs() {
        return attrs;
    }
}
