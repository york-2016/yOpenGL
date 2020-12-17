package com.york.media.opengl.egl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/**
 * author : York
 * date   : 2020/12/17 21:57
 * desc   :
 */
public class YGLSurfaceView extends GLSurfaceView {
    private YGLRender yGLRender;
    public YGLSurfaceView(Context context) {
        super(context);
    }

    public YGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        yGLRender =new YGLRender();
        setRenderer(yGLRender);
    }
}
