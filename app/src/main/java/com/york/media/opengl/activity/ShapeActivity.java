package com.york.media.opengl.activity;

import android.os.Bundle;

import com.york.media.opengl.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * author : York
 * date   : 2020/12/20 14:25
 * desc   : 绘制不同形状 四边形、圆形、三角形
 */
public class ShapeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shape);
    }
}
