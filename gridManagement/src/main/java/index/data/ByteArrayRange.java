package index.data;

import java.util.*;

/***
 * Defines a unit interval on a number line
 */
public class ByteArrayRange implements Comparable<ByteArrayRange>
{
    protected ByteArray start;
    protected ByteArray end;
    protected boolean singleValue;
    protected boolean contain;
    protected boolean spatialContain;
    protected boolean timeContain;

    public static ByteArrayRange LevelTerminator = new ByteArrayRange(new ByteArray(), new ByteArray() );

    /***
     *
     * @param start start of unit interval
     * @param end end of unit interval
     */
    public ByteArrayRange(
            final ByteArray start,
            final ByteArray end ) {
        this(
                start,
                end,
                false,
                false);
    }

    /***
     *
     * @param start start of unit interval
     * @param end end of unit interval
     */
    public ByteArrayRange(final ByteArray start, final ByteArray end, final boolean singleValue ) {
        this.start = start;
        this.end = end;
        this.singleValue = singleValue;
        this.contain = false;
        this.spatialContain = false;
        this.timeContain = false;
    }

    public ByteArrayRange(
            final ByteArray start,
            final ByteArray end,
            final boolean singleValue,
            final boolean contain) {
        this.start = start;
        this.end = end;
        this.singleValue = singleValue;
        this.contain = contain;
    }

    public ByteArrayRange(
            final ByteArrayRange range,
            final boolean contain ) {
        this.start = range.start;
        this.end = range.end;
        this.contain = contain;
    }

    public ByteArray getStart() {
        return start;
    }

    public ByteArray getEnd() {
        return end;
    }

    public ByteArray getEndAsNextPrefix() {
        return new ByteArray(end.getNextPrefix());
    }

    public boolean isSingleValue() {
        return singleValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((end == null) ? 0 : end.hashCode());
        result = (prime * result) + (singleValue ? 1231 : 1237);
        result = (prime * result) + ((start == null) ? 0 : start.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj ) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ByteArrayRange other = (ByteArrayRange) obj;
        if (end == null) {
            if (other.end != null) {
                return false;
            }
        }
        else if (!end.equals(other.end)) {
            return false;
        }
        if (singleValue != other.singleValue) {
            return false;
        }
        if (start == null) {
            if (other.start != null) {
                return false;
            }
        }
        else if (!start.equals(other.start)) {
            return false;
        }
        return true;
    }

    public boolean intersects(final ByteArrayRange other) {
        if (isSingleValue()) {
            if (other.isSingleValue()) {
                return getStart().equals(other.getStart());
            }
            return false;
        }
        return (((getStart().compareTo(other.getEndAsNextPrefix())) < 0) && ((getEndAsNextPrefix().compareTo(other.getStart())) > 0));
    }

    public boolean contains(final ByteArrayRange other ) {
        if (isSingleValue()) {
            if (other.isSingleValue()) {
                return getStart().equals(
                        other.getStart());
            }
            return false;
        }

        return  getStart().compareTo(other.getStart()) <= 0 && getEnd().compareTo(other.getStart()) >= 0
                && getStart().compareTo(other.getEndAsNextPrefix()) <= 0
                && getEndAsNextPrefix().compareTo(other.getEndAsNextPrefix()) >=0 ;
    }

    public ByteArrayRange intersection(final ByteArrayRange other ) {
        return new ByteArrayRange(
                start.compareTo(other.start) <= 0 ? other.start : start,//qu da
                getEndAsNextPrefix().compareTo(other.getEndAsNextPrefix()) >= 0 ? other.end : end);//qu xiao
    }

    public ByteArrayRange union(final ByteArrayRange other ) {
        return new ByteArrayRange(
                start.compareTo(other.start) <= 0 ? start : other.start,//qu xiao
                getEndAsNextPrefix().compareTo(other.getEndAsNextPrefix()) >= 0 ? end : other.end);//qu daqw
    }

    @Override
    public int compareTo(final ByteArrayRange other ) {
        final int diff = getStart().compareTo(other.getStart());
        return diff != 0 ? diff : getEndAsNextPrefix().compareTo(
                other.getEndAsNextPrefix());
    }

    public static enum MergeOperation {
        UNION,
        INTERSECTION
    }

    public static final Collection<ByteArrayRange> mergeIntersections(
            final Collection<ByteArrayRange> ranges,
            final MergeOperation op ) {
        List<ByteArrayRange> rangeList = new ArrayList<>(ranges);
        // sort order so the first range can consume following ranges
        Collections.sort(rangeList);
        final List<ByteArrayRange> result = new ArrayList<>();
        for (int i = 0; i < rangeList.size();) {
            ByteArrayRange r1 = rangeList.get(i);
            if(r1.isContained()){
                result.add(r1);
                i++;
                continue;
            }
            int j = i + 1;
            for (; j < rangeList.size(); j++) {
                final ByteArrayRange r2 = rangeList.get(j);
                if(r2.isContained()&&!r2.start.equals(r1.end)){
                    break;
                }
                if (r1.intersects(r2)) {
                    if (op.equals(MergeOperation.UNION)) {
                        r1 = r1.union(r2);
                    }
                    else {
                        r1 = r1.intersection(r2);
                    }
                }
                //union continuous ranges
                else if( r1.getEndAsNextPrefix().compareTo(r2.getStart()) == 0 ){
                    r1 = r1.union(r2);
                }
                else {
                    break;
                }
            }
            i = j;
            result.add(r1);
        }
        return result;
    }

    @Override
    public String toString(){
        return "RangeStart: " + this.getStart().toString() + ",RangeEnd: " + this.getEnd().toString() + ",contained: " + contain;
    }

    public boolean isContained(){
        return contain;
    }
}
