package com.york.media.opengl.demo.camera.api2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;
import android.view.WindowManager;

import com.york.media.opengl.demo.camera.YCameraRender;
import com.york.media.opengl.demo.fbo.YUsedFboRender;
import com.york.media.opengl.utils.LogUtil;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

/**
 * author : York
 * date   : 2020/12/20 20:26
 * desc   : Camera2 的简单工具类
 */
public class YCamera2 {

    private final Context mContext;

    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCameraCaptureSession;

    private Handler mainHandler;
    private SurfaceTexture mSurfaceTexture;
    private YCameraRender mYCameraRender;

    public YCamera2(Context context) {
        this.mContext = context;
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void initCamera(SurfaceTexture surfaceTexture, YCameraRender yCameraRender) {
        this.mSurfaceTexture = surfaceTexture;
        this.mYCameraRender = yCameraRender;
        startPreView();
    }

    private void startPreView() {
        CameraManager manager = (CameraManager) mContext.getSystemService(mContext.CAMERA_SERVICE);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            LogUtil.e("no CAMERA permission");
            return;
        }
        try {
            manager.openCamera("0", new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice cameraDevice) {
                    mCameraDevice = cameraDevice;
                    initCharacteristics(mCameraDevice);
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice cameraDevice) {
                    LogUtil.e("onDisconnected");
                }

                @Override
                public void onError(@NonNull CameraDevice cameraDevice, int i) {
                    LogUtil.e("open CAMERA failed");
                }
            }, mainHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initCharacteristics(CameraDevice mCameraDevice) {
        Surface surface = new Surface(mSurfaceTexture);
        try {
            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mCameraCaptureSession = session;
                    try {
                        CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                        builder.addTarget(surface);
                        mCameraCaptureSession.setRepeatingRequest(builder.build(), mCaptureCallback, mainHandler);
                        previewAngle(mContext,mCameraDevice);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    LogUtil.e("onConfigureFailed");
                }
            }, mainHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private final CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {

        }

    };
    /**
     * 矩阵调节图像角度
     *
     * @param context 上下文获取当前旋转角度
     */
    public void previewAngle(Context context,CameraDevice cameraDevice) {
        int angle = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        mYCameraRender.resetMatrix();
        switch (angle) {
            case Surface.ROTATION_0:
                if (cameraDevice.getId().equals("0") ) {
                    mYCameraRender.setAngle(90, 0, 0, 1);
                    mYCameraRender.setAngle(180, 1, 0, 0);
                } else {
                    mYCameraRender.setAngle(90, 0, 0, 1);
                }

                break;
            case Surface.ROTATION_90:
                if (cameraDevice.getId().equals("0") ) {
                    mYCameraRender.setAngle(180, 0, 0, 1);
                    mYCameraRender.setAngle(180, 0, 1, 0);
                } else {
                    mYCameraRender.setAngle(90f, 0f, 0f, 1f);
                }
                break;
            case Surface.ROTATION_180:
                if (cameraDevice.getId().equals("0") ) {
                    mYCameraRender.setAngle(90f, 0.0f, 0f, 1f);
                    mYCameraRender.setAngle(180f, 0.0f, 1f, 0f);
                } else {
                    mYCameraRender.setAngle(-90, 0f, 0f, 1f);
                }
                break;
            case Surface.ROTATION_270:
                if (cameraDevice.getId().equals("0") ) {
                    mYCameraRender.setAngle(180f, 0.0f, 1f, 0f);
                } else {
                    mYCameraRender.setAngle(0f, 0f, 0f, 1f);
                }
                break;
        }
    }
    public void onDestroy() {

        if (null != mCameraCaptureSession) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }
        if (null != mCameraDevice) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if (mainHandler != null) {
            mainHandler = null;
        }
    }
}
