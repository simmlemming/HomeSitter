package org.homesitter.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import org.homesitter.R;


/**
 * Created by mtkachenko on 13/09/16.
 */
public class PicturesWidget extends FrameLayout implements View.OnClickListener {

    public static final long MINUTE_MS = 60 * 1000;
    public static final long HOUR_MS = 60 * MINUTE_MS;
    public static final long DAY_MS = 24 * HOUR_MS;

    private OnClickListener seekClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (onSeekListener != null) {
                onSeekListener.onSeek((Long) v.getTag());
            }
        }
    };

    public interface OnSeekListener {
        void onSeek(long ms);
        void onSeekDone();
    }

    private View seekButtonsBlock;
    private OnSeekListener onSeekListener;

    public PicturesWidget(Context context) {
        super(context);
    }

    public PicturesWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PicturesWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public PicturesWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.widget_pictures, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        seekButtonsBlock = findViewById(R.id.seek_buttons);
        setOnClickListener(this);

        setupSeekButton(R.id.plus_day, DAY_MS);
        setupSeekButton(R.id.plus_hour, HOUR_MS);
        setupSeekButton(R.id.plus_minute, MINUTE_MS);
        setupSeekButton(R.id.minus_day, -DAY_MS);
        setupSeekButton(R.id.minus_hour, -HOUR_MS);
        setupSeekButton(R.id.minus_minute, -MINUTE_MS);

        findViewById(R.id.done).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onSeekListener != null) {
                    onSeekListener.onSeekDone();
                }
            }
        });
    }

    private void setupSeekButton(int id, long value) {
        View seekButton = findViewById(id);
        seekButton.setTag(value);
        seekButton.setOnClickListener(seekClickListener);
    }

    @Override
    public void onClick(View v) {
        toggleSeekButtons();
    }

    private void toggleSeekButtons() {
        if (seekButtonsBlock.getVisibility() == View.VISIBLE) {
            seekButtonsBlock.setVisibility(View.GONE);
        } else {
            seekButtonsBlock.setVisibility(View.VISIBLE);
        }
    }

    public void setOnSeekListener(OnSeekListener listener) {
        this.onSeekListener = listener;
    }
}
