package sunjc.realtimecamerademo;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;

public class MainActivity extends Activity {

    CameraCaptureFragment mCameraCaptureFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        // Adding the new fragment
        fTransaction.add(R.id.mainContainer, mCameraCaptureFragment);
        fTransaction.commit();
    }
}
