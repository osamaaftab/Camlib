package com.library.camlibrary.manager.listener;

import com.library.camlibrary.utils.Size;

public interface CameraOpenListener<CameraId, SurfaceListener> {
    void onCameraOpened(CameraId openedCameraId, Size previewSize, SurfaceListener surfaceListener);

    void onCameraOpenError();
}
