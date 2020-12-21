package com.york.media.opengl.demo.shape;

import android.content.Context;
import android.util.AttributeSet;

import com.york.media.opengl.egl.YGLSurfaceView;

/**
 * author : York
 * date   : 2020/12/20 16:48
 * desc   :
 */
public class YCircularSurface extends YGLSurfaceView {

    public YCircularSurface(Context context) {
        this(context,null);
    }

    public YCircularSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        YCircularRender yCircularRender = new YCircularRender(context);
        setRender(yCircularRender);
    }
}
