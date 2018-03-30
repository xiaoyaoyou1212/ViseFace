package com.vise.face;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.vise.log.ViseLog;

/**
 * @Description: 自定义相机
 * @author: <a href="http://xiaoyaoyou1212.360doc.com">DAWI</a>
 * @date: 2017/8/5 16:24
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private int mCameraId;
    private long mMinCameraPixels;
    private ICameraCheckListener mCheckListener;
    private IFaceDetector mFaceDetector;
    private int mDisplayOrientation;
    private int mCameraWidth;
    private int mCameraHeight;

    public CameraPreview(Context context) {
        super(context);
        init(context);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CameraPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mHolder = getHolder();
        mHolder.addCallback(this);

        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mCameraWidth = metrics.widthPixels;
        mCameraHeight = metrics.heightPixels;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        openCamera();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        closeCamera();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    }

    /**
     * 打开相机
     */
    public void openCamera() {
        if (null != mCamera) {
            return;
        }

        if (!FaceUtil.checkCameraPermission(getContext())) {
            ViseLog.i("摄像头权限未打开，请打开后再试");
            if (mCheckListener != null) {
                mCheckListener.checkPermission(false);
            }
            return;
        }

        // 只有一个摄相头，打开后置
        if (Camera.getNumberOfCameras() == 1) {
            mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }

        if (mFaceDetector != null) {
            mFaceDetector.setCameraId(mCameraId);
        }

        try {
            mCamera = Camera.open(mCameraId);
            // setParameters 针对部分手机通过Camera.open()拿到的Camera对象不为null
            Camera.Parameters mParameters = mCamera.getParameters();
            mCamera.setParameters(mParameters);
            if (Camera.CameraInfo.CAMERA_FACING_FRONT == mCameraId) {
                ViseLog.i("前置摄像头已开启");
            } else {
                ViseLog.i("后置摄像头已开启");
            }
        } catch (Exception e) {
            e.printStackTrace();

            //回调权限判定结果
            if (mCheckListener != null) {
                mCheckListener.checkPermission(false);
            }

            //关闭相机
            closeCamera();
            return;
        }

        if (mCamera == null || mHolder == null || mHolder.getSurface() == null) {
            return;
        }

        //回调权限判定结果
        if (mCheckListener != null) {
            mCheckListener.checkPermission(true);
        }

        long pixels = 0;
        try {
            //获取最大宽高，得出最大支持像素
            Camera.Parameters parameters = mCamera.getParameters();
            Camera.Size maxPictureSize = FaceUtil.findMaxCameraSize(parameters.getSupportedPictureSizes());
            if (maxPictureSize != null) {
                pixels = maxPictureSize.width * maxPictureSize.height;
            }
            ViseLog.i("camera max support pixels: " + pixels);
            //回调该手机像素值
            if (mCheckListener != null && mMinCameraPixels > 0) {
                if (pixels >= mMinCameraPixels) {
                    mCheckListener.checkPixels(pixels, true);
                } else {
                    closeCamera();
                    mCheckListener.checkPixels(pixels, false);
                    return;
                }
            }

            //设置预览回调
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {

                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    if (mFaceDetector != null) {
                        mFaceDetector.setCameraPreviewData(data, camera);
                        mFaceDetector.setOpenCamera(true);
                    }
                }
            });
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            mCamera.setPreviewDisplay(mHolder);
            ViseLog.i("camera size width:" + mCameraWidth + ",height:" + mCameraHeight);
            if (mFaceDetector != null) {
                mFaceDetector.setCameraWidth(mCameraWidth);
                mFaceDetector.setCameraHeight(mCameraHeight);
            }
            //设置相机参数
            mDisplayOrientation = FaceUtil.setCameraParams(this, mFaceDetector, mCamera, mCameraId, mCameraWidth, mCameraHeight);
            ViseLog.i("camera getPreviewSize width:" + mCamera.getParameters().getPreviewSize().width
                    + ",height:" + mCamera.getParameters().getPreviewSize().height);
            ViseLog.i("camera getPictureSize width:" + mCamera.getParameters().getPictureSize().width
                    + ",height:" + mCamera.getParameters().getPictureSize().height);
            //开始预览
            mCamera.startPreview();
        } catch (Exception e) {
            closeCamera();
            mCheckListener.checkPixels(pixels, false);
            ViseLog.d("Error starting camera preview: " + e.getMessage());
        }
    }

    /**
     * 关闭相机
     */
    public void closeCamera() {
        if (mFaceDetector != null) {
            mFaceDetector.setOpenCamera(false);
        }
        if (null != mCamera) {
            try {
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        closeCamera();
        if (mFaceDetector != null) {
            mFaceDetector.release();
        }
    }

    public Camera getCamera() {
        return mCamera;
    }

    public CameraPreview setFaceDetector(IFaceDetector mFaceDetector) {
        this.mFaceDetector = mFaceDetector;
        return this;
    }

    public CameraPreview setCheckListener(ICameraCheckListener mCheckListener) {
        this.mCheckListener = mCheckListener;
        return this;
    }

    public int getCameraId() {
        return mCameraId;
    }

    public CameraPreview setCameraId(int mCameraId) {
        this.mCameraId = mCameraId;
        return this;
    }

    public CameraPreview setMinCameraPixels(long mMinCameraPixels) {
        this.mMinCameraPixels = mMinCameraPixels;
        return this;
    }

    public int getCameraHeight() {
        return mCameraHeight;
    }

    public int getCameraWidth() {
        return mCameraWidth;
    }

    public int getDisplayOrientation() {
        return mDisplayOrientation;
    }

}
