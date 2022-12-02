package index.util;


import index.data.ByteArray;
import index.data.ByteArrayRange;
import index.sfc.SpaceFillingCurve;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static constant.CommonConstants.QUERY_ROWKEY_ID_CONNECTOR;

public class ByteArrayRangeUtils {
    public static boolean contain(SpaceFillingCurve sfc, ByteArrayRange range, ByteArray value) {
        long[] coordinates = sfc.getCoordinates(value);
        long[] rangeMin = sfc.getCoordinates(range.getStart());
        long[] rangeMax = sfc.getCoordinates(range.getEnd());

        boolean isContain = true;
        for (int i = 0; i < coordinates.length; i++) {

            isContain = isContain && (coordinates[i] >= rangeMin[i]) && (coordinates[i] <= rangeMax[i]);
        }
        return isContain;
    }

    public static boolean contain(SpaceFillingCurve sfc, ByteArrayRange range, ByteArrayRange value) {
        return contain(sfc, range, value.getStart()) && contain(sfc, range, value.getEnd());
    }

    public static boolean overlaps(SpaceFillingCurve sfc, ByteArrayRange range, ByteArrayRange value) {
        long[] corner_1 = sfc.getCoordinates(value.getStart());
        long[] corner_2 = sfc.getCoordinates(value.getEnd());
        long[] rangeMin = sfc.getCoordinates(range.getStart());
        long[] rangeMax = sfc.getCoordinates(range.getEnd());

        boolean isOverlap = true;
        for (int i = 0; i < corner_1.length; i++) {
            isOverlap = isOverlap && (Math.max(corner_1[i], rangeMin[i]) <= (Math.min(corner_2[i], rangeMax[i])));
        }
        return isOverlap;
    }

    public static List<ByteArrayRange> getPartitionIDRanges(List<ByteArrayRange> ranges, int maxPartitionID) {
        List<ByteArrayRange> res = new ArrayList<>();
        for (int i = 0; i <= maxPartitionID; i++) {
            for (ByteArrayRange range : ranges) {
                ByteArray start = new ByteArray(
                        ByteArrayUtils.combineArrays(Bytes.toBytes(i), range.getStart().getBytes()));
                ByteArray end = new ByteArray(
                        ByteArrayUtils.combineArrays(Bytes.toBytes(i), range.getEnd().getBytes()));
                res.add(new ByteArrayRange(start, end));
            }
        }
        return res;
    }

    /**
     * 避免漏查：数据rowkey末尾拼接了id，如果直接使用时空范围生成的ByteArrayRange会因为缺少id信息漏查一部分数据（rowkey按照字典序排列）
     * rowkey的id拼接连接符为TABLE_ROWKEY_ID_CONNECTOR，所以查询的ByteArrayRange的id拼接符的字典序大于TABLE_ROWKEY_ID_CONNECTOR即可
     *
     * @param ranges
     * @return
     */
    public static List<ByteArrayRange> getRangesWithIDConnector(List<ByteArrayRange> ranges) {
        return ranges.stream().map(range -> {
            ByteArray end = new ByteArray(
                    ByteArrayUtils.combineArrays(range.getEnd().getBytes(), Bytes.toBytes(QUERY_ROWKEY_ID_CONNECTOR))
            );
            return new ByteArrayRange(range.getStart(), end);
        }).collect(Collectors.toList());
    }

}
