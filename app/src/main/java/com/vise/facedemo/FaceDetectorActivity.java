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
    private CameraPreview mTake_photo_preview;
    private FaceRectView mTake_photo_face_rect;

    private DetectorData mDetectorData;
    private long mLastTime;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.getIntent().getBooleanExtra("isSdk", false)) {
            mFaceDetector = new XunFeiFaceDetector(this);
        } else {
            mFaceDetector = new NormalFaceDetector();
        }
        mDetectorProxy = new DetectorProxy();
        mDetectorProxy.init(mFaceDetector);
        setContentView(R.layout.activity_face_detector);
        mContext = this;
        init();
    }

    protected void init() {
        mTake_photo_preview = (CameraPreview) findViewById(R.id.face_detector_preview);
        mTake_photo_face_rect = (FaceRectView) findViewById(R.id.face_detector_face);
        mTake_photo_face_rect.setZOrderOnTop(true);
        mTake_photo_face_rect.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        // 点击SurfaceView，切换摄相头
        mTake_photo_preview.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 只有一个摄相头，不支持切换
                if (Camera.getNumberOfCameras() == 1) {
                    return;
                }
                mTake_photo_preview.closeCamera();
                if (mDetectorProxy != null) {
                    mDetectorProxy.release();
                }
                if (Camera.CameraInfo.CAMERA_FACING_FRONT == mTake_photo_preview.getCameraId()) {
                    mTake_photo_preview.setCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
                    mTake_photo_face_rect.setCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
                } else {
                    mTake_photo_preview.setCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);
                    mTake_photo_face_rect.setCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);
                }
                mTake_photo_preview.openCamera();
                if (mDetectorProxy != null) {
                    mDetectorProxy.detector();
                }
            }
        });

        mFaceDetector.setDataListener(new IDataListener<String>() {
            @Override
            public void onDetectorData(DetectorData<String> detectorData) {
                if (isFinishing()) {
                    return;
                }
                ViseLog.i("识别数据:" + detectorData);
                if (detectorData != null && detectorData.getFaceRectList() != null) {
                    mDetectorData = detectorData;
                    mTake_photo_face_rect.drawFaceRect(detectorData);
                }
            }
        });

        mTake_photo_preview.setCheckListener(mCameraCheckListener);
        mTake_photo_preview.setFaceDetector(mFaceDetector);
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
        System.gc();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTake_photo_preview != null) {
            mTake_photo_preview.release();
        }
        mTake_photo_preview = null;
        mDetectorProxy = null;
        System.gc();
    }

}
