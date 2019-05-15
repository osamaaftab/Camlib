package com.library.camlibrary;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.library.camlibrary.configuration.CameraConfiguration;
import com.library.camlibrary.manager.CameraOutputModel;
import com.library.camlibrary.ui.camera.Camera1Activity;
import com.library.camlibrary.ui.camera2.Camera2Activity;
import com.library.camlibrary.utils.CameraHelper;
import com.library.camlibrary.utils.CamLibBus;

import java.util.List;

import io.reactivex.functions.Consumer;

public class CamLibrary {

    private static CamLibrary mInstance = null;
    private static Activity mActivity;
    private int mediaAction = CameraConfiguration.MEDIA_ACTION_BOTH;
    private boolean showPicker = true;
    private boolean autoRecord = false;
    private int type = 501;
    private boolean enableImageCrop = false;
    private long videoSize = -1;
    private int videoDur = -1;

    public static CamLibrary with(Activity activity) {
        if (mInstance == null) {
            mInstance = new CamLibrary();
        }
        mActivity = activity;
        return mInstance;
    }

    /*public CamLibrary setShowPickerType(int type) {
        this.type = type;
        return mInstance;
    }*/

    public CamLibrary setShowPicker(boolean showPicker) {
        this.showPicker = showPicker;
        return mInstance;
    }

    public CamLibrary setMediaAction(int mediaAction) {
        this.mediaAction = mediaAction;
        return mInstance;
    }

    /*public CamLibrary enableImageCropping(boolean enableImageCrop) {
        this.enableImageCrop = enableImageCrop;
        return mInstance;
    }*/

    @SuppressWarnings("SameParameterValue")
    public CamLibrary setVideoFileSize(int fileSizeInMb) {
        this.videoSize = fileSizeInMb;
        return mInstance;
    }

    public CamLibrary setVideoDuration(int video_duration_in_seconds) {
        this.videoDur = (int) (video_duration_in_seconds * 1000.0);
        return mInstance;
    }

    /**
     * Only works if Media Action is set to Video
     */
    public CamLibrary setAutoRecord() {
        autoRecord = true;
        return mInstance;
    }

    public void launchCamera(final CameraCallback cameraCallback) {
        Dexter.withActivity(mActivity)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        launchIntent();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                    }
                }).check();

        CamLibBus.getBus()
                .toObserverable()
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        if (o instanceof CameraOutputModel) {
                            CameraOutputModel outputModel = (CameraOutputModel) o;
                            if (cameraCallback != null) {
                                cameraCallback.onComplete(outputModel);
                                mInstance = null;
                            }
                            CamLibBus.complete();
                        }
                    }
                });
    }

    private void launchIntent() {
        if (CameraHelper.hasCamera(mActivity)) {
            Intent cameraIntent;
            if (CameraHelper.hasCamera2(mActivity)) {
                cameraIntent = new Intent(mActivity, Camera2Activity.class);
            } else {
                cameraIntent = new Intent(mActivity, Camera1Activity.class);
            }
            cameraIntent.putExtra(CameraConfiguration.Arguments.SHOW_PICKER, showPicker);
            cameraIntent.putExtra(CameraConfiguration.Arguments.PICKER_TYPE, type);
            cameraIntent.putExtra(CameraConfiguration.Arguments.MEDIA_ACTION, mediaAction);
            cameraIntent.putExtra(CameraConfiguration.Arguments.ENABLE_CROP, enableImageCrop);
            cameraIntent.putExtra(CameraConfiguration.Arguments.AUTO_RECORD, autoRecord);

            if (videoSize > 0) {
                cameraIntent.putExtra(CameraConfiguration.Arguments.VIDEO_FILE_SIZE, videoSize * 1024 * 1024);
            }

            if(videoDur > 0){
                cameraIntent.putExtra(CameraConfiguration.Arguments.VIDEO_DURATION, videoDur);
            }
            mActivity.startActivity(cameraIntent);
        }
    }

    public interface CameraCallback {
        void onComplete(CameraOutputModel cameraOutputModel);
    }

    public class MediaType {
        public static final int PHOTO = 0;
        public static final int VIDEO = 1;
    }
}
