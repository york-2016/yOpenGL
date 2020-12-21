package com.york.media.opengl.demo.fbo;

import android.content.Context;
import android.util.AttributeSet;

import com.york.media.opengl.demo.vbo.YVboRender;
import com.york.media.opengl.egl.YGLSurfaceView;

/**
 * author : York
 * date   : 2020/12/20 20:35
 * desc   :
 */
public class YFboSurfaceView extends YGLSurfaceView {

    public YFboSurfaceView(Context context) {
        this(context, null);
    }

    public YFboSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        YUsedFboRender yUsedFboRender = new YUsedFboRender(context,1080,1840);
        setRender(yUsedFboRender);
    }
}