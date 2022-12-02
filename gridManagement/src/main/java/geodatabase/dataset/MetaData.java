package geodatabase.dataset;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.hadoop.hbase.Cell;

import java.util.HashMap;
import java.util.Map;
import static constant.ResourceParseConsts.*;
/**
 * This class represents the metadata information of a data set.
 */
public abstract class MetaData {
    protected JSONObject mbr;
    protected long count;
    protected JSONArray codingSchema;
    protected String spatialReference;
    protected String timeReference;
    protected long startTime;
    protected long endTime;
    protected String description;

    protected Map<String, String> options = new HashMap<>();

    public JSONObject getMbr() {
        return mbr;
    }

    public void setMbr(JSONObject mbr) {
        this.mbr = mbr;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public JSONObject getCodingSchemaById(int id) {
        for (int i = 0; i < codingSchema.size(); i++) {
            if (codingSchema.getJSONObject(i).getIntValue("id") == id) {
                return codingSchema.getJSONObject(i);
            }
        }
        return null;
    }

    public JSONArray getCodingSchema() {
        return codingSchema;
    }

    public void setCodingSchema(JSONArray codingSchema) {
        this.codingSchema = codingSchema;
    }

    public void createIndex(JSONObject indexInfo) {
        codingSchema.add(indexInfo);
    }

    public int getCodingSchemaNum() { return codingSchema.size(); }

    public int[] getCodingIds() {
        int[] ids = new int[getCodingSchemaNum()];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = codingSchema.getJSONObject(i).getInteger(CODING_ID);
        }
        return ids;
    }

    public String getSpatialReference() {
        return spatialReference;
    }

    public void setSpatialReference(String spatialReference) {
        this.spatialReference = spatialReference;
    }

    public String getTimeReference() {
        return timeReference;
    }

    public void setTimeReference(String timeReference) {
        this.timeReference = timeReference;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOption(String key, String value) {
        options.put(key, value);
    }

    public String getOptions(String key) {
        return options.get(key);
    }

    public abstract void initFromCells(Cell[] cells);

    public void initFromJSONObject(JSONObject metadata) {
        setCodingSchema(metadata.getJSONArray(CODING_SCHEMA));
        if (null != metadata.getJSONObject(BOUNDING_BOX)) {
            setMbr(metadata.getJSONObject(BOUNDING_BOX));
        }
        if (null != metadata.getLong(COUNT)) {
            setCount(metadata.getLong(COUNT));
        } else {
            setCount(0);
        }
        if (null != metadata.getString(SPATIAL_REFERENCE)){
            setSpatialReference(metadata.getString(SPATIAL_REFERENCE));
        }

        if (null != metadata.getString(TIME_REFERENCE)) {
            setTimeReference(metadata.getString(TIME_REFERENCE));
        }
        if (null != metadata.getLong(START_TIME)){
            setStartTime(metadata.getLong(START_TIME));
        } else {
            setStartTime(0);
        }
        if (null != metadata.getLong(END_TIME)){
            setEndTime(metadata.getLong(END_TIME));
        } else {
            setEndTime(0);
        }
        if (null != metadata.getString(DESCRIPTION)){
            setDescription(metadata.getString(DESCRIPTION));
        } else {
            setDescription("");
        }

        JSONArray optionsArray = metadata.getJSONArray(ATTRIBUTES);
        if (optionsArray != null) {
            for (int i = 0; i < optionsArray.size(); i++) {
                options.put("O" + i, optionsArray.getString(i));
            }
        }
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}