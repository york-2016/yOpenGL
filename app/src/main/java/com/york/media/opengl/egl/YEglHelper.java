package com.york.media.opengl.egl;

import android.opengl.EGL14;
import android.view.Surface;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

/**
 * author : York
 * date   : 2020/12/17 22:11
 * desc   : 搭建 EGL 环境类
 */
public class YEglHelper {

    private EGL10 mEgl;//EGL实例
    private EGLDisplay mEglDisplay;//默认的显示社保
    private EGLContext mEglContext;//EGL上下文
    private EGLSurface mEglSurface;//EGLSurface

    /**
     * 创建 EGL 环境
     * @param surface 外部传入的 Surface
     * @param eglContext EGL 上下文
     */
    public void initEgl(Surface surface, EGLContext eglContext) {

        //1.获取 Egl实例
        mEgl = (EGL10) EGLContext.getEGL();

        //2.获取一个默认的显示设备
        mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        if (mEglDisplay==EGL10.EGL_NO_DISPLAY){
            throw new RuntimeException("get EGL_DEFAULT_DISPLAY error");
        }

        //3.初始化默认显示设备
        int version[]=new int[2];
        if (!mEgl.eglInitialize(mEglDisplay,version)){
            throw new RuntimeException("init EGL_DEFAULT_DISPLAY error");
        }
        //4.设置显示设备的属性
        int[] attrs = new int[]{
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_DEPTH_SIZE, 8,
                EGL10.EGL_STENCIL_SIZE, 8,
                EGL10.EGL_RENDERABLE_TYPE, 4,
                EGL10.EGL_NONE};

        int[] num_config = new int[1];
        if (!mEgl.eglChooseConfig(mEglDisplay, attrs, null, 1, num_config)) {
            throw new IllegalArgumentException("set eglChooseConfig failed");
        }
        int numConfigs = num_config[0];
        if (numConfigs <= 0) {
            throw new IllegalArgumentException("No configs match configSpec");
        }

        //5.从系统中获取对应属性的配置
        EGLConfig[] configs = new EGLConfig[numConfigs];
        if (!mEgl.eglChooseConfig(mEglDisplay, attrs, configs, numConfigs, num_config)) {
            throw new IllegalArgumentException("eglChooseConfig#2 failed");
        }

        //6.创建 EglContext
        int[] attr_list = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL10.EGL_NONE
        };
        if (eglContext != null) {
            mEglContext = mEgl.eglCreateContext(mEglDisplay, configs[0], eglContext, attr_list);
        } else {
            mEglContext = mEgl.eglCreateContext(mEglDisplay, configs[0], EGL10.EGL_NO_CONTEXT, attr_list);
        }

        //7.创建渲染的 Surface,最后一个参数传空
        mEglSurface = mEgl.eglCreateWindowSurface(mEglDisplay, configs[0], surface, null);

        //8、绑定 EglContext和 Surface到显示设备中
        if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }

    /**
     * 刷新数据，进行显示
     * @return 是否成功
     */
    public boolean swapBuffers() {
        if (mEgl != null) {
            return mEgl.eglSwapBuffers(mEglDisplay, mEglSurface);
        } else {
            throw new RuntimeException("mEgl is null");
        }
    }

    /**
     * 从外部获取 EglContext，OpenGL整体是一个状态机，通过改变状态就能改变后续的渲染方式，而
     * EGLContext（EgL上下文）就保存有所有状态，因此可以通过共享 EGLContext
     * 来实现同一场景渲染到不同的 Surface上。
     * @return 返回 EglContext
     */
    public EGLContext getEglContext() {
        return mEglContext;
    }

    /**
     * 销毁
     */
    public void destroyEgl() {
        if (mEgl != null) {
            mEgl.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE,
                    EGL10.EGL_NO_SURFACE,
                    EGL10.EGL_NO_CONTEXT);

            mEgl.eglDestroySurface(mEglDisplay, mEglSurface);
            mEglSurface = null;

            mEgl.eglDestroyContext(mEglDisplay, mEglContext);
            mEglContext = null;

            mEgl.eglTerminate(mEglDisplay);
            mEglDisplay = null;
            mEgl = null;
        }
    }
}
