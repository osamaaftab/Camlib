package com.test.camlibrary;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.library.camlibrary.CamLibrary;
import com.library.camlibrary.configuration.CameraConfiguration;
import com.library.camlibrary.manager.CameraOutputModel;
import com.netcompss.ffmpeg4android.CommandValidationException;
import com.netcompss.ffmpeg4android.GeneralUtils;
import com.netcompss.ffmpeg4android.Prefs;
import com.netcompss.loader.LoadJNI;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;

        findViewById(R.id.both).setOnClickListener(onClickListener);
        findViewById(R.id.photo).setOnClickListener(onClickListener);
        findViewById(R.id.video).setOnClickListener(onClickListener);
    }

    private void checkanddeletefile(String path) {
        File fl = new File(path);
        if (fl.exists()) {
            if (fl.delete())
                Toast.makeText(activity, "deleted", Toast.LENGTH_SHORT).show();
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.both:
                    CamLibrary
                            .with(activity)
                            .setShowPicker(true)
                            //.setVideoFileSize(20)
                            .setVideoDuration(30)
                            .setMediaAction(CameraConfiguration.MEDIA_ACTION_BOTH)
                            .launchCamera(new CamLibrary.CameraCallback() {
                                @Override
                                public void onComplete(CameraOutputModel model) {
                                    Log.e("File", "" + model.getPath());
                                    Log.e("Type", "" + model.getType());
                                    Toast.makeText(getApplicationContext(), "Media captured.\n" +
                                            model.getType() + "\n" +
                                            model.getPath(), Toast.LENGTH_SHORT).show();

                                    checkanddeletefile(model.getPath());    //NOTE: make sure to delete the file after save/upload to final location, to avoid file accumulation in local storage
                                }
                            });
                    break;
                case R.id.photo:
                    CamLibrary
                            .with(activity)
                            .setShowPicker(true)
                            .setMediaAction(CameraConfiguration.MEDIA_ACTION_PHOTO)
                            .launchCamera(new CamLibrary.CameraCallback() {
                                @Override
                                public void onComplete(CameraOutputModel model) {
                                    Log.e("File", "" + model.getPath());
                                    Log.e("Type", "" + model.getType());
                                    Toast.makeText(getApplicationContext(), "Media captured.\n" +
                                            model.getType() + "\n" +
                                            model.getPath(), Toast.LENGTH_SHORT).show();

                                    checkanddeletefile(model.getPath());    //NOTE: make sure to delete the file after save/upload to final location, to avoid file accumulation in local storage
                                }
                            });
                    break;
                case R.id.video:
                    CamLibrary
                            .with(activity)
                            .setShowPicker(true)
                            .setMediaAction(CameraConfiguration.MEDIA_ACTION_VIDEO)
                            .setVideoDuration(30)
                            .launchCamera(new CamLibrary.CameraCallback() {
                                @Override
                                public void onComplete(CameraOutputModel model) {
                                    Log.e("File", "" + model.getPath());
                                    Log.e("Type", "" + model.getType());
                                    Toast.makeText(getApplicationContext(), "Media captured.\n" +
                                            model.getType() + "\n" +
                                            model.getPath(), Toast.LENGTH_SHORT).show();

                                    new TranscdingBackground(MainActivity.this, model.getPath()).execute();
                                    //checkanddeletefile(model.getPath());    //NOTE: make sure to delete the file after save/upload to final location, to avoid file accumulation in local storage
                                }
                            });
                    break;
            }
        }
    };

    public class TranscdingBackground extends AsyncTask<String, Integer, Integer> {

        ProgressDialog progressDialog;
        Activity _act;
        String commandStr;

        String workFolder = null;
        String demoVideoFolder = null;
        String demoVideoPath = null;
        String vkLogPath = null;
        private boolean commandValidationFailedFlag = false;

        public TranscdingBackground(Activity act, String src_path) {
            _act = act;
            demoVideoPath = src_path;
            workFolder = getApplicationContext().getFilesDir().getAbsolutePath() + "/tempmedia/";
            vkLogPath = workFolder + "vk.log";

            commandStr = "ffmpeg -y -i " + src_path + " -strict experimental -s 640x480 -r 30 -aspect 3:4 -ab 48000 -ac 2 -ar 22050 -vcodec mpeg4 -b 2097152 " + workFolder + "out.mp4";
        }

        @Override
        protected void onPreExecute() {
            //EditText commandText = (EditText)findViewById(R.id.CommandText);
            //commandStr = commandText.getText().toString();

            progressDialog = new ProgressDialog(_act);
            progressDialog.setMessage("FFmpeg4Android Transcoding in progress...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        protected Integer doInBackground(String... paths) {
            Log.i(Prefs.TAG, "doInBackground started...");

            // delete previous log
            //boolean isDeleted = GeneralUtils.deleteFileUtil(workFolder + "/vk.log");
            //Log.i(Prefs.TAG, "vk deleted: " + isDeleted);

            /*PowerManager powerManager = (PowerManager) _act.getSystemService(Activity.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "VK_LOCK");
            Log.d(Prefs.TAG, "Acquire wake lock");
            wakeLock.acquire();*/

            ///////////// Set Command using code (overriding the UI EditText) /////
            //commandStr = "ffmpeg -y -i /sdcard/videokit/in.mp4 -strict experimental -s 320x240 -r 30 -aspect 3:4 -ab 48000 -ac 2 -ar 22050 -vcodec mpeg4 -b 2097152 /sdcard/videokit/out.mp4";
            //String[] complexCommand = {"ffmpeg", "-y" ,"-i", "/sdcard/videokit/in.mp4","-strict","experimental","-s", "160x120","-r","25", "-vcodec", "mpeg4", "-b", "150k", "-ab","48000", "-ac", "2", "-ar", "22050", "/sdcard/videokit/out.mp4"};
            ///////////////////////////////////////////////////////////////////////


            LoadJNI vk = new LoadJNI();
            try {

                // complex command
                //vk.run(complexCommand, workFolder, getApplicationContext());

                vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, getApplicationContext());

                // running without command validation
                //vk.run(complexCommand, workFolder, getApplicationContext(), false);

                // copying vk.log (internal native log) to the videokit folder
                GeneralUtils.copyFileToFolder(vkLogPath, demoVideoFolder);

            } catch (CommandValidationException e) {
                Log.e(Prefs.TAG, "vk run exeption.", e);
                commandValidationFailedFlag = true;

            } catch (Throwable e) {
                Log.e(Prefs.TAG, "vk run exeption.", e);
            } finally {
                /*if (wakeLock.isHeld())
                    wakeLock.release();
                else {
                    Log.i(Prefs.TAG, "Wake lock is already released, doing nothing");
                }*/
            }
            Log.i(Prefs.TAG, "doInBackground finished");
            return Integer.valueOf(0);
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        @Override
        protected void onCancelled() {
            Log.i(Prefs.TAG, "onCancelled");
            //progressDialog.dismiss();
            super.onCancelled();
        }


        @Override
        protected void onPostExecute(Integer result) {
            Log.i(Prefs.TAG, "onPostExecute");
            progressDialog.dismiss();
            super.onPostExecute(result);

            // finished Toast
            String rc = null;
            if (commandValidationFailedFlag) {
                rc = "Command Vaidation Failed";
            } else {
                rc = GeneralUtils.getReturnCodeFromLog(vkLogPath);
            }
            final String status = rc;
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, status, Toast.LENGTH_LONG).show();
                    if (status.equals("Transcoding Status: Failed")) {
                        Toast.makeText(MainActivity.this, "Check: " + vkLogPath + " for more information.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

    }
}
