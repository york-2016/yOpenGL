package com.york.media.opengl.egl;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * author : York
 * date   : 2020/12/17 21:58
 * desc   :
 */
public class YGLRender implements GLSurfaceView.Renderer {

    public YGLRender() {
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int with, int height) {
        GLES20.glViewport(0, 0, with, height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1f,0f,0f,1f);
    }
}
