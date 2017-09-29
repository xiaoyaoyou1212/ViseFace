package com.vise.face;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.FaceDetector;

import com.vise.log.ViseLog;

import java.io.ByteArrayOutputStream;

/**
 * @Description: 利用系统提供的FaceDetector实现人脸识别
 * @author: <a href="http://xiaoyaoyou1212.360doc.com">DAWI</a>
 * @date: 2017/8/10 10:14
 */
public class NormalFaceDetector<T> extends BaseFaceDetector<T> {

    private FaceDetector.Face[] mFaces;
    private byte[] mPreviewBuffer;

    public NormalFaceDetector() {
        super();
    }

    @Override
    protected void detectionFaces() {
        if (mCamera == null || mDetectorData.getFaceData() == null || mDetectorData.getFaceData().length == 0) {
            return;
        }
        /**
         * 这里需要注意，回调出来的data不是我们直接意义上的RGB图 而是YUV图，因此我们需要
         * 将YUV转化为bitmap再进行相应的人脸检测，同时注意必须使用RGB_565，才能进行人脸检测，其余无效
         */
        try {
            Camera.Size size = mCamera.getParameters().getPreviewSize();
            YuvImage yuvImage = new YuvImage(mDetectorData.getFaceData(), ImageFormat.NV21, size.width, size.height, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, size.width, size.height), 100, baos);
            mPreviewBuffer = baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mPreviewBuffer == null || mPreviewBuffer.length == 0) {
            return;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;//必须设置为565，否则无法检测
        Bitmap bitmap = BitmapFactory.decodeByteArray(mPreviewBuffer, 0, mPreviewBuffer.length, options);
        if (bitmap == null) {
            return;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        //设置各个角度的相机，这样我们的检测效果才是最好
        switch (mOrientionOfCamera) {
            case 0:
                matrix.postRotate(0.0f, width / 2, height / 2);
                break;
            case 90:
                matrix.postRotate(-270.0f, height / 2, width / 2);
                break;
            case 180:
                matrix.postRotate(-180.0f, width / 2, height / 2);
                break;
            case 270:
                matrix.postRotate(-90.0f, height / 2, width / 2);
                break;
        }
        matrix.postScale(0.2f, 0.2f);//为了减小内存压力，将图片缩放，但是也不能太小，否则检测不到人脸
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

        //初始化人脸检测
        FaceDetector detector = new FaceDetector(bitmap.getWidth(), bitmap.getHeight(), mMaxFacesCount);
        mFaces = new FaceDetector.Face[mMaxFacesCount];
        //这里通过向findFaces中传递帧图转化后的bitmap和最大检测的人脸数face，返回检测后的人脸数
        mDetectorData.setFacesCount(detector.findFaces(bitmap, mFaces));
        //绘制识别后的人脸区域的类
        getFaceRect();
        bitmap.recycle();
    }

    /**
     * 计算识别框
     */
    private void getFaceRect() {
        Rect[] faceRectList = new Rect[mDetectorData.getFacesCount()];
        Rect rect = null;
        float distance = 0;
        for (int i = 0; i < mDetectorData.getFacesCount(); i++) {
            faceRectList[i] = new Rect();
            FaceDetector.Face face = mFaces[i];
            if (face != null) {
                float eyeDistance = face.eyesDistance();
                eyeDistance = eyeDistance * mZoomRatio;
                if (eyeDistance > distance) {
                    distance = eyeDistance;
                    rect = faceRectList[i];
                }
                PointF midEyesPoint = new PointF();
                face.getMidPoint(midEyesPoint);
                midEyesPoint.x = midEyesPoint.x * mZoomRatio;
                midEyesPoint.y = midEyesPoint.y * mZoomRatio;
                ViseLog.i("eyeDistance:" + eyeDistance + ",midEyesPoint.x:" + midEyesPoint.x
                        + ",midEyesPoint.y:" + midEyesPoint.y);
                faceRectList[i].set((int) (midEyesPoint.x - eyeDistance),
                        (int) (midEyesPoint.y - eyeDistance),
                        (int) (midEyesPoint.x + eyeDistance),
                        (int) (midEyesPoint.y + eyeDistance));
                ViseLog.i("FaceRectList[" + i + "]:" + faceRectList[i]);
            }
        }
        mDetectorData.setLightIntensity(FaceUtil.getYUVLight(mDetectorData.getFaceData(), rect, mCameraWidth));
        mDetectorData.setFaceRectList(faceRectList);
        if (mCameraWidth > 0) {
            mDetectorData.setDistance(distance * 2 / mCameraWidth);
        }
    }
}
