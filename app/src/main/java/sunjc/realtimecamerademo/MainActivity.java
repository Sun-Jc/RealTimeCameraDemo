package sunjc.realtimecamerademo;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {

    CameraCaptureFragment mCameraCaptureFragment;
    TextView mTestText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTestText=(TextView)findViewById(R.id.testText);

        if (null == savedInstanceState)
            createFragment();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void createFragment() {
        android.app.FragmentManager fManager = getFragmentManager();
        FragmentTransaction fTransaction = fManager.beginTransaction();

        mCameraCaptureFragment = new CameraCaptureFragment();
        mCameraCaptureFragment.setmOnMeasureListner(
                new CameraCaptureFragment.OnMeasurementListener() {
                    @Override
                    public void onMeasurementCallback(int heartRate) {
                        mTestText.setText("recv"+heartRate);
                    }
                }
        );

        // Adding the new fragment
        fTransaction.add(R.id.mainContainer, mCameraCaptureFragment);
        fTransaction.commit();
    }
}
