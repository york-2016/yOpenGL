package com.york.media.opengl.egl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLContext;

/**
 * author : York
 * date   : 2020/12/17 21:57
 * desc   : 自定义的 GLSurfaceView 继成了 SurfaceView，并实现其CallBack回调
 */
public class YGLSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private Surface surface;
    private EGLContext eglContext;
    private YEGLThread yEGLThread;
    private YGLRender yGLRender;
    public final static int RENDERMODE_WHEN_DIRTY = 0;//手动刷新
    public final static int RENDERMODE_CONTINUOUSLY = 1;//自动刷新

    private int mRenderMode = RENDERMODE_CONTINUOUSLY;

    public YGLSurfaceView(Context context) {
        this(context, null);
    }

    public YGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }

    public void setRender(YGLRender yRender) {
        this.yGLRender = yRender;
    }

    public void setRenderMode(int mRenderMode) {
        if (yGLRender == null) {
            throw new RuntimeException("must set render before set RenderMode");
        }
        this.mRenderMode = mRenderMode;
    }

    //添加设置Surface和EglContext的方法
    public void setSurfaceAndEglContext(Surface surface, EGLContext eglContext) {
        this.surface = surface;
        this.eglContext = eglContext;
    }

    public EGLContext getEglContext() {
        if (yEGLThread != null) {
            return yEGLThread.getEglContext();
        }
        return null;
    }

    public void requestRender() {
        if (yEGLThread != null) {
            yEGLThread.requestRender();
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (surface == null) {
            surface = holder.getSurface();
        }
        yEGLThread = new YEGLThread(new WeakReference<YGLSurfaceView>(this));
        yEGLThread.isCreate = true;
        yEGLThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        yEGLThread.width = width;
        yEGLThread.height = height;
        yEGLThread.isChange = true;

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        yEGLThread.onDestroy();
        yEGLThread = null;
        surface = null;
        eglContext = null;
    }

    public interface YGLRender {
        void onSurfaceCreated();

        void onSurfaceChanged(int width, int height);

        void onDrawFrame();
    }

    static class YEGLThread extends Thread {

        private WeakReference<YGLSurfaceView> yGlSurfaceViewWeakReference;
        private YEglHelper eglHelper = null;
        private Object object = null;

        private boolean isExit = false;
        private boolean isCreate = false;
        private boolean isChange = false;
        private boolean isStart = false;

        private int width;
        private int height;

        public YEGLThread(WeakReference<YGLSurfaceView> yglSurfaceViewWeakReference) {
            this.yGlSurfaceViewWeakReference = yglSurfaceViewWeakReference;
        }

        @Override
        public void run() {
            super.run();
            isExit = false;
            isStart = false;
            object = new Object();
            eglHelper = new YEglHelper();
            eglHelper.initEgl(yGlSurfaceViewWeakReference.get().surface, yGlSurfaceViewWeakReference.get().eglContext);

            while (true) {
                if (isExit) {
                    //释放资源
                    release();
                    break;
                }
                if (isStart) {
                    if (yGlSurfaceViewWeakReference.get().mRenderMode == RENDERMODE_WHEN_DIRTY) {
                        synchronized (object) {
                            try {
                                object.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (yGlSurfaceViewWeakReference.get().mRenderMode == RENDERMODE_CONTINUOUSLY) {
                        try {
                            Thread.sleep(1000 / 100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        throw new RuntimeException("mRenderMode is wrong value");
                    }
                }
                onCreate();
                onChange(width, height);
                onDraw();
                isStart = true;
            }
        }

        private void onCreate() {
            if (isCreate && yGlSurfaceViewWeakReference.get().yGLRender != null) {
                isCreate = false;
                yGlSurfaceViewWeakReference.get().yGLRender.onSurfaceCreated();
            }
        }

        private void onChange(int width, int height) {
            if (isChange && yGlSurfaceViewWeakReference.get().yGLRender != null) {
                isChange = false;
                yGlSurfaceViewWeakReference.get().yGLRender.onSurfaceChanged(width, height);
            }
        }

        private void onDraw() {
            if (yGlSurfaceViewWeakReference.get().yGLRender != null && eglHelper != null) {
                yGlSurfaceViewWeakReference.get().yGLRender.onDrawFrame();
                if (!isStart) {
                    yGlSurfaceViewWeakReference.get().yGLRender.onDrawFrame();
                }
                eglHelper.swapBuffers();

            }
        }

        private void requestRender() {
            if (object != null) {
                synchronized (object) {
                    object.notifyAll();
                }
            }
        }

        public void onDestroy() {
            isExit = true;
            requestRender();
        }

        public void release() {
            if (eglHelper != null) {
                eglHelper.destroyEgl();
                eglHelper = null;
                object = null;
                yGlSurfaceViewWeakReference = null;
            }
        }

        public EGLContext getEglContext() {
            if (eglHelper != null) {
                return eglHelper.getEglContext();
            }
            return null;
        }
    }
}
