package com.york.media.opengl.activity;

import android.os.Bundle;

import com.york.media.opengl.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * author : York
 * date   : 2020/12/20 14:38
 * desc   : OpenGL 清空屏幕
 */
public class EmptyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty);
    }
}
