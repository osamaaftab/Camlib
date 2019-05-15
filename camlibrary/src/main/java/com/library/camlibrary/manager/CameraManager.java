package com.library.camlibrary.manager;

import android.content.Context;

import com.library.camlibrary.configuration.CameraConfiguration;
import com.library.camlibrary.configuration.ConfigurationProvider;
import com.library.camlibrary.manager.listener.CameraCloseListener;
import com.library.camlibrary.manager.listener.CameraOpenListener;
import com.library.camlibrary.manager.listener.CameraPhotoListener;
import com.library.camlibrary.manager.listener.CameraVideoListener;
import com.library.camlibrary.utils.Size;

import java.io.File;

public interface CameraManager<CameraId, SurfaceListener> {

    void initializeCameraManager(ConfigurationProvider configurationProvider, Context context);

    void openCamera(CameraId cameraId, CameraOpenListener<CameraId, SurfaceListener> cameraOpenListener);

    void closeCamera(CameraCloseListener<CameraId> cameraCloseListener);

    void takePhoto(File photoFile, CameraPhotoListener cameraPhotoListener);

    void startVideoRecord(File videoFile, CameraVideoListener cameraVideoListener);

    Size getPhotoSizeForQuality(@CameraConfiguration.MediaQuality int mediaQuality);

    void setFlashMode(@CameraConfiguration.FlashMode int flashMode);

    void stopVideoRecord();

    void releaseCameraManager();

    CameraId getCurrentCameraId();

    CameraId getFaceFrontCameraId();

    CameraId getFaceBackCameraId();

    int getNumberOfCameras();

    int getFaceFrontCameraOrientation();

    int getFaceBackCameraOrientation();

    boolean isVideoRecording();
}
