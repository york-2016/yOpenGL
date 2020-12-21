package com.york.media.opengl.demo.shape;

import android.content.Context;
import android.util.AttributeSet;

import com.york.media.opengl.egl.YGLSurfaceView;

/**
 * author : York
 * date   : 2020/12/20 16:13
 * desc   : 三角形
 */
public class YTriangleSurface extends YGLSurfaceView {
    public YTriangleSurface(Context context) {
        this(context,null);
    }

    public YTriangleSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        YTriangleRender yTriangleRender = new YTriangleRender(context);
        setRender(yTriangleRender);
    }
}
