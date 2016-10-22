package com.ballfish.ShakePlayer;

import android.os.Message;
import android.widget.ImageView;

/**
 * Created by 慈吟 on 2016/1/11.
 */
public class ChangeColorsThread extends Thread {
    private boolean isRunning;
    private int milltime;
    private ChangeColorsHandler handler;

    public boolean isRunning() {
        return this.isRunning;
    }

    public ChangeColorsThread(ImageView bg, int time, int[] colors) {
        this.milltime = time * 1000;
        this.isRunning = true;
        this.handler = new ChangeColorsHandler(bg, colors);
    }

    @Override
    public void run() {
        while (isRunning) {
            Message m = new Message();
            m.what = 1;
            handler.sendMessage(m);
            try {
                Thread.sleep(milltime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopThread() {
        this.isRunning = false;
    }
}
