package index.coding.concatenate.time;


import index.data.ByteArray;

import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

/**
 * This class is the abstract class for time coding.
 *
 * @see TimeCoding
 */
public abstract class TemporalCoding {
    protected String timeCoarseBin;
    protected ChronoUnit timeCoarseUnit;

    public abstract ByteArray getIndex(Date targetDate);

    public abstract Date invert(ByteArray index) throws ParseException;

    public abstract Date[] preciseInvert(ByteArray index) throws ParseException;

    public abstract List<ByteArray> decomposeRangeContinuous(Date startDateTime,
                                                             Date endDateTime);

    protected void setCoarseUnit() {
        switch (timeCoarseBin) {
            case "y":
                timeCoarseUnit = ChronoUnit.YEARS;
                break;
            case "M":
                timeCoarseUnit = ChronoUnit.MONTHS;
                break;
            case "d":
                timeCoarseUnit = ChronoUnit.DAYS;
                break;
            case "H":
                timeCoarseUnit = ChronoUnit.HOURS;
                break;
            case "h":
                timeCoarseUnit = ChronoUnit.HOURS;
                break;
            case "m":
                timeCoarseUnit = ChronoUnit.MINUTES;
                break;
            case "s":
                timeCoarseUnit = ChronoUnit.SECONDS;
                break;
            default:
                timeCoarseUnit = ChronoUnit.MINUTES;
                break;
        }
    }

    public String getCoarseBin() {
        return timeCoarseBin;
    }

    enum MinGranularity {
        SECOND,
        MINUTE,
        HOUR,
        DAY,
        WEEK,
        MONTH,
        YEAR
    }
}
