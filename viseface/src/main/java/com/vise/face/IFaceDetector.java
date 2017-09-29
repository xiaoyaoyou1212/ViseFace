package com.vise.face;

import android.hardware.Camera;

/**
 * @Description: 识别接口
 * @author: <a href="http://xiaoyaoyou1212.360doc.com">DAWI</a>
 * @date: 2017/8/10 10:12
 */
public interface IFaceDetector<T> {
    void detector();
    void release();
    void setDataListener(IDataListener<T> mDataListener);
    void setCameraPreviewData(byte[] data, Camera camera);
    void setMaxFacesCount(int mMaxFacesCount);
    void setCameraHeight(int mCameraHeight);
    void setCameraWidth(int mCameraWidth);
    void setOrientionOfCamera(int mOrientionOfCamera);
    void setZoomRatio(float mZoomRatio);
    void setOpenCamera(boolean isOpenCamera);
}
