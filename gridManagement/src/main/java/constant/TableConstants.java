package constant;

/**
 * @Author zhangjianhao
 * @Date 2021/10/25
 */

/**
 * 与表结构有关的常量类
 */
public class TableConstants {

    /*********************** 表名相关 ******************************/

    /**
     * 元数据表名称
     */
    public static final String META_TABLE_NAME = "MetaTable";

    /**
     * 属性索引表名称（根据路段名）
     */
    public static final String ATTR_INDEX_TABLE_NAME = "AttrIndexTable";

    /**
     * 对象索引表名称
     */
    public static final String OBJECT_INDEX_TABLE_NAME = "objectIndexTable";

    /**
     * 子数据集表后缀
     */
    public static final String DATASET_TABLE_SUFFIX = "_dataset_data_table";

    /**
     * 街景影像数据表后缀
     */
    public static final String IMAGE_DATA_TABLE_SUFFIX = "_image_data_table";

    /**
     * 点云数据表后缀
     */
    public static final String POINTCLOUD_DATA_TABLE_SUFFIX = "_pointcloud_data_table";

    /**
     * 位姿数据表后缀
     */
    public static final String POS_DATA_TABLE_SUFFIX = "_pos_data_table";

    /**
     * 时空索引表后缀
     */
    public static final String ST_INDEX_TABLE_SUFFIX = "_st_index_table";

    /**
     * 点云索引表后缀
     */
    public static final String POINTCLOUD_INDEX_TABLE_SUFFIX = "_pointcloud_index_table";

    /**
     * 轨迹分段索引表后缀
     */
    public static final String SEGMENT_INDEX_TABLE_SUFFIX = "_segment_index_table";

    /*********************** 列族相关 ******************************/

    /**
     * 元数据表-数据集固定属性列族
     */
    public static final String META_COMMON_FAMILY = "F";

    /**
     * 属性索引表-唯一列族
     */
    public static final String ATTR_UNIQUE_FAMILY = "F";

    /**
     * 对象索引表-唯一列族
     */
    public static final String OBJECT_UNIQUE_FAMILY = "F";

//    /**
//     * 元数据表-数据集标定参数文件列族
//     */
//    public static final String META_CALIBRATION_FAMILY = "C";
//
//    /**
//     * 元数据表-数据集车辆位姿数据字段描述列族
//     */
//    public static final String META_OXTS_DESCRIPTION_FAMILY = "D";
//
//    /**
//     * 元数据表-数据集自定义列族
//     */
//    public static final String META_OPTIONAL_FAMILY = "U";

    /**
     * 子数据集表-通用属性列族
     */
    public static final String DATASET_COMMON_FAMILY = "F";

    /**
     * 子数据集表-标定参数文件列族
     */
    public static final String DATASET_CALIBRATION_FAMILY = "C";

    /**
     * 街景影像数据表-唯一列族
     */
    public static final String IMAGE_UNIQUE_FAMILY = "I";

    /**
     * 位姿数据表-位姿属性列族
     */
    public static final String POS_ATTR_FAMILY = "A";

//    /**
//     * 位姿数据表-扩展列族（存其它数据的元信息）
//     */
//    public static final String POS_OTHER_FAMILY = "O";

    /**
     * 点云数据表-唯一列族
     */
    public static final String PC_DATA_UNIQUE_FAMILY = "F";

    /**
     * 时空索引表-唯一列族
     */
    public static final String ST_UNIQUE_FAMILY = "F";

    /**
     * 点云索引表-唯一列族
     */
    public static final String PC_INDEX_UNIQUE_FAMILY = "F";

    /**
     * 轨迹分段索引表-唯一列族
     */
    public static final String SEGMENT_UNIQUE_FAMILY = "F";

    /*********************** 列相关 ******************************/

    /**
     * 元数据表-数据集固定属性列族-子数据集数量（列）
     */
    public static final String META_DATASET_COUNT_QUALIFIER = "DC";

    /**
     * 元数据表-数据集固定属性列族-总记录数（列）
     */
    public static final String META_TOTAL_COUNT_QUALIFIER = "TC";

    /**
     * 元数据表-数据集固定属性列族-描述信息（列）
     */
    public static final String META_DESCRIPTION_QUALIFIER = "DD";

    /**
     * 元数据表-数据集固定属性列族-空间范围（列）
     */
    public static final String META_BOUNDING_BOX_QUALIFIER = "BB";

    /**
     * 元数据表-数据集固定属性列族-时间范围（列）
     */
    public static final String META_TIME_BOX_QUALIFIER = "TB";

    /**
     * 元数据表-数据集固定属性列族-时间划分粒度（列）
     */
    public static final String META_TIME_SCHEMA_QUALIFIER = "TS";

    /**
     * 元数据表-数据集固定属性列族-数据集名称（列）
     */
    public static final String META_DATASET_NAME_QUALIFIER = "DS";

    /**
     * 元数据表-数据集固定属性列族-数据集类型（列）
     */
    public static final String META_DATA_TYPE_QUALIFIER = "DT";

    /**
     * 元数据表-数据集固定属性列族-服务器节点数（列）
     */
    public static final String META_DATA_NODE_QUALIFIER = "DN";

    /**
     * 元数据表-数据集固定属性列族-数据集个数阈值（列）
     */
    public static final String META_COUNT_THRESHOLD_QUALIFIER = "CT";

    /**
     * 元数据表-数据集固定属性列族-时间间隔阈值（单位是s）（列）
     */
    public static final String META_TIME_THRESHOLD_QUALIFIER = "TT";

    /**
     * 元数据表-数据集固定属性列族-最大划分层级（列）
     */
    public static final String META_MAX_LEVEL_QUALIFIER = "ML";

    /**
     * 元数据表-数据集固定属性列族-OXTS数据格式说明（列）
     */
    public static final String META_OXTS_DATA_FORMAT_QUALIFIER = "DF";

    /**
     * 元数据表-数据集固定属性列族-轨迹分段数量（列）
     */
    public static final String META_SEGMENT_COUNT_QUALIFIER = "SC";

    /**
     * 元数据表-数据集固定属性列族-轨迹分段最新序列号（列）
     */
    public static final String META_SEGMENT_SEQ_QUALIFIER = "SS";

    /**
     * 子数据集表-数据集固定属性列族-总记录数（列）
     */
    public static final String DATASET_TOTAL_COUNT_QUALIFIER = "TC";

    /**
     * 子数据集表-数据集固定属性列族-空间范围（列）
     */
    public static final String DATASET_BOUNDING_BOX_QUALIFIER = "BB";

    /**
     * 子数据集表-数据集固定属性列族-时间范围（列）
     */
    public static final String DATASET_TIME_BOX_QUALIFIER = "TB";

    /**
     * 子数据集表-数据集固定属性列族-父数据集的rowkey（列）
     */
    public static final String DATASET_PARENT_DATASET_QUALIFIER = "PD";

    /**
     * 子数据集表-数据集固定属性列族-轨迹分段数量（列）
     */
    public static final String DATASET_SEGMENGT_NUMBER_QUALIFIER = "SN";

    /**
     * 子数据集表-数据集固定属性列族-轨迹分段类型（列）
     */
    public static final String DATASET_SEGMENGT_TYPE = "ST";

    /**
     * 子数据集表-数据集固定属性列族-轨迹分段id列表（列）
     */
    public static final String DATASET_SEGMENGT_ID_QUALIFIER = "SI";

    /**
     * 子数据集表-数据集固定属性列族-融合点位id列表（列）
     */
    public static final String DATASET_CORE_ID_QUALIFIER = "CI";

    /**
     * 子数据集表-数据集标定参数文件列族-相机与相机标定（列）
     */
    public static final String DATASET_CALIB_CAM_TO_CAM_QUALIFIER = "CC";

    /**
     * 子数据集表-数据集标定参数文件列族-IMU与点云标定（列）
     */
    public static final String DATASET_CALIB_IMU_TO_VELO_QUALIFIER = "IV";

    /**
     * 子数据集表-数据集标定参数文件列族-点云与相机标定（列）
     */
    public static final String DATASET_CALIB_VELO_TO_CAM_QUALIFIER = "VC";

    /**
     * 街景数据表-唯一列族-image00
     */
    public static final String IMAGE0_QUALIFIER = "P0";

    /**
     * 街景数据表-唯一列族-image01
     */
    public static final String IMAGE1_QUALIFIER = "P1";

    /**
     * 街景数据表-唯一列族-image02
     */
    public static final String IMAGE2_QUALIFIER = "P2";

    /**
     * 街景数据表-唯一列族-image03
     */
    public static final String IMAGE3_QUALIFIER = "P3";

    /**
     * 属性索引表-唯一列族-id列
     */
    public static final String ATTR_QUALIFIER = "D";

    /**
     * 对象索引表-唯一列族-核心属性
     */
    public static final String OBJECT_CORE_QUALIFIER = "C";

    /**
     * 对象索引表-唯一列族-主数据集
     */
    public static final String OBJECT_DATASET_QUALIFIER = "D";

    /**
     * 对象索引表-唯一列族-分区号
     */
    public static final String OBJECT_PID_QUALIFIER = "P";

    /**
     * 对象索引表-唯一列族-融合点位标识
     */
    public static final String OBJECT_FLG_QUALIFIER = "F";

    /**
     * 时空索引表-唯一列族-id列
     */
    public static final String ST_ID_QUALIFIER = "D";

    /**
     * 轨迹分段索引表-唯一列族-id列
     */
    public static final String SEGMENT_ID_QUALIFIER = "D";

    /**
     * 轨迹分段索引表-唯一列族-分区号列
     */
    public static final String SEGMENT_PID_QUALIFIER = "P";

    /**
     * 轨迹分段索引表-唯一列族-代表点列
     */
    public static final String SEGMENT_CORE_QUALIFIER = "C";

    /**
     * 点云数据表-唯一列族-存储bin文件
     */
    public static final String PC_BIN_QUALIFIER = "B";

    /**
     * 点云数据表-唯一列族-存储转换后的pcd文件
     */
    public static final String PC_PCD_QUALIFIER = "P";

    /**
     * 点云数据表-唯一列族-点数量（只有转换成pcd的才有这个信息）
     */
    public static final String PC_COUNT_QUALIFIER = "C";

    /**
     * 点云数据表-唯一列族-点云数据立方体范围
     */
    public static final String PC_CUBE_QUALIFIER = "S";

    /**
     * 位姿数据表-位姿属性列族-核心属性
     */
    public static final String POS_CORE_ATTR_QUALIFIER = "C";

    /**
     * 位姿数据表-位姿属性列族-主数据集名称
     */
    public static final String POS_DATASET_NAME_QUALIFIER = "D";

    /**
     * 位姿数据表-位姿属性列族-融合点位标识
     */
    public static final String POS_CORE_FLG_QUALIFIER = "F";

    /**
     * 位姿数据表-位姿属性列族-姿态属性
     */
    public static final String POS_OTHER_ATTR_QUALIFIER = "O";

    /**
     * 位姿数据表-位姿属性列族-分区号
     */
    public static final String POS_PID_QUALIFIER = "P";

    /**
     * 位姿数据表-位姿属性列族-路段名称
     */
    public static final String POS_ROAD_NAME_QUALIFIER = "R";

    /**
     * 位姿数据表-位姿属性列族-轨迹分段id
     */
    public static final String POS_SEGMENT_ID_QUALIFIER = "S";

    /*********************** 连接符相关 ******************************/

    /**
     * 列中多个值的连接符
     */
    public static final String COLUMN_CONNECTOR = ",";

    /**
     * 列中多个值的连接符
     */
    public static final String ST_COLUMN_CONNECTOR = ";";

    /**
     * 元数据表的rowkey连接符
     */
    public static final String META_ROWKEY_CONNECTOR = "_";
}
