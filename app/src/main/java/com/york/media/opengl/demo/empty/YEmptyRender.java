package com.york.media.opengl.demo.empty;

import android.opengl.GLES20;

import com.york.media.opengl.egl.YGLSurfaceView;


/**
 * author : York
 * date   : 2020/12/20 14:45
 * desc   : 最简单的一个实例 用红颜色 清屏
 */
public class YEmptyRender implements YGLSurfaceView.YGLRender {


    public YEmptyRender() {
    }

    @Override
    public void onSurfaceCreated() {

    }

    @Override
    public void onSurfaceChanged(int width, int height) {

    }

    @Override
    public void onDrawFrame() {
        //用红颜色 清屏
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1f,0f,0f,1f);
    }
}
