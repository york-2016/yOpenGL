package com.york.media.opengl.demo.bitmap;

import android.content.Context;
import android.util.AttributeSet;

import com.york.media.opengl.egl.YGLSurfaceView;

/**
 * author : York
 * date   : 2020/12/21 4:17
 * desc   : 图片、纹理 加水印
 */
public class YWaterMarkSurface extends YGLSurfaceView {
    public YWaterMarkSurface(Context context) {
        this(context, null);
    }

    public YWaterMarkSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        YWaterMarkRender yWaterMarkRender=new YWaterMarkRender(context);
        setRender(yWaterMarkRender);
    }
}
