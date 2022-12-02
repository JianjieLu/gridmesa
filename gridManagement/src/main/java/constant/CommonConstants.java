package constant;

/**
 * @Author zhangjianhao
 * @Date 2021/10/27
 */

/**
 * 通用常量类
 */
public class CommonConstants {

    /**
     * 时间格式化形式
     */
    public static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 默认最大划分层级
     */
    public static final int DEFAULT_MAX_LEVEL = 24;

    /**
     * 路段字段说明
     */
    public static final String ROAD_DATA_FORMAT = "rn:  name of the road";

    /**
     * 路段字段
     */
    public static final String DEFAULT_ATTR_FIELD_NAME = "rn";

    /**
     * 时间编码字符串
     */
    //public static final String TIME_CODING_PATTERN = "yyyyMMddHHmmss";

    /**
     * 时间编码字符串-精确到小时
     */
    public static final String TIME_CODING_PATTERN= "yyMMddHH";

    /**
     * 时间编码字符串-属性索引表
     */
    public static final String ATTR_TIME_CODING_PATTERN = "yyyyMMddHHmmss";


    /**
     * 元数据表起始序列号
     */
    public static final String META_ROWKEY_START_SEQ = "00000000";

    /**
     * 子数据集表起始序列号
     */
    public static final String DATASET_ROWKEY_START_SEQ = "00000000";

    /**
     * 轨迹分段表起始序列号
     */
    public static final String SEGMENT_ROWKEY_START_SEQ = "00000000";

    public static final String TABLE_ROWKEY_ID_CONNECTOR = "_";

    //编码要比TABLE_ROWKEY_ID_CONNECTOR大，是为了查询的时候避免漏查。Rowkey拼接了id
    public static final String QUERY_ROWKEY_ID_CONNECTOR = "`";
    public static final String VERTEX_INDEX = "VERTEX_INDEX";

    /**
     * double精确度
     */
    public static final int PRECISION = 10000000;

    /**
     * XZ2Index byte length
     */
    public static final int XZ2IndexLength = 8;

}
