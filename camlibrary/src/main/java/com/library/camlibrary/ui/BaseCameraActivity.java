package com.library.camlibrary.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.RestrictTo;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.library.camlibrary.R;
import com.library.camlibrary.CamLibrary;
import com.library.camlibrary.configuration.CameraConfiguration;
import com.library.camlibrary.manager.CameraOutputModel;
import com.library.camlibrary.ui.model.PhotoQualityOption;
import com.library.camlibrary.ui.model.VideoQualityOption;
import com.library.camlibrary.ui.preview.PreviewActivity;
import com.library.camlibrary.ui.view.CameraControlPanel;
import com.library.camlibrary.ui.view.CameraSwitchView;
import com.library.camlibrary.ui.view.FlashSwitchView;
import com.library.camlibrary.ui.view.ImageGalleryAdapter;
import com.library.camlibrary.ui.view.MediaActionSwitchView;
import com.library.camlibrary.ui.view.RecordButton;
import com.library.camlibrary.utils.CamLibBus;
import com.library.camlibrary.utils.Size;
import com.library.camlibrary.utils.Utils;
import com.yalantis.ucrop.util.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public abstract class BaseCameraActivity<CameraId> extends CameraLibActivity<CameraId>
        implements
        RecordButton.RecordButtonListener,
        FlashSwitchView.FlashModeSwitchListener,
        MediaActionSwitchView.OnMediaActionStateChangeListener,
        CameraSwitchView.OnCameraTypeChangeListener, CameraControlPanel.SettingsClickListener,
        CameraControlPanel.PickerItemClickListener, CameraControlPanel.GalleryClickListener {

    public static final int ACTION_CONFIRM = 900;
    public static final int ACTION_RETAKE = 901;
    public static final int ACTION_CANCEL = 902;
    public static final int MEDIA_PICK_IMAGE = 430;
    public static final int MEDIA_PICK_VIDEO = 431;
    protected static final int REQUEST_PREVIEW_CODE = 1001;
    @CameraConfiguration.MediaAction
    protected int mediaAction = CameraConfiguration.MEDIA_ACTION_BOTH;
    @CameraConfiguration.MediaQuality
    protected int mediaQuality = CameraConfiguration.MEDIA_QUALITY_HIGHEST;
    @CameraConfiguration.MediaQuality
    protected int passedMediaQuality = CameraConfiguration.MEDIA_QUALITY_HIGHEST;
    protected CharSequence[] videoQualities;
    protected CharSequence[] photoQualities;
    protected boolean enableImageCrop = true;
    protected int videoDuration = 20;
    protected long videoFileSize = -1;
    protected boolean autoRecord = false;
    protected int minimumVideoDuration = -1;
    protected boolean showPicker = true;
    protected int type;
    @MediaActionSwitchView.MediaActionState
    protected int currentMediaActionState;
    @CameraSwitchView.CameraType
    protected int currentCameraType = CameraSwitchView.CAMERA_TYPE_REAR;
    @CameraConfiguration.MediaQuality
    protected int newQuality = -1;
    @CameraConfiguration.FlashMode
    protected int flashMode = CameraConfiguration.FLASH_MODE_AUTO;
    private CameraControlPanel cameraControlPanel;
    private AlertDialog settingsDialog;
    private BottomSheetDialog mBottomSheetDialog_Dates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onProcessBundle(Bundle savedInstanceState) {
        super.onProcessBundle(savedInstanceState);

        extractConfiguration(getIntent().getExtras());
        currentMediaActionState = mediaAction == CameraConfiguration.MEDIA_ACTION_VIDEO ?
                MediaActionSwitchView.ACTION_VIDEO : MediaActionSwitchView.ACTION_PHOTO;
    }

    @Override
    protected void onCameraControllerReady() {
        super.onCameraControllerReady();

        videoQualities = getVideoQualityOptions();
        photoQualities = getPhotoQualityOptions();
    }

    @Override
    protected void onResume() {
        super.onResume();

        cameraControlPanel.lockControls();
        cameraControlPanel.allowRecord(false);
        cameraControlPanel.showPicker(showPicker);
    }

    @Override
    protected void onPause() {
        super.onPause();

        cameraControlPanel.lockControls();
        cameraControlPanel.allowRecord(false);
    }

    private void extractConfiguration(Bundle bundle) {
        if (bundle != null) {
            if (bundle.containsKey(CameraConfiguration.Arguments.MEDIA_ACTION)) {
                switch (bundle.getInt(CameraConfiguration.Arguments.MEDIA_ACTION)) {
                    case CameraConfiguration.MEDIA_ACTION_PHOTO:
                        mediaAction = CameraConfiguration.MEDIA_ACTION_PHOTO;
                        break;
                    case CameraConfiguration.MEDIA_ACTION_VIDEO:
                        mediaAction = CameraConfiguration.MEDIA_ACTION_VIDEO;
                        break;
                    default:
                        mediaAction = CameraConfiguration.MEDIA_ACTION_BOTH;
                        break;
                }
            }

            if (bundle.containsKey(CameraConfiguration.Arguments.MEDIA_QUALITY)) {
                switch (bundle.getInt(CameraConfiguration.Arguments.MEDIA_QUALITY)) {
                    case CameraConfiguration.MEDIA_QUALITY_AUTO:
                        mediaQuality = CameraConfiguration.MEDIA_QUALITY_AUTO;
                        break;
                    case CameraConfiguration.MEDIA_QUALITY_HIGHEST:
                        mediaQuality = CameraConfiguration.MEDIA_QUALITY_HIGHEST;
                        break;
                    case CameraConfiguration.MEDIA_QUALITY_HIGH:
                        mediaQuality = CameraConfiguration.MEDIA_QUALITY_HIGH;
                        break;
                    case CameraConfiguration.MEDIA_QUALITY_MEDIUM:
                        mediaQuality = CameraConfiguration.MEDIA_QUALITY_MEDIUM;
                        break;
                    case CameraConfiguration.MEDIA_QUALITY_LOW:
                        mediaQuality = CameraConfiguration.MEDIA_QUALITY_LOW;
                        break;
                    case CameraConfiguration.MEDIA_QUALITY_LOWEST:
                        mediaQuality = CameraConfiguration.MEDIA_QUALITY_LOWEST;
                        break;
                    default:
                        mediaQuality = CameraConfiguration.MEDIA_QUALITY_MEDIUM;
                        break;
                }
                passedMediaQuality = mediaQuality;
            }

            if (bundle.containsKey(CameraConfiguration.Arguments.VIDEO_DURATION))
                videoDuration = bundle.getInt(CameraConfiguration.Arguments.VIDEO_DURATION);

            if (bundle.containsKey(CameraConfiguration.Arguments.VIDEO_FILE_SIZE))
                videoFileSize = bundle.getLong(CameraConfiguration.Arguments.VIDEO_FILE_SIZE);

            if (bundle.containsKey(CameraConfiguration.Arguments.MINIMUM_VIDEO_DURATION))
                minimumVideoDuration = bundle.getInt(CameraConfiguration.Arguments.MINIMUM_VIDEO_DURATION);

            if (bundle.containsKey(CameraConfiguration.Arguments.SHOW_PICKER))
                showPicker = bundle.getBoolean(CameraConfiguration.Arguments.SHOW_PICKER);

            if (bundle.containsKey(CameraConfiguration.Arguments.PICKER_TYPE))
                type = bundle.getInt(CameraConfiguration.Arguments.PICKER_TYPE);

            //if (bundle.containsKey(CameraConfiguration.Arguments.ENABLE_CROP))
            //enableImageCrop = bundle.getBoolean(CameraConfiguration.Arguments.ENABLE_CROP);

            if (bundle.containsKey(CameraConfiguration.Arguments.FLASH_MODE))
                switch (bundle.getInt(CameraConfiguration.Arguments.FLASH_MODE)) {
                    case CameraConfiguration.FLASH_MODE_AUTO:
                        flashMode = CameraConfiguration.FLASH_MODE_AUTO;
                        break;
                    case CameraConfiguration.FLASH_MODE_ON:
                        flashMode = CameraConfiguration.FLASH_MODE_ON;
                        break;
                    case CameraConfiguration.FLASH_MODE_OFF:
                        flashMode = CameraConfiguration.FLASH_MODE_OFF;
                        break;
                    default:
                        flashMode = CameraConfiguration.FLASH_MODE_AUTO;
                        break;
                }
            if (bundle.containsKey(CameraConfiguration.Arguments.AUTO_RECORD)) {
                if (mediaAction == CameraConfiguration.MEDIA_ACTION_VIDEO) {
                    autoRecord = bundle.getBoolean(CameraConfiguration.Arguments.AUTO_RECORD);
                }
            }
        }
    }

    @Override
    View getUserContentView(LayoutInflater layoutInflater, ViewGroup parent) {
        cameraControlPanel = (CameraControlPanel) layoutInflater.inflate(R.layout.user_control_layout, parent, false);
        //cameraControlPanel.postInit(type);

        if (cameraControlPanel != null) {
            cameraControlPanel.setup(getMediaAction());

            switch (flashMode) {
                case CameraConfiguration.FLASH_MODE_AUTO:
                    cameraControlPanel.setFlasMode(FlashSwitchView.FLASH_AUTO);
                    break;
                case CameraConfiguration.FLASH_MODE_ON:
                    cameraControlPanel.setFlasMode(FlashSwitchView.FLASH_ON);
                    break;
                case CameraConfiguration.FLASH_MODE_OFF:
                    cameraControlPanel.setFlasMode(FlashSwitchView.FLASH_OFF);
                    break;
            }

            cameraControlPanel.setRecordButtonListener(this);
            cameraControlPanel.setFlashModeSwitchListener(this);
            cameraControlPanel.setOnMediaActionStateChangeListener(this);
            cameraControlPanel.setOnCameraTypeChangeListener(this);
            cameraControlPanel.setMaxVideoDuration(getVideoDuration());
            cameraControlPanel.setMaxVideoFileSize(getVideoFileSize());
            cameraControlPanel.setSettingsClickListener(this);
            //cameraControlPanel.setPickerItemClickListener(this);
            cameraControlPanel.setGalleryClickListener(this);
            cameraControlPanel.shouldShowCrop(enableImageCrop);

            if (autoRecord) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        cameraControlPanel.startRecording();
                    }
                }, 1500);
            }
        }
        return cameraControlPanel;
    }

    @Override
    public void onSettingsClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (currentMediaActionState == MediaActionSwitchView.ACTION_VIDEO) {
            builder.setSingleChoiceItems(videoQualities, getVideoOptionCheckedIndex(), getVideoOptionSelectedListener());
            if (getVideoFileSize() > 0)
                builder.setTitle(String.format(getString(R.string.settings_video_quality_title),
                        "(Max " + String.valueOf(getVideoFileSize() / (1024 * 1024) + " MB)")));
            else
                builder.setTitle(String.format(getString(R.string.settings_video_quality_title), ""));
        } else {
            builder.setSingleChoiceItems(photoQualities, getPhotoOptionCheckedIndex(), getPhotoOptionSelectedListener());
            builder.setTitle(R.string.settings_photo_quality_title);
        }

        builder.setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (newQuality > 0 && newQuality != mediaQuality) {
                    mediaQuality = newQuality;
                    dialogInterface.dismiss();
                    cameraControlPanel.lockControls();
                    getCameraController().switchQuality();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel_label, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        settingsDialog = builder.create();
        settingsDialog.show();
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(settingsDialog.getWindow().getAttributes());
        layoutParams.width = Utils.convertDipToPixels(this, 350);
        layoutParams.height = Utils.convertDipToPixels(this, 350);
        settingsDialog.getWindow().setAttributes(layoutParams);
    }

    @Override
    public void onItemClick(Uri filePath) {
        int mimeType = getMimeType(filePath.toString());
        CamLibBus.getBus().send(new CameraOutputModel(mimeType, filePath.toString()));
        this.finish();
    }

    @Override
    public void onCameraTypeChanged(@CameraSwitchView.CameraType int cameraType) {
        if (currentCameraType == cameraType) return;
        currentCameraType = cameraType;

        cameraControlPanel.lockControls();
        cameraControlPanel.allowRecord(false);

        int cameraFace = cameraType == CameraSwitchView.CAMERA_TYPE_FRONT
                ? CameraConfiguration.CAMERA_FACE_FRONT : CameraConfiguration.CAMERA_FACE_REAR;

        getCameraController().switchCamera(cameraFace);
    }


    @Override
    public void onFlashModeChanged(@FlashSwitchView.FlashMode int mode) {
        switch (mode) {
            case FlashSwitchView.FLASH_AUTO:
                flashMode = CameraConfiguration.FLASH_MODE_AUTO;
                getCameraController().setFlashMode(CameraConfiguration.FLASH_MODE_AUTO);
                break;
            case FlashSwitchView.FLASH_ON:
                flashMode = CameraConfiguration.FLASH_MODE_ON;
                getCameraController().setFlashMode(CameraConfiguration.FLASH_MODE_ON);
                break;
            case FlashSwitchView.FLASH_OFF:
                flashMode = CameraConfiguration.FLASH_MODE_OFF;
                getCameraController().setFlashMode(CameraConfiguration.FLASH_MODE_OFF);
                break;
        }
    }


    @Override
    public void onMediaActionChanged(int mediaActionState) {
        if (currentMediaActionState == mediaActionState) return;
        currentMediaActionState = mediaActionState;
    }

    @Override
    public void onTakePhotoButtonPressed() {
        getCameraController().takePhoto();
    }

    @Override
    public void onStartRecordingButtonPressed() {
        getCameraController().startVideoRecord();
    }

    @Override
    public void onStopRecordingButtonPressed() {
        getCameraController().stopVideoRecord();
    }

    @Override
    protected void onScreenRotation(int degrees) {
        cameraControlPanel.rotateControls(degrees);
        rotateSettingsDialog(degrees);
    }

    @Override
    public int getMediaAction() {
        return mediaAction;
    }

    @Override
    public int getMediaQuality() {
        return mediaQuality;
    }

    @Override
    public int getVideoDuration() {
        return videoDuration;
    }

    @Override
    public long getVideoFileSize() {
        return videoFileSize;
    }

    @Override
    public int getFlashMode() {
        return flashMode;
    }

    @Override
    public int getMinimumVideoDuration() {
        return minimumVideoDuration / 1000;
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void updateCameraPreview(Size size, View cameraPreview) {
        cameraControlPanel.unLockControls();
        cameraControlPanel.allowRecord(true);

        setCameraPreview(cameraPreview, size);
    }

    @Override
    public void updateUiForMediaAction(@CameraConfiguration.MediaAction int mediaAction) {

    }

    @Override
    public void updateCameraSwitcher(int numberOfCameras) {
        cameraControlPanel.allowCameraSwitching(numberOfCameras > 1);
    }

    @Override
    public void onPhotoTaken() {
        startPreviewActivity(getCameraController().getOutputFile().toString());
    }

    @Override
    public void onVideoRecordStart(int width, int height) {
        cameraControlPanel.onStartVideoRecord(getCameraController().getOutputFile());
    }

    @Override
    public void onVideoRecordStop() {
        cameraControlPanel.allowRecord(false);
        cameraControlPanel.onStopVideoRecord();
        //startPreviewActivity();
        startVideoCropActivity(getCameraController().getOutputFile().toString());
    }

    @Override
    public void releaseCameraPreview() {
        clearCameraPreview();
    }

    @Override
    public void onGalleryClick() {
        Toast.makeText(this, "Gallery click", Toast.LENGTH_SHORT).show();
        showBottomSheetDialog();
    }

    private void startPreviewActivity(String filepath) {
        Intent intent = PreviewActivity.newIntent(this,
                getMediaAction(), filepath,
                cameraControlPanel.showCrop(), videoDuration);
        startActivityForResult(intent, REQUEST_PREVIEW_CODE);
    }

    private void startVideoCropActivity(String filepath){
        Intent intent = new Intent(this, TrimmerActivity.class);
        intent.putExtra(TrimmerActivity.EXTRA_VIDEO_PATH, filepath);
        //intent.putExtra(TrimmerActivity.VIDEO_TOTAL_DURATION, mediaPlayer.getDuration());
        intent.putExtra(TrimmerActivity.VIDEO_TOTAL_DURATION, videoDuration);
        startActivityForResult(intent, TrimmerActivity.REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PREVIEW_CODE) {
                if (PreviewActivity.isResultConfirm(data)) {
                    String path = PreviewActivity.getMediaFilePatch(data);
                    int mimeType = getMimeType(path);
                    CamLibBus.getBus().send(new CameraOutputModel(mimeType, path));
                    this.finish();
                } else if (PreviewActivity.isResultCancel(data)) {
                    this.finish();
                } else if (PreviewActivity.isResultRetake(data)) {
                    //ignore, just proceed the camera
                }
            }/*else if (requestCode == ImagePicker.IMAGE_PICKER_REQUEST_CODE) {
                List<String> mPaths = (List<String>) data.getSerializableExtra(ImagePicker.EXTRA_IMAGE_PATH);
                if(mPaths.size() > 0){
                    Intent intent = PreviewActivity.newIntent(this,
                            getMediaAction(), mPaths.get(0), cameraControlPanel.showCrop(), videoDuration);
                    startActivityForResult(intent, REQUEST_PREVIEW_CODE);
                }
            }else if (requestCode == VideoPicker.VIDEO_PICKER_REQUEST_CODE) {
                List<String> mPaths = (List<String>) data.getSerializableExtra(VideoPicker.EXTRA_VIDEO_PATH);
                if(mPaths.size() > 0){
                    Intent intent = PreviewActivity.newIntent(this,
                            getMediaAction(), mPaths.get(0), cameraControlPanel.showCrop(), videoDuration);
                    startActivityForResult(intent, REQUEST_PREVIEW_CODE);
                }
            }*/
            else if (requestCode == TrimmerActivity.REQUEST_CODE) {
                //deleteMediaFile();
                String path = data.getStringExtra("filepath");
                int mimeType = getMimeType(path);
                CamLibBus.getBus().send(new CameraOutputModel(mimeType, path));
                Log.i("previewactivity", "onresult : " + path);
                this.finish();
            }
        }
    }

    private int getMimeType(String path) {
        Uri uri = Uri.fromFile(new File(path));
        String extension;
        //Check uri format to avoid null
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(path);
        }
        String mimeTypeString
                = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        int mimeType = CamLibrary.MediaType.PHOTO;
        if (mimeTypeString.toLowerCase().contains("video")) {
            mimeType = CamLibrary.MediaType.VIDEO;
        }
        return mimeType;
    }

    private void rotateSettingsDialog(int degrees) {
        if (settingsDialog != null && settingsDialog.isShowing()) {
            ViewGroup dialogView = (ViewGroup) settingsDialog.getWindow().getDecorView();
            for (int i = 0; i < dialogView.getChildCount(); i++) {
                dialogView.getChildAt(i).setRotation(degrees);
            }
        }
    }

    protected abstract CharSequence[] getVideoQualityOptions();

    protected abstract CharSequence[] getPhotoQualityOptions();

    protected int getVideoOptionCheckedIndex() {
        int checkedIndex = -1;
        if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_AUTO) checkedIndex = 0;
        else if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_HIGH) checkedIndex = 1;
        else if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_MEDIUM) checkedIndex = 2;
        else if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_LOW) checkedIndex = 3;

        if (passedMediaQuality != CameraConfiguration.MEDIA_QUALITY_AUTO) checkedIndex--;

        return checkedIndex;
    }

    protected int getPhotoOptionCheckedIndex() {
        int checkedIndex = -1;
        if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_HIGHEST) checkedIndex = 0;
        else if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_HIGH) checkedIndex = 1;
        else if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_MEDIUM) checkedIndex = 2;
        else if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_LOWEST) checkedIndex = 3;
        return checkedIndex;
    }

    protected DialogInterface.OnClickListener getVideoOptionSelectedListener() {
        return new DialogInterface.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(DialogInterface dialogInterface, int index) {
                newQuality = ((VideoQualityOption) videoQualities[index]).getMediaQuality();
            }
        };
    }

    protected DialogInterface.OnClickListener getPhotoOptionSelectedListener() {
        return new DialogInterface.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(DialogInterface dialogInterface, int index) {
                newQuality = ((PhotoQualityOption) photoQualities[index]).getMediaQuality();
            }
        };
    }

    private void showBottomSheetDialog() {
        mBottomSheetDialog_Dates = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottomsheet_recycler, null);
        RecyclerView recyclerView = view.findViewById(R.id.bottomsheet_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));

        final ImageGalleryAdapter imageGalleryAdapter;
        if (currentMediaActionState == MediaActionSwitchView.ACTION_VIDEO)
            imageGalleryAdapter = new ImageGalleryAdapter(this, CameraConfiguration.VIDEO);
        else
            imageGalleryAdapter = new ImageGalleryAdapter(this);

        imageGalleryAdapter.setOnItemClickListener(new ImageGalleryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mBottomSheetDialog_Dates.dismiss();
                if(currentMediaActionState == MediaActionSwitchView.ACTION_PHOTO) {
                    startPreviewActivity(imageGalleryAdapter.getItem(position).getImageUri().getPath());
                }else{
                    startVideoCropActivity(imageGalleryAdapter.getItem(position).getImageUri().getPath());
                }
            }
        });

        recyclerView.setAdapter(imageGalleryAdapter);

        mBottomSheetDialog_Dates.setContentView(view);
        mBottomSheetDialog_Dates.show();
        mBottomSheetDialog_Dates.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                //mBottomSheetDialog_State = null;
            }
        });
        (view.findViewById(R.id.bottomsheet_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog_Dates.dismiss();
            }
        });
    }
}
