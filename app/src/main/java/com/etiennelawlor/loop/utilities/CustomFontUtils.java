package com.etiennelawlor.loop.utilities;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.etiennelawlor.loop.R;

/**
 * Created by etiennelawlor on 12/14/15.
 */
public class CustomFontUtils {

    // region Constants
    private static final int UBUNTU_BOLD = 0;
    private static final int UBUNTU_BOLD_ITALIC = 1;
    private static final int UBUNTU_MEDIUM = 2;
    private static final int UBUNTU_MEDIUM_ITALIC = 3;
    private static final int UBUNTU_REGULAR = 4;
    private static final int UBUNTU_ITALIC = 5;
    private static final int UBUNTU_LIGHT = 6;
    private static final int UBUNTU_LIGHT_ITALIC = 7;
    // endregion

    public static void applyCustomFont(TextView customFontTextView, Context context, AttributeSet attrs) {
        TypedArray attributeArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.CustomFontTextView);

        try {
            int font = attributeArray.getInteger(R.styleable.CustomFontTextView_textFont, 4);
            Typeface customFont = getTypeface(context, font);
            customFontTextView.setTypeface(customFont);
        } finally {
            attributeArray.recycle();
        }
    }

    private static Typeface getTypeface(Context context, int font){
        switch (font) {
            case UBUNTU_BOLD :
                return FontCache.getTypeface("Ubuntu-Bold.ttf", context);
            case UBUNTU_BOLD_ITALIC :
                return FontCache.getTypeface("Ubuntu-BoldItalic.ttf", context);
            case UBUNTU_MEDIUM :
                return FontCache.getTypeface("Ubuntu-Medium.ttf", context);
            case UBUNTU_MEDIUM_ITALIC :
                return FontCache.getTypeface("Ubuntu-MediumItalic.ttf", context);
            case UBUNTU_REGULAR :
                return FontCache.getTypeface("Ubuntu-Regular.ttf", context);
            case UBUNTU_ITALIC :
                return FontCache.getTypeface("Ubuntu-Italic.ttf", context);
            case UBUNTU_LIGHT :
                return FontCache.getTypeface("Ubuntu-Light.ttf", context);
            case UBUNTU_LIGHT_ITALIC :
                return FontCache.getTypeface("Ubuntu-LightItalic.ttf", context);
            default:
                // no matching font found
                // return null so Android just uses the standard font (Roboto)
                return null;
        }
    }
}
