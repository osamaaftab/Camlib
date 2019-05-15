package com.library.camlibrary.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;


import com.library.camlibrary.R;
import com.library.camlibrary.configuration.CameraConfiguration;
import com.library.camlibrary.utils.CameraHelper;
import com.library.camlibrary.videoTrimmer.HgLVideoTrimmer;
import com.library.camlibrary.videoTrimmer.interfaces.OnHgLVideoListener;
import com.library.camlibrary.videoTrimmer.interfaces.OnTrimVideoListener;

import java.io.File;

public class TrimmerActivity extends AppCompatActivity implements OnTrimVideoListener, OnHgLVideoListener {
    public static final int REQUEST_CODE = 143;
    public static final String EXTRA_VIDEO_PATH = "EXTRA_VIDEO_PATH";
    public static final String VIDEO_TOTAL_DURATION = "VIDEO_TOTAL_DURATION";

    private HgLVideoTrimmer mVideoTrimmer;
    private ProgressDialog mProgressDialog;
    private String previewFilePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trimmer);

        Intent extraIntent = getIntent();
        previewFilePath = "";
        int maxDuration = 10;

        if (extraIntent != null) {
            previewFilePath = extraIntent.getStringExtra(EXTRA_VIDEO_PATH);
            maxDuration = extraIntent.getIntExtra(VIDEO_TOTAL_DURATION, 20);
        }

        //setting progressbar
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(getString(R.string.trimming_progress));

        mVideoTrimmer = ((HgLVideoTrimmer) findViewById(R.id.timeLine));
        if (mVideoTrimmer != null) {
            /**
             * get total duration of video file
             */
            Log.e("tg", "maxDuration = " + maxDuration);
             //mVideoTrimmer.setMaxDuration(maxDuration);
            mVideoTrimmer.setMaxDuration(maxDuration);
            mVideoTrimmer.setOnTrimVideoListener(this);
            mVideoTrimmer.setOnHgLVideoListener(this);
            //mVideoTrimmer.setDestinationPath("/storage/emulated/0/DCIM/CameraCustom/");
            mVideoTrimmer.setDestinationPath(CameraHelper.getOutputMediaFile(this,
                    CameraConfiguration.MEDIA_ACTION_VIDEO).getPath());
            mVideoTrimmer.setVideoURI(Uri.parse(previewFilePath));
            mVideoTrimmer.setVideoInformationVisibility(true);
        }
    }

    @Override
    public void onTrimStarted() {
        mProgressDialog.show();
    }

    @Override
    public void getResult(final Uri contentUri) {
        mProgressDialog.cancel();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
               // Toast.makeText(TrimmerActivity.this, getString(R.string.video_saved_at, contentUri.getPath()), Toast.LENGTH_SHORT).show();

            }
        });

        try {
            String path = contentUri.getPath();
            /*File file = new File(path);
            Log.e("tg", " path1 = " + path + " uri1 = " + Uri.fromFile(file));
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromFile(file));
            intent.setDataAndType(Uri.fromFile(file), "video/*");
            startActivity(intent);
            finish();*/
            //deleteMediaFile();
            Log.i("TrimmerActivity", "onResult : " + path);
            Intent intent = new Intent();
            intent.putExtra("filepath", path);
            setResult(Activity.RESULT_OK, intent);
            finish();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(TrimmerActivity.this, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }


    private void playUriOnVLC(Uri uri) {

        int vlcRequestCode = 42;
        Intent vlcIntent = new Intent(Intent.ACTION_VIEW);
        vlcIntent.setPackage("org.videolan.vlc");
        vlcIntent.setData(uri);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            vlcIntent.setDataAndTypeAndNormalize(uri, "video/*");
        }else
            vlcIntent.setDataAndType(uri, "video/*");
        vlcIntent.putExtra("title", "Kung Fury");
        vlcIntent.putExtra("from_start", false);
        vlcIntent.putExtra("position", 90000l);
        startActivityForResult(vlcIntent, vlcRequestCode);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e("tg", "resultCode = " + resultCode + " data " + data);
    }

    @Override
    public void cancelAction() {
        mProgressDialog.cancel();
        mVideoTrimmer.destroy();
        deleteMediaFile();
        finish();
    }

    @Override
    public void onError(final String message) {
        mProgressDialog.cancel();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
               // Toast.makeText(TrimmerActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onVideoPrepared() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
               // Toast.makeText(TrimmerActivity.this, "onVideoPrepared", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mVideoTrimmer.destroy();
        deleteMediaFile();
    }

    /*To delete src file if it is recorded*/
    private boolean deleteMediaFile() {
        if(previewFilePath.contains(getFilesDir().getPath())) {
            File mediaFile = new File(previewFilePath);
            Log.i("previewactivity", "destroyed : " + mediaFile.getPath());
            mediaFile.delete();
        }

        return false;
    }
}
