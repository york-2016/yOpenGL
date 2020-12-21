package com.york.media.opengl.demo.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;

import com.york.media.opengl.R;
import com.york.media.opengl.egl.TextureUtils;
import com.york.media.opengl.egl.YShaderUtil;
import com.york.media.opengl.utils.LogUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * author : York
 * date   : 2020/12/21 1:26
 * desc   : Camera 使用 Fbo 的 render
 */
public class YCameraFboRender {

    private final Context mContext;

    private final float[] vertexData = {
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f,

            0f, 0f,
            0f, 0f,
            0f, 0f,
            0f, 0f,

            -1f, 0.75f,
            -0.5f, 0.75f,
            -1f, 1f,
            -0.5f, 1f,

    };
    private final FloatBuffer vertexBuffer;

    private final float[] fragmentData = {
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f
    };
    private final FloatBuffer fragmentBuffer;

    private int program;
    private int vPosition;
    private int fPosition;
    private int vboId;

    private int txtTextureId;
    private int bitmapTextureId;
    private Bitmap bitmap;

    public YCameraFboRender(Context context) {
        this.mContext = context;
        bitmap = TextureUtils.createTextImage("York的IT旅途", 36, "#ffffff", "#00000000", 0);
        float r = 1.0f * bitmap.getWidth() / bitmap.getHeight();
        float w = r * 0.1f;

        vertexData[8] = 0.9f - w;
        vertexData[9] = -0.9f;
        vertexData[10] = 0.9f;
        vertexData[11] = -0.9f;
        vertexData[12] = 0.9f - w;
        vertexData[13] = -0.8f;
        vertexData[14] = 0.9f;
        vertexData[15] = -0.8f;

        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertexData);
        vertexBuffer.position(0);

        fragmentBuffer = ByteBuffer.allocateDirect(fragmentData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(fragmentData);
        fragmentBuffer.position(0);

    }

    public void onCreate() {

        String vertexSource = YShaderUtil.getRawResource(mContext, R.raw.screen_vert);
        String fragmentSource = YShaderUtil.getRawResource(mContext, R.raw.screen_frag);

        program = YShaderUtil.createProgram(vertexSource, fragmentSource);

        vPosition = GLES20.glGetAttribLocation(program, "vPosition");
        fPosition = GLES20.glGetAttribLocation(program, "fPosition");
        //设置支持透明
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        int[] vbo = new int[1];
        GLES20.glGenBuffers(1, vbo, 0);
        vboId = vbo[0];

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4 + fragmentData.length * 4, null, GLES20.GL_STATIC_DRAW);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexData.length * 4, vertexBuffer);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4, fragmentData.length * 4, fragmentBuffer);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        txtTextureId = TextureUtils.loadBitmapTexture(bitmap);
        bitmap.recycle();
        bitmapTextureId = TextureUtils.createImageTexture(mContext, R.drawable.byg);
    }

    public void onChange(int width, int height) {

        GLES20.glViewport(0, 0, width, height);
    }

    public void onDraw(int textureId) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1f, 0f, 0f, 1f);
        GLES20.glUseProgram(program);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);

        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, 0);
        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8,
                vertexData.length * 4);

        //绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        //绘制纹理
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        //解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);


        //文字水印
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, 32);
        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, vertexData.length * 4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, txtTextureId);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        //图片水印
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, 64);
        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, vertexData.length * 4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, bitmapTextureId);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        //解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }
}
