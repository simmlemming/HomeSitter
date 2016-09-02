package org.homesitter;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    private ImageView lastImageView;
    private View takePictureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lastImageView = (ImageView) findViewById(R.id.last_image);
        takePictureView = findViewById(R.id.take_picture);

        getApplicationContext().getEventBus().register(this);
    }

    @SuppressWarnings("unused")
    public void onEvent(PubnubService.StateUpdatedEvent event) {
        if (!TextUtils.isEmpty(event.userFriendlyMessage)) {
            Snackbar.make(lastImageView, event.userFriendlyMessage, Snackbar.LENGTH_SHORT).show();
        }

        updateUi(event.state);
    }

    private void updateUi(PubnubService.State state) {
        takePictureView.setEnabled(state.isConnected && state.isHomeConnected);
    }

    @Override
    protected void onDestroy() {
        getApplicationContext().getEventBus().unregister(this);
        super.onDestroy();
    }

    @Override
    public HomeSitter getApplicationContext() {
        return (HomeSitter) super.getApplicationContext();
    }
}
