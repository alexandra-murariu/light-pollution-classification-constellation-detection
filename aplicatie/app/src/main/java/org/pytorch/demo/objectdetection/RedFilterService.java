package org.pytorch.demo.objectdetection;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;

public class RedFilterService extends Service {

    private WindowManager mWindowManager;
    private View mRedFilterView;

    @Override
    public void onCreate() {
        super.onCreate();

        // Create the red filter view
        mRedFilterView = new View(this);
        mRedFilterView.setBackgroundColor(Color.RED);
        mRedFilterView.setAlpha(0.5f);

        // Get the window manager
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // Add the red filter view as a system overlay view
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        mWindowManager.addView(mRedFilterView, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Remove the red filter view
        mWindowManager.removeView(mRedFilterView);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
