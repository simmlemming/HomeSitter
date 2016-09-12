package org.homesitter;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.homesitter.model.Picture;
import org.homesitter.model.State;
import org.homesitter.service.PubnubService;

import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d',' HH:mm:ss");
    private ImageView lastImageView;
    private View takePictureView;
    private TextView stateView, timeView;
    private PubnubService pubnubService;

    private ServiceConnection serviceConnection = new PubnubServiceConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lastImageView = (ImageView) findViewById(R.id.last_image);
        takePictureView = findViewById(R.id.take_picture);
        stateView = (TextView) findViewById(R.id.state);
        timeView = (TextView) findViewById(R.id.time);

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

    private void loadPicture(Picture picture) {
        Picasso.with(this)
                .load(picture.link)
                .into(lastImageView);

        timeView.setText(DATE_FORMAT.format(picture.timeMs));
    }

    private void updateUi(State state) {
        int colorResId;
        int textResId;

        if (state.isHomeConnected && state.isConnected) {
            colorResId = R.color.ok;
            textResId = R.string.status_ok;
        } else if (!state.isConnected) {
            colorResId = R.color.error;
            textResId = R.string.status_disconnected;
        } else {
            colorResId = R.color.error;
            textResId = R.string.status_home_disconnected;
        }

        stateView.setText(getString(textResId));
        stateView.setBackgroundColor(getResources().getColor(colorResId));

        if (state.lastPicture != null) {
            loadPicture(state.lastPicture);
        }
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
