package com.vise.facedemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.vise.log.ViseLog;
import com.vise.log.inner.LogcatTree;

/**
 * @Description: 主页
 * @author: <a href="http://xiaoyaoyou1212.360doc.com">DAWI</a>
 * @date: 2017/8/2 14:26
 */
public class MainActivity extends Activity {

    private Context mContext;
    private Button mFace_detector_demo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViseLog.plant(new LogcatTree());
        this.mContext = this;
        init();
    }

    private void init() {
        mFace_detector_demo = (Button) findViewById(R.id.face_detector_demo);
        mFace_detector_demo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, FaceDetectorActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }
}
