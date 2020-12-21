package com.york.media.opengl.egl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;

/**
 * author : York
 * date   : 2020/12/20 20:13
 * desc   : 纹理工具
 */
public class TextureUtils {

    /**
     * 加载图片为 2D纹理
     *
     * @param context 上下文
     * @param rawID   资源ID
     * @return 生成的纹理
     */
    public static int createImageTexture(Context context, int rawID) {
        //生产一个纹理
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        //绑定为 2D纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0]);
        //设置环绕模式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        //设置过滤模式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), rawID);
        //绑定 bitmap到 textureIds[0] 这个2D纹理上
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        //退出 纹理的设置，进入下一环节
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return textureIds[0];

    }

    /**
     * 设置文字水印
     *
     * @param text      文本内容
     * @param textSize  文字大小
     * @param textColor 文字颜色
     * @param bgColor   文字背景颜色 #00000000
     * @param padding   文字与边距距离
     * @return 文字水印的 bitmap
     */
    public static Bitmap createTextImage(String text, int textSize, String textColor, String bgColor, int padding) {

        Paint paint = new Paint();
        paint.setColor(Color.parseColor(textColor));
        paint.setTextSize(textSize);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        float width = paint.measureText(text, 0, text.length());
        float top = paint.getFontMetrics().top;
        float bottom = paint.getFontMetrics().bottom;

        Bitmap bm = Bitmap.createBitmap((int) (width + padding * 2), (int) ((bottom - top) + padding * 2), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        canvas.drawColor(Color.parseColor(bgColor));
        canvas.drawText(text, padding, -top + padding, paint);
        return bm;
    }

    /**
     * 加载 bitmap为 2D纹理
     *
     * @param bitmap 图片 bitmap
     * @return 纹理
     */
    public static int loadBitmapTexture(Bitmap bitmap) {
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        ByteBuffer bitmapBuffer = ByteBuffer.allocate(bitmap.getHeight() * bitmap.getWidth() * 4);
        bitmap.copyPixelsToBuffer(bitmapBuffer);
        bitmapBuffer.flip();

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap.getWidth(),
                bitmap.getHeight(), 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, bitmapBuffer);
        return textureIds[0];
    }

}
