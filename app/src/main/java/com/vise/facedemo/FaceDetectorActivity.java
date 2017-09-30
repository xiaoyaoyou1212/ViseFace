package com.vise.facedemo;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.vise.face.CameraPreview;
import com.vise.face.DetectorData;
import com.vise.face.DetectorProxy;
import com.vise.face.FaceRectView;
import com.vise.face.ICameraCheckListener;
import com.vise.face.IDataListener;
import com.vise.face.IFaceDetector;
import com.vise.face.NormalFaceDetector;
import com.vise.log.ViseLog;

/**
 * @Description: 拍照测肤界面
 * @author: <a href="http://xiaoyaoyou1212.360doc.com">DAWI</a>
 * @date: 2017/8/15 11:12
 */
public class FaceDetectorActivity extends Activity {

    private Context mContext;
    private CameraPreview mFace_detector_preview;
    private FaceRectView mFace_detector_face;

    private DetectorProxy mDetectorProxy;
    private IFaceDetector mFaceDetector;

    private ICameraCheckListener mCameraCheckListener = new ICameraCheckListener() {
        @Override
        public void checkPermission(boolean isAllow) {
            ViseLog.i("checkPermission" + isAllow);
        }

        @Override
        public void checkPixels(long pixels, boolean isSupport) {
            ViseLog.i("checkPixels" + pixels);
        }
    };

    private IDataListener mDataListener = new IDataListener() {
        @Override
        public void onDetectorData(DetectorData detectorData) {
            ViseLog.i("识别数据:" + detectorData);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.getIntent().getBooleanExtra("isSdk", false)) {
            mFaceDetector = new XunFeiFaceDetector(this);
        } else {
            mFaceDetector = new NormalFaceDetector();
        }
        setContentView(R.layout.activity_face_detector);
        mContext = this;
        init();
    }

    protected void init() {
        mFace_detector_preview = (CameraPreview) findViewById(R.id.face_detector_preview);
        mFace_detector_face = (FaceRectView) findViewById(R.id.face_detector_face);
        mFace_detector_face.setZOrderOnTop(true);
        mFace_detector_face.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        // 点击SurfaceView，切换摄相头
        mFace_detector_preview.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 只有一个摄相头，不支持切换
                if (Camera.getNumberOfCameras() == 1) {
                    return;
                }
                if (mDetectorProxy == null) {
                    return;
                }
                mDetectorProxy.closeCamera();
                if (Camera.CameraInfo.CAMERA_FACING_FRONT == mDetectorProxy.getCameraId()) {
                    mDetectorProxy.setCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
                } else {
                    mDetectorProxy.setCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);
                }
                mDetectorProxy.openCamera();
            }
        });

        mDetectorProxy = new DetectorProxy.Builder(mFace_detector_preview)
                .setCheckListener(mCameraCheckListener)
                .setFaceDetector(mFaceDetector)
                .setDataListener(mDataListener)
                .setFaceRectView(mFace_detector_face)
                .setDrawFaceRect(true)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDetectorProxy != null) {
            mDetectorProxy.detector();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mDetectorProxy != null) {
            mDetectorProxy.release();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
    }

}
