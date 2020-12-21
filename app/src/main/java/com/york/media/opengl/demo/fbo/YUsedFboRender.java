package com.york.media.opengl.demo.fbo;

import android.content.Context;
import android.opengl.GLES20;
import com.york.media.opengl.R;
import com.york.media.opengl.egl.TextureUtils;
import com.york.media.opengl.egl.YGLSurfaceView;
import com.york.media.opengl.egl.YShaderUtil;
import com.york.media.opengl.utils.LogUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * author : York
 * date   : 2020/12/20 19:42
 * desc   : FBO 帧缓冲对象
 * <p>
 * 当需要对纹理进行多次渲染时，而这些渲染采样是不需要展示给用户看的，就可以用一个单独的缓冲对象（离屏渲染）
 * 来存储多次渲染采样的结果，等处理完后再显示到窗口上。
 */
public class YUsedFboRender implements YGLSurfaceView.YGLRender {

    private final Context mContext;
    private final FloatBuffer vertexBuffer;
    private final FloatBuffer fragmentBuffer;
    private int program;
    private int vPosition;
    private int fPosition;

    private int fboTextureID;
    private int vbo;
    private int fbo;

    private int imgTextureId;
    private int fboWidth;
    private int fboHeight;

    private final YFboRender yFboRender;
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


    public YUsedFboRender(Context context, int width, int height) {
        this.mContext = context;
        fboWidth = width;
        fboHeight = height;
        yFboRender = new YFboRender(context);
        //读取顶点坐标
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertexData);
        vertexBuffer.position(0);

        //读取纹理坐标
        fragmentBuffer = ByteBuffer.allocateDirect(fragmentData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(fragmentData);
        fragmentBuffer.position(0);

    }

    @Override
    public void onSurfaceCreated() {
        yFboRender.onCreate();

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
        int[] vbo_s = new int[1];
        GLES20.glGenBuffers(1, vbo_s, 0);
        vbo = vbo_s[0];
        //绑定 VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);

        //分配 VBO需要的缓存大小
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4 + fragmentData.length * 4, null, GLES20.GL_STATIC_DRAW);
        //设置顶点坐标数据的值到 VBO
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexData.length * 4, vertexBuffer);
        //设置纹理坐标数据的值到 VBO
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4, fragmentData.length * 4, fragmentBuffer);
        //解绑 VBO，指的是离开对 VBO的配置，进入下一个状态
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        //创建 fbo
        int[] fbo_s = new int[1];
        GLES20.glGenBuffers(1, fbo_s, 0);
        fbo = fbo_s[0];
        //使 fbo 成为 fbo对象  GL_FRAMEBUFFER
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo);

        //创建 1个 fbo 纹理 fboTextureID
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);//第三个参数是指从哪儿开始取
        fboTextureID = textureIds[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fboTextureID);// 在没设置点的情况下默认是绑定 0号纹理

        //设置纹理的环绕方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        //设置纹理的过滤方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        LogUtil.d("fbo_Width=" + fboWidth + ",fboHeight=" + fboHeight);
        //分配FBO内存大小
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, fboWidth, fboHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        //把 2D纹理 fboTextureID 绑定到 FBO 对象
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, fboTextureID, 0);
        //检查FBO绑定是否成功
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            LogUtil.e("fbo bind error !");
        }

        //绑定纹理到 fbo成功后 退出纹理绑定，进入下一环节
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        imgTextureId = TextureUtils.createImageTexture(mContext, R.drawable.nobb);

    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        //设置窗口大小
        GLES20.glViewport(0, 0, width, height);
        fboWidth = width;
        fboHeight = height;
        yFboRender.onChange(fboWidth, fboHeight);
    }

    @Override
    public void onDrawFrame() {
        //清除屏幕，此处用的是红色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1f, 0f, 0f, 1f);
        //使用着色器源程序
        GLES20.glUseProgram(program);

        //开始使用 VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
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

        //绑定 fbo 开始使用 FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo);

        //绑定 imgTextureId 开始使用纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, imgTextureId);
        //绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        //解绑 FBO纹理
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        yFboRender.onDraw(fboTextureID);

    }
}
