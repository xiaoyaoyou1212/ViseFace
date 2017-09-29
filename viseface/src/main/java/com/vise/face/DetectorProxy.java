package com.vise.face;

/**
 * @Description: 识别代理类
 * @author: <a href="http://xiaoyaoyou1212.360doc.com">DAWI</a>
 * @date: 2017/8/10 10:16
 */
public class DetectorProxy<T>{

    private IFaceDetector<T> mFaceDetector;

    /**
     * 代理类初始化，需传入人脸识别类，默认使用原生识别类，可以替换成第三方库识别类
     * @param faceDetector 人脸识别类
     * @return
     */
    public DetectorProxy<T> init(IFaceDetector<T> faceDetector) {
        if (faceDetector != null) {
            this.mFaceDetector = faceDetector;
        }
        return this;
    }

    /**
     * 开启识别
     */
    public DetectorProxy<T> detector() {
        if (mFaceDetector != null) {
            mFaceDetector.detector();
        }
        return this;
    }

    /**
     * 释放资源
     */
    public DetectorProxy<T> release() {
        if (mFaceDetector != null) {
            mFaceDetector.release();
        }
        return this;
    }

    /**
     * 设置检测监听
     * @param mDataListener
     */
    public DetectorProxy<T> setDataListener(IDataListener<T> mDataListener) {
        if (mFaceDetector != null) {
            mFaceDetector.setDataListener(mDataListener);
        }
        return this;
    }

    /**
     * 设置识别最大人脸数量
     * @param mMaxFacesCount
     * @return
     */
    public DetectorProxy<T> setMaxFacesCount(int mMaxFacesCount) {
        if (mFaceDetector != null) {
            mFaceDetector.setMaxFacesCount(mMaxFacesCount);
        }
        return this;
    }

    public static class Builder {

    }

}
