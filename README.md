# ViseFace
简易人脸检测库，不依赖三方库，可快速接入人脸检测功能。

[![License](https://img.shields.io/badge/License-Apache--2.0-green.svg)](https://github.com/xiaoyaoyou1212/ViseFace/blob/master/LICENSE) [![API](https://img.shields.io/badge/API-9%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=9)

- 项目地址：[https://github.com/xiaoyaoyou1212/ViseFace](https://github.com/xiaoyaoyou1212/ViseFace)

- 项目依赖：`compile 'com.vise.xiaoyaoyou:viseface:1.1.0'`

### 版本说明
[![LatestVersion](https://img.shields.io/badge/LatestVersion-1.1.0-orange.svg)](https://github.com/xiaoyaoyou1212/ViseFace/blob/master/VERSION.md)

### 代码托管
[![JCenter](https://img.shields.io/badge/JCenter-1.1.0-orange.svg)](https://jcenter.bintray.com/com/vise/xiaoyaoyou/viseface/1.1.0/)

### 为什么打造该库
1、想简单快速接入人脸检测功能；

2、Google 提供的人脸检测功能部分手机无法适配；

3、第三方提供的人脸检测功能接入门槛过高；

4、依赖第三方库会增加 APK 大小。

### 功能介绍
1、可快速识别人脸；

2、可适配所有机型；

3、可配置最大检测人脸数；

4、可配置是否显示人脸检测框；

5、可配置当前检测人脸摄像头为前置和后置；

6、可检测到最近人脸范围的光照值，光照范围 0 - 255；

7、可检测到的最近人脸相对于屏幕宽度的比例。

### 效果演示
![效果演示](http://img.blog.csdn.net/20171009161954864)

### 使用介绍
1、导入人脸检测库
在工程的 build 文件中添加如下依赖：
`compile 'com.vise.xiaoyaoyou:viseface:1.1.0'`

2、创建相机预览布局
```
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
	>
	<!--相机预览界面，必须设置-->
	<com.vise.face.CameraPreview
	    android:id="@+id/face_detector_preview"
	    android:layout_width="match_parent"
	    android:layout_height="match_parent"/>
	<!--绘制人脸识别框，可依需配置-->
	<com.vise.face.FaceRectView
	    android:id="@+id/face_detector_face"
	    android:layout_width="match_parent"
	    android:layout_height="match_parent"/>
	<!--拍照按钮，点击后进行拍照，按照需要进行添加-->
	<Button
		android:id="@+id/face_detector_take_photo"
		android:layout_width="100dp"
		android:layout_height="100dp"
		android:layout_marginBottom="30dp"
		android:layout_gravity="bottom|center_horizontal"
		android:background="@android:drawable/ic_menu_camera"
		/>
</FrameLayout>
```
注意：最外层布局必须是 `FrameLayout`；如果代码中配置需要绘制人脸检测框，那么布局必须添加 `FaceRectView`。

3、创建人脸检测实现对象
```
IFaceDetector mFaceDetector = new NormalFaceDetector();
```

4、创建权限检查监听
```
ICameraCheckListener mCameraCheckListener = new ICameraCheckListener() {
    @Override
    public void checkPermission(boolean isAllow) {
    	//权限是否允许
        ViseLog.i("checkPermission" + isAllow);
    }

    @Override
    public void checkPixels(long pixels, boolean isSupport) {
    	//手机像素是否满足要求
        ViseLog.i("checkPixels" + pixels);
    }
};
```

5、创建检测数据监听
```
IDataListener mDataListener = new IDataListener() {
    @Override
    public void onDetectorData(DetectorData detectorData) {
    	//回调识别到的数据
        ViseLog.i("识别数据:" + detectorData);
    }
};
```

6、设置相关配置，创建人脸检测代理
该库的核心思想就是快速接入人脸检测功能，所以该库的功能都是通过 `DetectorProxy` 代理类来实现，使用简单明了。具体使用场景如下：
```
//创建代理类，必须传入相机预览界面
DetectorProxy mDetectorProxy = new DetectorProxy.Builder(mFace_detector_preview)
				//设置权限检查监听
                .setCheckListener(mCameraCheckListener)
                //设置人脸检测实现
                .setFaceDetector(mFaceDetector)
                //设置检测数据回调监听
                .setDataListener(mDataListener)
                //设置绘制人脸识别框界面
                .setFaceRectView(mFace_detector_face)
                //设置是否绘制人脸检测框
                .setDrawFaceRect(true)
                //设置预览相机的相机ID
                .setCameraId(Camera.CameraInfo.CAMERA_FACING_BACK)
                //设置可检测的最大人脸数
                .setMaxFacesCount(5)
                //设置人脸识别框是否为完整矩形
                .setFaceIsRect(false)
                //设置人脸识别框的RGB颜色
                .setFaceRectColor(Color.rgb(255, 203, 15))
                //创建代理类
                .build();
```

7、开启人脸检测
```
if (mDetectorProxy != null) {
    mDetectorProxy.detector();
}
```

8、释放资源
```
if (mDetectorProxy != null) {
    mDetectorProxy.release();
}
```

### 关于我
[![Website](https://img.shields.io/badge/Website-huwei-blue.svg)](http://www.huwei.tech/)

[![GitHub](https://img.shields.io/badge/GitHub-xiaoyaoyou1212-blue.svg)](https://github.com/xiaoyaoyou1212)

[![CSDN](https://img.shields.io/badge/CSDN-xiaoyaoyou1212-blue.svg)](http://blog.csdn.net/xiaoyaoyou1212)

### 最后
如果觉得该项目有帮助，请点下Star，您的支持是我开源的动力。如果有好的想法和建议，也欢迎Fork项目参与进来。使用中如果有任何问题和建议都可以进群交流，QQ群二维码如下：

![QQ群](https://github.com/xiaoyaoyou1212/XSnow/blob/master/screenshot/qq_chat_first.png)
(此群已满)

![QQ群](https://github.com/xiaoyaoyou1212/XSnow/blob/master/screenshot/qq_chat_second.png)

