package org.homesitter;

import org.homesitter.model.State;

/**
 * Created by mtkachenko on 12/09/16.
 */
class UiState {
    public final int colorResId, textResId;

    private UiState(int colorResId, int textResId) {
        this.colorResId = colorResId;
        this.textResId = textResId;
    }

    static UiState forState(State state) {
        switch (state.connectionState) {
            case DISCONNECTED:
                return new UiState(R.color.error, R.string.status_disconnected);

            case CONNECTED:
                return UiState.forHomeState(state);

            default:
            case CONNECTING:
                return new UiState(R.color.neutral, R.string.status_connecting);
        }
    }

    private static UiState forHomeState(State state) {
        switch (state.connectionState) {
            case DISCONNECTED:
                return new UiState(R.color.error, R.string.status_home_disconnected);

            case CONNECTED:
                return new UiState(R.color.ok, R.string.status_ok);

            default:
            case CONNECTING:
                return new UiState(R.color.neutral, R.string.status_connecting);
        }
    }
}
