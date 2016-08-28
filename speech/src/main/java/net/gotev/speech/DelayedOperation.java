package net.gotev.speech;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Aleksandar Gotev
 */
public class DelayedOperation {

    public interface Operation {
        void onDelayedOperation();
        boolean shouldExecuteDelayedOperation();
    }

    private long mDelay;
    private Operation mOperation;
    private Timer mTimer;
    private boolean started;
    private Context mContext;
    private String mTag;

    public DelayedOperation(Context context, String tag, long delayInMilliseconds) {
        if (context == null) {
            throw new IllegalArgumentException("Context is null");
        }

        if (delayInMilliseconds <= 0) {
            throw new IllegalArgumentException("The delay in milliseconds must be > 0");
        }

        mContext = context;
        mTag = tag;
        mDelay = delayInMilliseconds;
    }

    public void start(final Operation operation) {
        if (operation == null) {
            throw new IllegalArgumentException("The operation must be defined!");
        }

        Log.d(Speech.class.getSimpleName(), "Starting delayed operation with tag: " + mTag);
        mOperation = operation;
        cancel();
        started = true;
        resetTimer();
    }

    public void resetTimer() {
        if (!started) {
            throw new IllegalStateException("Start the operation first!");
        }

        if (mTimer != null) mTimer.cancel();

        Log.d(mTag, "Resetting delayed operation");
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mOperation.shouldExecuteDelayedOperation()) {
                    Log.d(Speech.class.getSimpleName(), "Executing delayed operation with tag: "
                            + mTag + " on the main thread");

                    new Handler(mContext.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mOperation.onDelayedOperation();
                        }
                    });
                }
                cancel();
            }
        }, mDelay);
    }

    public void cancel() {
        if (mTimer != null) {
            Log.d(Speech.class.getSimpleName(), "Cancelled delayed operation with tag: " + mTag);
            mTimer.cancel();
            mTimer = null;
        }

        started = false;
    }
}
