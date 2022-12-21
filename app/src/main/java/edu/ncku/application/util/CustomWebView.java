package edu.ncku.application.util;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.WebView;

public class CustomWebView extends WebView {

    public CustomWebView(Context context) {
        this(context, null);
    }

    public CustomWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        /* any initialisation work here */
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection ic = super.onCreateInputConnection(outAttrs);

        outAttrs.inputType &= ~EditorInfo.TYPE_MASK_VARIATION; /* clear VARIATION type to be able to set new value */
        outAttrs.inputType |= InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD; /* WEB_PASSWORD type will prevent form suggestions */

        return ic;
    }

}
