package com.vise.facedemo;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.text.TextUtils;

import com.iflytek.cloud.FaceDetector;
import com.iflytek.cloud.util.Accelerometer;
import com.vise.face.BaseFaceDetector;
import com.vise.log.ViseLog;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * @Description: 讯飞人脸识别
 * @author: <a href="http://xiaoyaoyou1212.360doc.com">DAWI</a>
 * @date: 2017/8/11 18:18
 */
public class XunFeiFaceDetector<T> extends BaseFaceDetector<T> {

    // 加速度感应器，用于获取手机的朝向
    private Accelerometer mAcc;
    // FaceDetector对象，集成了离线人脸识别：人脸检测、视频流检测功能
    private FaceDetector mFaceDetector;

    public XunFeiFaceDetector(Context context) {
        super();
        mAcc = new Accelerometer(context);
        mAcc.start();
        mFaceDetector = FaceDetector.createDetector(context, null);
    }

    /**
     * 离线人脸框结果解析方法
     * @param json
     * @return
     */
    private Rect[] parseResult(String json){
        Rect[] rect = null;
        if(TextUtils.isEmpty(json)) {
            return null;
        }
        try {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);
            int ret = joResult.optInt("ret");
            if(ret != 0) {
                return null;
            }
            // 获取每个人脸的结果
            JSONArray items = joResult.getJSONArray("face");
            // 获取人脸数目
            rect = new Rect[items.length()];
            for(int i=0; i<items.length(); i++) {

                JSONObject position = items.getJSONObject(i).getJSONObject("position");
                // 提取关键点数据
                rect[i].left = position.getInt("left");
                rect[i].top = position.getInt("top");
                rect[i].right = position.getInt("right");
                rect[i].bottom = position.getInt("bottom");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rect;
    }

    @Override
    protected void detectionFaces() {
        // 获取手机朝向，返回值0,1,2,3分别表示0,90,180和270度
        int direction = Accelerometer.getDirection();
        boolean frontCamera = (Camera.CameraInfo.CAMERA_FACING_FRONT == mCameraId);
        // 前置摄像头预览显示的是镜像，需要将手机朝向换算成摄相头视角下的朝向。
        // 转换公式：a' = (360 - a)%360，a为人眼视角下的朝向（单位：角度）
        if (frontCamera) {
            // SDK中使用0,1,2,3,4分别表示0,90,180,270和360度
            direction = (4 - direction) % 4;
        }

        if (mFaceDetector == null) {
            /**
             * 离线视频流检测功能需要单独下载支持离线人脸的SDK
             * 请开发者前往语音云官网下载对应SDK
             */
            // 创建单例失败，与 21001 错误为同样原因，参考http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
            ViseLog.e("创建对象失败，请确认 libmsc.so 放置正确，\n 且有调用 createUtility 进行初始化");
            return;
        }

        String result = mFaceDetector.trackNV21(mDetectorData.getFaceData(), mPreviewWidth, mPreviewHeight, 0, direction);
        ViseLog.d("result:" + result);

        Rect[] rects = parseResult(result);
        if (rects != null) {
            mDetectorData.setFacesCount(rects.length);
            mDetectorData.setFaceRectList(rects);
        }
    }
}
