# 最通俗易懂的OpenGL 教程，你学废了吗？

## 1.OpenGL的简单使用
>OpenGL（英语：Open Graphics Library，译名：开放图形库或者“开放式图形库”）是用于渲染2D、3D矢量图形的跨语言、跨平台的应用程序编程接口（API）。
这个接口由近350个不同的函数调用组成，用来绘制从简单的图形比特到复杂的三维景象。学好了OpenGL ，就可以在那些支持OpenGL的机器上正常使用这些接口，在屏幕上看到绘制的结果

1. 继承GLSurfaceView
2. 实现接口 GLSurfaceView.Renderer
3. 编写glsl脚本（shader）

## 2.搭建EGL环境
>EGL 是OpenGL ES和本地窗口系统的接口，不同平台上EGL配置是不一样的，而
 OpenGL的调用方式是一致的，就是说：OpenGL跨平台就是依赖于EGL接口。

 OpenGL整体是一个状态机，通过改变状态就能改变后续的渲染方式，而
 EGLContext（EgL上下文）就保存有所有状态，因此可以通过共享EGLContext
 来实现同一场景渲染到不同的Surface上

1. 获取Egl实例
2. 获取默认的显示设备（就是窗口）
3. 初始化默认显示设备
4. 设置显示设备的属性
5. 从系统中获取对应属性的配置
6. 创建EglContext
7. 创建渲染的Surface
8. 绑定EglContext和Surface到显示设备中

## 3.OpenGL渲染图片纹理

1. 编写着色器（顶点着色器和片元着色器）
2. 设置顶点. 纹理坐标
3. 加载着色器 （shader）
4. 创建纹理
5. 渲染图片

### 3.1 OpenGL加载着色器 shader

```java

1.创建shader（着色器：顶点或片元）

    int shader = GLES20.glCreateShader(shaderType);

2.加载shader源码并编译shader

    GLES20.glShaderSource(shader, source);
    GLES20.glCompileShader(shader);

3.检查是否编译成功：

    GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);

4.创建一个渲染程序：

    int program = GLES20.glCreateProgram();

5.将着色器程序添加到渲染程序中：

    GLES20.glAttachShader(program, vertexShader);

6.链接源程序：

    GLES20.glLinkProgram(program);

7.检查链接源程序是否成功

    GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);

8.得到着色器中的属性：

    int vPosition= GLES20.glGetAttribLocation(program, "v_Position");

9.使用源程序：

    GLES20.glUseProgram(program);

10.使顶点属性数组有效：

    GLES20.glEnableVertexAttribArray(vPosition);

11.为顶点属性赋值：

    GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8,vertexBuffer);

12.绘制图形：

    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

```
### 3.2 OpenGL ES绘制纹理过程

```java

1. 加载shader和生成program, 见2中的过程

2. 创建和绑定纹理

    GLES20.glGenTextures(1, textureId, 0);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureid);

3. 设置环绕和过滤方式

    //环绕（超出纹理坐标范围）：（s==x t==y GL_REPEAT 重复）
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

    //过滤（纹理像素映射到坐标点）：（缩小. 放大：GL_LINEAR线性）
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

4.设置图片（bitmap）

    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

5.绑定顶点坐标和纹理坐标

6.绘制图形

    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

 ```

## 4 OpenGL VBO 顶点缓冲对象

> OpenGL VBO 即顶点缓冲对象 ，目的是提高顶点坐标获取的效率,
不使用 VBO时，每次绘制（ glDrawArrays ）图形时都是从本地内存处获取顶点数据然后传输给 OpenGL来绘制，这样就会频繁的操作 CPU->GPU增大开销，从而降低效率。
使用 VBO时，能把顶点数据缓存到GPU开辟的一段内存中，然后使用时不必再从本地获取，而是直接从显存中获取，这样就能提升绘制的效率。


### 4.1 VBO创建

```java

1. 创建VBO

    GLES20.glGenBuffers(1, vbos, 0);

2. 绑定VBO

    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbos[0]);

3. 分配VBO需要的缓存大小

    GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertex.length * 4,null, GLES20. GL_STATIC_DRAW);

4. 为VBO设置顶点数据的值

    GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexData.length * 4, vertexBuffer);

5. 解绑VBO

    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

```

### 4.2 VBO使用
```java


1. 绑定VBO

    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbos[0]);

2. 设置顶点数据

    GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, 0);

3. 解绑VBO

    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

```
 ## 5 OpenGL FBO 帧缓冲对象

> 当需要对纹理进行多次渲染时，而这些渲染采样是不需要展示给用户看的，就可以用一个单独的缓冲对象（离屏渲染）
来存储多次渲染采样的结果，等处理完后再显示到窗口上。

 ### 5.1 FBO创建


 ```java
 1. 创建FBO

    GLES20.glGenBuffers(1, fbos, 0);

 2. 绑定FBO

     GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbos[0]);

 3. 设置FBO分配内存大小

    GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 720, 1280, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

 4. 把纹理绑定到FBO

     GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textureid, 0);

 5. 检查FBO绑定是否成功

    GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE)

 6. 解绑FBO

    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

 ```

### 5.2 FBO使用

```java

1. 绑定FBO

    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbos[0]);

2. 获取需要绘制的图片纹理，然后绘制渲染

3. 解绑FBO

    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

4. 再把绑定到FBO的纹理绘制渲染出来


```