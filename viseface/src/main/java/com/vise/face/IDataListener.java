package com.vise.face;

/**
 * @Description: 识别数据监听
 * @author: <a href="http://xiaoyaoyou1212.360doc.com">DAWI</a>
 * @date: 2017/8/10 11:32
 */
public interface IDataListener<T> {
    void onDetectorData(DetectorData<T> detectorData);
}
