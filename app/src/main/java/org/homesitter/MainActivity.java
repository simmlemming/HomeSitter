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
import android.widget.RadioGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.homesitter.model.ViewModel;
import org.homesitter.service.PubnubService;
import org.homesitter.widget.PicturesWidget;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private ImageView lastImageView;
    private View takePictureView;
    private TextView stateView, timeView;
    private PicturesWidget picturesWidget;
    private RadioGroup camIndexGroup;

    private PubnubService pubnubService;
    private ServiceConnection serviceConnection = new PubnubServiceConnection();

    private Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lastImageView = (ImageView) findViewById(R.id.last_image);
        takePictureView = findViewById(R.id.take_picture);
        stateView = (TextView) findViewById(R.id.state);
        timeView = (TextView) findViewById(R.id.time);
        picturesWidget = (PicturesWidget) findViewById(R.id.pictures);
        camIndexGroup = (RadioGroup) findViewById(R.id.cam_index_group);

        presenter = new Presenter(getApplicationContext(), HomeSitter.CAMERAS_COUNT);

        picturesWidget.setOnSeekListener(new PicturesWidget.OnSeekListener() {

            @Override
            public void onSeek(long ms) {
                presenter.seekBy(pubnubService, ms);
            }

            @Override
            public void onSeekToNow() {
                presenter.seekTo(pubnubService, Calendar.getInstance().getTimeInMillis());
            }

            @Override
            public void onEnterSeekMode() {
                presenter.enterSeekingMode();
            }

            @Override
            public void onExitSeekMode() {
                presenter.exitSeekingMode(pubnubService);
            }
        });

        // Before listeners set
        ViewModel lastViewModel = presenter.restoreState();
        updateView(lastViewModel);

        takePictureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onTakePictureClick(pubnubService);
            }
        });

        camIndexGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int camIndex = checkedId == R.id.cam_index_0 ? 0 : 1;
                presenter.onCameraChange(pubnubService, camIndex);
            }
        });

        getApplicationContext().getEventBus().register(this);
        getApplicationContext().getEventBus().register(presenter);
        bindService(PubnubService.intent(this), serviceConnection, BIND_AUTO_CREATE);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ViewModel viewModel) {
        updateView(viewModel);
    }

    private void updateView(ViewModel viewModel) {
        if (viewModel.picture == null) {
            lastImageView.setScaleType(ImageView.ScaleType.CENTER);
            lastImageView.setImageResource(R.drawable.ic_launcher_icon);
        } else {
            lastImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Picasso.with(this)
                    .load(viewModel.picture.link)
                    .into(lastImageView);
        }

        timeView.setText(viewModel.timeText);
        takePictureView.setEnabled(!viewModel.takePictureButtonEnabled);

        stateView.setBackgroundColor(getResources().getColor(viewModel.stateColorResId));
        stateView.setText(viewModel.stateTextResId);

        camIndexGroup.check(viewModel.camIndex == 0 ? R.id.cam_index_0 : R.id.cam_index_1);

        if (!TextUtils.isEmpty(viewModel.userFriendlyErrorMessage)) {
            Snackbar.make(lastImageView, viewModel.userFriendlyErrorMessage, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        presenter.saveState();
        getApplicationContext().getEventBus().unregister(this);
        getApplicationContext().getEventBus().unregister(presenter);
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
            presenter.requestCurrentState(pubnubService);
            presenter.requestLastPictureIfNeeded(pubnubService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            pubnubService = null;
        }
    }
}
