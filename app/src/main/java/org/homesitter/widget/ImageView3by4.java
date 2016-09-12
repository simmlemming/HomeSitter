package org.homesitter.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by mtkachenko on 11/09/16.
 */
public class ImageView3by4 extends ImageView {
    private float ratio = 3f / 4;

    public ImageView3by4(Context context) {
        super(context);
    }

    public ImageView3by4(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageView3by4(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ImageView3by4(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = Math.round(width * ratio);

        setMeasuredDimension(width, height);
    }
}
