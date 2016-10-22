package com.ballfish.ShakePlayer;

import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

/**
 * Created by 慈吟 on 2016/1/11.
 */
public class ChangeColorsHandler extends Handler {
    private ImageView bg;
    private int[] colors;
    private int size;
    private int count;

    public ChangeColorsHandler(ImageView bg, int[] colors) {
        this.bg = bg;
        this.colors = colors;
        this.size = colors.length;
        this.count = 0;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                bg.setBackgroundColor(colors[count]);
                count = (count + 1) % size;
                break;
        }
        super.handleMessage(msg);
    }
}
