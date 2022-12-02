package geodatabase.table;

import org.apache.hadoop.hbase.client.Table;

/**
 * This class represents a data table in HBase of a data set.
 *
 * @see GeoTable
 */
public class SecondaryIndexTable extends GeoTable {

    public SecondaryIndexTable(Table table) {
        super(table);
    }

}
