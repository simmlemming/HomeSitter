package org.homesitter;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.homesitter.model.Picture;
import org.homesitter.model.State;
import org.homesitter.service.PubnubService;
import org.homesitter.widget.PicturesWidget;

import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d',' HH:mm:ss");

    private ImageView lastImageView;
    private View takePictureView;
    private TextView stateView, timeView;
    private PicturesWidget picturesWidget;

    private PubnubService pubnubService;
    private ServiceConnection serviceConnection = new PubnubServiceConnection();

    private long pictureTimeMs = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lastImageView = (ImageView) findViewById(R.id.last_image);
        takePictureView = findViewById(R.id.take_picture);
        stateView = (TextView) findViewById(R.id.state);
        timeView = (TextView) findViewById(R.id.time);
        picturesWidget = (PicturesWidget) findViewById(R.id.pictures);

        picturesWidget.setOnSeekListener(new PicturesWidget.OnSeekListener() {

            @Override
            public void onSeek(long ms) {
                Log.i(HomeSitter.TAG, getClass().getSimpleName() + ".onSeek() called with: " + "ms = [" + ms + "]");
                pictureTimeMs += ms;
                updateTimeView(pictureTimeMs);
            }

            @Override
            public void onSeekDone() {
                Log.i(HomeSitter.TAG, getClass().getSimpleName() + ".onSeekDone() called with: " + "");
                pubnubService.requestPictureAt(pictureTimeMs);
            }
        });

        takePictureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pubnubService.requestNewPicture();
            }
        });

        getApplicationContext().getEventBus().register(this);
        bindService(PubnubService.intent(this), serviceConnection, BIND_AUTO_CREATE);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(PubnubService.StateUpdatedEvent event) {
        if (!TextUtils.isEmpty(event.userFriendlyMessage)) {
            Snackbar.make(lastImageView, event.userFriendlyMessage, Snackbar.LENGTH_SHORT).show();
        }

        updateUi(event.state);
    }

    private void loadPicture(@Nullable Picture picture) {
        Log.i(HomeSitter.TAG, getClass().getSimpleName() + ".loadPicture() called with: " + "picture = [" + picture + "]");
        if (picture == null) {
            lastImageView.setScaleType(ImageView.ScaleType.CENTER);
            lastImageView.setImageResource(R.drawable.ic_launcher_icon);
            updateTimeView(pictureTimeMs);
        } else {
            lastImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Picasso.with(this)
                    .load(picture.link)
                    .into(lastImageView);

            updateTimeView(picture.timeMs);
            pictureTimeMs = picture.timeMs;
        }
    }

    private void updateTimeView(long timeMs) {
        timeView.setText(DATE_FORMAT.format(timeMs));
    }

    private void updateUi(State state) {
        UiState uiState = UiState.forState(state);

        stateView.setText(getString(uiState.textResId));
        stateView.setBackgroundColor(getResources().getColor(uiState.colorResId));

        takePictureView.setEnabled(!state.isPictureRequestInProgress);

        loadPicture(state.lastPicture);
    }

    @Override
    protected void onDestroy() {
        getApplicationContext().getEventBus().unregister(this);
        unbindService(serviceConnection);
        super.onDestroy();
    }

    @Override
    public HomeSitter getApplicationContext() {
        return (HomeSitter) super.getApplicationContext();
    }

    private class PubnubServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            pubnubService = ((PubnubService.LocalBinder) service).service();
            pubnubService.requestRefreshState();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            pubnubService = null;
        }
    }
}
