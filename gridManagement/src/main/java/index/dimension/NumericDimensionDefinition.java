package index.dimension;



import index.data.NumericData;
import index.data.NumericRange;
import index.dimension.bin.BinRange;
import index.persist.Persistable;

import java.io.Serializable;

/**
 * The Numeric Dimension Definition interface defines the attributes and methods
 * of a class which forms the Space Filling Curve dimension.
 *
 * @see BasicDimensionDefinition
 */
public interface NumericDimensionDefinition extends Persistable, Serializable {
    public double getRange();

    /**
     * Used to normalize a value within the bounds of the range to a percentage
     * of the range between 0 and 1
     *
     * @return normalized value
     */
    public double normalize(double value);

    /**
     * Used to denormalize the numeric data set from a value between 0 and 1
     * scaled to fit within its native bounds
     *
     * @return the denormalized value
     */
    public double denormalize(double value);

    /**
     * Returns the set of normalized ranges
     *
     * @param range a numeric range of the data set
     * @return an array of BinRange[] objects
     */
    public BinRange[] getNormalizedRanges(NumericData range);

    /**
     * Returns a range in the native bounds of the dimension definition,
     * denormalized from a bin and separate range
     *
     * @param range a numeric range of the data set, with a bin
     * @return a NumericRange representing the given bin and range
     */
    public NumericRange getDenormalizedRange(BinRange range);

    /**
     * If this numeric dimension definition uses bins, it is given a fixed
     * length for the bin ID
     *
     * @return the fixed length for this dimensions bin ID
     */
    public int getFixedBinIdSize();

    /**
     * Returns the native bounds of the dimension definition
     *
     * @return a range representing the minimum value and the maximum value for
     * this dimension definition
     */
    public NumericRange getBounds();

    /**
     * Provide the entire allowed range
     *
     * @return
     */
    public NumericData getFullRange();

}
