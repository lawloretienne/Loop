package com.etiennelawlor.loop.analytics;

import com.flurry.android.FlurryAgent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by etiennelawlor on 9/22/15.
 */
public class EventLogger {

    public static void logEvent(Event event){
//        logFabricEvent(event);
        logFlurryEvent(event);
//        logGoogleAnalyticsEvent(event);
    }

//    private static void logFabricEvent(Event event){
//        String name = event.getName();
//        HashMap<String, Object> map = event.getMap();
//
//        CustomEvent customEvent = new CustomEvent(name);
//
//        for (Object o : map.entrySet()) {
//            Map.Entry pair = (Map.Entry) o;
//
//            String key = (String) pair.getKey();
//            Object value = pair.getValue();
//
//            if (value instanceof Number) {
//                customEvent.putCustomAttribute(key, (Number) value);
//            } else if (value instanceof String) {
//                customEvent.putCustomAttribute(key, (String) value);
//            }
//        }
//
//        Answers.getInstance().logCustom(customEvent);
//    }

    private static void logFlurryEvent(Event event){
        String name = event.getName();
        HashMap<String, Object> map = event.getMap();

        Map<String, String> flurryMap = new HashMap<>();

        for (Object o : map.entrySet()) {
            Map.Entry pair = (Map.Entry) o;

            String key = (String) pair.getKey();
            Object value = pair.getValue();

            flurryMap.put(key, String.valueOf(value));
        }


        FlurryAgent.logEvent(name, flurryMap);
    }

    private static void logGoogleAnalyticsEvent(Event event) {
        String name = event.getName();
        HashMap<String, Object> map = event.getMap();
    }
}
