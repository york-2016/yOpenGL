package com.york.media.opengl.demo.camera.api1;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.util.Size;

import com.york.media.opengl.utils.LogUtil;

import java.util.List;

import androidx.annotation.RequiresApi;

/**
 * author : York
 * date   : 2020/12/21 1:25
 * desc   : Camera1 的 简单工具类
 */
public class YCamera1 {

    private Camera mCamera;
    private int mCameraId;
    private int mZoom;
    private int width;
    private int height;

    private OnCameraListener mOnCameraListener = null;
    private SurfaceTexture surfaceTexture;
    private boolean isFacesListener = false;

    public YCamera1(Context context, int width, int height) {
        this.width = width;
        this.height = height;
    }


    public void initCamera(SurfaceTexture surfaceTexture, int mCameraId) {
        this.surfaceTexture = surfaceTexture;
        setCameraParameters(mCameraId);
    }

    public Camera getCamera() {
        return mCamera;
    }

    public int getCameraId() {
        return mCameraId;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }


    /**
     * 获取支持的场景模式
     *
     * @return supportSceneMode
     */
    public List<String> getSupportedSceneModes() {
        if (mCamera == null) {
            return null;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters == null) {
            return null;
        }
        List<String> supportSceneMode = parameters.getSupportedSceneModes();
        for (int i = 0; i < supportSceneMode.size(); i++) {
            LogUtil.d("supportSceneMode : " + i + " : " + supportSceneMode.get(i));
        }
        return supportSceneMode;
    }

    /**
     * 设置场景模式
     *
     * @param sceneMode 场景模式
     */
    public void setSceneMode(String sceneMode) {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters == null) {
            return;
        }
        List<String> supportSceneMode = parameters.getSupportedSceneModes();

        if (supportSceneMode.contains(sceneMode)) {
            parameters.setSceneMode(sceneMode);
            mCamera.setParameters(parameters);
        } else {
            LogUtil.e(sceneMode + " --> mode is not supported");
        }
    }

    /**
     * 获取场景模式
     *
     * @return mode
     */
    public String getSceneMode() {
        String mode = "";
        if (mCamera != null) {
            mode = mCamera.getParameters().getSceneMode();
        }
        return mode;
    }

    /**
     * 切换摄像头
     *
     * @param mCameraId 摄像头序号
     */
    public void changeCamera(int mCameraId) {
        if (mCamera != null) {
            stopPreview();
        }
        setCameraParameters(mCameraId);
    }


    public void setFacesListener(boolean isBack) {
        isFacesListener = isBack;
    }

    /**
     * 设置相机参数
     *
     * @param mCameraId 摄像头序号
     */
    private void setCameraParameters(int mCameraId) {
        this.mCameraId = mCameraId;
        try {
            mCamera = Camera.open(mCameraId);
            if (mCamera != null) {
                mCamera.setPreviewTexture(surfaceTexture);
                Camera.Parameters parameters = mCamera.getParameters();
                if (parameters != null) {
                    parameters.setFlashMode("off");
                    parameters.setPreviewFormat(ImageFormat.NV21);//yuv420
                    Camera.Size size = getFitSize(parameters.getSupportedPictureSizes());
                    parameters.setPictureSize(size.width, size.height);
                    size = getFitSize(parameters.getSupportedPreviewSizes());
                    parameters.setPreviewSize(size.width, size.height);
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    mCamera.setParameters(parameters);
                    if (mOnCameraListener != null) {
                        mOnCameraListener.created();
                    }
                    mCamera.startPreview();
                }
                if (isFacesListener) {
                    mCamera.setFaceDetectionListener(new Camera.FaceDetectionListener() {
                        @Override
                        public void onFaceDetection(Camera.Face[] faces, Camera camera) {

                            if (mOnCameraListener != null) {
                                mOnCameraListener.onFaceDetection(faces, camera);
                            }
                        }
                    });
                    mCamera.startFaceDetection();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止预览
     */
    public void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Size getPreViewSize() {
        Size fitSize = null;
        try {
            mCamera = Camera.open(mCameraId);
            if (mCamera != null) {
                mCamera.setPreviewTexture(surfaceTexture);
                Camera.Parameters parameters = mCamera.getParameters();
                if (parameters != null) {
                    Camera.Size size = getFitSize(parameters.getSupportedPictureSizes());
                    fitSize = new Size(size.width, size.height);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fitSize;
    }

    /**
     * 获取合适的大小
     *
     * @param sizes sizes
     * @return 合适的大小
     */
    private Camera.Size getFitSize(List<Camera.Size> sizes) {
        if (width < height) {
            int t = height;
            height = width;
            width = t;
        }
        for (Camera.Size size : sizes) {
            if (1.0f * size.width / size.height == 1.0f * width / height) {
                return size;
            }
        }
        return sizes.get(0);
    }

    /**
     * 设置闪光灯
     *
     * @param turnSwitch 开关
     * @return turnSwitch
     */
    public int setFlashLight(int turnSwitch) {
        if (mCamera == null) {
            return -1;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters == null) {
            return -1;
        }
        if (turnSwitch == 0) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        } else if (turnSwitch == 1) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
        } else if (turnSwitch == 2) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        } else if (turnSwitch == 3) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        }
        mCamera.setParameters(parameters);
        return turnSwitch;
    }

    /**
     * 获取闪光灯状态
     *
     * @return turnSwitch
     */
    public int getFlashMode() {
        if (mCamera == null) {
            return -1;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters == null) {
            return -1;
        }
        int turnSwitch = -1;
        if (parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)) {
            turnSwitch = 0;
        } else if (parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_ON)) {
            turnSwitch = 1;
        } else if (parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_AUTO)) {
            turnSwitch = 2;
        } else if (parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
            turnSwitch = 3;
        }
        return turnSwitch;
    }

    /**
     * 设置曝光度
     */
    public void setExposure(int value) {
        if (mCamera == null) {
            return;
        }
        float present = value / 100f;
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters == null) {
            return;
        }
        int minExposure = parameters.getMinExposureCompensation();
        int maxExposure = parameters.getMaxExposureCompensation();
        int value_real = (int) (present * (maxExposure - minExposure)) + minExposure;
        parameters.setExposureCompensation(value_real);
        mCamera.setParameters(parameters);
    }

    /**
     * 获取曝光度
     *
     * @return 曝光度
     */
    public int getExposure() {
        if (mCamera == null) {
            return 0;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters == null) {
            return 0;
        }
        int value_real = parameters.getExposureCompensation();
        int maxExposure = parameters.getMaxExposureCompensation();
        float percent = 0;
        if (value_real > 0) {
            percent = value_real / (maxExposure * 2f) + 0.5f;
        } else if (value_real < 0) {
            percent = 0.5f + value_real / (maxExposure * 2f);
        } else {
            percent = 0.5f;
        }
        int value = (int) (percent * 100);
        return value;
    }


    /**
     * 变焦
     *
     * @param zoom 缩放系数
     */
    public void setZoom(int zoom) {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters parameters;
        parameters = mCamera.getParameters();
        if (!parameters.isZoomSupported()) {
            return;
        }
        parameters.setZoom(zoom);
        mCamera.setParameters(parameters);
        mZoom = zoom;
    }

    /**
     * 返回当前焦距
     *
     * @return 返回缩放值
     */
    public int getZoom() {
        return mZoom;
    }

    /**
     * 获取最大焦距
     *
     * @return zoom
     */
    public int getMaxZoom() {
        if (mCamera == null) {
            return -1;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if (!parameters.isZoomSupported()) {
            return -1;
        }
        return parameters.getMaxZoom() > 50 ? 50 : parameters.getMaxZoom();
    }

    /**
     * 设置相机参数
     *
     * @param mOnCameraListener OnCameraListener
     */
    public void setCameraListener(OnCameraListener mOnCameraListener) {
        this.mOnCameraListener = mOnCameraListener;
    }

    public interface OnCameraListener {
        void created();

        void onFaceDetection(Camera.Face[] faces, Camera camera);
    }
}
