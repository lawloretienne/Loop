package com.etiennelawlor.loop.utilities;

import android.text.format.DateUtils;

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

    public static String getFormattedDate(String createdTime) {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'", Locale.ENGLISH);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        String uploadedDate = "";

        try {
            Date date = sdf.parse(createdTime);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            uploadedDate = DateUtility.getDate(calendar);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return uploadedDate;
    }

    public static long getDaysFromTimestamp(String timestamp){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        long days = -1L;

        try {
            Date date = sdf.parse(timestamp);

            Calendar futureCalendar = Calendar.getInstance();
            futureCalendar.setTime(date);

            days = getDateDiff(futureCalendar.getTime(), Calendar.getInstance().getTime(), TimeUnit.DAYS);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return days;
    }

    public static boolean isSameYear(Calendar cal1, Calendar cal2){
        int year1 = cal1.get(Calendar.YEAR);
        int year2 = cal2.get(Calendar.YEAR);

        return (year1 == year2);
    }

    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    public static String getDate(Calendar calendar) {

        String customDate;

        long days = getDateDiff(calendar.getTime(), Calendar.getInstance().getTime(), TimeUnit.DAYS);

        if (days < 7) {
            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(calendar.getTimeInMillis(), System.currentTimeMillis(),
                    DateUtils.SECOND_IN_MILLIS);

            customDate = relativeTime.toString();
        } else if (days >= 7 && days < 30){
            customDate = String.format("%d days ago", days);
        } else if (days >= 30 && isSameYear(calendar, Calendar.getInstance())){
            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(calendar.getTimeInMillis(), System.currentTimeMillis(),
                    DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_MONTH);

            customDate = relativeTime.toString();

        } else {
            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(calendar.getTimeInMillis(), System.currentTimeMillis(),
                    DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_MONTH|DateUtils.FORMAT_SHOW_YEAR);

            customDate = relativeTime.toString();
        }

//        Timber.d("getMagicDate() : NO_FORMAT : %s",DateUtils.getRelativeTimeSpanString(future.getTimeInMillis(), System.currentTimeMillis(),
//                DateUtils.SECOND_IN_MILLIS).toString());
//
//        Timber.d("getMagicDate() : FORMAT_ABBREV_ALL : %s", DateUtils.getRelativeTimeSpanString(future.getTimeInMillis(), System.currentTimeMillis(),
//                DateUtils.SECOND_IN_MILLIS,
//                DateUtils.FORMAT_ABBREV_ALL).toString());
//
//        Timber.d("getMagicDate() : FORMAT_ABBREV_MONTH : %s",DateUtils.getRelativeTimeSpanString(future.getTimeInMillis(), System.currentTimeMillis(),
//                DateUtils.SECOND_IN_MILLIS,
//                DateUtils.FORMAT_ABBREV_MONTH).toString());
//
//        Timber.d("getMagicDate() : FORMAT_ABBREV_TIME : %s",DateUtils.getRelativeTimeSpanString(future.getTimeInMillis(), System.currentTimeMillis(),
//                DateUtils.SECOND_IN_MILLIS,
//                DateUtils.FORMAT_ABBREV_TIME).toString());
//
//        Timber.d("getMagicDate() : FORMAT_ABBREV_RELATIVE : %s",DateUtils.getRelativeTimeSpanString(future.getTimeInMillis(), System.currentTimeMillis(),
//                DateUtils.SECOND_IN_MILLIS,
//                DateUtils.FORMAT_ABBREV_RELATIVE).toString());
//
//        Timber.d("getMagicDate() : FORMAT_ABBREV_WEEKDAY : %s",DateUtils.getRelativeTimeSpanString(future.getTimeInMillis(), System.currentTimeMillis(),
//                DateUtils.SECOND_IN_MILLIS,
//                DateUtils.FORMAT_ABBREV_WEEKDAY).toString());
//
//        Timber.d("getMagicDate() : FORMAT_NO_MIDNIGHT : %s",DateUtils.getRelativeTimeSpanString(future.getTimeInMillis(), System.currentTimeMillis(),
//                DateUtils.SECOND_IN_MILLIS,
//                DateUtils.FORMAT_NO_MIDNIGHT).toString());
//
//        Timber.d("getMagicDate() : FORMAT_NO_MONTH_DAY : %s",DateUtils.getRelativeTimeSpanString(future.getTimeInMillis(), System.currentTimeMillis(),
//                DateUtils.SECOND_IN_MILLIS,
//                DateUtils.FORMAT_NO_MONTH_DAY).toString());
//
//        Timber.d("getMagicDate() : FORMAT_NO_NOON : %s",DateUtils.getRelativeTimeSpanString(future.getTimeInMillis(), System.currentTimeMillis(),
//                DateUtils.SECOND_IN_MILLIS,
//                DateUtils.FORMAT_NO_NOON).toString());
//
//        Timber.d("getMagicDate() : FORMAT_NO_YEAR : %s",DateUtils.getRelativeTimeSpanString(future.getTimeInMillis(), System.currentTimeMillis(),
//                DateUtils.SECOND_IN_MILLIS,
//                DateUtils.FORMAT_NO_YEAR).toString());
//
//        Timber.d("getMagicDate() : FORMAT_NUMERIC_DATE : %s",DateUtils.getRelativeTimeSpanString(future.getTimeInMillis(), System.currentTimeMillis(),
//                DateUtils.SECOND_IN_MILLIS,
//                DateUtils.FORMAT_NUMERIC_DATE).toString());
//
//        Timber.d("getMagicDate() : FORMAT_SHOW_DATE : %s",DateUtils.getRelativeTimeSpanString(future.getTimeInMillis(), System.currentTimeMillis(),
//                DateUtils.SECOND_IN_MILLIS,
//                DateUtils.FORMAT_SHOW_DATE).toString());
//
//        Timber.d("getMagicDate() : FORMAT_SHOW_TIME : %s",DateUtils.getRelativeTimeSpanString(future.getTimeInMillis(), System.currentTimeMillis(),
//                DateUtils.SECOND_IN_MILLIS,
//                DateUtils.FORMAT_SHOW_TIME).toString());
//
//        Timber.d("getMagicDate() : FORMAT_SHOW_WEEKDAY : %s",DateUtils.getRelativeTimeSpanString(future.getTimeInMillis(), System.currentTimeMillis(),
//                DateUtils.SECOND_IN_MILLIS,
//                DateUtils.FORMAT_SHOW_WEEKDAY).toString());
//
//        Timber.d("getMagicDate() : FORMAT_SHOW_YEAR : %s",DateUtils.getRelativeTimeSpanString(future.getTimeInMillis(), System.currentTimeMillis(),
//                DateUtils.SECOND_IN_MILLIS,
//                DateUtils.FORMAT_SHOW_YEAR).toString());

        return customDate;
    }


//    public static String getRegularDate(Calendar future) {
//
//        String formattedDate = "";
//
//        long days = getDateDiff(future.getTime(), Calendar.getInstance().getTime(), TimeUnit.DAYS);
//
//        if (days < 1) {
//            if(future.get(Calendar.DAY_OF_WEEK) != Calendar.getInstance().get(Calendar.DAY_OF_WEEK)){
//                formattedDate = String.format("%s %d:%02d %s", getDayOfWeek(future.get(Calendar.DAY_OF_WEEK)), getHour(future.get(Calendar.HOUR)), future.get(Calendar.MINUTE), getMeridiem(future.get(Calendar.AM_PM)));
//            } else {
//                formattedDate = String.format("%d:%02d %s", getHour(future.get(Calendar.HOUR)), future.get(Calendar.MINUTE), getMeridiem(future.get(Calendar.AM_PM)));
//            }
//        } else if (days < 7) {
//            formattedDate = String.format("%s %d:%02d %s", getDayOfWeek(future.get(Calendar.DAY_OF_WEEK)), getHour(future.get(Calendar.HOUR)), future.get(Calendar.MINUTE), getMeridiem(future.get(Calendar.AM_PM)));
//        } else if (days >= 7){
//            formattedDate = getDate(future);
//        }
//
//        return formattedDate;
//    }

//    public static String getRelativeDate(Calendar future) {
//
//        String relativeDate = "";
//
//        long days = getDateDiff(future.getTime(), Calendar.getInstance().getTime(), TimeUnit.DAYS);
//
//        if (days < 7) {
//            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(future.getTimeInMillis(), System.currentTimeMillis(),
//                    DateUtils.SECOND_IN_MILLIS,
//                    DateUtils.FORMAT_ABBREV_ALL);
//
////      Timber.d("relativeTime - " + relativeTime);
//
//            if (relativeTime.toString().equals("0 minutes ago")
//                    || relativeTime.toString().equals("in 0 minutes")) {
//                relativeDate = "Just now";
//            } else if(relativeTime.toString().contains("sec.")){
//                if(relativeTime.toString().equals("0 sec. ago") || relativeTime.toString().equals("In 0 sec.")){
//                    relativeDate = "Just now";
//                } else {
//                    relativeDate = relativeTime.toString().replace("sec. ", "seconds ");
//                }
//            } else if(relativeTime.toString().contains("min.")){
//                relativeDate = relativeTime.toString().replace("min. ", "minutes ");
//            } else if(relativeTime.toString().contains("hr. ")){
//                if(relativeTime.toString().equals("1 hr. ago")){
//                    relativeDate = "1 hour ago";
//                } else {
//                    relativeDate = relativeTime.toString().replace("hr. ", "hours ");
//                }
//            } else {
//                relativeDate = relativeTime.toString();
//            }
//        } else if (days >= 7 && days < 14) {
//            relativeDate = "A week ago";
//        } else if (days >= 14 && days < 21) {
//            relativeDate = "2 weeks ago";
//        } else if (days >= 21 && days < 30) {
//            relativeDate = "3 weeks ago";
//        } else if ((days / 30) == 1) {
//            relativeDate = "1 month ago";
//        } else if ((days / 30) >= 2 && (days / 30) < 12) {
//            relativeDate = String.format("%d months ago", (days / 30));
//        } else if ((days / 365) == 1) {
//            relativeDate = "1 year ago";
//        } else if ((days / 365) > 1) {
//            relativeDate = String.format("%d years ago", (days / 365));
//        }
//
////        Timber.d("getRelativeDate() : days - " + days);
////        Timber.d("getRelativeDate() : relativeDate - " + relativeDate);
//
//        return relativeDate;
//    }


//    public static String getDate(Calendar future) {
//        String date;
//
//        int year = future.get(Calendar.YEAR);
//        String month = getMonth(future.get(Calendar.MONTH));
//        int day = future.get(Calendar.DAY_OF_MONTH);
//
//        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
//
//        if (year != currentYear) {
//            date = String.format("%s %d, %d", month, day, year);
//        } else {
//            date = String.format("%s %d", month, day);
//        }
//
//        return date;
//    }
//
//    private static String getMonth(int month) {
//        switch (month) {
//            case 0:
//                return "Jan";
//            case 1:
//                return "Feb";
//            case 2:
//                return "Mar";
//            case 3:
//                return "Apr";
//            case 4:
//                return "May";
//            case 5:
//                return "Jun";
//            case 6:
//                return "Jul";
//            case 7:
//                return "Aug";
//            case 8:
//                return "Sept";
//            case 9:
//                return "Oct";
//            case 10:
//                return "Nov";
//            case 11:
//                return "Dec";
//            default:
//                return "";
//        }
//    }
//
//    private static String getDayOfWeek(int dayOfWeek){
//        switch (dayOfWeek) {
//            case 1:
//                return "Sun";
//            case 2:
//                return "Mon";
//            case 3:
//                return "Tue";
//            case 4:
//                return "Wed";
//            case 5:
//                return "Thur";
//            case 6:
//                return "Fri";
//            case 7:
//                return "Sat";
//            default:
//                return "";
//        }
//    }
//
//    private static String getMeridiem(int m){
//        switch (m){
//            case Calendar.AM:
//                return "AM";
//            case Calendar.PM:
//                return  "PM";
//            default:
//                return "";
//        }
//    }
//
//    private static int getHour(int h){
//        if(h == 0)
//            return 12;
//        else
//            return h;
//    }

}
