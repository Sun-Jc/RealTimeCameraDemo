package sunjc.realtimecamerademo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * this view draws plot figure
 * Author: SunJc
 * in SG and NUS
 * Date: Aug. 2015
 * helps to display the signal processing result
 */
public class FigureDisp extends SurfaceView implements SurfaceHolder.Callback{

    final static int margin = 50;
    int mWidth;
    int mHeight;
    double[] vals;
    int length;

    SurfaceHolder mDrawSurface;

    public FigureDisp(Context context) {
        super(context);
        initSurface(context);
    }

    public FigureDisp(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSurface(context);
    }

    private void initSurface(Context context) {
        mDrawSurface = getHolder();
        mDrawSurface.addCallback(this);

    }

    // Region  these are from SurfaceHodler.CallBack
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //Log.i("MyDebug", "Surface:Created");
        setWillNotDraw(false);
        // setBackgroundColor(Color.GRAY);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        //Log.i("MyDebug", "Surface:" + width + "x" + height);
        mWidth = width-2*margin;
        mHeight = height-2*margin;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
    // EndRegion

    void set(double[] data){
        vals = data;
        length = data.length;
    }

    @Override
    protected void onDraw(Canvas c) {

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(5);

        double maxVal = -10000;
        double minVal = 100000000;
        for(int i=0;i<length;i++){
            if(vals[i]>maxVal)
                maxVal = vals[i];
            if(vals[i]<minVal)
                minVal = vals[i];
        }

        int xstep = (int)Math.floor(mWidth/(length-1));
        int ystep = (int)Math.floor(mHeight/(maxVal-minVal));

        for(int i=1;i<length;i++){
            c.drawLine(xstep * i + margin, margin + mHeight - (int) Math.floor(ystep * (vals[i] - minVal)), xstep * (i - 1) + margin, margin + mHeight - (int) Math.floor(ystep * (vals[i - 1] - minVal)), paint);
        }
    }
}
