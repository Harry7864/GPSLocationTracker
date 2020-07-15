package com.example.gpslocationtracker.utility;

public class ConnectivityListner {

    private static ConnectivityListner mInstance;
    private OnCustomStateListener mListener;
    private boolean mState;

    ConnectivityListner() {
    }

    public static ConnectivityListner getInstance() {
        if (mInstance == null) {
            mInstance = new ConnectivityListner();
        }
        return mInstance;
    }

    public void setListener(OnCustomStateListener listener) {
        mListener = listener;
    }

    public void changeState(boolean state) {
        if (mListener != null) {
            mState = state;
            notifyStateChange();
        }
    }

    public boolean getState() {
        return mState;
    }

    private void notifyStateChange() {
        mListener.stateChanged();
    }

    public interface OnCustomStateListener {
        void stateChanged();

    }
}
