package com.vise.face;

import android.hardware.Camera;

/**
 * @Description: 识别抽象类
 * @author: <a href="http://xiaoyaoyou1212.360doc.com">DAWI</a>
 * @date: 2017/8/11 17:28
 */
public abstract class BaseFaceDetector<T> implements IFaceDetector<T>, Runnable {

    private Thread mThread;
    private boolean mStopTrack;
    private IDataListener<T> mDataListener;

    protected DetectorData<T> mDetectorData;
    protected Camera mCamera;
    protected int mCameraId;
    protected float mZoomRatio;//缩放比例
    protected int mCameraWidth;
    protected int mCameraHeight;
    protected int mPreviewWidth;
    protected int mPreviewHeight;
    protected int mOrientionOfCamera;
    protected int mMaxFacesCount;
    protected boolean mOpenCamera = false;

    public BaseFaceDetector() {
        mDetectorData = new DetectorData<>();
    }

    @Override
    public void run() {
        mStopTrack = false;
        while (!mStopTrack) {
            if (!mOpenCamera) {
                continue;
            }
            detectionFaces();
            if (mDataListener != null) {
                mDataListener.onDetectorData(mDetectorData);
            }
        }
    }

    protected abstract void detectionFaces();

    /**
     * 开启识别
     */
    @Override
    public void detector() {
        mThread = new Thread(this);
        mThread.start();
    }

    /**
     * 释放资源
     */
    @Override
    public void release() {
        if (mDetectorData != null) {
            mDetectorData.setFaceData(null);
        }
        mStopTrack = true;
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
    }

    /**
     * 设置检测监听
     *
     * @param mDataListener
     */
    @Override
    public void setDataListener(IDataListener<T> mDataListener) {
        this.mDataListener = mDataListener;
    }

    /**
     * 设置预览数据
     *
     * @param data
     * @param camera
     */
    @Override
    public void setCameraPreviewData(byte[] data, Camera camera) {
        if (mDetectorData != null) {
            mDetectorData.setFaceData(data);
        }
        mCamera = camera;
    }

    /**
     * 设置识别最大人脸数量
     *
     * @param mMaxFacesCount
     * @return
     */
    @Override
    public void setMaxFacesCount(int mMaxFacesCount) {
        this.mMaxFacesCount = mMaxFacesCount;
    }

    /**
     * 设置相机高度
     *
     * @param mCameraHeight 相机高度
     * @return
     */
    @Override
    public void setCameraHeight(int mCameraHeight) {
        this.mCameraHeight = mCameraHeight;
    }

    /**
     * 设置相机宽度
     *
     * @param mCameraWidth 相机宽度
     * @return
     */
    @Override
    public void setCameraWidth(int mCameraWidth) {
        this.mCameraWidth = mCameraWidth;
    }

    /**
     * 设置相机方向
     *
     * @param mOrientionOfCamera 相机方向
     * @return
     */
    @Override
    public void setOrientionOfCamera(int mOrientionOfCamera) {
        this.mOrientionOfCamera = mOrientionOfCamera;
    }

    /**
     * 设置缩放比例
     *
     * @param mZoomRatio
     * @return
     */
    @Override
    public void setZoomRatio(float mZoomRatio) {
        this.mZoomRatio = mZoomRatio;
    }

    /**
     * 设置相机是否打开
     *
     * @param isOpenCamera
     */
    @Override
    public void setOpenCamera(boolean isOpenCamera) {
        this.mOpenCamera = isOpenCamera;
    }

    /**
     * 设置相机ID
     *
     * @param mCameraId
     */
    @Override
    public void setCameraId(int mCameraId) {
        this.mCameraId = mCameraId;
    }

    /**
     * 设置预览高度
     *
     * @param mPreviewHeight
     */
    @Override
    public void setPreviewHeight(int mPreviewHeight) {
        this.mPreviewHeight = mPreviewHeight;
    }

    /**
     * 设置预览宽度
     *
     * @param mPreviewWidth
     */
    @Override
    public void setPreviewWidth(int mPreviewWidth) {
        this.mPreviewWidth = mPreviewWidth;
    }
}
