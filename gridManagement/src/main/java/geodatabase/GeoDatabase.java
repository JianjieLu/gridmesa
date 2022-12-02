//package geodatabase;
//
//import constant.*;
//import geodatabase.dataset.DataSet;
//import geodatabase.dataset.STFMetaData;
//import geodatabase.dataset.STFeatureDataSet;
//import geodatabase.table.IndexTable;
//import com.alibaba.fastjson.JSONObject;
//import org.apache.hadoop.conf.Configuration;
//import org.apache.hadoop.fs.Path;
//import org.apache.hadoop.hbase.*;
//import org.apache.hadoop.hbase.client.*;
//import org.apache.hadoop.hbase.util.Bytes;
//
//import java.io.IOException;
//import java.io.Serializable;
//import java.text.ParseException;
//import java.util.*;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.ForkJoinPool;
//
///**
// * This class gives the connection to HBase, and encapsulate
// * the management of Namespace and table API for HBase.
// * This class is singleton, and could only exist one instance.
// * <p>
// * In the stdb, a Namespace represents a sub database.
// * <p>
// * GeoDataBase Guarantee concurrency safe.
// */
//
//
//public class GeoDatabase implements Serializable {
//    // The only instance. Must be initialized by static field to support concurrency.
//    private static final GeoDatabase instance = new GeoDatabase();
//
//    private Configuration configuration;
//    private Connection connection = null;
//    private Admin admin = null;
//
//    // Must be private.
//    private GeoDatabase() {
//        configuration = HBaseConfiguration.create();
//        configuration.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
//        try {
//            openConnection();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static GeoDatabase getInstance() {
//        return instance;
//    }
//
//    /**
//     * Configuration
//     */
//    public void addConfigurationResource(String path) {
//        configuration.addResource(new Path(path));
//    }
//
//    public Configuration getConfiguration() {
//        return configuration;
//    }
//
//    /**
//     * Dataset operation
//     */
//    public List<String> listDataSets() throws IOException {
//        Table metaTable = getTable(ResourceParseConsts.META_TABLE_KEY);
//        Scan scan = new Scan();
//        ResultScanner scanner = metaTable.getScanner(scan);
//        List<String> ret = new ArrayList<>();
//        for (Result r : scanner) {
//            ret.add(Bytes.toString(r.getRow()));
//        }
//        return ret;
//    }
//
//    public DataSet getDataSet(String dataSetName) throws IOException, ParseException {
//        //1、判断数据集是否存在
//        Table metaTable = getTable(ResourceParseConsts.META_TABLE_KEY);
//        Get get = new Get(Bytes.toBytes(dataSetName));
//        Result result = metaTable.get(get);
//        if (result.isEmpty()) {
//            metaTable.close();
//            throw new IOException("Dataset doesn't exist, please check and try again.");
//        }
//        //2、创建MetaData
//        STFMetaData metaData = new STFMetaData();
//        metaData.initFromCells(result.rawCells());
//
//        //3、创建数据集
//        String indexTableName = DataSet.getIndexTableName(dataSetName);
//        IndexTable indexTable = new IndexTable(getTable(indexTableName));
//        return new STFeatureDataSet(dataSetName, metaData, metaTable, indexTable);
//    }
//
//    public boolean dataSetExists(String dataSetName) throws IOException {
//        Table metaTable = getTable(ResourceParseConsts.META_TABLE_KEY);
//        Get get = new Get(Bytes.toBytes(dataSetName));
//        Result result = metaTable.get(get);
//        metaTable.close();
//        return !result.isEmpty();
//    }
//
//    public void createDataSet(JSONObject dataSetInfoJson) throws IOException, ParseException {
//        //1、判断数据集是否存在
//        String dataSetName = dataSetInfoJson.getString(ResourceParseConsts.DATA_SET_NAME);
//        if (dataSetExists(dataSetName)) {
//            throw new IOException("Data Set already exists.");
//        }
//
//        //2、创建MetaData
//        STFMetaData metaData = new STFMetaData();
//        metaData.initFromJSONObject(dataSetInfoJson);
//
//        String indexTableName = DataSet.getIndexTableName(dataSetName);
//
//        //3、创建数据集
//        try {
//            createTable(indexTableName, new String[]{ResourceParseConsts.COLUMN_FAMILY});
//        } catch (IOException e) {
//            System.out.println("table " + indexTableName + " already exists. Skip...");
//        }
//        IndexTable indexTable = new IndexTable(getTable(indexTableName));
//
//        //5、写入元数据表
//        DataSet dataSet = new STFeatureDataSet(
//                dataSetName,
//                metaData,
//                getTable(ResourceParseConsts.META_TABLE_KEY),
//                indexTable
//        );
//        dataSet.writeMetaData();
//        dataSet.close();
//    }
//
//    public void deleteDataSet(String dataSetName) throws IOException {
//        //1、判断数据集是否存在
//        if (!dataSetExists(dataSetName)) {
//            System.out.println("Dataset does not exist.");
//            return;
//        }
//        //2、获取前缀并删除辅助索引表和索引表
//        HTableDescriptor[] indexTables = admin.listTables(dataSetName + "_index.*");
//        for (HTableDescriptor table : indexTables) {
//            deleteTable(table.getNameAsString());
//            System.out.println("table '" + table.getNameAsString() + "' is deleted.");
//        }
//        HTableDescriptor[] SecondIndexTables = admin.listTables(dataSetName + "_secondary_index.*");
//        for (HTableDescriptor table : SecondIndexTables) {
//            deleteTable(table.getNameAsString());
//            System.out.println("table '" + table.getNameAsString() + "' is deleted.");
//        }
//
//        //3、删除元数据表条目
//        Table metaTable = getTable(ResourceParseConsts.META_TABLE_KEY);
//        Delete delete = new Delete(Bytes.toBytes(dataSetName));
//        metaTable.delete(delete);
//        metaTable.close();
//    }
//
//    /**
//     * Connection
//     */
//    public void closeConnection() throws IOException {
//        if (connection != null) {
//            connection.close();
//            connection = null;
//        }
//        if (admin != null) {
//            admin.close();
//            admin = null;
//        }
//    }
//
//    public void openConnection() throws IOException {
//        int threads = Runtime.getRuntime().availableProcessors() * 4;
//        ExecutorService service = new ForkJoinPool(threads);
//        connection = ConnectionFactory.createConnection(configuration, service);
//        admin = connection.getAdmin();
//        // init Geo-database
//        initSubDatabase();
//    }
//
//    private void initSubDatabase() throws IOException {
//        if (!tableExists(ResourceParseConsts.META_TABLE_KEY)) {
//            createTable(ResourceParseConsts.META_TABLE_KEY, new String[] {"E", "O"});
//        }
//    }
//
//    /**
//     * HBase table options
//     */
//    public void createTable(String tableName, String... columnFamilies) throws IOException {
//        if (this.tableExists(tableName)) {
//            throw new IOException("Table " + tableName + " already exists.");
//        } else {
//            HTableDescriptor hTableDescriptor = new HTableDescriptor(TableName.valueOf(tableName));
//            for (String str : columnFamilies) {
//                HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(str);
//                hTableDescriptor.addFamily(hColumnDescriptor);
//            }
//            admin.createTable(hTableDescriptor);
//        }
//    }
//
//    public boolean tableExists(String tableName) throws IOException {
//        return admin.tableExists(TableName.valueOf(tableName));
//    }
//
//    public void deleteTable(String tableName) throws IOException {
//        if (this.tableExists(tableName)) {
//            admin.disableTable(TableName.valueOf(tableName));
//            admin.deleteTable(TableName.valueOf(tableName));
//        } else {
//            throw new IOException("Table does not exists.");
//        }
//    }
//
//    public Table getTable(String tableName) throws IOException {
//        return connection.getTable(TableName.valueOf(tableName));
//    }
//
//    public boolean insertRow(String tableName, byte[] rowKey, byte[] colFamily, byte[] col, byte[] val) {
//        boolean isPutted;
//        Table table;
//        try {
//            table = connection.getTable(TableName.valueOf(tableName));
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//        Put put = new Put(rowKey);
//        put.addColumn(colFamily, col, val);
//        try {
//            table.put(put);
//            table.close();
//            isPutted = true;
//        } catch (IOException e) {
//            e.printStackTrace();
//            isPutted = false;
//        }
//        return isPutted;
//    }
//
//    public boolean insertRow(String tableName, byte[] rowKey, byte[] colFamily, List<byte[]> cols, List<byte[]> vals) {
//        boolean isPutted;
//        Table table;
//        try {
//            table = connection.getTable(TableName.valueOf(tableName));
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//        Put put = new Put(rowKey);
//        if (cols.size() != vals.size()) {
//            return false;
//        }
//        for (int i = 0; i < cols.size(); i++) {
//            put.addColumn(colFamily, cols.get(i), vals.get(i));
//        }
//        try {
//            table.put(put);
//            table.close();
//            isPutted = true;
//        } catch (IOException e) {
//            e.printStackTrace();
//            isPutted = false;
//        }
//        return isPutted;
//    }
//}
