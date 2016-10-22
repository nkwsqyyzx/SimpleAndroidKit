package space.wsq.kit.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import space.wsq.kit.devkit.ILogger;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View vi = findViewById(R.id.tv_test);
        vi.setOnClickListener((v) -> ILogger.defaultLogger.d(TAG, "View clicked!"));
    }
}
