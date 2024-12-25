package ch.nostromo.flightsimlog.utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class OutputFormatter {

    public static String createTime(Calendar cal) {
        return new SimpleDateFormat("yyyy.MM.dd HH:mm").format(cal.getTime());
    }

    public static String createDistance(double distance) {
        return new DecimalFormat("####0.00").format(distance);
    }

    public static String createDuration(int durationSec) {
        long hours = durationSec / 3600;
        long minutes = (durationSec % 3600) / 60;

        return String.format("%02d:%02d", hours, minutes);
    }

}
