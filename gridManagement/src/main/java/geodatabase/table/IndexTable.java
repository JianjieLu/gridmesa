package geodatabase.table;

import index.coding.Coding;
import org.apache.hadoop.hbase.client.Table;

/**
 * This class represents a index table in HBase of a data set.
 *
 * @see GeoTable
 */
public class IndexTable extends GeoTable {
    /**
     * Coding object corresponding to this index table.
     */
    private Coding coding;

    public IndexTable(Table table) {
        super(table);
    }

    public Coding getCoding() {
        return coding;
    }

    public void setCoding(Coding coding) {
        this.coding = coding;
    }
}

