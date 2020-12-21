package com.york.media.opengl.demo.bitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import com.york.media.opengl.R;
import com.york.media.opengl.egl.YGLSurfaceView;
import com.york.media.opengl.egl.YShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * author : York
 * date   : 2020/12/20 17:06
 * desc   : 加入正交投影
 */
public class YBitmapOrthogonalRender implements YGLSurfaceView.YGLRender {

    private final Context mContext;
    private final FloatBuffer vertexBuffer;
    private final FloatBuffer fragmentBuffer;
    private int program;
    private int vPosition;
    private int fPosition;
    private int[] textureIds;
    private int u_matrix;
    private final float[] matrix = new float[16];

    public YBitmapOrthogonalRender(Context context) {
        this.mContext = context;

        //顶点坐标
        float[] vertexData = {
                -1f, -1f,
                1f, -1f,
                -1f, 1f,
                1f, 1f
        };
        //读取顶点坐标
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertexData);
        vertexBuffer.position(0);

        //纹理坐标
        float[] fragmentData = {
                0f, 1f,
                1f, 1f,
                0f, 0f,
                1f, 0f
        };
        //读取纹理坐标
        fragmentBuffer = ByteBuffer.allocateDirect(fragmentData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(fragmentData);
        fragmentBuffer.position(0);

    }

    @Override
    public void onSurfaceCreated() {
        //加载顶点着色器 shader
        String vertexSource = YShaderUtil.getRawResource(mContext, R.raw.screen_vert_matrix);
        //加载片元着色器 shader
        String fragmentSource = YShaderUtil.getRawResource(mContext, R.raw.screen_frag);
        //获取源程序
        program = YShaderUtil.createProgram(vertexSource, fragmentSource);
        //从渲染程序中得到着顶点色器中的属性
        vPosition = GLES20.glGetAttribLocation(program, "vPosition");
        //从渲染程序中得到片元着色器中的属性
        fPosition = GLES20.glGetAttribLocation(program, "fPosition");
        //从渲染程序中得到投影的属性
        u_matrix = GLES20.glGetUniformLocation(program, "u_Matrix");

        //创建 1个纹理,放入到 int [] textureIds, 一共有 30多个 纹理
        textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);//第三个参数是指从哪儿开始取
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0]);

        //设置环绕方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        //设置过滤方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        //设置窗口大小
        GLES20.glViewport(0, 0, width, height);
        //只获取一下要加载图片的大小，不加载
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(mContext.getResources(), R.drawable.nobb, options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;

        if (width > height) {
            Matrix.orthoM(matrix, 0, -width / ((height / imageHeight * 1f) * imageWidth * 1f), width / ((height / imageHeight * 1f) * imageWidth * 1f), -1f, 1f, -1f, 1f);
        } else {
            Matrix.orthoM(matrix, 0, -1, 1, -height / ((width / imageWidth * 1f) * imageHeight * 1f), height / ((width / imageWidth * 1f) * imageHeight * 1f), -1f, 1f);
        }

    }

    @Override
    public void onDrawFrame() {
        //清除屏幕，此处用的是红色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1f, 0f, 0f, 1f);

        //使用着色器源程序
        GLES20.glUseProgram(program);
        GLES20.glUniformMatrix4fv(u_matrix, 1, false, matrix, 0);

        //使能顶点属性数组，使之有效
        GLES20.glEnableVertexAttribArray(vPosition);
        //使能之后，为顶点属性赋值，绑定顶点坐标
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer);
        //使能片元属性数组，使之有效
        GLES20.glEnableVertexAttribArray(fPosition);
        //使能之后，为片元属性赋值，绑定纹理坐标
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, fragmentBuffer);

        //激活纹理 0号
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //绑定 2D纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0]);
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.nobb);
        //设置图片 bitmap
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();//用完及时回收啊

        //绘制图形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        //解绑 2D纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);


    }
}
