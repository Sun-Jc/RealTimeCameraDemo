package sunjc.realtimecamerademo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


public class AboutUs extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        Button b = (Button) findViewById(R.id.backButton);
        Log.d("sunjc-debug","about us");
        b.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        back();
                    }
                }
        );
    }
    void back(){
        finish();
    }
}
