package util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class DateUtil {

    //日期格式化为字符串
    public static Date add(Date date, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.SECOND, second);
        return calendar.getTime();
    }

    //日期格式化为字符串
    public static String convertDateToString(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    //将日期字符串按照指定的格式解析为日期对象
    public static Date convertStringToDate(String dateString, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date date = null;
        try {
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static boolean isLater(Date date1, Date date2, int second) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date1);
        cal.add(Calendar.SECOND, second);
        return cal.getTime().compareTo(date2) > 0;
    }

    public static boolean isLater(String d1, String d2, int second, String format) {
        Date date1 = convertStringToDate(d1, format);
        Date date2 = convertStringToDate(d2, format);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date1);
        cal.add(Calendar.SECOND, second);
        return cal.getTime().compareTo(date2) > 0;
    }

    public static boolean isPrevious(String d1, String d2, int second, String format) {
        Date date1 = convertStringToDate(d1, format);
        Date date2 = convertStringToDate(d2, format);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date1);
        cal.add(Calendar.SECOND, second);
        return cal.getTime().compareTo(date2) < 0;
    }

    public static boolean isPrevious(Date date1, Date date2, int second) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date1);
        cal.add(Calendar.SECOND, second);
        return cal.getTime().compareTo(date2) < 0;
    }

    public static boolean isPrevious(Date date1, Date date2) {
        return date1.compareTo(date2) < 0;
    }

    public static boolean isNext(Date date1, Date date2) {
        return date1.compareTo(date2) > 0;
    }
}
