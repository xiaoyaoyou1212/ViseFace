package com.vise.face;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Process;
import android.view.Surface;
import android.widget.FrameLayout;

import com.vise.log.ViseLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * @Description: 工具类
 * @author: <a href="http://xiaoyaoyou1212.360doc.com">DAWI</a>
 * @date: 2017/8/9 14:54
 */
public class FaceUtil {

    public static int getYUVLight(byte[] data, Rect rect, int width) {
        if (data == null || rect == null) {
            return 0;
        }
        int sum = 0;
        int index = 0;
        if (rect.top < 0 || rect.left < 0 || rect.left > width || rect.right > width) {
            return 0;
        }
        try {
            for (int i = rect.left; i < rect.right; ) {
                for (int j = rect.top; j < rect.bottom; ) {
                    sum += (0xFF & data[i * width + j]);
                    j += 100;
                    index++;
                }
                i += 100;
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        if (sum == 0 || index == 0) {
            return 0;
        }
        return sum / index;
    }

    public static void drawFaceRect(Canvas canvas, Rect rect, int rectColor, boolean drawRect) {
        if (canvas == null) {
            return;
        }

        Paint paint = new Paint();
        paint.setColor(rectColor);
        float len = (rect.bottom - rect.top) / 8;
        if (len / 8 >= 2) paint.setStrokeWidth(len / 8);
        else paint.setStrokeWidth(2);

        if (drawRect) {
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(rect, paint);
        } else {
            float drawl = rect.left - len;
            float drawr = rect.right + len;
            float drawu = rect.top - len;
            float drawd = rect.bottom + len;

            canvas.drawLine(drawl, drawd, drawl, drawd - len, paint);
            canvas.drawLine(drawl, drawd, drawl + len, drawd, paint);
            canvas.drawLine(drawr, drawd, drawr, drawd - len, paint);
            canvas.drawLine(drawr, drawd, drawr - len, drawd, paint);
            canvas.drawLine(drawl, drawu, drawl, drawu + len, paint);
            canvas.drawLine(drawl, drawu, drawl + len, drawu, paint);
            canvas.drawLine(drawr, drawu, drawr, drawu + len, paint);
            canvas.drawLine(drawr, drawu, drawr - len, drawu, paint);
        }
    }

    /**
     * 找出最大像素组合
     *
     * @param cameraSizes
     * @return
     */
    public static Camera.Size findMaxCameraSize(List<Camera.Size> cameraSizes) {
        // 按照分辨率从大到小排序
        List<Camera.Size> supportedResolutions = new ArrayList(cameraSizes);
        Collections.sort(supportedResolutions, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {
                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;
                if (bPixels < aPixels) {
                    return -1;
                }
                if (bPixels > aPixels) {
                    return 1;
                }
                return 0;
            }
        });
        return supportedResolutions.get(0);
    }

    /**
     * 找出最适合的分辨率
     *
     * @return
     */
    public static Point findBestResolution(List<Camera.Size> cameraSizes, Point screenResolution, boolean isPictureSize, float maxDistortion) {
        Point defaultResolution = new Point();
        if (isPictureSize) {
            defaultResolution.x = 2000;
            defaultResolution.y = 1500;
        } else {
            defaultResolution.x = 1920;
            defaultResolution.y = 1080;
        }
        if (cameraSizes == null) {
            ViseLog.w("Device returned no supported preview sizes; using default");
            return defaultResolution;
        }

        // 按照分辨率从大到小排序
        List<Camera.Size> supportedResolutions = new ArrayList(cameraSizes);
        Collections.sort(supportedResolutions, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {
                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;
                if (bPixels < aPixels) {
                    return -1;
                }
                if (bPixels > aPixels) {
                    return 1;
                }
                return 0;
            }
        });

        if (supportedResolutions.size() > 0) {
            defaultResolution.x = supportedResolutions.get(0).width;
            defaultResolution.y = supportedResolutions.get(0).height;
        }

        // 移除不符合条件的分辨率
        double screenAspectRatio = (double) screenResolution.x / (double) screenResolution.y;
        Iterator<Camera.Size> it = supportedResolutions.iterator();
        while (it.hasNext()) {
            Camera.Size supportedResolution = it.next();
            int width = supportedResolution.width;
            int height = supportedResolution.height;
            // 移除低于下限的分辨率，尽可能取高分辨率
            if (isPictureSize) {
                if (width * height < 2000 * 1500) {
                    it.remove();
                    continue;
                }
            } else {
                if (width * height < 1280 * 720) {
                    it.remove();
                    continue;
                }
            }

            /**
             * 在camera分辨率与屏幕分辨率宽高比不相等的情况下，找出差距最小的一组分辨率
             * 由于camera的分辨率是width>height，我们设置的portrait模式中，width<height
             * 因此这里要先交换然preview宽高比后在比较
             */
            boolean isCandidatePortrait = width > height;
            int maybeFlippedWidth = isCandidatePortrait ? height : width;
            int maybeFlippedHeight = isCandidatePortrait ? width : height;
            double aspectRatio = (double) maybeFlippedWidth / (double) maybeFlippedHeight;
            double distortion = Math.abs(aspectRatio - screenAspectRatio);
            if (distortion > maxDistortion) {
                it.remove();
                continue;
            }

            // 找到与屏幕分辨率完全匹配的预览界面分辨率直接返回
            if (maybeFlippedWidth == screenResolution.x && maybeFlippedHeight == screenResolution.y) {
                Point exactPoint = new Point(width, height);
                ViseLog.d("found preview resolution exactly matching screen resolutions: " + exactPoint);
                return exactPoint;
            }
        }

        // 如果没有找到合适的，并且还有候选的像素，则设置其中最大尺寸的，对于配置比较低的机器不太合适
        if (!supportedResolutions.isEmpty()) {
            Camera.Size largestPreview = supportedResolutions.get(0);
            Point largestSize = new Point(largestPreview.width, largestPreview.height);
            ViseLog.d("using largest suitable preview resolution: " + largestSize);
            return largestSize;
        }

        // 没有找到合适的，就返回默认的
        ViseLog.d("No suitable preview resolutions, using default: " + defaultResolution);
        return defaultResolution;
    }

    /**
     * 检查相机权限
     *
     * @param context
     * @return
     */
    public static boolean checkCameraPermission(Context context) {
        if (context != null) {
            int status = context.checkPermission(Manifest.permission.CAMERA, Process.myPid(), Process.myUid());
            if (PackageManager.PERMISSION_GRANTED == status) {
                return true;
            }
        }
        return false;
    }

    /**
     * 在摄像头启动前设置参数
     *
     * @param cameraPreview
     * @param faceDetector
     * @param camera
     * @param cameraId
     * @param width
     * @param height
     * @param <T>
     */
    public static <T> int setCameraParams(CameraPreview cameraPreview, IFaceDetector<T> faceDetector, Camera camera,
                                          int cameraId, int width, int height) {
        if (camera == null) {
            return 0;
        }
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
        if (cameraPreview != null) {
            if (tempW >= width) {
                cameraPreview.setLayoutParams(new FrameLayout.LayoutParams(tempW, height));
                scale = tempW / h;
            } else if (tempH >= height) {
                cameraPreview.setLayoutParams(new FrameLayout.LayoutParams(width, tempH));
                scale = tempH / w;
            } else {
                cameraPreview.setLayoutParams(new FrameLayout.LayoutParams(width, height));
            }
        }
        if (faceDetector != null) {
            faceDetector.setZoomRatio(5f * scale);
            faceDetector.setPreviewWidth((int) w);
            faceDetector.setPreviewHeight((int) h);
        }

        parameters.setJpegQuality(100);
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            // 连续对焦
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        camera.cancelAutoFocus();
        int displayOrientation = setCameraDisplayOrientation(cameraPreview.getContext(), faceDetector, camera, cameraId);
        camera.setParameters(parameters);
        return displayOrientation;
    }

    /**
     * 设置相机显示方向
     *
     * @param context
     * @param faceDetector
     * @param camera
     * @param cameraId
     * @param <T>
     * @return
     */
    public static <T> int setCameraDisplayOrientation(Context context, IFaceDetector<T> faceDetector, Camera camera, int cameraId) {
        if (context == null) {
            return 0;
        }
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = ((Activity) context).getWindowManager().getDefaultDisplay().getRotation();
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
        if (faceDetector != null) {
            faceDetector.setOrientionOfCamera(info.orientation);
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degree) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degree + 360) % 360;
        }
        if (camera != null) {
            camera.setDisplayOrientation(result);
        }
        return result;
    }

}
