package com.etiennelawlor.loop.utilities;

import android.text.format.DateUtils;

import com.etiennelawlor.loop.LoopApplication;
import com.etiennelawlor.loop.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by etiennelawlor on 11/8/15.
 */
public class DateUtility {

    // region Constants
    public static final String PATTERN = "yyyy-MM-dd'T'hh:mm:ssZ";
    public static final int FORMAT_RELATIVE = 0;
    public static final int FORMAT_ABSOLUTE = 1;
    // endregion

    public static Calendar getCalendar(String timestamp){
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN, Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            Date date = sdf.parse(timestamp);
            calendar.setTime(date);
        } catch (ParseException e){
            e.printStackTrace();
        }

        return calendar;
    }

    public static boolean isSameYear(Calendar cal1, Calendar cal2){
        int year1 = cal1.get(Calendar.YEAR);
        int year2 = cal2.get(Calendar.YEAR);

        return (year1 == year2);
    }

    public static long getTimeUnitDiff(Calendar cal1, Calendar cal2, TimeUnit timeUnit) {
        long diffInMillies = cal2.getTime().getTime() - cal1.getTime().getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    public static String getFormattedTime(Calendar calendar, int format) {
        String formattedTime = "";
        long days = getTimeUnitDiff(calendar, Calendar.getInstance(), TimeUnit.DAYS);
        switch (format) {
            case FORMAT_ABSOLUTE:
                if(days>=7){
                    formattedTime = getFormattedAbsoluteDate(calendar);
                } else {
                    formattedTime = getFormattedAbsoluteTime(calendar);
                }
                break;
            case FORMAT_RELATIVE:
                if(days>=30){
                    formattedTime = getFormattedAbsoluteDate(calendar);
                } else {
                    formattedTime = getFormattedRelativeTime(calendar);
                }
                break;
            default:
                break;
        }

        return formattedTime;
    }

    public static String getFormattedAbsoluteTime(Calendar calendar) {
        String formattedAbsoluteTime = "";

        long days = getTimeUnitDiff(calendar, Calendar.getInstance(), TimeUnit.DAYS);

        if (days < 1) {
            if(calendar.get(Calendar.DAY_OF_WEEK) != Calendar.getInstance().get(Calendar.DAY_OF_WEEK)){
                formattedAbsoluteTime = String.format("%s %d:%02d %s",
                        getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)),
                        getHour(calendar.get(Calendar.HOUR)),
                        calendar.get(Calendar.MINUTE),
                        getMeridiem(calendar.get(Calendar.AM_PM)));
            } else {
                formattedAbsoluteTime = String.format("%d:%02d %s",
                        getHour(calendar.get(Calendar.HOUR)),
                        calendar.get(Calendar.MINUTE),
                        getMeridiem(calendar.get(Calendar.AM_PM)));
            }
        } else if (days < 7) {
            formattedAbsoluteTime = String.format("%s %d:%02d %s",
                    getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)),
                    getHour(calendar.get(Calendar.HOUR)),
                    calendar.get(Calendar.MINUTE),
                    getMeridiem(calendar.get(Calendar.AM_PM)));
        }

        return formattedAbsoluteTime;
    }

    public static String getFormattedRelativeTime(Calendar calendar) {
        String formattedRelativeTime = "";
        long days = getTimeUnitDiff(calendar, Calendar.getInstance(), TimeUnit.DAYS);
        if (days < 7) {
            long seconds = getTimeUnitDiff(calendar, Calendar.getInstance(), TimeUnit.SECONDS);

            if(seconds < 60){
                formattedRelativeTime = "Just now";
            } else {
                CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(calendar.getTimeInMillis(), System.currentTimeMillis(),
                        DateUtils.SECOND_IN_MILLIS);

                formattedRelativeTime = relativeTime.toString();
            }
        } else if (days >= 7 && days < 30){
            formattedRelativeTime = String.format("%d days ago", days);
        }

        return formattedRelativeTime;
    }

    public static String getFormattedAbsoluteDate(Calendar calendar) {
        String formattedAbsoluteDate = "";

        int year = calendar.get(Calendar.YEAR);
        String month = getMonth(calendar.get(Calendar.MONTH));
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        if (isSameYear(calendar, Calendar.getInstance())) {
            formattedAbsoluteDate = String.format("%s %d", month, day);
        } else {
            formattedAbsoluteDate = String.format("%s %d, %d", month, day, year);
        }

        return formattedAbsoluteDate;
    }

    private static String getMonth(int month) {
        String[] months = LoopApplication.getInstance().getResources().getStringArray(R.array.months);
        return months[month];
    }

    private static String getDayOfWeek(int dayOfWeek){
        String[] days = LoopApplication.getInstance().getResources().getStringArray(R.array.days);
        return days[dayOfWeek];
    }

    private static String getMeridiem(int meridiem){
        String[] meridiems = LoopApplication.getInstance().getResources().getStringArray(R.array.meridiems);
        return meridiems[meridiem];
    }

    private static int getHour(int h){
        if(h == 0)
            return 12;
        else
            return h;
    }

}
