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
    private static final int ROBOTO_BLACK = 0;
    private static final int ROBOTO_BLACK_ITALIC = 1;
    private static final int ROBOTO_BOLD = 2;
    private static final int ROBOTO_BOLD_ITALIC = 3;
    private static final int ROBOTO_MEDIUM = 4;
    private static final int ROBOTO_MEDIUM_ITALIC = 5;
    private static final int ROBOTO_REGULAR = 6;
    private static final int ROBOTO_ITALIC = 7;
    private static final int ROBOTO_LIGHT = 8;
    private static final int ROBOTO_LIGHT_ITALIC = 9;
    private static final int ROBOTO_THIN = 10;
    private static final int ROBOTO_THIN_ITALIC = 11;
    private static final int ROBOTO_CONDENSED_BOLD = 12;
    private static final int ROBOTO_CONDENSED_BOLD_ITALIC = 13;
    private static final int ROBOTO_CONDENSED_ITALIC = 14;
    private static final int ROBOTO_CONDENSED_LIGHT = 15;
    private static final int ROBOTO_CONDENSED_LIGHT_ITALIC = 16;
    private static final int ROBOTO_CONDENSED_REGULAR = 17;
    // endregion

    public static void applyCustomFont(TextView customFontTextView, Context context, AttributeSet attrs) {
        TypedArray attributeArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.CustomFontTextView);

        try {
            int font = attributeArray.getInteger(R.styleable.CustomFontTextView_textFont, 6);
            Typeface customFont = getTypeface(context, font);
            customFontTextView.setTypeface(customFont);
        } finally {
            attributeArray.recycle();
        }
    }

    private static Typeface getTypeface(Context context, int font){
        switch (font) {
            case ROBOTO_BLACK :
                return FontCache.getTypeface("Roboto-Black.ttf", context);
            case ROBOTO_BLACK_ITALIC :
                return FontCache.getTypeface("Roboto-BlackItalic.ttf", context);
            case ROBOTO_BOLD :
                return FontCache.getTypeface("Roboto-Bold.ttf", context);
            case ROBOTO_BOLD_ITALIC :
                return FontCache.getTypeface("Roboto-BoldItalic.ttf", context);
            case ROBOTO_MEDIUM :
                return FontCache.getTypeface("Roboto-Medium.ttf", context);
            case ROBOTO_MEDIUM_ITALIC :
                return FontCache.getTypeface("Roboto-MediumItalic.ttf", context);
            case ROBOTO_REGULAR :
                return FontCache.getTypeface("Roboto-Regular.ttf", context);
            case ROBOTO_ITALIC :
                return FontCache.getTypeface("Roboto-Italic.ttf", context);
            case ROBOTO_LIGHT :
                return FontCache.getTypeface("Roboto-Light.ttf", context);
            case ROBOTO_LIGHT_ITALIC :
                return FontCache.getTypeface("Roboto-LightItalic.ttf", context);
            case ROBOTO_THIN :
                return FontCache.getTypeface("Roboto-Thin.ttf", context);
            case ROBOTO_THIN_ITALIC :
                return FontCache.getTypeface("Roboto-ThinItalic.ttf", context);
            case ROBOTO_CONDENSED_BOLD :
                return FontCache.getTypeface("RobotoCondensed-Bold.ttf", context);
            case ROBOTO_CONDENSED_BOLD_ITALIC :
                return FontCache.getTypeface("RobotoCondensed-BoldItalic.ttf", context);
            case ROBOTO_CONDENSED_ITALIC :
                return FontCache.getTypeface("RobotoCondensed-Italic.ttf", context);
            case ROBOTO_CONDENSED_LIGHT :
                return FontCache.getTypeface("RobotoCondensed-Light.ttf", context);
            case ROBOTO_CONDENSED_LIGHT_ITALIC :
                return FontCache.getTypeface("RobotoCondensed-LightItalic.ttf", context);
            case ROBOTO_CONDENSED_REGULAR :
                return FontCache.getTypeface("RobotoCondensed-Regular.ttf", context);
            default:
                // no matching font found
                // return null so Android just uses the standard font (Roboto)
                return null;
        }
    }
}
