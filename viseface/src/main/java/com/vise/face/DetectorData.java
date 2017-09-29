package com.vise.face;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.ArrayMap;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @Description: 识别数据
 * @author: <a href="http://xiaoyaoyou1212.360doc.com">DAWI</a>
 * @date: 2017/8/10 9:15
 */
public class DetectorData<T> implements Cloneable, Serializable {
    //原始数据
    private byte[] faceData;
    //脸部区域集合
    private Rect[] faceRectList;
    //脸部关键点集合
    private ArrayMap<String, Point[]> facePointMap;
    //图片
    private Bitmap faceBitmap;
    //光线强度
    private float lightIntensity;
    //脸部数量
    private int facesCount;
    //距离
    private float distance;
    //外部导入数据
    private T externalData;

    public float getDistance() {
        return distance;
    }

    public DetectorData setDistance(float distance) {
        this.distance = distance;
        return this;
    }

    public T getExternalData() {
        return externalData;
    }

    public DetectorData setExternalData(T externalData) {
        this.externalData = externalData;
        return this;
    }

    public Bitmap getFaceBitmap() {
        return faceBitmap;
    }

    public DetectorData setFaceBitmap(Bitmap faceBitmap) {
        this.faceBitmap = faceBitmap;
        return this;
    }

    public byte[] getFaceData() {
        return faceData;
    }

    public DetectorData setFaceData(byte[] faceData) {
        this.faceData = faceData;
        return this;
    }

    public ArrayMap<String, Point[]> getFacePointMap() {
        return facePointMap;
    }

    public DetectorData setFacePointMap(ArrayMap<String, Point[]> facePointMap) {
        this.facePointMap = facePointMap;
        return this;
    }

    public Rect[] getFaceRectList() {
        return faceRectList;
    }

    public DetectorData setFaceRectList(Rect[] faceRectList) {
        this.faceRectList = faceRectList;
        return this;
    }

    public int getFacesCount() {
        return facesCount;
    }

    public DetectorData setFacesCount(int facesCount) {
        this.facesCount = facesCount;
        return this;
    }

    public float getLightIntensity() {
        return lightIntensity;
    }

    public DetectorData setLightIntensity(float lightIntensity) {
        this.lightIntensity = lightIntensity;
        return this;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "DetectorData{" +
                "distance=" + distance +
                ", faceRectList=" + Arrays.toString(faceRectList) +
                ", facePointMap=" + facePointMap +
                ", faceBitmap=" + faceBitmap +
                ", lightIntensity=" + lightIntensity +
                ", facesCount=" + facesCount +
                ", externalData=" + externalData +
                '}';
    }
}
