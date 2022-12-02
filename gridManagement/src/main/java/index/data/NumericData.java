package index.data;

import index.persist.Persistable;

import java.io.Serializable;

/**
 * NumericData Interface used to define numeric data associated with a space filling curve.
 *
 * @see NumericValue
 * @see NumericRange
 */
public interface NumericData extends Serializable, Persistable {
    public double getMin();

    public double getMax();

    public double getCentroid();

    public boolean isRange();
}
