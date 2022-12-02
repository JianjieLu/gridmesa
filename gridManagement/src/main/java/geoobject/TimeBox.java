package geoobject;

import util.DateUtil;
import com.alibaba.fastjson.JSON;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 时间范围
 *
 * @Author zhangjianhao
 * @Date 2021/11/26
 */
public class TimeBox {

    private Date start;

    private Date end;

    private String schema;

    private TimeBox() {
    }

    public static geoobject.TimeBox of(Date start, Date end, String schema) {
        geoobject.TimeBox timeBox = new geoobject.TimeBox();
        timeBox.start = start;
        timeBox.end = end;
        timeBox.schema = schema;
        return timeBox;
    }

    public static geoobject.TimeBox initFromSTList(List<STPoint> stPointList, String format) {
        if (stPointList == null || stPointList.size() == 0) {
            return null;
        }
        STPoint p1 = stPointList.get(0);
        Date start = p1.getTime();
        Date end = p1.getTime();
        for (STPoint stPoint : stPointList) {
            Date time = stPoint.getTime();
            if (DateUtil.isNext(start, time)) {
                start = time;
            }
            if (DateUtil.isPrevious(end, time)) {
                end = time;
            }
        }
        return geoobject.TimeBox.of(start, end, format);
    }

    public geoobject.TimeBox getBigger(geoobject.TimeBox other) {
        Date start = this.start;
        Date end = other.end;
        return geoobject.TimeBox.of(start, end, this.schema);
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat(this.schema);
        return "start:" + sdf.format(start) + ",end:" + sdf.format(end);
    }

    public String toJsonString() {
        return JSON.toJSONString(this);
    }

    public boolean interact(geoobject.TimeBox other) {
        return !(DateUtil.isNext(other.start, this.end) || DateUtil.isPrevious(other.end, this.start));
    }
}
