package query;

import query.condition.QueryCondition;
import serialize.SerializeGeometry;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.hadoop.hbase.client.Table;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * This class is the abstract class for query action.
 * @author yangxiangyang
 * create on 2022-11-5
 */
public abstract class QueryOptions {
    protected Table secondaryTable;
    protected Table mainTable;

    /**
     * for multi-thread supporting.
     * */

    protected final ThreadPoolExecutor queryWorkersPool;
    protected final LinkedTransferQueue<SerializeGeometry> resultQueue;
    protected int threadNum= 4;

    public QueryOptions(Table mainTable,Table secondaryTable,int threadNum){
        //threadNum = Runtime.getRuntime().availableProcessors() + 1;
        this.mainTable=mainTable;
        this.secondaryTable=secondaryTable;
        this.threadNum=threadNum;
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setDaemon(true).setNameFormat("internal-pol-%d").build();
        queryWorkersPool =
                (ThreadPoolExecutor) Executors.newFixedThreadPool(threadNum, threadFactory);
        queryWorkersPool.prestartAllCoreThreads();
        resultQueue = new LinkedTransferQueue<>();
    }
    public QueryOptions(Table mainTable,Table secondaryTable){
        //threadNum = Runtime.getRuntime().availableProcessors() + 1;
        this.mainTable=mainTable;
        this.secondaryTable=secondaryTable;
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setDaemon(true).setNameFormat("internal-pol-%d").build();
        queryWorkersPool =
                (ThreadPoolExecutor) Executors.newFixedThreadPool(threadNum, threadFactory);
        queryWorkersPool.prestartAllCoreThreads();
        resultQueue = new LinkedTransferQueue<>();
    }

    public abstract Set<String> executeQuery(QueryCondition queryCondition) throws Throwable;

}