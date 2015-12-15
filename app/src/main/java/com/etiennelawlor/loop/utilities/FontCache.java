package com.etiennelawlor.loop.utilities;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.util.LruCache;

/**
 * Created by etiennelawlor on 12/14/15.
 */
public class FontCache {

    private static LruCache<String, Typeface> sFontCache = new LruCache<>(17);

    public static Typeface getTypeface(String fontname, Context context) {
        Typeface typeface = sFontCache.get(fontname);

        if (typeface == null) {
            try {
                typeface = Typeface.createFromAsset(context.getAssets(), String.format("fonts/%s", fontname));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            sFontCache.put(fontname, typeface);
        }

        return typeface;
    }
}
