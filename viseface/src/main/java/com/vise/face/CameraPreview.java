package com.vise.face;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.os.Process;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import com.vise.log.ViseLog;

import java.util.List;

/**
 * @Description: 自定义相机
 * @author: <a href="http://xiaoyaoyou1212.360doc.com">DAWI</a>
 * @date: 2017/8/5 16:24
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private ICameraCheckListener mCheckListener;
    private IFaceDetector mFaceDetector;
    private int mDisplayOrientation;
    private int mWidth;
    private int mHeight;

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
        mWidth = metrics.widthPixels;
        mHeight = metrics.heightPixels;
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

        if (!checkCameraPermission()) {
            ViseLog.i("摄像头权限未打开，请打开后再试");
            return;
        }

        // 只有一个摄相头，打开后置
        if (Camera.getNumberOfCameras() == 1) {
            mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
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

        try {
            //获取最大宽高，得出最大支持像素
            Camera.Parameters parameters = mCamera.getParameters();
            List<Camera.Size> pictureSizeList = parameters.getSupportedPictureSizes();
            long pixels;
            if (pictureSizeList.get(0).height < pictureSizeList.get(pictureSizeList.size() - 1).height) {
                pixels = pictureSizeList.get(pictureSizeList.size() - 1).width * pictureSizeList.get(pictureSizeList.size() - 1).height;
            } else {
                pixels = pictureSizeList.get(0).width * pictureSizeList.get(0).height;
            }
            //回调该手机像素值
            if (mCheckListener != null) {
                if (pixels > 300 * 10000) {
                    mCheckListener.checkPixels(pixels, true);
                } else {
                    mCheckListener.checkPixels(pixels, false);
                    closeCamera();
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
            ViseLog.i("camera size width:" + mWidth + ",height:" + mHeight);
            if (mFaceDetector != null) {
                mFaceDetector.setCameraWidth(mWidth);
                mFaceDetector.setCameraHeight(mHeight);
            }
            //设置相机参数
            setCameraParams(mCamera, mWidth, mHeight);
            ViseLog.i("camera getPreviewSize width:" + mCamera.getParameters().getPreviewSize().width
                    + ",height:" + mCamera.getParameters().getPreviewSize().height);
            ViseLog.i("camera getPictureSize width:" + mCamera.getParameters().getPictureSize().width
                    + ",height:" + mCamera.getParameters().getPictureSize().height);
            //开始预览
            mCamera.startPreview();
        } catch (Exception e) {
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
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        closeCamera();
        if (mHolder != null) {
            mHolder.getSurface().release();
            mHolder = null;
        }
        if (mFaceDetector != null) {
            mFaceDetector.release();
            mFaceDetector = null;
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

    public int getDisplayOrientation() {
        return mDisplayOrientation;
    }

    /**
     * 检查相机权限
     *
     * @return
     */
    private boolean checkCameraPermission() {
        int status = getContext().checkPermission(Manifest.permission.CAMERA, Process.myPid(), Process.myUid());
        if (PackageManager.PERMISSION_GRANTED == status) {
            return true;
        }
        return false;
    }

    /**
     * 在摄像头启动前设置参数
     *
     * @param camera
     * @param width
     * @param height
     */
    private void setCameraParams(Camera camera, int width, int height) {
        // 获取摄像头支持的pictureSize列表
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> pictureSizeList = parameters.getSupportedPictureSizes();
        // 从列表中选择合适的分辨率
        Point pictureSize = FaceUtil.findBestResolution(pictureSizeList, new Point(width, height), true, 0.15f);
        // 根据选出的PictureSize重新设置SurfaceView大小
        parameters.setPictureSize(pictureSize.x, pictureSize.y);

        // 获取摄像头支持的PreviewSize列表
        List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();
        Point preSize = FaceUtil.findBestResolution(previewSizeList, new Point(width, height), false, 0.15f);
        parameters.setPreviewSize(preSize.x, preSize.y);

        float w = preSize.x;
        float h = preSize.y;
        float scale = 1.0f;
        int tempW = (int) (height * (h / w));
        int tempH = (int) (width * (w / h));
        if (tempW >= width) {
            setLayoutParams(new FrameLayout.LayoutParams(tempW, height));
            scale = tempW / h;
        } else if (tempH >= height) {
            setLayoutParams(new FrameLayout.LayoutParams(width, tempH));
            scale = tempH / w;
        } else {
            setLayoutParams(new FrameLayout.LayoutParams(width, height));
        }
        if (mFaceDetector != null) {
            mFaceDetector.setZoomRatio(5f * scale);
        }

        parameters.setJpegQuality(100);
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            // 连续对焦
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        camera.cancelAutoFocus();
        setCameraDisplayOrientation();
        camera.setParameters(parameters);
    }

    /**
     * 设置相机显示方向
     */
    private void setCameraDisplayOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraId, info);
        int rotation = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getRotation();
        int degree = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 0;
                break;
            case Surface.ROTATION_90:
                degree = 90;
                break;
            case Surface.ROTATION_180:
                degree = 180;
                break;
            case Surface.ROTATION_270:
                degree = 270;
                break;
        }
        if (mFaceDetector != null) {
            mFaceDetector.setOrientionOfCamera(info.orientation);
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degree) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degree + 360) % 360;
        }
        mDisplayOrientation = result;
        mCamera.setDisplayOrientation(result);
    }
}
