package index.data;

import index.persist.Persistable;

/**
 * MultiDimensionalNumericData Interface which defines the methods associated with a multi-dimensional numeric data range.
 *
 * @see BasicNumericDataset
 */
public interface MultiDimensionalNumericData extends Persistable {
    /**
     * @return an array of object QueryRange
     */
    public NumericData[] getDataPerDimension();

    public double[] getMaxValuesPerDimension();

    public double[] getMinValuesPerDimension();

    public double[] getCentroidPerDimension();

    public int getDimensionCount();

    /**
     * @return return if unconstrained on a dimension
     */
    public boolean isEmpty();
}
