package geodatabase.dataset;

import geodatabase.table.IndexTable;
import geodatabase.table.SecondaryIndexTable;
import query.condition.QueryCondition;
import serialize.SerializeGeometry;
import org.apache.hadoop.hbase.client.Table;

import java.io.IOException;
import java.text.ParseException;
import java.util.Set;

/**
 * This class represents a data set in the stdb. A data set at least has a
 * data table, and one or several index table.
 */
public abstract class DataSet {
    protected String dataSetName;
    protected Table metaTable;
    protected SecondaryIndexTable secondaryIndexTable;
    protected IndexTable indexTable;

    // make constructor protected, avoid to create by new.
    protected DataSet() {
    }

    public abstract Set<SerializeGeometry> query(QueryCondition condition) throws Throwable;

    public abstract void insert(SerializeGeometry geometry);

    public abstract void delete(String id);

    public abstract SerializeGeometry get(String id);

    public abstract void update(String id);

    public abstract MetaData getMetaData();

    public abstract void writeMetaData() throws IOException, ParseException;

    public void close() throws IOException {
//        writeMetaData();
        secondaryIndexTable.close();
        indexTable.close();
        metaTable.close();
    }

    public String getDataSetName() {
        return dataSetName;
    }

    public Table getMetaTable() {
        return metaTable;
    }

    public SecondaryIndexTable getSecondaryIndexTable() {
        return secondaryIndexTable;
    }

    public IndexTable getIndexTable() {
        return indexTable;
    }

}
