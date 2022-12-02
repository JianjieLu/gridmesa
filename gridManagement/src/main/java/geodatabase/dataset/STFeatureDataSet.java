//package geodatabase.dataset;
//
//import geodatabase.DataTypeEnum;
//import geodatabase.table.IndexTable;
//import query.temp.QueryOptions;
//import query.temp.QueryType;
//import query.condition.KNNCondition;
//import query.condition.QueryCondition;
//import query.condition.SRangeCondition;
//import query.condition.STRangeCondition;
//import serialize.SerializeGeometry;
//import com.alibaba.fastjson.JSONObject;
//import org.apache.hadoop.hbase.client.Get;
//import org.apache.hadoop.hbase.client.Put;
//import org.apache.hadoop.hbase.client.Result;
//import org.apache.hadoop.hbase.client.Table;
//import org.apache.hadoop.hbase.util.Bytes;
//
//import java.io.IOException;
//import java.util.Map;
//import java.util.Set;
//
///**
// * This class is the concrete implementation of STFeature dataset.
// */
//public class STFeatureDataSet extends DataSet {
//    private STFMetaData metaData;
//
//    public STFeatureDataSet(String dataSetName, STFMetaData metaData,
//                            Table metaTable, IndexTable indexTable) {
//        this.dataSetName = dataSetName;
//        this.metaData = metaData;
//        this.metaTable = metaTable;
//        this.indexTable = indexTable;
//    }
//
//    @Override
//    public Set<SerializeGeometry> query(QueryCondition condition, int tableIndex) throws Throwable {
//        if (condition instanceof STRangeCondition) {
//            QueryOptions options = indexTable.getQuery(QueryType.STRANGE_QUERY,DataTypeEnum.STFEATURE);
//            return options.executeQuery(condition);
//        } else if (condition instanceof KNNCondition) {
//            QueryOptions options = indexTable.getQuery(QueryType.KNN_QUERY,DataTypeEnum.STFEATURE);
//            return options.executeQuery(condition);
//        } else if (condition instanceof SRangeCondition) {
//            QueryOptions options = indexTable.getQuery(QueryType.SRANGE_QUERY, DataTypeEnum.STFEATURE);
//            return options.executeQuery(condition);
//        } else {
//            throw new UnsupportedOperationException("Unsupported query type of STFeature");
//        }
//    }
//
//    @Override
//    public void insert(SerializeGeometry geometry) {
//
//    }
//
//    @Override
//    public SerializeGeometry get(String id) {
//        return null;
//    }
//
//    @Override
//    public STFMetaData readMetaData() throws IOException {
//        Get get = new Get(Bytes.toBytes(dataSetName));
//        Result result = metaTable.get(get);
//        STFMetaData metaData = new STFMetaData();
//        metaData.initFromCells(result.rawCells());
//
//        return metaData;
//    }
//
//    @Override
//    public void writeMetaData() throws IOException {
//        Put put = new Put(Bytes.toBytes(dataSetName));
//
//        put.addColumn(Bytes.toBytes("E"), Bytes.toBytes("C"),
//                Bytes.toBytes(metaData.getCount()));
//        put.addColumn(Bytes.toBytes("E"), Bytes.toBytes("CS"),
//                Bytes.toBytes(metaData.getCodingSchema().toJSONString()));
//        put.addColumn(Bytes.toBytes("E"), Bytes.toBytes("DT"),
//                Bytes.toBytes(metaData.getDataType()));
//        put.addColumn(Bytes.toBytes("E"), Bytes.toBytes("FT"),
//                Bytes.toBytes(metaData.getFeatureType()));
//        if (null != metaData.getSpatialReference()) {
//            put.addColumn(Bytes.toBytes("E"), Bytes.toBytes("SR"),
//                    Bytes.toBytes(metaData.getSpatialReference()));
//        }
//        if (null != metaData.getMbr()) {
//            put.addColumn(Bytes.toBytes("E"), Bytes.toBytes("Box"),
//                    Bytes.toBytes(metaData.getMbr().toJSONString()));
//        }
//        if (null != metaData.getTimeReference()) {
//            put.addColumn(Bytes.toBytes("E"), Bytes.toBytes("TR"),
//                    Bytes.toBytes(metaData.getTimeReference()));
//            put.addColumn(Bytes.toBytes("E"), Bytes.toBytes("ST"),
//                    Bytes.toBytes(metaData.getStartTime()));
//            put.addColumn(Bytes.toBytes("E"), Bytes.toBytes("ET"),
//                    Bytes.toBytes(metaData.getEndTime()));
//        }
//        if (null != metaData.getDescription()) {
//            put.addColumn(Bytes.toBytes("E"), Bytes.toBytes("D"),
//                    Bytes.toBytes(metaData.getDescription()));
//        }
//        for (Map.Entry<String, String> entry : metaData.options.entrySet()) {
//            put.addColumn(Bytes.toBytes("O"), Bytes.toBytes(entry.getKey()),
//                    Bytes.toBytes(entry.getValue()));
//        }
//
//        metaTable.put(put);
//    }
//
//    @Override
//    public void createIndex(JSONObject indexInfo) {
//
//    }
//
//    @Override
//    public void deleteIndex(JSONObject indexInfo) {
//
//    }
//}
