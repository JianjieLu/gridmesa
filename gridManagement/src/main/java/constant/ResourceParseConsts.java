package constant;

/**
 * @author Yu Liebing
 * Create on 1/17/19.
 */
public class ResourceParseConsts {
    public static final String META_TABLE_KEY = "MetaData";
    public static final String COLUMN_FAMILY = "F";
    public static final String NONE_VALUE = "NONE";
    // constants related to parse the json file of creating data set
    public static final String DATA_TYPE = "data_type";
    public static final String FEATURE_TYPE = "feature_type";
    public static final String DATA_SET_NAME = "name";
    // meta data
    public static final String META_DATA = "metadata";

    public static final String CODING_SCHEMA = "coding_schema";
    public static final String INDEX= "index";
    public static final String INDEX_NUM = "num";

    public static final String BOUNDING_BOX = "bounding_box";
    public static final String BOTTOM_LEFT = "bottom_left";
    public static final String UP_RIGHT = "up_right";

    public static final String COUNT = "count";
    public static final String QUERY_TYPE = "query_type";
    public static final String QUERY_CONDITION = "query_condition";
    public static final long DEFAULT_COUNT = 0L;

    public static final String SPATIAL_REFERENCE = "spatial_reference";
    public static final String DEFAULT_SPATIAL_REFERENCE = "WGS84";

    public static final String TIME_REFERENCE = "time_reference";
    public static final String DEFAULT_TIME_REFERENCE = "unix";

    public static final String START_TIME = "start_time";
    public static final long DEFAULT_START_TIME = -28800000;

    public static final String END_TIME = "end_time";

    public static final String GENERATE_TIME = "generate_time";

    public static final String IMPORT_TIME = "import_time";

    public static final String DESCRIPTION = "description";

    public static final String ATTRIBUTES = "attributes";

    public static final String MAX_SPATIAL_LEVEL = "max_spatial_level";

    public static final String MIN_SPATIAL_LEVEL = "min_spatial_level";

    public static final String MAX_TIME_LEVEL = "max_time_level";

    public static final String MIN_TIME_LEVEL = "min_time_level";

    public static final String MAX_ATTRIBUTE_LEVEL = "max_attribute_level";

    public static final String MIN_ATTRIBUTE_LEVEL = "min_attribute_level";

    public static final String ATTRIBUTE_EXIST = "attribute_exist";

    public static final String ATTRIBUTE_COUNT = "attribute_count";

    public static final String CODING_INFORMATION = "information";
    public static final String CODING_ID = "id";
    public static final String DATA_TYPE_INVALIDATE = "Must specify data type" +
            " by \"data_type\": \"STFeature\"/\"DGObject\"/\"TSTile\"";
    public static final String DATA_SET_NAME_INVALIDATE = "Must specify data " +
            "set name by \"name\": \"data_set_name\"";
    public static final String META_DATA_INVALIDATE = "Must specify meta data" +
            " by \"metadata\": ...";

    public static final String TABLE_ROWKEY_ID_CONNECTOR = "_";

}
