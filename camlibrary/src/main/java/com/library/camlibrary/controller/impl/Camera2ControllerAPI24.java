package com.library.camlibrary.controller.impl;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;

import com.library.camlibrary.configuration.CameraConfiguration;
import com.library.camlibrary.configuration.ConfigurationProvider;
import com.library.camlibrary.controller.CameraController;
import com.library.camlibrary.controller.view.CameraView;
import com.library.camlibrary.manager.CameraManager;
import com.library.camlibrary.manager.impl.Camera1Manager;
import com.library.camlibrary.manager.listener.CameraCloseListener;
import com.library.camlibrary.manager.listener.CameraOpenListener;
import com.library.camlibrary.manager.listener.CameraPhotoListener;
import com.library.camlibrary.manager.listener.CameraVideoListener;
import com.library.camlibrary.ui.view.AutoFitSurfaceView;
import com.library.camlibrary.utils.CameraHelper;
import com.library.camlibrary.utils.Size;

import java.io.File;

@TargetApi(Build.VERSION_CODES.N)
public class Camera2ControllerAPI24 implements CameraController<String>,
        CameraOpenListener<Integer, SurfaceHolder.Callback>,
        CameraPhotoListener, CameraVideoListener, CameraCloseListener<Integer> {

    private final static String TAG = "Camera2Controller";

    private String currentCameraId;
    private ConfigurationProvider configurationProvider;
    private CameraManager<Integer, SurfaceHolder.Callback> camera2Manager;
    private CameraView cameraView;

    private File outputFile;

    public Camera2ControllerAPI24(CameraView cameraView, ConfigurationProvider configurationProvider) {
        this.cameraView = cameraView;
        this.configurationProvider = configurationProvider;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        camera2Manager = Camera1Manager.getInstance();
        camera2Manager.initializeCameraManager(configurationProvider, cameraView.getActivity());
        currentCameraId = String.valueOf(camera2Manager.getFaceBackCameraId());
    }

    @Override
    public void onResume() {
        camera2Manager.openCamera(Integer.valueOf(currentCameraId), this);
    }

    @Override
    public void onPause() {
        camera2Manager.closeCamera(null);
        cameraView.releaseCameraPreview();
    }

    @Override
    public void onDestroy() {
        camera2Manager.releaseCameraManager();
    }

    @Override
    public void takePhoto() {
        outputFile = CameraHelper.getOutputMediaFile(cameraView.getActivity(), CameraConfiguration.MEDIA_ACTION_PHOTO);
        camera2Manager.takePhoto(outputFile, this);
    }

    @Override
    public void startVideoRecord() {
        outputFile = CameraHelper.getOutputMediaFile(cameraView.getActivity(), CameraConfiguration.MEDIA_ACTION_VIDEO);
        camera2Manager.startVideoRecord(outputFile, this);
    }

    @Override
    public void stopVideoRecord() {
        camera2Manager.stopVideoRecord();
    }

    @Override
    public boolean isVideoRecording() {
        return camera2Manager.isVideoRecording();
    }

    @Override
    public void switchCamera(final @CameraConfiguration.CameraFace int cameraFace) {
        currentCameraId = String.valueOf(camera2Manager.getCurrentCameraId().equals(camera2Manager.getFaceFrontCameraId()) ?
                camera2Manager.getFaceBackCameraId() : camera2Manager.getFaceFrontCameraId());

        camera2Manager.closeCamera(this);
    }

    @Override
    public void setFlashMode(@CameraConfiguration.FlashMode int flashMode) {
        camera2Manager.setFlashMode(flashMode);
    }

    @Override
    public void switchQuality() {
        camera2Manager.closeCamera(this);
    }

    @Override
    public int getNumberOfCameras() {
        return camera2Manager.getNumberOfCameras();
    }

    @Override
    public int getMediaAction() {
        return configurationProvider.getMediaAction();
    }

    @Override
    public File getOutputFile() {
        return outputFile;
    }

    @Override
    public String getCurrentCameraId() {
        return currentCameraId;
    }

    @Override
    public void onCameraOpened(Integer openedCameraId, Size previewSize, SurfaceHolder.Callback surfaceTextureListener) {
        cameraView.updateUiForMediaAction(CameraConfiguration.MEDIA_ACTION_BOTH);
        cameraView.updateCameraPreview(previewSize, new AutoFitSurfaceView(cameraView.getActivity(), surfaceTextureListener));
        cameraView.updateCameraSwitcher(camera2Manager.getNumberOfCameras());
    }

    @Override
    public void onCameraOpenError() {
        Log.e(TAG, "onCameraOpenError");
    }

    @Override
    public void onCameraClosed(Integer closedCameraId) {
        cameraView.releaseCameraPreview();

        camera2Manager.openCamera(Integer.valueOf(currentCameraId), this);
    }

    @Override
    public void onPhotoTaken(File photoFile) {
        cameraView.onPhotoTaken();
    }

    @Override
    public void onPhotoTakeError() {
    }

    @Override
    public void onVideoRecordStarted(Size videoSize) {
        cameraView.onVideoRecordStart(videoSize.getWidth(), videoSize.getHeight());
    }

    @Override
    public void onVideoRecordStopped(File videoFile) {
        cameraView.onVideoRecordStop();
    }

    @Override
    public void onVideoRecordError() {

    }

    @Override
    public CameraManager getCameraManager() {
        return camera2Manager;
    }
}
