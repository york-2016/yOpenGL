package com.york.media.opengl.demo.fbo;

import android.content.Context;
import android.opengl.GLES20;

import com.york.media.opengl.R;
import com.york.media.opengl.egl.YShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * author : York
 * date   : 2020/12/20 20:29
 * desc   :
 */
public class YFboRender {

    private final Context context;
    private final FloatBuffer vertexBuffer;
    private final FloatBuffer fragmentBuffer;
    private int vPosition;
    private int fPosition;
    private int program;
    private int vbo;


    private final float[] vertexData = {
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f
    };
    //因为 fbo的片元坐标系与安卓的不一样，所以看上去是倒的
    private final float[] fragmentData = {
            0f, 0f,
            1f, 0f,
            0f, 1f,
            1f, 1f
    };

    public YFboRender(Context context) {
        this.context = context;

        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexBuffer.position(0);

        fragmentBuffer = ByteBuffer.allocateDirect(fragmentData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(fragmentData);
        fragmentBuffer.position(0);

    }

    public void onCreate() {

        String vertexSource = YShaderUtil.getRawResource(context, R.raw.screen_vert);
        String fragmentSource = YShaderUtil.getRawResource(context, R.raw.screen_frag);

        program = YShaderUtil.createProgram(vertexSource, fragmentSource);

        vPosition = GLES20.glGetAttribLocation(program, "vPosition");
        fPosition = GLES20.glGetAttribLocation(program, "fPosition");

        int[] vbo_s = new int[1];
        GLES20.glGenBuffers(1, vbo_s, 0);
        vbo = vbo_s[0];

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4 + fragmentData.length * 4, null, GLES20.GL_STATIC_DRAW);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexData.length * 4, vertexBuffer);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4, fragmentData.length * 4, fragmentBuffer);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    public void onChange(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    public void onDraw(int textureId) {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0f, 1f, 0f, 1f);
        GLES20.glUseProgram(program);
        //使用 vbo
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, 0);
        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, vertexData.length * 4);
        //解绑vbo，退出使用vbo 进入下一环节，绘制纹理
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        //绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        //绘制纹理
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        //解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

    }
}
