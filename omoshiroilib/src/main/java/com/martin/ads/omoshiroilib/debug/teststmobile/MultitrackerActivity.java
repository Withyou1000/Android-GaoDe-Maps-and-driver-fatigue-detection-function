package com.martin.ads.omoshiroilib.debug.teststmobile;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/*import com.example.chatgpt.ChatGPTActivity;*/
import com.martin.ads.omoshiroilib.R;
import com.martin.ads.omoshiroilib.gpt.ChatGPTActivity;
import com.sensetime.stmobileapi.STMobile106;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author MatrixCV
 * <p>
 * Activity
 */
public class MultitrackerActivity extends AppCompatActivity {

    static MultitrackerActivity instance = null;
    /**
     * 重力传感器
     */
    static Accelerometer acc;
    private ProgressBar pb_eye;
    private ProgressBar pb_mouth;
    private ProgressBar pb_head;
    private int progress_eye = 0;
    private int progress_mouth = 0;
    private int progress_head = 0;
    private int yawn_time = 0;
    private int blank_time = 0;
    private Button bt_reset;
    private double pitch_line;
    private double pitch_cur;
    private boolean is_first = true;
    private boolean have_face = false;
    private static int musicId, streamId;
    private SoundPool soundPool;
    private static final long MIN_PLAY_INTERVAL = 3000; // 设置最小播放间隔为3秒
    private long lastPlayTime = 0; // 记录上次播放音效的时间戳
    private ProgressBar load;

    private static final int DETECTION_FRAMES = 90;
    private static double normalYaw, normalPitch;
    private static boolean FACE_COLLECTED = false;
    private static boolean IS_FATIGUE = false;
    private boolean eye_flag = false;
    private boolean mouth_flag = false;
    //采集1秒的人脸特征
    STMobile106[] faceLandmarks = new STMobile106[DETECTION_FRAMES];
    //3s内的人脸特征点
    STMobile106[] faceLandmarksPer3s = new STMobile106[DETECTION_FRAMES];
    private static double yawPer3s, pitchPer3s;
    private static double eyeDistance, lipDistance;
    private TextView yawn;
    private TextView blank;
    private TextView eye_distance;
    private TextView lip_distance;
    private TextView head_yaw;
    private TextView head_pitch;
    private int eyeCloseFrames;
    private int lipOpenFrames;
    private int second_progress = 0;
    private HistoryDBHelper helper;
    MediaPlayer mediaPlayer = new MediaPlayer();
    private LocationManager lm = null;
    private int audioResourceId;
    private ActivityResultLauncher<Intent> register;
    private ActivityResultLauncher<Intent> register2;
    private String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
    private final int PERMS_REQUEST_CODE = 200;
    private Location mLocation;
    private MyLocationListner mLocationListner;
    private Button hah;
    private String call_number;
    private int sleep_times=0;
    private int call_time=3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//		显示方向
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.debug_activity_multitracker);
        hah = findViewById(R.id.bt_reset_pos);
        lm = (LocationManager)getSystemService(this.LOCATION_SERVICE);
        //Android 6.0以上版本需要临时获取权限
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP_MR1&&
                PackageManager.PERMISSION_GRANTED!=checkSelfPermission(perms[0])) {
            requestPermissions(perms,PERMS_REQUEST_CODE);
        }else{
            initLocation();
        }
        register = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                Intent intent = result.getData();
                if (intent != null && result.getResultCode() == Activity.RESULT_OK) {
                    String path = intent.getStringExtra("path");
                    try {
                        mediaPlayer.reset();
                        //没有选择则使用默认音乐
                        if(path.equals("0"))
                        {
                            AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.hint);
                            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                            mediaPlayer.prepare();
                            afd.close();
                        }
                        else{
                            mediaPlayer.setDataSource(path);
                            mediaPlayer.prepare();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        register2 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                Intent intent = result.getData();
                if (intent != null && result.getResultCode() == Activity.RESULT_OK) {
                     call_number = intent.getStringExtra("number");
                    call_time= Integer.parseInt(intent.getStringExtra("time"));
                }
            }
        });
        helper = HistoryDBHelper.getInstance(this);
        helper.openRead();
        helper.openWrite();
        pb_eye = findViewById(R.id.pb_eye);
        pb_mouth = findViewById(R.id.pb_mouth);
        pb_head = findViewById(R.id.pb_head);
        bt_reset = findViewById(R.id.bt_reset_pos);
        blank = findViewById(R.id.tv_eye_blank);
        yawn = findViewById(R.id.tv_yawn);
        eye_distance = findViewById(R.id.tv_eye_distance);
        lip_distance = findViewById(R.id.tv_lip_distance);
        head_yaw = findViewById(R.id.tv_head_yaw);
        head_pitch = findViewById(R.id.tv_head_pitch);
        load = findViewById(R.id.load);
        bt_reset.setOnClickListener(v -> {
            pitch_line = pitch_cur + 12;
            have_face = false;
            FACE_COLLECTED = false;//再次点击要重新收集人脸数据，故设为false
            is_first = false;
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            eye_distance.setVisibility(View.GONE);
                            lip_distance.setVisibility(View.GONE);
                            blank.setVisibility(View.GONE);
                            yawn.setVisibility(View.GONE);
                            head_yaw.setVisibility(View.GONE);
                            head_pitch.setVisibility(View.GONE);
                            load.setVisibility(View.VISIBLE);
                        }
                    });
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            blank.setText("眨眼：" + String.valueOf(blank_time) + "  ");
                            yawn.setText("哈欠：" + String.valueOf(yawn_time) + "  ");
                            eye_distance.setText("眼睛帧数：" + "0/90  ");
                            head_pitch.setText("水平偏离程度：0");
                            lip_distance.setText("嘴唇帧数：0/90  ");
                            head_yaw.setText("垂直偏离程度：0");
                            load.setVisibility(View.GONE);
                            eye_distance.setVisibility(View.VISIBLE);
                            lip_distance.setVisibility(View.VISIBLE);
                            blank.setVisibility(View.VISIBLE);
                            yawn.setVisibility(View.VISIBLE);
                            head_yaw.setVisibility(View.VISIBLE);
                            head_pitch.setVisibility(View.VISIBLE);
                            if (have_face) {
                                Toast.makeText(MultitrackerActivity.this, "采集成功！", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MultitrackerActivity.this, "采集失败！", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }.start();
        });
      /*  AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build();
        musicId = soundPool.load(this, R.raw.hint, 1);*/
        // 将 "your_audio_file" 替换为您实际的音频文件名（不含扩展名）
        audioResourceId = R.raw.hint;
        try {
            AssetFileDescriptor afd = getResources().openRawResourceFd(audioResourceId);
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mediaPlayer.prepare();
            afd.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        instance = this;

        /**
         *
         * 开启重力传感器监听
         *
         */
        acc = new Accelerometer(this);
        acc.start();

        new Thread() {
            @Override
            public void run() {
                super.run();
                while (true) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (FACE_COLLECTED) {
                                second_progress += 2;
                                pb_eye.setSecondaryProgress(second_progress);
                                if (second_progress == 120) {
                                    pb_eye.setSecondaryProgress(0);
                                    pb_eye.setProgress(0);
                                    progress_eye = 0;
                                    second_progress = 0;
                                }
                            }
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }


//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.multitracker, menu);
//		return true;
//	}

    @Override
    public void onResume() {
        super.onResume();
        final FaceOverlapFragment fragment = (FaceOverlapFragment) getFragmentManager()
                .findFragmentById(R.id.overlapFragment);
        fragment.registTrackCallback(new FaceOverlapFragment.TrackCallBack() {
            int frameNum = 0;

            @Override
            public void onTrackdetected(final int value, final float pitch, final float roll, final float yaw, final int eye_dist,
                                        final int id, final int eyeBlink, final int mouthAh, final int headYaw, final int headPitch, final int browJump, STMobile106 landmarks) {
                if(sleep_times==call_time)
                {
                    sleep_times=0;
                  /*  Intent intent = new Intent(Intent.ACTION_CALL);
                    startActivity(intent);*/
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(MultitrackerActivity.this, ChatGPTActivity.class);
                            startActivity(intent);

                        }
                    });
                }
                if (!is_first) {
                    have_face = true;
                    pitch_cur = pitch;
                    long currentTime = System.currentTimeMillis();
                    // 检查时间间隔
                    if (currentTime - lastPlayTime > MIN_PLAY_INTERVAL && (progress_head == 100 ||/* progress_mouth == 100 ||*/ progress_eye == 120)) {
                        if (progress_head == 100) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("您在 yyyy 年 MM 月 dd 日 HH 时 mm 分 ss 秒 头部过低");
                            String formattedTime = dateFormat.format(new Date());
                            helper.insert_history(formattedTime);
                        } else if (progress_eye == 120) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("您在 yyyy 年 MM 月 dd 日 HH 时 mm 分 ss 秒 一分钟内眨眼次数过多");
                            String formattedTime = dateFormat.format(new Date());
                            helper.insert_history(formattedTime);
                        }
                        sleep_times++;
                        // 更新上次播放时间戳
                        lastPlayTime = currentTime;
                        // 时间间隔太短，不播放音效
                        /* soundPool.play(musicId, 1, 1, 0, 0, 1);*/
                        if (mediaPlayer.isPlaying()) {
// 如果正在播放，重新播放
                            mediaPlayer.seekTo(0);
                            mediaPlayer.start();
                        } else {
// 如果没有播放，则开始播放
                            mediaPlayer.start();
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (eyeBlink == 1) {
                                eye_flag = true;
                            }
                            if (eye_flag == true && eyeBlink == 0) {
                                blank_time++;
                                blank.setText("眨眼：" + String.valueOf(blank_time) + "  ");
                                eye_flag = false;
                                progress_eye += 6;
                                pb_eye.setProgress(progress_eye);
                            }
                        }
                    });
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mouthAh == 1) {
                                progress_mouth += 3;
                                if (progress_mouth >= 100) {
                                    progress_mouth = 100;
                                    mouth_flag = true;
                                }
                                pb_mouth.setProgress(progress_mouth);
                            } else {
                                if (mouth_flag == true) {
                                    mouth_flag = false;
                                    yawn_time++;
                                    yawn.setText("哈欠：" + String.valueOf(yawn_time) + "  ");
                                }
                                progress_mouth -= 10;
                                if (progress_mouth <= 0) {
                                    progress_mouth = 0;
                                }
                                pb_mouth.setProgress(progress_mouth);
                            }
                        }
                    });
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (pitch > pitch_line) {
                                progress_head += 7;
                                if (progress_head >= 100) {
                                    progress_head = 100;
                                }
                                pb_head.setProgress(progress_head);
                            } else {
                                progress_head -= 10;
                                if (progress_head <= 0) {
                                    progress_head = 0;
                                }
                                pb_head.setProgress(progress_head);
                            }
                        }
                    });
                    // 脸部特征未采集
                    if (!FACE_COLLECTED) {
                        if (frameNum < DETECTION_FRAMES) {//要点击了按钮才执行这里
                            faceLandmarks[frameNum] = landmarks;
                            frameNum++;
                            normalYaw += yaw;
                            normalPitch += pitch;
                        } else {
                            //过了三秒之后frameNUM=90，故执行这里
                            getFaceFeature();
                            FACE_COLLECTED = true;
                            frameNum = 0;
                           /* runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                eye_distance.setText("眼睛闭合程度：0/23(90*0.25)   ");
                                head_pitch.setText("头水平摇晃程度：0/"+(normalYaw+20));
                                }
                            });*/
                        }
                    }
//                实时疲劳检测
                    else {
                        if (frameNum < DETECTION_FRAMES) {
                            faceLandmarksPer3s[frameNum] = landmarks;
                            yawPer3s += yaw;
                            pitchPer3s += pitch;
                            frameNum++;
                           /* runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    PointF[] tempFace = landmarks.getPointsArray();
//                                newactionText.setText("上下眼平均距离："+ ((tempFace[72].x - tempFace[73].x)+(tempFace[75].x - tempFace[76].x))/2+ "\n上下唇平均距离："+ ((tempFace[86].x - tempFace[94].x)+(tempFace[87].x - tempFace[93].x)+(tempFace[88].x - tempFace[92].x))/3);
                                }
                            });*/
                        } else {
                            IS_FATIGUE = isFatigue();
                            if (IS_FATIGUE) {
                                sleep_times++;
                                /* streamId = soundPool.play(musicId, 1, 1, 0, 0, 1);*/
                                if (mediaPlayer.isPlaying()) {
// 如果正在播放，重新播放
                                    mediaPlayer.seekTo(0);
                                    mediaPlayer.start();
                                } else {
// 如果没有播放，则开始播放
                                    mediaPlayer.start();
                                }
                              /*  runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                    }
                                });*/
                            }/* else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                    }
                                });
                            }*/
                            frameNum = 0;
                            yawPer3s = 0;
                            pitchPer3s = 0;
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        helper.closelink();
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
            mediaPlayer.stop();
        }
        FACE_COLLECTED = false;
    }

    public void goto_history(View view) {
       /* Intent intent = new Intent();
        intent.setClassName("com.example.chatgpt", "com.example.chatgpt.ChatGPTActivity");
// 检查是否获取到了 Intent
        if (intent != null) {
            startActivity(intent);

        } else {
// 目标应用未安装
            Toast.makeText(this, "应用未安装", Toast.LENGTH_SHORT).show();
        }*/
        Intent intent = new Intent(this, SearchHistoryActivity.class);
        startActivity(intent);
    }

    public void goto_music(View view) {
        Intent intent = new Intent(this, ChoseMusicActivity.class);
        register.launch(intent);
    }
    public void goto_call(View view) {
        Intent intent = new Intent(this, CallActivity.class);
        register2.launch(intent);
    }
    // 判断是否处于疲劳状态
    private boolean isFatigue() {
        eyeCloseFrames = 0;
        lipOpenFrames = 0;
        for (int i = 0; i < DETECTION_FRAMES; i++) {
            PointF[] tempFace = faceLandmarksPer3s[i].getPointsArray();
            double curEyeDistance = ((tempFace[72].x - tempFace[73].x) + (tempFace[75].x - tempFace[76].x)) / 2;
            double curLipDistance = ((tempFace[86].x - tempFace[94].x) + (tempFace[87].x - tempFace[93].x) + (tempFace[88].x - tempFace[92].x)) / 3;
            if (curEyeDistance < 0.7 * eyeDistance) {
                eyeCloseFrames++;
            }
            if (curLipDistance > 1.5 * lipDistance) {
                lipOpenFrames++;
            }
        }
        double yawAver = yawPer3s / DETECTION_FRAMES;
        double pitchAver = pitchPer3s / DETECTION_FRAMES;

        String str_pitch = String.format("%.1f", pitchAver);
        String str_yaw = String.format("%.1f", Math.abs(yawAver));
        new Thread() {
            @Override
            public void run() {
                super.run();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        eye_distance.setText("眼睛帧数：" + eyeCloseFrames + "/90  ");
                        head_yaw.setText("水平偏离程度：" + str_yaw);
                        lip_distance.setText("嘴唇帧数：" + lipOpenFrames + "/90 ");
                        head_pitch.setText("垂直偏离程度：" + str_pitch);
                    }
                });
            }
        }.start();
        if (eyeCloseFrames >= 0.25 * DETECTION_FRAMES || lipOpenFrames >= 0.3 * DETECTION_FRAMES ||
                pitchAver >= normalPitch + 10 || Math.abs(yawAver) >= normalYaw + 20) {
            if (eyeCloseFrames >= 0.25 * DETECTION_FRAMES) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("您在 yyyy 年 MM 月 dd 日 HH 时 mm 分 ss 秒 眼睛闭合程度低于平均值");
                String formattedTime = dateFormat.format(new Date());
                helper.insert_history(formattedTime);
            } else if (lipOpenFrames >= 0.3 * DETECTION_FRAMES) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("您在 yyyy 年 MM 月 dd 日 HH 时 mm 分 ss 秒 嘴巴张开程度高于平均值");
                String formattedTime = dateFormat.format(new Date());
                helper.insert_history(formattedTime);
            } else if (pitchAver >= normalPitch + 10) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("您在 yyyy 年 MM 月 dd 日 HH 时 mm 分 ss 秒 头部垂直方向上晃动程度过大");
                String formattedTime = dateFormat.format(new Date());
                helper.insert_history(formattedTime);
            } else if (Math.abs(yawAver) >= normalYaw + 20) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("您在 yyyy 年 MM 月 dd 日 HH 时 mm 分 ss 秒 头部水平方向上晃动程度过大");
                String formattedTime = dateFormat.format(new Date());
                helper.insert_history(formattedTime);
            }
            return true;
        } else
            return false;
    }

    //    获取脸部特征
    private void getFaceFeature() {
        double eyeSumDistance = 0, lipSumDistance = 0;
        for (int i = 0; i < DETECTION_FRAMES; i++) {
            PointF[] tempFace = faceLandmarks[i].getPointsArray();
            eyeSumDistance += ((tempFace[72].x - tempFace[73].x) + (tempFace[75].x - tempFace[76].x)) / 2;
            lipSumDistance += ((tempFace[86].x - tempFace[94].x) + (tempFace[87].x - tempFace[93].x) + (tempFace[88].x - tempFace[92].x)) / 3;
        }
        eyeDistance = eyeSumDistance / DETECTION_FRAMES;
        lipDistance = lipSumDistance / DETECTION_FRAMES;
        normalPitch = normalPitch / DETECTION_FRAMES;
        normalYaw = normalYaw / DETECTION_FRAMES;
    }
    private void initLocation(){
        //判断GPS是否正常启动
        if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Toast.makeText(MultitrackerActivity.this, "请开启GPS...",Toast.LENGTH_SHORT);
            //返回开启GPS导航设置界面
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent,0);
            return;
        }
        if (mLocationListner == null)
        {
            mLocationListner = new MyLocationListner();
        }
        try{
            mLocation = lm.getLastKnownLocation(lm.GPS_PROVIDER);
            updateView(mLocation);
        }catch (SecurityException se){
        }
        try{
            /**
             * 开启定位监听变化
             * 参数1，定位方式：主要有GPS_PROVIDER和NETWORK_PROVIDER，前者是GPS,后者是GPRS以及WIFI定位
             * 参数2，位置信息更新周期.单位是毫秒
             * 参数3，位置变化最小距离：当位置距离变化超过此值时，将更新位置信息
             * 参数4，监听
             * 备注：参数2和3，如果参数3不为0，则以参数3为准；参数3为0，则通过时间来定时更新；两者为0，则随时刷新
             */
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListner);
        }catch (SecurityException se){
        }
    }
    private class MyLocationListner implements LocationListener
    {
       /* @Override
        public void onProviderEnabled(String provider)
        {
            try{
                updateView(lm.getLastKnownLocation(provider));
            }catch (SecurityException e){}
        }*/

        @Override
        public void onLocationChanged(@NonNull Location location) {

        }
    }

    private void updateView(Location location)
    {
        if (location!=null) {

        }else{

        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(permsRequestCode, permissions, grantResults);
        switch (permsRequestCode) {
            case PERMS_REQUEST_CODE:
                boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (storageAccepted) {
                    initLocation();
                }
                break;

        }
    }
}
