package com.york.media.opengl.demo.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Build;

import com.york.media.opengl.R;
import com.york.media.opengl.egl.YGLSurfaceView;
import com.york.media.opengl.egl.YShaderUtil;
import com.york.media.opengl.utils.LogUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import androidx.annotation.RequiresApi;

/**
 * author : York
 * date   : 2020/12/20 14:05
 * desc   : Camera 预览的 Render
 */
public class YCameraRender implements YGLSurfaceView.YGLRender {

    private final Context mContext;

    private final FloatBuffer vertexBuffer;
    private final FloatBuffer fragmentBuffer;
    private int program;

    private int vPosition;
    private int fPosition;
    private final float[] matrix = new float[16];
    private int u_matrix;
    private int vboId;
    private int fboId;


    private int showWidth;//预览窗口的大小，宽度
    private int showHeight;//预览窗口的大小，高度


    //顶点坐标
    private final float[] vertexData = {
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f
    };
    //纹理坐标
    private final float[] fragmentData = {
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f
    };

    private final int[] textureIds = new int[1];//纹理 ID
    private int fboTextureId;

    private final YCameraFboRender yCameraFboRender;
    private SurfaceTexture surfaceTexture;
    private OnSurfaceCreateListener onSurfaceCreateListener;

    private boolean isStop = false;

    public YCameraRender(Context mContext, int width, int height) {
        this.mContext = mContext;
        this.showWidth = width;
        this.showHeight = height;
        //读取顶点坐标
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertexData);
        vertexBuffer.position(0);
        //从读取纹理坐标
        fragmentBuffer = ByteBuffer.allocateDirect(fragmentData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(fragmentData);
        fragmentBuffer.position(0);
        yCameraFboRender = new YCameraFboRender(mContext);
    }



    @Override
    public void onSurfaceCreated() {
        yCameraFboRender.onCreate();
        String vertexSource = YShaderUtil.getRawResource(mContext, R.raw.camera_vertex);//加载顶点着色器 shader
        String fragmentSource = YShaderUtil.getRawResource(mContext, R.raw.camera_frag);//加载片元着色器 shader

        program = YShaderUtil.createProgram(vertexSource, fragmentSource);//获取源程序
        vPosition = GLES20.glGetAttribLocation(program, "vPosition");
        fPosition = GLES20.glGetAttribLocation(program, "fPosition");
        u_matrix = GLES20.glGetUniformLocation(program, "u_Matrix");//从渲染程序中得到矩阵属性
        //顶点缓冲对象 vbo ，目的是提高顶点坐标获取的效率
        //不使用 VBO时，每次绘制（ glDrawArrays ）图形时都是从本地内存处获取顶点数据然后传输给 OpenGL来绘制，这样就会频繁的操作CPU->GPU增大开销，从而降低效率。
        //使用 VBO时，能把顶点数据缓存到GPU开辟的一段内存中，然后使用时不必再从本地获取，而是直接从显存中获取，这样就能提升绘制的效率。
        int[] vbo = new int[1];
        GLES20.glGenBuffers(1, vbo, 0);
        vboId = vbo[0];

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);//绑定vbo
        //分配 VBO需要的缓存大小
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4 + fragmentData.length * 4, null, GLES20.GL_STATIC_DRAW);
        //为 VBO设置顶点坐标数据的值
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexData.length * 4, vertexBuffer);
        //为 VBO设置纹理坐标数据的值
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4, fragmentData.length * 4, fragmentBuffer);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);//解绑 VBO

        //FBO 当我们需要对纹理进行多次渲染采样时，而这些渲染采样是不需要展示给用户看的，所以我们就可以用一个单独的缓冲对象（离屏渲染）来存储我们的这几次渲染采样的结果，等处理完后才显示到窗口上。
        int[] fbo = new int[1];
        GLES20.glGenBuffers(1, fbo, 0);
        fboId = fbo[0];
        //绑定FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);

        //创建 1个纹理,放入到 int [] textureIds
        GLES20.glGenTextures(1, textureIds, 0);
        fboTextureId = textureIds[0];
        //绑定到 纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fboTextureId);

        //设置纹理环绕方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        //设置纹理过滤方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        //分配FBO内存大小
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, showWidth, showHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        //把纹理绑定到 FBO
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, fboTextureId, 0);
        //检查FBO绑定是否成功
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            LogUtil.e("fbo wrong !");
        }
        //解绑 纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        //解绑 FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        //在有了fbo的基础上 再创建一个纹理，渲染摄像头数据，
        int[] textureIdCamera = new int[1];
        GLES20.glGenTextures(1, textureIdCamera, 0);
        int cameraTextureId = textureIdCamera[0];
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTextureId);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        //将cameraTextureId 绑定到 surfaceTexture上
        surfaceTexture = new SurfaceTexture(cameraTextureId);

        if (onSurfaceCreateListener != null) {
            onSurfaceCreateListener.onSurfaceCreate(surfaceTexture, fboTextureId);
        }
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onSurfaceChanged(int width, int height) {
        this.showWidth = width;
        this.showHeight = height;
        yCameraFboRender.onChange(showWidth, showHeight);
    }

    @Override
    public void onDrawFrame() {
        surfaceTexture.updateTexImage();
        //清屏
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glClearColor(0f, 0f, 0f, 1f);
        if (isStop) {
            return;
        }
        GLES20.glUseProgram(program);//使用源程序
        GLES20.glUniformMatrix4fv(u_matrix, 1, false, matrix, 0);//使用矩阵
        GLES20.glViewport(0, 0, showWidth, showHeight);//设置绘制的窗口大小
        //绑定 VBO，开始使用
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        //绑定 FBO 开始使用
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);

        //使能顶点属性数据，使之有效,使能之后，为顶点属性赋值，绑定顶点坐标
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, 0);
        //使能片元属性数据，使之有效, 使能之后，为片元属性赋值，绑定纹理坐标
        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, vertexData.length * 4);
        //绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);//解绑 2D纹理
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);//解绑 VBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);//解绑 FBO

        yCameraFboRender.onDraw(fboTextureId);
    }


    public void updatePreviewStates(boolean isStop) {
        this.isStop = isStop;
    }


    public void resetMatrix() {
        Matrix.setIdentityM(matrix, 0);
    }

    /**
     * 矩阵调整预览方向
     */
    public void setAngle(float angle, float x, float y, float z) {
        Matrix.rotateM(matrix, 0, angle, x, y, z);
    }

    public void setOnSurfaceCreateListener(OnSurfaceCreateListener onSurfaceCreateListener) {
        this.onSurfaceCreateListener = onSurfaceCreateListener;
    }

    public interface OnSurfaceCreateListener {
        void onSurfaceCreate(SurfaceTexture surfaceTexture, int textureId);
    }
}
