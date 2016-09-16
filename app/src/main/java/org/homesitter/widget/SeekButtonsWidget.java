package org.homesitter.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import org.homesitter.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mtkachenko on 16/09/16.
 */
public class SeekButtonsWidget extends FrameLayout {
    public static final long MINUTE_MS = 60 * 1000;
    public static final long HOUR_MS = 60 * MINUTE_MS;
    public static final long DAY_MS = 24 * HOUR_MS;

    public interface Listener {
        void onSeek(long ms);
        void onSeekToNow();
        void onEnterSeekMode();
        void onExitSeekMode();
        void onTakePictureClick();
    }

    private View takePictureView, seekButtonsBlock;
    private AdjustableTextView dateView, hourView, minuteView;

    private boolean isInSeekMode = false;

    public SeekButtonsWidget(Context context) {
        super(context);
        init();
    }

    public SeekButtonsWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SeekButtonsWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SeekButtonsWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.widget_seek_buttons, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        takePictureView = findViewById(R.id.take_picture);
        seekButtonsBlock = findViewById(R.id.seek_buttons);

        dateView = (AdjustableTextView) findViewById(R.id.date);
        hourView = (AdjustableTextView) findViewById(R.id.hour);
        minuteView = (AdjustableTextView) findViewById(R.id.minute);

        dateView.setTag(DAY_MS);
        hourView.setTag(HOUR_MS);
        minuteView.setTag(MINUTE_MS);

        dateView.setListener(onAdjustSeekTimeListener);
        hourView.setListener(onAdjustSeekTimeListener);
        minuteView.setListener(onAdjustSeekTimeListener);

        takePictureView.setOnClickListener(onTakePictureClickListener);
        takePictureView.setOnLongClickListener(toggleSeekModeListener);
    }

    public void setListener(@NonNull Listener listener) {
        this.listener = listener;
    }

    public View getTakePictureView() {
        return takePictureView;
    }

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd");
    private static final SimpleDateFormat HOUR_FORMAT = new SimpleDateFormat("HH");
    private static final SimpleDateFormat MINUTE_FORMAT = new SimpleDateFormat("mm");

    public void setSeekTime(long timeMs) {
        Date seekTime = new Date(timeMs);
        dateView.setText(DATE_FORMAT.format(seekTime));
        hourView.setText(HOUR_FORMAT.format(seekTime));
        minuteView.setText(MINUTE_FORMAT.format(seekTime));
    }

    @NonNull
    private Listener listener = new Listener() {
        @Override
        public void onSeek(long ms) {

        }

        @Override
        public void onSeekToNow() {

        }

        @Override
        public void onEnterSeekMode() {

        }

        @Override
        public void onExitSeekMode() {

        }

        @Override
        public void onTakePictureClick() {

        }
    };

    private AdjustableTextView.OnAdjustListener onAdjustSeekTimeListener = new AdjustableTextView.OnAdjustListener() {
        @Override
        public void inc(AdjustableTextView view) {
            long timeMs = (long) view.getTag();
            listener.onSeek(timeMs);
        }

        @Override
        public void dec(AdjustableTextView view) {
            long timeMs = (long) view.getTag();
            listener.onSeek(-timeMs);
        }
    };

    private OnLongClickListener toggleSeekModeListener = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            isInSeekMode = !isInSeekMode;
            seekButtonsBlock.setVisibility(isInSeekMode ? View.VISIBLE : View.GONE);
            int bkgColorResId = isInSeekMode ? R.color.black : android.R.color.transparent;
            setBackgroundColor(getResources().getColor(bkgColorResId));

            if (listener != null) {
                if (isInSeekMode) {
                    listener.onEnterSeekMode();
                } else {
                    listener.onExitSeekMode();
                }
            }

            return true;
        }
    };

    private OnClickListener onTakePictureClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            listener.onTakePictureClick();
        }
    };
}
