package com.york.media.opengl.demo.shape;

import android.content.Context;
import android.opengl.GLES20;

import com.york.media.opengl.R;
import com.york.media.opengl.egl.YGLSurfaceView;
import com.york.media.opengl.egl.YShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * author : York
 * date   : 2020/12/20 16:33
 * desc   : 绘制圆形
 */
public class YCircularRender implements YGLSurfaceView.YGLRender {
    private final Context mContext;
    private final FloatBuffer vertexBuffer;
    private int program;
    private int vPosition;
    private int fPosition;

    public YCircularRender(Context mContext) {
        this.mContext = mContext;
        //顶点坐标
        float[] vertexData = new float[720];
        for (int i = 0; i < 720; i += 2) {
            // x value
            vertexData[i] = (float) Math.cos(i);
            // y value
            vertexData[i + 1] = (float) Math.sin(i);
        }
        //读取顶点坐标
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertexData);
        vertexBuffer.position(0);

    }

    @Override
    public void onSurfaceCreated() {
        //加载顶点着色器 shader
        String vertexSource = YShaderUtil.getRawResource(mContext, R.raw.screen_vert);
        //加载片元着色器 shader
        String fragmentSource = YShaderUtil.getRawResource(mContext, R.raw.screen_frag_color);
        //获取源程序
        program = YShaderUtil.createProgram(vertexSource, fragmentSource);
        //从渲染程序中得到着顶点色器中的属性
        vPosition = GLES20.glGetAttribLocation(program, "vPosition");
        //从渲染程序中得到片元着色器中的属性
        fPosition = GLES20.glGetUniformLocation(program, "f_Color");
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        //设置窗口大小
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame() {
        //清除屏幕，此处用的是红色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0f, 0f, 1f, 1f);
        //使用着色器源程序
        GLES20.glUseProgram(program);
        //绘制绿色
        GLES20.glUniform4f(fPosition, .5f, .5f, 1f, 1f);
        //使能顶点属性数组，使之有效
        GLES20.glEnableVertexAttribArray(vPosition);
        //使能之后，为顶点属性赋值，绑定顶点坐标
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        //绘制图形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 360);//圆形

    }
}
