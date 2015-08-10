package sunjc.realtimecamerademo;

import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.app.Fragment;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import org.apache.commons.math3.transform.FastFourierTransformer;


public class CameraCaptureFragment extends Fragment {

    //consts
    static final String DEBUG = "SunJc_debug";

    //camera and the surfaceview
    private SurfaceView mCameraSurfacePreview;
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;
    private PowerManager.WakeLock wakeLock;

    //measurement vars
    TextView textDisp;
    private long startTime;
    String disp;

    /*************fragment and camera surface view stuff********/
    public CameraCaptureFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_camera_capture_frame, container, false);

        textDisp = (TextView) rootView.findViewById(R.id.textDisp);

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
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }

        startTime = System.currentTimeMillis();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(DEBUG,"pause");
        wakeLock.release();
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    private void initCameraSurfacePreview(View v) {
        mCameraSurfacePreview = (SurfaceView) v.findViewById(R.id.cameraPreview);
        mSurfaceHolder = mCameraSurfacePreview.getHolder();
        mSurfaceHolder.addCallback(mCameraSurfaceCallback);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
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

            int imgAvg = ImageProcessing.decodeYUV420SPtoRedAvg(data.clone(), height, width);

            Log.i(DEBUG,"Avg:"+Integer.toString(imgAvg));

            long nowTime = System.currentTimeMillis();

            long delay = nowTime - startTime;
            startTime = nowTime;

            disp += delay + ", ";
            textDisp.setText(disp);

            FastFourierTransformer

            if (imgAvg == 0 || imgAvg == 255) {
                Log.i(DEBUG,"bad imgAvg");
                return;
            }
        }
    };

}
