package com.york.media.opengl.egl;

import android.content.Context;
import android.opengl.GLES20;

import com.york.media.opengl.utils.LogUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class YShaderUtil {

    /**
     * 从资源文件夹 raw 读取 shader
     *
     * @param context 上下文
     * @param rawId   资源 ID
     * @return shader程序代码的字符串
     */
    public static String getRawResource(Context context, int rawId) {
        InputStream inputStream = context.getResources().openRawResource(rawId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuffer sb = new StringBuffer();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * 加载 、编译 shader源程序
     *
     * @param shaderType 着色器类型 GL_VERTEX_SHADER ：顶点 ；GL_FRAGMENT_SHADER：片元
     * @param source     shader 代码的字符串
     * @return 返回 ID
     */
    private static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            //加载 shader
            GLES20.glShaderSource(shader, source);
            //编译 shader
            GLES20.glCompileShader(shader);

            //检查是否编译成功
            int[] compile = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compile, 0);
            if (compile[0] != GLES20.GL_TRUE) {
                LogUtil.e("shader compile error");
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
            //编译成功返回 shader ID
            return shader;
        } else {
            return 0;
        }
    }

    public static int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);

        if (vertexShader != 0 && fragmentShader != 0) {
            //创建一个渲染程序 program
            int program = GLES20.glCreateProgram();
            //将顶点着色器程序添加到渲染程序中
            GLES20.glAttachShader(program, vertexShader);
            //将片元着色器程序也添加到渲染程序中
            GLES20.glAttachShader(program, fragmentShader);
            //链接源程序
            GLES20.glLinkProgram(program);

            return program;
        }
        return 0;
    }

}
