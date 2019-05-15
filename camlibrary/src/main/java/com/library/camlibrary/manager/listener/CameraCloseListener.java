package com.library.camlibrary.manager.listener;

public interface CameraCloseListener<CameraId> {
    void onCameraClosed(CameraId closedCameraId);
}
