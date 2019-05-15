package com.library.camlibrary.configuration;

public interface ConfigurationProvider {

    @CameraConfiguration.MediaAction
    int getMediaAction();

    @CameraConfiguration.MediaQuality
    int getMediaQuality();

    int getVideoDuration();

    long getVideoFileSize();

    @CameraConfiguration.SensorPosition
    int getSensorPosition();

    int getDegrees();

    int getMinimumVideoDuration();

    @CameraConfiguration.FlashMode
    int getFlashMode();

}
