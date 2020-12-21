package com.york.media.opengl.activity;

import android.os.Bundle;


import com.york.media.opengl.R;
import com.york.media.opengl.demo.camera.api1.YCamera1View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * author : York
 * date   : 2020/12/20 17:17
 * desc   : Camera1预览界面
 */
public class Camera1Activity extends AppCompatActivity {
    private YCamera1View yCamera1View;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera1);
        yCamera1View =findViewById(R.id.yCameraView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        yCamera1View.stopPreView();
        yCamera1View.onDestroy();
    }
}
