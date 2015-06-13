package com.etiennelawlor.loop.utilities;

import android.graphics.Typeface;
import android.util.LruCache;
import android.widget.TextView;

import com.etiennelawlor.loop.LoopApplication;

public class TypefaceUtil {

    // region Static Variables
    private static LruCache<String, Typeface> sTypefaceCache = new LruCache<>(19);
    // endregion

    // region Utility Methods
    public static void apply(TypefaceId id, TextView tv) {
        if (tv == null || tv.isInEditMode()) {
            return;
        }
        tv.setTypeface(getTypeface(id));
    }

    public static Typeface getTypeface(TypefaceId id) {
        Typeface typeface = sTypefaceCache.get(id.getFilePath());
        if (typeface == null) {
            typeface = Typeface.createFromAsset(LoopApplication.get().getAssets(), id.getFilePath());
            sTypefaceCache.put(id.getFilePath(), typeface);
        }
        return typeface;
    }
    // endregion

    // region Interfaces
    public interface TypefaceId {
        Typeface get();

        String getFilePath();
    }
    // endregion
}