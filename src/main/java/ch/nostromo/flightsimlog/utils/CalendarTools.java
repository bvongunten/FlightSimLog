package ch.nostromo.flightsimlog.utils;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class CalendarTools {

    public static Calendar createDateTime(int year, int month, int day, int hour, int minute, int sec) {
        Calendar calendar = createDate(year, month, day);

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, sec);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar;
    }

    public static Calendar createDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar;
    }

    public static String calToString(Calendar cal) {
        if (cal == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        return sdf.format(cal.getTime());
    }

    public static Calendar stringToCal(String str) {
        return stringToCal(str, true);
    }

    public static Calendar stringToCal(String str, boolean failSafe) {
        try {
            if (!str.isEmpty() || !failSafe) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                Date date = sdf.parse(str);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                return cal;
            }
        } catch (ParseException e) {
            // decided later ...
        }

        if (failSafe) {
            return null;
        } else {
            throw new IllegalArgumentException("unable to parse string: " + str);
        }

    }

    public static Calendar convertXmlDateToCalendar(String dateString) {
        try {
            // Create an XMLGregorianCalendar from the date string
            XMLGregorianCalendar xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(dateString);

            // Convert XMLGregorianCalendar to GregorianCalendar
            GregorianCalendar gregorianCalendar = xmlCalendar.toGregorianCalendar();

            // Return as Calendar
            return gregorianCalendar;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
