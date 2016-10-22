package com.ballfish.ShakePlayer;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.ImageView;

/**
 * Created by 慈吟 on 2016/1/7.
 * 與MainActivity相關工作分離
 */
public class Utility {

    private static ChangeColorsThread timer;

    // 抬頭顯示儀服務
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void createNoti(Context context, NotificationManager NM, int nid) {
        Intent notiIntent = new Intent(context, Play.class);
        notiIntent.putExtra("end", true);
        PendingIntent PI = PendingIntent.getService(context, 1, notiIntent, PendingIntent.FLAG_ONE_SHOT);
        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(com.ballfish.ShakePlayer.R.mipmap.ic_launcher)
                .setContentTitle(context.getString(com.ballfish.ShakePlayer.R.string.noti_title))
                .setContentText(context.getString(com.ballfish.ShakePlayer.R.string.noti_text))
                .setTicker(context.getString(com.ballfish.ShakePlayer.R.string.noti_title))
                .setContentIntent(PI)
                .setAutoCancel(true)
                .setOngoing(true);
        NM.notify(nid, builder.build());
    }

    /**
     *
     * @param bg 背景
     * @param time 每幾『秒』換一次
     * @param colors 色組
     */
    public static void autoChangeColors(ImageView bg, int time, int[] colors) {
        stopTimer();

        timer = new ChangeColorsThread(bg, time, colors);

        timer.start();
    }

    public static void setOneColor(ImageView bg, int color) {
        stopTimer();
        bg.setBackgroundColor(color);
    }

    private static void stopTimer() {
        if (timer != null) {
            if (timer.isRunning()) {
                timer.stopThread();
            }
        }
    }
}
