package com.etiennelawlor.loop.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;

import com.etiennelawlor.loop.utilities.CustomFontUtils;

/**
 * Created by etiennelawlor on 12/14/15.
 */
public class CustomFontEditText extends EditText {

    // region Constructors
    public CustomFontEditText(Context context) {
        super(context);
        init(context, null);
    }

    public CustomFontEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CustomFontEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }
    // endregion

    // region Helper Methods
    private void init(Context context, AttributeSet attrs) {
        if (!isInEditMode()) {
            CustomFontUtils.applyCustomFont(this, context, attrs);
        }
    }
    // endregion
}
