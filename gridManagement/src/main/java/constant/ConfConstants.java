package constant;

/**
 * @Author zhangjianhao
 * @Date 2021/10/27
 */

/**
 * 与配置有关的常量类
 */
public class ConfConstants {

    /**
     * 批导类型
     */
    public static final String BULKLOAD_TYPE = "bulkload_type";

    /**
     * 时空点批导
     */
    public static final String STPOINT_BULKLOAD = "stpoint_bulkload";

    /**
     * 面格网表批导
     */
    public static final String POLYGON_GRID_BULKLOAD = "polygon_grid_bulkload";

    /**
     * 面对象表批导
     */
    public static final String POLYGON_OBJECT_BULKLOAD = "polygon_object_bulkload";

    /**
     * 空间点批导
     */
    public static final String SPOINT_BULKLOAD = "spoint_bulkload";

    /**
     * 轨迹批导
     */
    public static final String TRAJECTORY_BULKLOAD = "trajectory_bulkload";

    /**
     * 轨迹索引批导
     */
    public static final String TRAJECTORY_INDEX_BULKLOAD = "trajectory_index_bulkload";

    /**
     * 数据集对应的rowkey
     */
    public static final String DATASET_ROWKEY = "dataset_rowkey";

    /**
     * 父数据集名称
     */
    public static final String DATASET_NAME = "dataset_name";

    /**
     * 子数据集名称
     */
    public static final String SUB_DATASET_NAME = "sub_dataset_name";

    /**
     * 最大层级
     */
    public static final String MAX_LEVEL = "max_level";

    /**
     * 数据类型
     */
    public static final String DATA_TYPE = "data_type";

    /**
     * 空间范围
     */
    public static final String BOUNDING_BOX = "bounding_box";

    /**
     * 时间范围
     */
    public static final String TIME_BOX = "time_box";

    /**
     * 曝光点位数据量
     */
    public static final String TOTAL_COUNT = "total_count";

    /**
     * 子数据集数据量
     */
    public static final String SUB_DATASET_COUNT = "sub_dataset_count";

    /**
     * 时间分段
     */
    public static final String TIME_SCHEMA = "time_schema";

    /**
     * 数据集描述
     */
    public static final String DESCRIPTION = "description";

    /**
     * 轨迹分段数量
     */
    public static final String SEGMENT_NUMBER = "segment_number";

    /**
     * 轨迹分段类型
     */
    public static final String SEGMENT_TYPE = "segment_type";

    /**
     * 轨迹分段名称
     */
    public static final String SEGMENT_NAME = "segment_name";

    /**
     * 轨迹分段条件
     */
    public static final String SEGMENT_CONDITION = "segment_condition";

    /**
     * 轨迹分段id列表
     */
    public static final String SEGMENT_IDS = "segment_ids";

    /**
     * 轨迹分段id序列号
     */
    public static final String SEGMENT_SEQ = "segment_seq";

    /**
     * 融合点位id列表
     */
    public static final String CORE_IDS = "core_ids";

    /**
     * 自定义属性
     */
    public static final String ATTRIBUTES = "attributes";

    /**
     * 服务器节点个数
     */
    public static final String DATA_NODE = "data_node";

    /**
     * 子数据集阈值个数
     */
    public static final String COUNT_THRESHOLD = "count_threshold";

    /**
     * 时间间隔阈值
     */
    public static final String TIME_THRESHOLD = "time_threshold";

    /**
     * 本批次数据的起始时间
     */
    public static final String BEGIN_TIME = "begin_time";

    /**
     * 标定文件路径（相机与相机）
     */
    public static final String CALIB_CAM_TO_CAM_PATH = "calib_cam_to_cam_path";

    /**
     * 标定文件路径（imu与点云）
     */
    public static final String CALIB_IMU_TO_VELO_PATH = "calib_imu_to_velo_path";

    /**
     * 标定文件路径（点云与相机）
     */
    public static final String CALIB_VELO_TO_CAM_PATH = "calib_velo_to_cam_path";

    /**
     * oxts数据格式说明文件路径
     */
    public static final String OXTS_DATA_FORMAT_PATH = "oxts_data_format_path";

    /**
     * oxts数据格式说明文件内容
     */
    public static final String OXTS_DATA_FORMAT_DATA = "oxts_data_format_data";

    /**
     * 查询类型
     */
    public static final String QUERY_TYPE = "query_type";

    /**
     * 查询条件
     */
    public static final String QUERY_CONDITION = "query_condition";

    /**
     * 原始文件输入路径
     */
    public static final String INPUT_PATH = "input_path";

    /**
     * 00相机路径
     */
    public static final String INPUT_PATH_IMAGE00 = "input_path_image00";

    /**
     * 01相机路径
     */
    public static final String INPUT_PATH_IMAGE01 = "input_path_image01";

    /**
     * 02相机路径
     */
    public static final String INPUT_PATH_IMAGE02 = "input_path_image02";

    /**
     * 03相机路径
     */
    public static final String INPUT_PATH_IMAGE03 = "input_path_image03";

    /**
     * 需要添加属性索引的属性名称
     */
    public static final String ATTR_INDEX_NAME = "attr_index_name";

    /**
     * 位姿数据路径
     */
    public static final String INPUT_PATH_OXTS = "input_path_oxts";

    /**
     * 位姿时间戳路径
     */
    public static final String INPUT_PATH_OXTS_TIMESTAMP = "input_path_oxts_timestamp";

    /**
     * 点云数据路径
     */
    public static final String INPUT_PATH_VELODYNE = "input_path_velodyne";

    /**
     * 路段数据路径
     */
    public static final String INPUT_PATH_ATTR = "input_path_attr";

    /**
     * spark配置
     */
    public static final String SPARK_CONF = "spark_conf";

    /**
     * 时空查询
     */
    public static final String ST_QUERY = "st_query";

    /**
     * 空间查询
     */
    public static final String S_QUERY = "spatial_query";

    /**
     * 空间查询
     */
    public static final String ATTR_QUERY = "attr_query";

    /**
     * 点查询
     */
    public static final String POINT_QUERY = "point_query";

    /**
     * 最小经度
     */
    public static final String MIN_LONGITUDE = "min_lng";

    /**
     * 最小纬度
     */
    public static final String MIN_LATITUDE = "min_lat";

    /**
     * 最大经度
     */
    public static final String MAX_LONGITUDE = "max_lng";

    /**
     * 最大纬度
     */
    public static final String MAX_LATITUDE = "max_lat";

    /**
     * 最大纬度
     */
    public static final String STQUERY_TYPE = "stquery_type";

    /**
     * 最大递归层数
     */
    public static final String MAX_RECURSIVE = "maxRecursive";

    /**
     * 起始时间
     */
    public static final String START_TIME = "start_time";

    /**
     * 结束时间
     */
    public static final String END_TIME = "end_time";

    /**
     * 经度
     */
    public static final String LONGITUDE = "lng";

    /**
     * 纬度
     */
    public static final String LATITUDE = "lat";

    /**
     * 最大搜索距离
     */
    public static final String MAX_DISTANCE = "max_distance";

    /**
     * 路段名称
     */
    public static final String ROAD_NAME = "road_name";

    /**
     * 路段查询类型
     */
    public static final String ROAD_TYPE = "road_type";

    /**
     * 路段名称
     */
    public static final String ROAD_DATASET = "road_dataset";

    /**
     * knn标识
     */
    public static final String K = "k";

    /**
     * 查询模式
     */
    public static final String QUERY_MODE = "query_mode";

    /**
     * 导入的数据表名
     */
    public static final String INGEST_TABLE_NAME = "ingest_table_name";

    /**
     * 下面这几个本系统没有用
     */
    public static final String THRESHOLD = "threshold";
    public static final String START_BIT_PRECISION = "start_bit_precision";
    public static final String MAX_BIT_PRECISION = "max_bit_precision";
    public static final String INDEX_TABLE_NAME = "index_table_name";
    public static final String SECONDARY_TABLE_NAME = "secondary_table_name";
    public static final String USE_ADAPTIVE_INDEX = "useAdaptiveIndex";
}
