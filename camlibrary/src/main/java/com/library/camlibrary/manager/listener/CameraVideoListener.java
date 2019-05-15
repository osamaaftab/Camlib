package com.library.camlibrary.manager.listener;

import com.library.camlibrary.utils.Size;

import java.io.File;

public interface CameraVideoListener {
    void onVideoRecordStarted(Size videoSize);

    void onVideoRecordStopped(File videoFile);

    void onVideoRecordError();
}
