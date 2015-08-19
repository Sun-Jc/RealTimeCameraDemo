package sunjc.realtimecamerademo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


/**
 * This activity is upper layer of measurement module (cameraCaptureFragment)
 * Author: SunJc
 * in SG and NUS
 * Date: Aug. 2015
 */
public class MainActivity extends FragmentActivity {

    CameraCaptureFragment mCameraCaptureFragment;
    TextView mTestText;
    Button aboutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTestText=(TextView)findViewById(R.id.testText);
        mTestText.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, AboutUs.class);
                        startActivity(intent);
                    }
                }
        );

        if (null == savedInstanceState)
            createFragment();

    }

    /*@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }*/

    private void createFragment() {
        FragmentManager fManager = getSupportFragmentManager();
        FragmentTransaction fTransaction = fManager.beginTransaction();

        //here create an instance of CCF to measure heart rate
        mCameraCaptureFragment = new CameraCaptureFragment();
        //here
        mCameraCaptureFragment.setmOnMeasureListner(
                new CameraCaptureFragment.OnMeasurementListener() {
                    @Override
                    public void onMeasurementCallback(int heartRate) {
                        mTestText.setText("heart rate"+heartRate);
                    }
                    @Override
                    public void onNotOnFinger(){
                        mTestText.setText("please put finger on camera");
                    }
                }
        );

        // Adding the new fragment
        fTransaction.add(R.id.mainContainer, mCameraCaptureFragment);
        fTransaction.commit();
    }
}
