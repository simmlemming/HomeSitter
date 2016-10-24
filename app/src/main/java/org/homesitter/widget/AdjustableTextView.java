package org.homesitter.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.TextView;

/**
 * Created by mtkachenko on 16/09/16.
 */
public class AdjustableTextView extends TextView {
    private int touchSlop;

    public interface OnAdjustListener {
        void inc(AdjustableTextView view);

        void dec(AdjustableTextView view);
    }

    @NonNull
    private OnAdjustListener listener = new OnAdjustListener() {
        @Override
        public void inc(AdjustableTextView view) {
        }

        @Override
        public void dec(AdjustableTextView view) {
        }
    };

    public AdjustableTextView(Context context) {
        super(context);
    }

    public AdjustableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AdjustableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AdjustableTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ViewConfiguration vc = ViewConfiguration.get(getContext());
        touchSlop = vc.getScaledTouchSlop();
    }

    private float downEventY;
    private boolean adjusted = false;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();

        switch (action) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                return true;

            case MotionEvent.ACTION_DOWN:
                downEventY = ev.getY();
                adjusted = false;
                return true;

            case MotionEvent.ACTION_MOVE:
                if (adjusted) {
                    return true;
                }

                float movedDistance = Math.abs(downEventY - ev.getY());
                if (movedDistance > touchSlop) {
                    if (downEventY < ev.getY()) {
                        listener.dec(this);
                    } else {
                        listener.inc(this);
                    }

                    adjusted = true;
                }
                return true;

            default:
                return true;
        }
    }

    public void setListener(@NonNull OnAdjustListener listener) {
        this.listener = listener;
    }
}
