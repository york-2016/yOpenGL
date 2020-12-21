package com.york.media.opengl.demo.vbo;

import android.content.Context;
import android.util.AttributeSet;

import com.york.media.opengl.demo.bitmap.YBitmapRender;
import com.york.media.opengl.egl.YGLSurfaceView;

/**
 * author : York
 * date   : 2020/12/20 18:40
 * desc   :
 */
public class YVboSurfaceView extends YGLSurfaceView {

    public YVboSurfaceView(Context context) {
        this(context, null);
    }

    public YVboSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        YVboRender yVboRender = new YVboRender(context);
        setRender(yVboRender);
    }
}
