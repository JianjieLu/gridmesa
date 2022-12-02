package index.coding.concatenate;


import index.coding.Coding;
import index.coding.concatenate.time.TimeCoding;

/**
 * ConcatenateCoding interface extends Coding, for concatenate Coding such as TST.
 *
 * @author Shendannan
 * Create on 2019-06-06.
 */
public interface ConcatenateCoding extends Coding {
    IndexType indexType = IndexType.STConcatenate;

    ConcatenateType getConcatenateType();

    TimeCoding getTimeCoding();
}
