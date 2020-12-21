package com.york.media.opengl.demo.camera.api1;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.WindowManager;

import com.york.media.opengl.demo.camera.YCameraRender;
import com.york.media.opengl.egl.YGLSurfaceView;
import com.york.media.opengl.utils.DisplayUtil;
import com.york.media.opengl.utils.LogUtil;

/**
 * author : York
 * date   : 2020/12/20 14:04
 * desc   : Camera1 的预览控件
 */
public class YCamera1View extends YGLSurfaceView {

    private YCameraRender yCameraRender;
    private YCamera1 yCamera1;
    private final int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    public YCamera1View(Context context) {
        this(context, null);
    }

    public YCamera1View(Context context, AttributeSet attrs) {
        super(context, attrs);
        int width = DisplayUtil.getScreenWidth(context);
        int height = DisplayUtil.getScreenHeight(context);
        LogUtil.d("width:" + width + ",height:" + height);
        yCamera1 = new YCamera1(context, width, height);
        yCameraRender = new YCameraRender(context, width, height);
        previewAngle(context);
        yCameraRender.setOnSurfaceCreateListener(new YCameraRender.OnSurfaceCreateListener() {
            @Override
            public void onSurfaceCreate(SurfaceTexture surfaceTexture, int tid) {
                yCamera1.initCamera(surfaceTexture, cameraId);
            }
        });
        setRender(yCameraRender);
    }

    /**
     * 关闭预览
     */
    public void stopPreView() {
        if (yCamera1 != null) {
            yCamera1.stopPreview();
            yCameraRender.updatePreviewStates(true);
        }
    }

    /**
     * 矩阵调节图像角度
     *
     * @param context 上下文获取当前旋转角度
     */
    public void previewAngle(Context context) {
        int angle = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        yCameraRender.resetMatrix();
        switch (angle) {
            case Surface.ROTATION_0:
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    yCameraRender.setAngle(90, 0, 0, 1);
                    yCameraRender.setAngle(180, 1, 0, 0);
                } else {
                    yCameraRender.setAngle(90, 0, 0, 1);
                }

                break;
            case Surface.ROTATION_90:
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    yCameraRender.setAngle(180, 0, 0, 1);
                    yCameraRender.setAngle(180, 0, 1, 0);
                } else {
                    yCameraRender.setAngle(90f, 0f, 0f, 1f);
                }
                break;
            case Surface.ROTATION_180:
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    yCameraRender.setAngle(90f, 0.0f, 0f, 1f);
                    yCameraRender.setAngle(180f, 0.0f, 1f, 0f);
                } else {
                    yCameraRender.setAngle(-90, 0f, 0f, 1f);
                }
                break;
            case Surface.ROTATION_270:
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    yCameraRender.setAngle(180f, 0.0f, 1f, 0f);
                } else {
                    yCameraRender.setAngle(0f, 0f, 0f, 1f);
                }
                break;
        }
    }

    public void onDestroy() {
        if (yCamera1 != null) {
            yCamera1.stopPreview();
            yCamera1 = null;
            yCameraRender = null;
        }
    }
}
