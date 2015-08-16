package sunjc.realtimecamerademo;

import android.hardware.Camera;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;


public class CameraCaptureFragment extends Fragment {

    //consts
    static final String DEBUG = "SunJc_debug";

    //camera and the surfaceview
    private SurfaceView mCameraSurfacePreview;
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;

    //measurement vars
    TextView textDisp;
    FigureDisp figureAbove;
    FigureDisp figureBelow;
    private long startTime;
    String disp;
    Queue<Integer> windowSignal;
    Queue<Long> windowTime;

    //CallBack
    OnMeasurementListener mOnMeasureListener;

    /*************fragment and camera surface view stuff********/
    public CameraCaptureFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_camera_capture_frame, container, false);

        initProcessView(rootView);

        initCameraSurfacePreview(rootView);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            if (mCamera != null) {
                mCamera.release();
                mCamera = null;
            }
            mCamera = Camera.open();

            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }

        startTime = System.currentTimeMillis();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(DEBUG, "pause");
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void initCameraSurfacePreview(View v) {
        mCameraSurfacePreview = (SurfaceView) v.findViewById(R.id.cameraPreview);
        mSurfaceHolder = mCameraSurfacePreview.getHolder();
        mSurfaceHolder.addCallback(mCameraSurfaceCallback);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private void initProcessView(View v){
        textDisp = (TextView) v.findViewById(R.id.textDisp);
        figureAbove = (FigureDisp) v.findViewById(R.id.figureAbove);
        figureBelow = (FigureDisp) v.findViewById(R.id.figureBelow);

        windowSignal = new LinkedList<Integer>();
        windowTime = new LinkedList<Long>();
    }

    private SurfaceHolder.Callback mCameraSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                Log.i(DEBUG, "SurfaceHolder.Callback：surface Created");
                mCamera.setPreviewDisplay(mSurfaceHolder);//set the surface to be used for live preview
                mCamera.setPreviewCallback(mPreviewCallback);
            } catch (Exception ex) {
                if (null != mCamera) {
                    mCamera.release();
                    mCamera = null;
                }
                Log.i(DEBUG + "initCamera", ex.getMessage());
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.i(DEBUG, "SurfaceHolder.Callback：Surface Changed");
            initCamera(width, height);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

            Log.i(DEBUG, "SurfaceHolder.Callback：Surface Destroyed");
        }
    };

    private void initCamera(int width, int height) {//call in surfaceChanged
        Log.i(DEBUG, "going into initCamera");
        if (null != mCamera) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                Camera.Size size = getSmallestPreviewSize(width, height, parameters);
                if (size != null) {
                    parameters.setPreviewSize(size.width, size.height);
                    Log.d(DEBUG, "Using width=" + size.width + " height=" + size.height);
                }
                mCamera.setParameters(parameters);
                mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Camera.Size getSmallestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;
                    if (newArea < resultArea) result = size;
                }
            }
        }
        return result;
    }

    /*************mesurement and signal processing********/
    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (data == null) throw new NullPointerException();
            Camera.Size size = camera.getParameters().getPreviewSize();
            if (size == null) throw new NullPointerException();

            int width = size.width;
            int height = size.height;

            long nowTime = System.currentTimeMillis();
            int imgAvg = ImageProcessing.decodeYUV420SPtoRedAvg(data.clone(), width,height);

            //Log.i(DEBUG,"Avg:"+Integer.toString(imgAvg));

            //long delay = nowTime - startTime;
            //startTime = nowTime;

            //disp += delay + ", ";
            //textDisp.setText(disp);


            /**********processing********/
            windowSignal.offer(imgAvg);
            windowTime.offer(nowTime);

            int windowSize = 256;

            if(windowSignal.size()>windowSize){
                windowSignal.poll();
                startTime= windowTime.poll();

                double delay = ( nowTime - startTime)/1000.0;
                double sampleRate = windowSize/delay;
                double freqResolution = sampleRate/2/(windowSize/2);

                double[] sig = new double[windowSignal.size()];

                int count = 0;
                for(Integer it:windowSignal){
                    sig[count] = it;
                    count++;
                }

                FastFourierTransformer ffter = new FastFourierTransformer(DftNormalization.STANDARD);
                Complex[] freqDom = ffter.transform(sig, TransformType.INVERSE);
                double[] ampInFreq = new double[freqDom.length];
                for(int i =0;i<ampInFreq.length;i++){
                    ampInFreq[i] = freqDom[i].abs();
                }
                ampInFreq[0] = 0;

                double maxAmp = -1;
                for(int i=0;i<ampInFreq.length;i++){
                    if(ampInFreq[i]>maxAmp){
                        maxAmp = ampInFreq[i];
                    }
                }

                double[] acutalFreqDom = new double[ampInFreq.length/2];
                for(int i=5;i<ampInFreq.length/2;i++){
                    acutalFreqDom[i] = ampInFreq[i+1];
                }

                //Log.d("sunjc-debug","max"+maxAmp);

                int freqIndex = findPeakIndex(acutalFreqDom);

                figureAbove.set(acutalFreqDom);
                figureAbove.invalidate();
                figureBelow.set(sig);
                figureBelow.invalidate();

                int heartRate = (int)Math.ceil(freqIndex*freqResolution*60);
                Log.d("sunjc-debug","freq"+heartRate);
                textDisp.setText("Heart Rate: " + heartRate);

                mOnMeasureListener.onMeasurementCallback(heartRate);

                if (imgAvg == 0 || imgAvg == 255) {
                    Log.i(DEBUG,"bad imgAvg");
                    return;
                }
            }

        }
    };


    int findPeakIndex(double[] s){
        if(s.length>=3) {
            double max = -20000;
            int index = -1;
            for(int i=1;i<s.length-1;i++){
                if( s[i-1]<s[i] && s[i+1]<s[i] ){
                    if(s[i]>max){
                        max = s[i];
                        index = i;
                    }
                }
            }
            return index;
        }else{
            return -1;
        }
    }

    void setmOnMeasureListner(OnMeasurementListener listener){
        mOnMeasureListener = listener;
    }

    interface OnMeasurementListener{
        void onMeasurementCallback(int heartRate);
    }
}
