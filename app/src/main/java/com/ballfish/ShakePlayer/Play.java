package com.ballfish.ShakePlayer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;

import com.squareup.seismic.ShakeDetector;

import java.io.File;

public class Play extends Service implements ShakeDetector.Listener{

    private MediaPlayer MP;

    private ShakeDetector SD;
    private SensorManager SM;

    private boolean playLoop;
    private boolean shakeStop;

    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        // 搖動偵測
        SM = (SensorManager) getSystemService(SENSOR_SERVICE);
        SD = new ShakeDetector(this);
        SD.setSensitivity(ShakeDetector.SENSITIVITY_LIGHT);
        SD.start(SM);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getBooleanExtra("end", false)) {
            stopSelf();
        } else {
            set(intent);
            if (shakeStop && playLoop) {
                play(playLoop);
            }
        }
        return Service.START_NOT_STICKY;
    }

    // 播放
    public void play(boolean isLooping) {
        MP.setLooping(isLooping);

        if (!MP.isPlaying()) {
            MP.start();
        }
    }

    // 設置音源
    public void set(Intent intent) {
        playLoop = intent.getBooleanExtra(SPManager.OtherSettingConst.LOOPING, false);
        shakeStop = !intent.getBooleanExtra(SPManager.OtherSettingConst.SHAKE_ON,
                SPManager.OtherSettingConst.SHAKE_ON_DEF);
        int id = intent.getIntExtra(SPManager.MusicConst.MODE,
                SPManager.MusicConst.MODE_DEF);
        switch (id) {
            case SPManager.MusicConst.MODE_RESOURCE:
                MP = MediaPlayer.create(context, intent.getIntExtra(SPManager.MusicConst.RESOURCE, SPManager.MusicConst.RESOURCE_DEF));
                break;
            case SPManager.MusicConst.MODE_FILE_PATH:
                File f = new File(intent.getStringExtra(SPManager.MusicConst.FILE_PATH));
                Uri uri = Uri.fromFile(f);
                MP = MediaPlayer.create(context, uri);
                break;
        }
    }

    // 搖動偵測
    @Override
    public void hearShake() {
        if (!shakeStop) {
            play(playLoop);
        }
    }

    @Override
    public void onDestroy() {
        SD.stop();
        MP.release();
        super.onDestroy();
    }
}
