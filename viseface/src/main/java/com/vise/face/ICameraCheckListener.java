package com.vise.face;

/**
 * @Description: 相机检查回调
 * @author: <a href="http://xiaoyaoyou1212.360doc.com">DAWI</a>
 * @date: 2017/8/30 16:44
 */
public interface ICameraCheckListener {
    void checkPermission(boolean isAllow);

    void checkPixels(long pixels, boolean isSupport);
}
