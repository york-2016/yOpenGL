package com.york.media.opengl.demo.vbo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.york.media.opengl.R;
import com.york.media.opengl.egl.YGLSurfaceView;
import com.york.media.opengl.egl.YShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * author : York
 * date   : 2020/12/20 18:01
 * desc   : OpenGL VBO 即顶点缓冲对象 ，目的是提高顶点坐标获取的效率
 * <p>
 * 不使用 VBO时，每次绘制（ glDrawArrays ）图形时都是从本地内存处获取顶点数据然后传输给 OpenGL来绘制，这样就会频繁的操作 CPU->GPU增大开销，从而降低效率。
 * 使用 VBO时，能把顶点数据缓存到GPU开辟的一段内存中，然后使用时不必再从本地获取，而是直接从显存中获取，这样就能提升绘制的效率。
 */
public class YVboRender implements YGLSurfaceView.YGLRender {

    private final Context mContext;
    private final FloatBuffer vertexBuffer;
    private final FloatBuffer fragmentBuffer;
    private int program;
    private int vPosition;
    private int fPosition;

    private int bitmapTexture;
    private int vboID;
    //顶点坐标
    float[] vertexData = {
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f
    };

    //纹理坐标
    float[] fragmentData = {
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f
    };


    public YVboRender(Context context) {
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

        //创建 VBO
        int[] vbo = new int[1];
        GLES20.glGenBuffers(1, vbo, 0);
        vboID = vbo[0];
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboID);

        //分配 VBO需要的缓存大小
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4 + fragmentData.length * 4, null, GLES20.GL_STATIC_DRAW);
        //设置顶点坐标数据的值到 VBO
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexData.length * 4, vertexBuffer);
        //设置纹理坐标数据的值到 VBO
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4, fragmentData.length * 4, fragmentBuffer);
        //解绑 VBO，指的是离开对 VBO的配置，进入下一个状态
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        //创建 1个纹理,放入到 int [] textureIds
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);//第三个参数是指从哪儿开始取
        bitmapTexture = textureIds[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, bitmapTexture);//在没设置点的情况下默认是绑定 0号纹理

        //设置纹理的环绕方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        //设置纹理的过滤方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        //绑定bitmap 到  textureIds[0] 2D纹理
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.nobb);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();

        //解绑纹理 指的是离开对 纹理的配置，进入下一个状态
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
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

        //开始使用 VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboID);
        //使能顶点属性数组，使之有效
        GLES20.glEnableVertexAttribArray(vPosition);
        //使能之后，为顶点属性赋值，从VBO里获取 绑定顶点坐标; 注意：最后一个参数如果是 vertexBuffer，那么就没有用到 VBO，那就还是从CPU里取顶点
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, 0);
        //使能片元属性数组，使之有效
        GLES20.glEnableVertexAttribArray(fPosition);
        //使能之后，为片元属性赋值，从VBO里获取 绑定纹理坐标; 注意：最后一个参数为 VBO里的偏移量
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, vertexData.length * 4);

        //退出 VBO的使用
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        //要开始绘制纹理了，激活纹理 0号， 之所以激活 0号，是因为在没设置点的情况下默认是 0号
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //绑定 bitmapTexture 到纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, bitmapTexture);
        //绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        //解绑 2D纹理，退出对纹理的使用
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

}
