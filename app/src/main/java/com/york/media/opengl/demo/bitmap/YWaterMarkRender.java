package com.york.media.opengl.demo.bitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.york.media.opengl.R;
import com.york.media.opengl.egl.TextureUtils;
import com.york.media.opengl.egl.YGLSurfaceView;
import com.york.media.opengl.egl.YShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * author : York
 * date   : 2020/12/21 4:16
 * desc   :
 */
public class YWaterMarkRender implements YGLSurfaceView.YGLRender {
    private final Context mContext;
    private final FloatBuffer vertexBuffer;
    private final FloatBuffer fragmentBuffer;
    private int program;
    private int vPosition;
    private int fPosition;
    private int[] textureIds;
    private int bitmapTextureId;
    private int textTextureId;
    private int vboId;

    //顶点坐标
    float[] vertexData = {
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f,
            //用来 加一个 图片水印 到左上角
            -1f, 0.5f,
            0f, 0.5f,
            -1f, 1f,
            0f, 1f,

            //用来 加一个文字水印 到右下角
            0f, -1f,
            1f, -1f,
            0f, -0.8f,
            1f, -0.8f
    };
    //纹理坐标
    float[] fragmentData = {
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f
    };

    public YWaterMarkRender(Context context) {
        this.mContext = context;
        //读取顶点坐标
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertexData);
        vertexBuffer.position(0);

        //读取纹理坐标
        fragmentBuffer = ByteBuffer.allocateDirect(fragmentData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(fragmentData);
        fragmentBuffer.position(0);


    }

    @Override
    public void onSurfaceCreated() {
        //加载顶点着色器 shader
        String vertexSource = YShaderUtil.getRawResource(mContext, R.raw.screen_vert);
        //加载片元着色器 shader
        String fragmentSource = YShaderUtil.getRawResource(mContext, R.raw.screen_frag);
        //获取源程序
        program = YShaderUtil.createProgram(vertexSource, fragmentSource);
        //从渲染程序中得到着顶点色器中的属性
        vPosition = GLES20.glGetAttribLocation(program, "vPosition");
        //从渲染程序中得到片元着色器中的属性
        fPosition = GLES20.glGetAttribLocation(program, "fPosition");

        //设置文字支持透明
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        int [] vbo_s = new int[1];
        GLES20.glGenBuffers(1, vbo_s, 0);
        vboId = vbo_s[0];

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4 + fragmentData.length * 4, null, GLES20. GL_STATIC_DRAW);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexData.length * 4, vertexBuffer);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4, fragmentData.length * 4, fragmentBuffer);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);


        //创建 1个纹理,放入到 int [] textureIds, 一共有 30多个 纹理
        textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);//第三个参数是指从哪儿开始取
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0]);
        //设置纹理的环绕方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        //设置纹理的过滤方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        //解绑纹理 指的是离开对 纹理的配置，进入下一个状态
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        bitmapTextureId = TextureUtils.createImageTexture(mContext, R.drawable.bjwd);

        Bitmap txtBitmap = TextureUtils.createTextImage("York的IT旅途", 36, "#ff0000", "#00000000", 0);
        textTextureId = TextureUtils.loadBitmapTexture(txtBitmap);
        txtBitmap.recycle();
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
        GLES20.glClearColor(1f, 0f, 0f, 1f);

        //使用着色器源程序
        GLES20.glUseProgram(program);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);

        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8,0);

        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8,vertexData.length * 4);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0]);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        //绑定 textureIds[0] 到已激活的 2D纹理 GL_TEXTURE0上
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0]);
        //获取图片的 bitmap
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.byg);
        //绑定 bitmap 到textureIds[0]纹理
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();//用完及时回收
        //绘制原图片
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        //解绑 2D纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        //图片水印
        //从VBO中获取图片水印的坐标，并使能
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8,32);
        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8,vertexData.length * 4);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, bitmapTextureId);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        //文字水印
        //从VBO中获取图片水印的坐标，并使能
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8,64);
        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8,vertexData.length * 4);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textTextureId);
        //绘制水印
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        //解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);


        //解绑 VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }


}
