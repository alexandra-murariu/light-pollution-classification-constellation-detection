package org.pytorch.demo.objectdetection;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class RedFilterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start the RedFilterService
        Intent intent = new Intent(this, RedFilterService.class);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Stop the RedFilterService
        Intent intent = new Intent(this, RedFilterService.class);
        stopService(intent);
    }
}
