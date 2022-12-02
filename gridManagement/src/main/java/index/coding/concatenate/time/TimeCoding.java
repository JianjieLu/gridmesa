package index.coding.concatenate.time;

import index.data.ByteArray;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * This class is used for time coding.
 *
 * @author Liu yishi
 * create on 2019-1-7.
 */
public class TimeCoding extends TemporalCoding implements Serializable {
    private final SimpleDateFormat dateFormat;
    private final ZoneId zoneId = ZoneId.systemDefault();
    private MinGranularity minGranularity = MinGranularity.SECOND;

    public TimeCoding(String format, String timeCoarseBin) {
        dateFormat = new SimpleDateFormat(format);
        super.timeCoarseBin = timeCoarseBin;
        setCoarseUnit();
        if (format.contains("ss")) {
            minGranularity = MinGranularity.SECOND;
        } else if (format.contains("mm")) {
            minGranularity = MinGranularity.MINUTE;
        } else if (format.contains("hh")) {
            minGranularity = MinGranularity.HOUR;
        } else if (format.contains("HH")) {
            minGranularity = MinGranularity.HOUR;
        } else if (format.contains("dd")) {
            minGranularity = MinGranularity.DAY;
        } else if (format.contains("MM")) {
            minGranularity = MinGranularity.MONTH;
        } else if (format.contains("yy")) {
            minGranularity = MinGranularity.YEAR;
        }
    }

    public SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    @Override
    public ByteArray getIndex(Date targetDate) {
        String initIndex = dateFormat.format(targetDate);
        byte[] indexByte = new byte[initIndex.length() / 2];
        for (int i = 0; i < indexByte.length; i++) {
            indexByte[i] = Byte.parseByte(initIndex.substring(i << 1, (i << 1) + 2));
        }
        return new ByteArray(indexByte);
    }

    public ByteArray indexPrefix(Date targetDate) {
        String prefixIndex = dateFormat.format(targetDate);
        byte[] indexByte = new byte[prefixIndex.length() / 2];
        for (int i = 0; i < indexByte.length; i++) {
            indexByte[i] = Byte.parseByte(prefixIndex.substring(i << 1,
                    (i << 1) + 2));
        }
        return new ByteArray(indexByte);
    }

//    public String invertToString(ByteArray indexTime) {
//        byte[] indexByte = indexTime.getBytes();
//        List<String> timeSequence = new ArrayList<>();
//        for (byte b : indexByte) {
//            String timeBin = Byte.toString(b);
//            if (timeBin.length() < 2) {
//                timeBin = "0".concat(timeBin);
//            }
//            timeSequence.add(timeBin);
//        }
//        return StringUtils.join(timeSequence, null);
//    }

    public String invertToString(ByteArray indexTime) {
        byte[] indexByte = indexTime.getBytes();
        StringBuilder timeCoding = new StringBuilder();
        for (byte b : indexByte) {
            String timeBin = Byte.toString(b);
            if (timeBin.length() < 2) {
                timeBin = "0".concat(timeBin);
            }
            timeCoding.append(timeBin);
        }
        return timeCoding.toString();
    }

    @Override
    public Date invert(ByteArray indexTime) throws ParseException {
        return dateFormat.parse(invertToString(indexTime));
    }

    @Override
    public Date[] preciseInvert(ByteArray indexTime) throws ParseException {
        Date startPreciseDate = invert(indexTime);
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(startPreciseDate);
        int offsetInMilliSeconds = 1000;
        Date endPreciseDate = new Date();
        switch (this.minGranularity) {
            case SECOND:
                offsetInMilliSeconds *= 1;
                break;
            case MINUTE:
                offsetInMilliSeconds *= 60;
                break;
            case HOUR:
                offsetInMilliSeconds *= (60 * 60);
                break;
            case DAY:
                offsetInMilliSeconds *= (60 * 60 * 24);
                break;
            case MONTH:
                offsetInMilliSeconds *= (60 * 60 * 24 * calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                break;
            case YEAR:
                offsetInMilliSeconds *= (60 * 60 * 24 * calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
                break;
            default:
                offsetInMilliSeconds *= (60 * 60);
        }
        endPreciseDate.setTime(startPreciseDate.getTime() + offsetInMilliSeconds - 1);
        return new Date[]{startPreciseDate, endPreciseDate};
    }

    @Override
    public List<ByteArray> decomposeRangeContinuous(Date startDate, Date endDate) {
        List<ByteArray> rangeResult = new ArrayList<>();
        Instant startInstant = startDate.toInstant();//An instantaneous point on the time-line
        LocalDateTime curDateTime = startInstant.atZone(zoneId).toLocalDateTime();
        Instant instant = endDate.toInstant();
        LocalDateTime endDateTime = instant.atZone(zoneId).toLocalDateTime();
        rangeCompose(curDateTime, endDateTime, rangeResult);
        return rangeResult;
    }

    private void rangeCompose(LocalDateTime currentDateTime,
                              LocalDateTime endDateTime,
                              List<ByteArray> rangeResult) {
        ZonedDateTime zdt;
        while (currentDateTime.isBefore(endDateTime)) {
            zdt = currentDateTime.atZone(zoneId);
            if (!rangeResult.contains(indexPrefix(Date.from(zdt.toInstant())))) {
                rangeResult.add(indexPrefix(Date.from(zdt.toInstant())));
            }
            currentDateTime = currentDateTime.plus(1, timeCoarseUnit);
        }
        zdt = endDateTime.atZone(zoneId);
        if (!rangeResult.contains(indexPrefix(Date.from(zdt.toInstant())))) {
            rangeResult.add(indexPrefix(Date.from(zdt.toInstant())));
        }
    }
}
