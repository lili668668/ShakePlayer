package com.ballfish.ShakePlayer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.ToggleButton;

import java.io.File;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Context context;

    // 偏好設定
    private SharedPreferences SP;
    private SPManager SPM;

    // 偏好
    private Music music;
    private Screen screen;
    private ColorSet colorSet;
    private OtherSetting otherSetting;

    // 動畫
    private AnimationDrawable AD;

    // 音樂撥放服務
    private Intent playIntent;

    // 抬頭顯示儀服務
    private NotificationManager NM;

    // 全螢幕
    private View touchPoint;
    private boolean mVisible;

    // 全螢幕toggle
    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }

        // 因為mVisible在上面會改變，所以下面判斷剛好跟他相反
        play_button_toggle(mVisible);
    }

    private void play_button_toggle (boolean show) {
        ToggleButton play_music = (ToggleButton) findViewById(com.ballfish.ShakePlayer.R.id.play_music);
        if (!otherSetting.shakeOn) {
            if (show) {
                play_music.setVisibility(View.VISIBLE);
            } else {
                play_music.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;
    }

    @SuppressLint("InlinedApi")
    private void show() {
        mVisible = true;

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(com.ballfish.ShakePlayer.R.layout.activity_main);

        context = this;

        // fullscreen
        mVisible = true;
        touchPoint = findViewById(com.ballfish.ShakePlayer.R.id.touch_point);
        touchPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle();
            }
        });

        // 偏好設定
        SP = getSharedPreferences(SPManager.PreferencesConst.NAME, Context.MODE_PRIVATE);
        SPM = new SPManager(SP);

        // 準備工作
        playIntent = new Intent(MainActivity.this, Play.class);
        NM = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        otherSetting = SPM.getOtherSetting();
        prepareMusic();
        prepareScreen();
        prepareColor();
        if (otherSetting.keepOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        // toolbar
        Toolbar toolbar = (Toolbar) findViewById(com.ballfish.ShakePlayer.R.id.toolbar);
        setSupportActionBar(toolbar);

        // drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(com.ballfish.ShakePlayer.R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, com.ballfish.ShakePlayer.R.string.navigation_drawer_open, com.ballfish.ShakePlayer.R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(com.ballfish.ShakePlayer.R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    // 音源
    private void prepareMusic() {
        music = SPM.getMusic();

        playIntent.putExtra(SPManager.OtherSettingConst.LOOPING,
                otherSetting.isLooping);
        playIntent.putExtra(SPManager.OtherSettingConst.SHAKE_ON,
                otherSetting.shakeOn);
        playIntent.putExtra(SPManager.MusicConst.MODE,
                music.mode);
        playIntent.putExtra(SPManager.MusicConst.RESOURCE,
                music.resource);
        playIntent.putExtra(SPManager.MusicConst.FILE_PATH,
                music.filePath);

        prepareTitleString(music.name);
        startService(playIntent);
    }

    // 螢幕運作程式
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void prepareScreen() {
        screen = SPM.getScreen();

        ImageView img = (ImageView) findViewById(com.ballfish.ShakePlayer.R.id.mascot);

        switch (screen.mode) {
            case SPManager.ScreenConst.MODE_RESOURCE:
                if (AD != null) {
                    if (AD.isRunning()) {
                        AD.stop();
                    }
                }
                img.setImageBitmap(null);
                img.setScaleType(ImageView.ScaleType.FIT_CENTER);
                img.setBackgroundResource(screen.resource);
                AD = (AnimationDrawable) img.getBackground();
                AD.start();
                break;
            case SPManager.ScreenConst.MODE_NONE:
                if (AD != null) {
                    if (AD.isRunning()) {
                        AD.stop();
                    }
                }
                img.setImageBitmap(null);
                img.setBackgroundResource(Const.MASCOT_NONE);
                AD = (AnimationDrawable) img.getBackground();
                AD.start();
                break;
            case SPManager.ScreenConst.MODE_FILE_PATH:
                File f = new File(screen.filePath);

                if (AD != null) {
                    if (AD.isRunning()) {
                        AD.stop();
                    }
                }

                Bitmap b = BitmapFactory.decodeFile(f.getAbsolutePath(), new BitmapFactory.Options());
                b = preparePicture(b, img);
                img.setImageBitmap(b);
                img.setBackgroundResource(com.ballfish.ShakePlayer.R.drawable.none1);
                break;
        }
    }

    private void prepareColor() {
        colorSet = SPM.getColoSet();

        ImageView bgColor = (ImageView) findViewById(com.ballfish.ShakePlayer.R.id.background_color);
        switch (colorSet.mode) {
            case SPManager.ColorConst.MODE_ONE:
                Utility.setOneColor(bgColor,colorSet.one);
                break;
            case SPManager.ColorConst.MODE_GROUP:
                int[] colors_res = getResources().getIntArray(colorSet.group);
                Utility.autoChangeColors(bgColor, colorSet.changeTime, colors_res);
                break;
        }
    }

    private void prepareTitleString(String str) {
        if (str.equals(SPManager.MusicConst.NAME_DEF)) {
            setTitle(getResources().getStringArray(com.ballfish.ShakePlayer.R.array.music_basic_items)[0]);
        } else {
            setTitle(str);
        }
    }

    private Bitmap preparePicture(Bitmap b, ImageView img) {
        // 取得螢幕大小
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int screenHeight = metrics.heightPixels;
        int screenWidth = metrics.widthPixels;

        // 取的圖片大小
        int bitmapHeight = b.getHeight();
        int bitmapWidth = b.getWidth();

        // 橫圖問題
        if (bitmapHeight < bitmapWidth) {
            Matrix m = new Matrix();
            m.postRotate(90);
            Bitmap bnew = Bitmap.createBitmap(b, 0, 0, bitmapWidth, bitmapHeight, m, true);
            b = bnew;
            int tmp = bitmapHeight;
            bitmapHeight = bitmapWidth;
            bitmapWidth = tmp;
        }

        // 小圖問題
        if (bitmapHeight <= screenHeight) {
            double multiple = 0;
            if (screenHeight / bitmapHeight < screenWidth / bitmapWidth) {
                multiple = screenHeight * 1.0 / bitmapHeight;
            } else {
                multiple = screenWidth * 1.0 / bitmapWidth;
            }
            img.setMinimumHeight((int) (bitmapHeight * multiple));
            img.setMinimumWidth((int) (bitmapWidth * multiple));
            img.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else {
            double multiple = 0;
            if (bitmapHeight / screenHeight < bitmapWidth / screenWidth) {
                multiple = bitmapHeight * 1.0 / screenHeight;
            } else {
                multiple = bitmapWidth * 1.0 / screenWidth;
            }
            img.setMaxHeight((int) (bitmapHeight / multiple));
            img.setMaxWidth((int) (bitmapWidth / multiple));
            img.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
        return b;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(com.ballfish.ShakePlayer.R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.ballfish.ShakePlayer.R.menu.main, menu);

        ToggleButton shake_toggle = (ToggleButton) menu.findItem(com.ballfish.ShakePlayer.R.id.shake_choose).getActionView().findViewById(com.ballfish.ShakePlayer.R.id.shake_appbar);
        final ToggleButton play_toggle = (ToggleButton) findViewById(com.ballfish.ShakePlayer.R.id.play_music);

        // shake toggle button
        shake_toggle.setChecked(otherSetting.shakeOn);

        shake_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    otherSetting.shakeOn = false;
                    play_toggle.setVisibility(View.VISIBLE);
                } else {
                    otherSetting.shakeOn = true;
                    play_toggle.setVisibility(View.INVISIBLE);
                    if (play_toggle.isChecked()) {
                        play_toggle.setChecked(false);
                    }
                }
                SPM.OtherSettingSet(SPManager.OtherSettingConst.SHAKE_ON,
                        otherSetting.shakeOn);
                stopService(playIntent);
                prepareMusic();
            }
        });

        // play toggle button
        if (otherSetting.shakeOn) {
            play_toggle.setVisibility(View.INVISIBLE);
        }

        play_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    otherSetting.isLooping = false;
                } else {
                    otherSetting.isLooping = true;
                }
                SPM.OtherSettingSet(SPManager.OtherSettingConst.LOOPING,
                        otherSetting.isLooping);
                stopService(playIntent);
                prepareMusic();
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    // 抽屜選單監聽
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        final int id = item.getItemId();

        switch (id) {

            // 音源設定
            case com.ballfish.ShakePlayer.R.id.nav_music:
                changeActivity(com.ballfish.ShakePlayer.R.id.nav_music);
                break;

            // 螢幕圖片設定
            case com.ballfish.ShakePlayer.R.id.nav_screen:
                changeActivity(com.ballfish.ShakePlayer.R.id.nav_screen);
                break;

            // 系統性偏好設定
            case com.ballfish.ShakePlayer.R.id.nav_set:
                LayoutInflater LI = getLayoutInflater();
                View layout = LI.inflate(com.ballfish.ShakePlayer.R.layout.set_layout, (ViewGroup) findViewById(com.ballfish.ShakePlayer.R.id.set_layout));

                Switch background_switch = (Switch) layout.findViewById(com.ballfish.ShakePlayer.R.id.switch1);
                Switch keepon_switch = (Switch) layout.findViewById(com.ballfish.ShakePlayer.R.id.switch2);

                background_switch.setChecked(otherSetting.backgroundPlay);
                keepon_switch.setChecked(otherSetting.keepOn);

                background_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (!isChecked) {
                            otherSetting.backgroundPlay = false;
                        } else {
                            otherSetting.backgroundPlay = true;
                        }
                        SPM.OtherSettingSet(SPManager.OtherSettingConst.BACKGROUND_PLAY,
                                otherSetting.backgroundPlay);
                    }
                });

                keepon_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (!isChecked) {
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                            otherSetting.keepOn = false;
                        } else {
                            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                            otherSetting.keepOn = true;
                        }
                        SPM.OtherSettingSet(SPManager.OtherSettingConst.KEEP_ON,
                                otherSetting.keepOn);
                    }
                });

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(com.ballfish.ShakePlayer.R.string.nav_set);
                builder.setView(layout);
                builder.create().show();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(com.ballfish.ShakePlayer.R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void changeActivity(int id) {
        switch (id) {
            case com.ballfish.ShakePlayer.R.id.nav_music:
                stopService(playIntent);
                Intent MusicIntent = new Intent();
                MusicIntent.setClass(MainActivity.this, MusicChooseActivity.class);
                startActivityForResult(MusicIntent, Const.MUSIC_CHOOSE_INTENT_CODE);
                break;
            case com.ballfish.ShakePlayer.R.id.nav_screen:
                final Intent ScreenIntent = new Intent();
                ScreenIntent.setClass(MainActivity.this, ScreenChooseActivity.class);

                final ProgressDialog progressDialog = new ProgressDialog(context);
                progressDialog.setCancelable(false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setTitle(com.ballfish.ShakePlayer.R.string.loading);
                CountDownTimer CDT = new CountDownTimer(3000, 1000) {
                    int count = 0;
                    @Override
                    public void onTick(long millisUntilFinished) {
                        count++;
                        if (count == 2) {
                            startActivityForResult(ScreenIntent, Const.SCREEN_CHOOSE_INTENT_CODE);
                        }
                    }

                    // 過場
                    @Override
                    public void onFinish() {
                        progressDialog.dismiss();
                    }
                };

                progressDialog.show();
                CDT.start();
                break;
        }
    }

    // 檔案選擇
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Const.MUSIC_CHOOSE_INTENT_CODE) {
            if (resultCode == Const.MUSIC_CHOOSE_INTENT_CODE) {
                prepareMusic();
            }
        } else if(requestCode == Const.SCREEN_CHOOSE_INTENT_CODE) {
            if (resultCode == Const.SCREEN_CHOOSE_INTENT_CODE) {
                prepareScreen();
                prepareColor();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (otherSetting.backgroundPlay) {
            Utility.createNoti(context, NM, 0);
        } else {
            stopService(playIntent);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (otherSetting.backgroundPlay) {
            stopService(playIntent);
            prepareMusic();
            NM.cancel(0);
        } else {
            prepareMusic();
        }
    }

    @Override
    protected void onDestroy() {
        if (otherSetting.backgroundPlay) {
            stopService(playIntent);
            NM.cancel(0);
        }
        super.onDestroy();
    }
}
