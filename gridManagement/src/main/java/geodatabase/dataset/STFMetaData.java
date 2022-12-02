package geodatabase.dataset;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.util.Bytes;

import static constant.ResourceParseConsts.*;

/**
 * This class is the concrete implementation of STFeature metadata..
 *
 * Create on 2018-12-29
 */
public class STFMetaData extends MetaData {
    private String dataType;
    protected String featureType;

    public String getFeatureType() {
        return featureType;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public void setFeatureType(String featureType) {
        this.featureType = featureType;
    }

    @Override
    public void initFromCells(Cell[] cells) {
        JSONObject metaData = new JSONObject();
        for (Cell cell : cells) {
            switch (Bytes.toString(CellUtil.cloneFamily(cell))) {
                case "E":
                    switch (Bytes.toString(CellUtil.cloneQualifier(cell))) {
                        case "CS":
                            JSONArray codingSchema =
                                    JSONObject.parseArray(Bytes.toString(CellUtil.cloneValue(cell)));
                            metaData.put(CODING_SCHEMA, codingSchema);
                            break;
                        case "DT":
                            metaData.put(DATA_TYPE,
                                    Bytes.toString(CellUtil.cloneValue(cell)));
                            break;
                        case "FT":
                            metaData.put(FEATURE_TYPE,
                                    Bytes.toString(CellUtil.cloneValue(cell)));
                            break;
                        case "ST":
                            metaData.put(START_TIME,
                                    Bytes.toLong(CellUtil.cloneValue(cell)));
                            break;
                        case "ET":
                            metaData.put(END_TIME,
                                    Bytes.toLong(CellUtil.cloneValue(cell)));
                            break;
                        case "SR":
                            metaData.put(SPATIAL_REFERENCE,
                                    Bytes.toString(CellUtil.cloneValue(cell)));
                            break;
                        case "Box":
                            JSONObject bbox = JSONObject.parseObject(Bytes.toString(CellUtil.cloneValue(cell)));
                            metaData.put(BOUNDING_BOX, bbox);
                            break;
                        case "C":
                            metaData.put(COUNT,
                                    Bytes.toLong(CellUtil.cloneValue(cell)));
                            break;
                        case "D":
                            metaData.put(DESCRIPTION,
                                    Bytes.toString(CellUtil.cloneValue(cell)));
                            break;
                        case "A":
                            metaData.put(ATTRIBUTE_EXIST,
                                    Bytes.toBoolean(CellUtil.cloneValue(cell)));
                            break;
                        case "AC":
                            metaData.put(ATTRIBUTE_COUNT,
                                    Bytes.toInt(CellUtil.cloneValue(cell)));
                            break;
                    }
                    break;
                case "O":
                    options.put(Bytes.toString(CellUtil.cloneQualifier(cell)),
                            Bytes.toString(CellUtil.cloneValue(cell)));
                    break;
            }
        }
        initFromJSONObject(metaData);
    }

    @Override
    public void initFromJSONObject(JSONObject metadata) {
        super.initFromJSONObject(metadata);
        setDataType(metadata.getString(DATA_TYPE));
        setFeatureType(metadata.getString(FEATURE_TYPE));
    }
}
