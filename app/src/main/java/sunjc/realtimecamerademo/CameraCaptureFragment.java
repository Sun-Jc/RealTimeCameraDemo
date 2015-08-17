package sunjc.realtimecamerademo;

import android.app.Fragment;
import android.hardware.Camera;
import android.os.Bundle;
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

import java.util.LinkedList;
import java.util.Queue;

/**
 * this fragment measures heart rate
 * Author: SunJc
 * in SG and NUS
 * Date: Aug. 2015
 * Reference and Thanks to: Justin Wetherell <phishman3579@gmail.com>
 * Libraries and Thanks to: Apache Math3
 * key algorithm of DSP in onPreviewFrame
 * you may not change other parts
 */

public class CameraCaptureFragment extends Fragment {

    //consts
    static final String DEBUG = "SunJc_debug";

    //consts about processing
    static final int windowSize = 256;
    //measurement vars
    TextView textDisp;
    FigureDisp figureAbove;
    FigureDisp figureBelow;
    String disp;
    Queue<Integer> windowSignal;
    Queue<Long> windowTime;
    //CallBack
    OnMeasurementListener mOnMeasureListener;
    //camera and the surfaceview
    private SurfaceView mCameraSurfacePreview;
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;
    private long startTime;

    /*************************************
     * measurement and signal processing
     ************************************/
    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {

            //get camera size
            if (data == null)
                throw new NullPointerException();
            Camera.Size size = camera.getParameters().getPreviewSize();
            if (size == null)
                throw new NullPointerException();
            int width = size.width;
            int height = size.height;

            //get time and real-time camera data
            long nowTime = System.currentTimeMillis();
            int imgAvg = ImageProcessing.decodeYUV420SPtoRedAvg(data.clone(), width, height);

            /**********processing********/
            windowSignal.offer(imgAvg);
            windowTime.offer(nowTime);

            if (windowSignal.size() > windowSize) {

                //remain window of a fixed size
                windowSignal.poll();
                startTime = windowTime.poll();

                /**
                 * here to filter
                 */

                //get basic aux vars
                //during time of this window
                double delay = (nowTime - startTime) / 1000.0;
                //sampleRate of this window
                double sampleRate = windowSize / delay;
                //max frequency of corresponding frequency domain
                double freqDomRange = sampleRate / 2;
                //number of *intervals* in the corresponding frequency domain
                int freqDomLen = windowSize / 2;
                //how many Hz(s) does one freqDom interval means
                double freqResolution = freqDomRange / freqDomLen;

                //sig: this window
                double[] sig = new double[windowSignal.size()];
                int count = 0;
                for (Integer it : windowSignal) {
                    sig[count] = it;
                    count++;
                }

                //FFT
                //ampInFreq: frequency domain, size: freqDomLen
                FastFourierTransformer ffter = new FastFourierTransformer(DftNormalization.STANDARD);
                Complex[] freqDom = ffter.transform(sig, TransformType.FORWARD);
                double[] ampInFreq = new double[freqDomLen];
                for (int i = 0; i < freqDomLen; i++) {
                    ampInFreq[i] = freqDom[i + 1].abs();
                }

                //here to select peak

                //find peak
                int freqIndex = findPeakIndex(ampInFreq);

                //get the final result and pass it back to upper layer
                //heartRate: the measurement result of heart rate
                int heartRate = (int) Math.ceil(freqIndex * freqResolution * 60);
                mOnMeasureListener.onMeasurementCallback(heartRate);

                /***********************************/
//FFT
                //ampInFreq: frequency domain, size: freqDomLen
                FastFourierTransformer ffter2 = new FastFourierTransformer(DftNormalization.STANDARD);
                //@?? INVERSE?
                Complex[] freqDom2 = ffter2.transform(sig, TransformType.INVERSE);
                double[] ampInFreq2 = new double[freqDomLen];
                for (int i = 0; i < freqDomLen; i++) {
                    ampInFreq2[i] = freqDom2[i + 1].abs();
                }

                /**
                 * here to select peak
                 */

                //find peak
                int freqIndex2 = findPeakIndex(ampInFreq2);

                //get the final result and pass it back to upper layer
                //heartRate: the measurement result of heart rate
                int heartRate2 = (int) Math.ceil(freqIndex2 * freqResolution * 60);
/*************/
                //FFT/***********************************/
                //ampInFreq: frequency domain, size: freqDomLen
                FastFourierTransformer ffter3 = new FastFourierTransformer(DftNormalization.UNITARY);
                //@?? INVERSE?
                Complex[] freqDom3 = ffter3.transform(sig, TransformType.FORWARD);
                double[] ampInFreq3 = new double[freqDomLen];
                for (int i = 0; i < freqDomLen; i++) {
                    ampInFreq3[i] = freqDom3[i + 1].abs();
                }

                /**
                 * here to select peak
                 */

                //find peak
                int freqIndex3 = findPeakIndex(ampInFreq3);

                //get the final result and pass it back to upper layer
                //heartRate: the measurement result of heart rate
                int heartRate3 = (int) Math.ceil(freqIndex3 * freqResolution * 60);
                /***********************************/

                //FFT/***********************************/
//FFT
                //ampInFreq: frequency domain, size: freqDomLen
                FastFourierTransformer ffter4 = new FastFourierTransformer(DftNormalization.UNITARY);
                //@?? INVERSE?
                Complex[] freqDom4 = ffter4.transform(sig, TransformType.INVERSE);
                double[] ampInFreq4 = new double[freqDomLen];
                for (int i = 0; i < freqDomLen; i++) {
                    ampInFreq4[i] = freqDom4[i + 1].abs();
                }

                /**
                 * here to select peak
                 */

                //find peak
                int freqIndex4 = findPeakIndex(ampInFreq4);

                //get the final result and pass it back to upper layer
                //heartRate: the measurement result of heart rate
                int heartRate4 = (int) Math.ceil(freqIndex4 * freqResolution * 60);
                //FFT/***********************************/



                //output result for debug
                //Log.d("sunjc-debug","freq"+heartRate);
                textDisp.setText("Heart Rate: " + heartRate+"\n"+heartRate2+"\n"+heartRate3+"\n"+heartRate4);

                //draw real-time figure
                figureAbove.set(ampInFreq2);
                figureAbove.invalidate();
                figureBelow.set(sig);
                figureBelow.invalidate();

            }
        }
    };






    /*******************************************************
     * no modifying at following part
     *********************************************************/
    private SurfaceHolder.Callback mCameraSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                //Log.i(DEBUG, "SurfaceHolder.Callback：surface Created");
                mCamera.setPreviewDisplay(mSurfaceHolder);//set the surface to be used for live preview
                mCamera.setPreviewCallback(mPreviewCallback);
            } catch (Exception ex) {
                if (null != mCamera) {
                    mCamera.release();
                    mCamera = null;
                }
                //Log.i(DEBUG + "initCamera", ex.getMessage());
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            //Log.i(DEBUG, "SurfaceHolder.Callback：Surface Changed");
            initCamera(width, height);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

            //Log.i(DEBUG, "SurfaceHolder.Callback：Surface Destroyed");
        }
    };

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
            //Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }

        startTime = System.currentTimeMillis();
    }

    @Override
    public void onPause() {
        super.onPause();
        //Log.i(DEBUG, "pause");
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

    private void initCamera(int width, int height) {//call in surfaceChanged
        //Log.i(DEBUG, "going into initCamera");
        if (null != mCamera) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                Camera.Size size = getSmallestPreviewSize(width, height, parameters);
                if (size != null) {
                    parameters.setPreviewSize(size.width, size.height);
                    //Log.d(DEBUG, "Using width=" + size.width + " height=" + size.height);
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
