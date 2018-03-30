package com.vise.facedemo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.vise.xsnow.permission.OnPermissionCallback;
import com.vise.xsnow.permission.PermissionManager;

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
        this.mContext = this;
        init();
    }

    private void init() {
        mFace_detector_demo = (Button) findViewById(R.id.face_detector_demo);
        mFace_detector_demo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PermissionManager.instance().with((Activity) mContext).request(new OnPermissionCallback() {
                        @Override
                        public void onRequestAllow(String permissionName) {
                            Intent intent = new Intent(mContext, FaceDetectorActivity.class);
                            startActivity(intent);
                        }

                        @Override
                        public void onRequestRefuse(String permissionName) {
                            Toast.makeText(mContext, "权限申请被拒绝，请重试！", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onRequestNoAsk(String permissionName) {
                            Toast.makeText(mContext, "权限申请被拒绝且不再询问，请进入设置打开权限再试！", Toast.LENGTH_SHORT).show();
                        }
                    }, Manifest.permission.CAMERA);
                } else {
                    Intent intent = new Intent(mContext, FaceDetectorActivity.class);
                    startActivity(intent);
                }
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PermissionManager.instance().with((Activity) mContext).request(new OnPermissionCallback() {
                @Override
                public void onRequestAllow(String permissionName) {
                }

                @Override
                public void onRequestRefuse(String permissionName) {
                    Toast.makeText(mContext, "权限申请被拒绝，请重试！", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onRequestNoAsk(String permissionName) {
                    Toast.makeText(mContext, "权限申请被拒绝且不再询问，请进入设置打开权限再试！", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }
}
