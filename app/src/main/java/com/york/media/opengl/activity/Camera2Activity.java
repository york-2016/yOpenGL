package com.york.media.opengl.activity;

import android.os.Bundle;


import com.york.media.opengl.R;
import com.york.media.opengl.demo.camera.api2.YCamera2View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * author : York
 * date   : 2020/12/20 22:20
 * desc   : Camera2 使用 openGL
 */
public class Camera2Activity extends AppCompatActivity {
    private YCamera2View yCamera2View;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);
        yCamera2View = findViewById(R.id.yCamera2View);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        yCamera2View.onDestroy();
    }
}
