package com.york.media.opengl.demo.empty;

import android.content.Context;
import android.util.AttributeSet;

import com.york.media.opengl.egl.YGLSurfaceView;

/**
 * author : York
 * date   : 2020/12/20 14:44
 * desc   :
 */
public class YEmptySurfaceView extends YGLSurfaceView {

    public YEmptySurfaceView(Context context) {
        this(context, null);
    }

    public YEmptySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        YEmptyRender yEmptyRender = new YEmptyRender();
        setRender(yEmptyRender);

    }
}
