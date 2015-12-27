package com.etiennelawlor.loop.utilities;

import android.text.format.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by etiennelawlor on 11/8/15.
 */
public class DateUtility {

    public static String getRelativeDate(Calendar future) {

        String relativeDate = "";

        long days = getDateDiff(future.getTime(), Calendar.getInstance().getTime(), TimeUnit.DAYS);

        if (days < 7) {
            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(future.getTimeInMillis(), System.currentTimeMillis(),
                    DateUtils.SECOND_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_ALL);

//      Timber.d("relativeTime - " + relativeTime);

            if (relativeTime.toString().equals("0 minutes ago")
                    || relativeTime.toString().equals("in 0 minutes")) {
                relativeDate = "Just now";
            } else if(relativeTime.toString().contains("hr. ")){
                if(relativeTime.toString().equals("1 hr. ago")){
                    relativeDate = "1 hour ago";
                } else {
                    relativeDate = relativeTime.toString().replace("hr. ", "hours ");
                }
            } else {
                relativeDate = relativeTime.toString();
            }
        } else if (days >= 7 && days < 14) {
            relativeDate = "A week ago";
        } else if (days >= 14 && days < 21) {
            relativeDate = "2 weeks ago";
        } else if (days >= 21 && days < 30) {
            relativeDate = "3 weeks ago";
        } else if ((days / 30) == 1) {
            relativeDate = "1 month ago";
        } else if ((days / 30) >= 2 && (days / 30) < 12) {
            relativeDate = String.format("%d months ago", (days / 30));
        } else if ((days / 365) == 1) {
            relativeDate = "1 year ago";
        } else if ((days / 365) > 1) {
            relativeDate = String.format("%d years ago", (days / 365));
        }

//        Timber.d("getRelativeDate() : days - " + days);
//        Timber.d("getRelativeDate() : relativeDate - " + relativeDate);

        return relativeDate;
    }

    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

}
