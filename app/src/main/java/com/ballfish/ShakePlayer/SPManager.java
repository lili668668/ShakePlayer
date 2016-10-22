package com.ballfish.ShakePlayer;

import android.content.SharedPreferences;
import android.graphics.Color;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by 慈吟 on 2016/1/17.
 * 偏好設定管理
 */
public class SPManager {
    private SharedPreferences SP;
    private SharedPreferences.Editor edit;

    public SPManager(SharedPreferences SP) {
        this.SP = SP;
        this.edit = SP.edit();
    }

    // 偏好設定常數
    public interface PreferencesConst {
        String NAME = "PREFERENCES";
        String MODE= "PreferenceMode";
        boolean MODE_DEF= false;
    }

    // 音源偏好設定常數
    public interface MusicConst {
        String MODE = "MusicMode";
        int MODE_DEF = 1;
        int MODE_RESOURCE = 1;
        int MODE_FILE_PATH = 2;
        String RESOURCE = "MusicResource";
        int RESOURCE_DEF = com.ballfish.ShakePlayer.R.raw.monstervox;
        String FILE_PATH = "MusicFilePath";
        String FILE_PATH_DEF = "None";
        String NAME = "MusicName";
        String NAME_DEF = "";
        String OFTEN_FILE = "MusicOftenFile";
        Set<String> OFTEN_FILE_DEF = new HashSet<>();
    }

    // 圖片偏好設定常數
    public interface ScreenConst {
        String MODE = "ScreenMode";
        int MODE_DEF = 1;
        int MODE_RESOURCE = 1;
        int MODE_NONE = 2;
        int MODE_FILE_PATH = 3;
        String RESOURCE = "ScreenResource";
        int RESOURCE_DEF = com.ballfish.ShakePlayer.R.drawable.none;
        String FILE_PATH= "ScreenFilePath";
        String FILE_PATH_DEF = "None";
        String NAME = "ScreenName";
        String NAME_DEF = "";
        String MODE_BEFORE = "ScreenModeBefore";
        String RESOURCE_BEFORE = "MascotBefore";
        String FILE_PATH_BEFORE= "PictureBefore";
        String NAME_BEFORE = "NameBefore";
    }

    // 顏色偏好設定常數
    public interface ColorConst {
        String MODE = "ColorMode";
        int MODE_DEF = 2;
        int MODE_ONE = 1;
        int MODE_GROUP = 2;
        String ONE = "ColorOne";
        int ONE_DEF = Color.WHITE;
        String GROUP = "ColorGroup";
        int GROUP_DEF = com.ballfish.ShakePlayer.R.array.color4;
        String GROUP_POS = "ColorGroupPosition";
        int GROUP_POS_DEF = 3;
        String CHANGE_TIME = "ColorChangeTime";
        int CHANGE_TIME_DEF = 2;
    }

    // 其他偏好設定常數
    public interface OtherSettingConst {
        String SHAKE_ON = "ShakeMode";
        boolean SHAKE_ON_DEF = true;
        String BACKGROUND_PLAY= "BackgroundPlay";
        boolean BACKGROUND_PLAY_DEF = true;
        String KEEP_ON = "KeepOn";
        boolean KEEP_ON_DEF = true;
        String LOOPING = "isLoop";
        boolean LOOPING_DEF = false;
    }

    // 初始化偏好設定
    public boolean DefSet() {
        if (!SP.getBoolean(PreferencesConst.MODE, PreferencesConst.MODE_DEF)) {
            edit.putInt(MusicConst.MODE, MusicConst.MODE_DEF);
            edit.putInt(MusicConst.RESOURCE, MusicConst.RESOURCE_DEF);
            edit.putString(MusicConst.FILE_PATH, MusicConst.FILE_PATH_DEF);
            edit.putString(MusicConst.NAME, MusicConst.NAME_DEF);
            edit.putStringSet(MusicConst.OFTEN_FILE, MusicConst.OFTEN_FILE_DEF);

            edit.putInt(ScreenConst.MODE, ScreenConst.MODE_DEF);
            edit.putInt(ScreenConst.RESOURCE, ScreenConst.RESOURCE_DEF);
            edit.putString(ScreenConst.FILE_PATH, ScreenConst.FILE_PATH_DEF);
            edit.putString(ScreenConst.NAME, ScreenConst.NAME_DEF);
            edit.putInt(ScreenConst.MODE_BEFORE, ScreenConst.MODE_DEF);
            edit.putInt(ScreenConst.RESOURCE_BEFORE, ScreenConst.RESOURCE_DEF);
            edit.putString(ScreenConst.FILE_PATH_BEFORE, ScreenConst.FILE_PATH_DEF);
            edit.putString(ScreenConst.NAME_BEFORE, ScreenConst.NAME_DEF);

            edit.putInt(ColorConst.MODE, ColorConst.MODE_DEF);
            edit.putInt(ColorConst.ONE, ColorConst.ONE_DEF);
            edit.putInt(ColorConst.GROUP, ColorConst.GROUP_DEF);
            edit.putInt(ColorConst.GROUP_POS, ColorConst.GROUP_POS_DEF);
            edit.putInt(ColorConst.CHANGE_TIME, ColorConst.CHANGE_TIME_DEF);

            edit.putBoolean(OtherSettingConst.SHAKE_ON, OtherSettingConst.SHAKE_ON_DEF);
            edit.putBoolean(OtherSettingConst.BACKGROUND_PLAY, OtherSettingConst.BACKGROUND_PLAY_DEF);
            edit.putBoolean(OtherSettingConst.KEEP_ON, OtherSettingConst.KEEP_ON_DEF);
            edit.putBoolean(OtherSettingConst.LOOPING, OtherSettingConst.LOOPING_DEF);

            edit.putBoolean(PreferencesConst.MODE, true);
            return edit.commit();
        }

        return true;
    }

    public boolean MusicSet(Music m) {
        edit.putInt(MusicConst.MODE, m.mode);
        edit.putInt(MusicConst.RESOURCE, m.resource);
        edit.putString(MusicConst.FILE_PATH, m.filePath);
        edit.putString(MusicConst.NAME, m.name);
        edit.putStringSet(MusicConst.OFTEN_FILE, m.oftenFiles);

        return edit.commit();
    }

    public void MusicSetApply(Music m) {
        edit.putInt(MusicConst.MODE, m.mode);
        edit.putInt(MusicConst.RESOURCE, m.resource);
        edit.putString(MusicConst.FILE_PATH, m.filePath);
        edit.putString(MusicConst.NAME, m.name);
        edit.putStringSet(MusicConst.OFTEN_FILE, m.oftenFiles);

        edit.apply();
    }

    public void MusicOftenFileSet(Set<String> s) {
        edit.putStringSet(MusicConst.OFTEN_FILE, s);
        edit.apply();
    }

    public Music getMusic() {
        Music m = new Music();
        m.mode = SP.getInt(MusicConst.MODE, MusicConst.MODE_DEF);
        m.resource = SP.getInt(MusicConst.RESOURCE, MusicConst.RESOURCE_DEF);
        m.filePath = SP.getString(MusicConst.FILE_PATH, MusicConst.FILE_PATH_DEF);
        m.name = SP.getString(MusicConst.NAME, MusicConst.NAME_DEF);
        m.oftenFiles = SP.getStringSet(MusicConst.OFTEN_FILE, MusicConst.OFTEN_FILE_DEF);
        if (m.mode == MusicConst.MODE_FILE_PATH) {
            File f = new File(m.filePath);
            if (!f.exists()) {
                m.mode = MusicConst.MODE_DEF;
                m.resource = MusicConst.RESOURCE_DEF;
                m.name = MusicConst.NAME_DEF;

                edit.putInt(MusicConst.MODE, MusicConst.MODE_DEF);
                edit.putInt(MusicConst.RESOURCE, MusicConst.RESOURCE_DEF);
                edit.putString(MusicConst.NAME, MusicConst.NAME_DEF);
                edit.apply();
            }
        }
        return m;
    }

    public boolean ScreenSet(Screen s) {
        edit.putInt(ScreenConst.MODE, s.mode);
        edit.putInt(ScreenConst.RESOURCE, s.resource);
        edit.putString(ScreenConst.FILE_PATH, s.filePath);
        edit.putString(ScreenConst.NAME, s.name);

        return edit.commit();
    }

    public Screen getScreen() {
        Screen s = new Screen();
        s.mode = SP.getInt(ScreenConst.MODE, ScreenConst.MODE_DEF);
        if (s.mode == ScreenConst.MODE_FILE_PATH) {
            s.filePath = SP.getString(ScreenConst.FILE_PATH, ScreenConst.FILE_PATH_DEF);
            File f = new File(s.filePath);
            if (!f.exists()) {
                s.mode = ScreenConst.MODE_DEF;
                edit.putInt(ScreenConst.MODE, ScreenConst.MODE_DEF);
                edit.apply();
            }
        }
        s.resource = SP.getInt(ScreenConst.RESOURCE, ScreenConst.RESOURCE_DEF);
        s.name = SP.getString(ScreenConst.NAME, ScreenConst.NAME_DEF);
        return s;
    }

    public void ScreenBeforeSet(Screen s) {
        edit.putInt(ScreenConst.MODE_BEFORE, s.mode);
        edit.putInt(ScreenConst.RESOURCE_BEFORE, s.resource);
        edit.putString(ScreenConst.FILE_PATH_BEFORE, s.filePath);
        edit.putString(ScreenConst.NAME_BEFORE, s.name);

        edit.apply();
    }

    public Screen getScreenBefore() {
        Screen s = new Screen();
        s.mode = SP.getInt(ScreenConst.MODE_BEFORE, ScreenConst.MODE_DEF);
        if (s.mode == ScreenConst.MODE_FILE_PATH) {
            s.filePath = SP.getString(ScreenConst.FILE_PATH_BEFORE, ScreenConst.FILE_PATH_DEF);
            File f = new File(s.filePath);
            if (!f.exists()) {
                s.mode = ScreenConst.MODE_DEF;
                edit.putInt(ScreenConst.MODE_BEFORE, ScreenConst.MODE_DEF);
                edit.commit();
            }
        }
        s.resource = SP.getInt(ScreenConst.RESOURCE_BEFORE, ScreenConst.RESOURCE_DEF);
        s.name = SP.getString(ScreenConst.NAME_BEFORE, ScreenConst.NAME_DEF);
        return s;
    }

    public boolean ColorSetSet(ColorSet cs) {
        edit.putInt(ColorConst.MODE, cs.mode);
        edit.putInt(ColorConst.ONE, cs.one);
        edit.putInt(ColorConst.GROUP, cs.group);
        edit.putInt(ColorConst.GROUP_POS, cs.groupPos);
        edit.putInt(ColorConst.CHANGE_TIME, cs.changeTime);

        return edit.commit();
    }

    public ColorSet getColoSet() {
        ColorSet cs = new ColorSet();
        cs.mode = SP.getInt(ColorConst.MODE, ColorConst.MODE_DEF);
        cs.one = SP.getInt(ColorConst.ONE, ColorConst.ONE_DEF);
        cs.group = SP.getInt(ColorConst.GROUP, ColorConst.GROUP_DEF);
        cs.groupPos = SP.getInt(ColorConst.GROUP_POS, ColorConst.GROUP_POS_DEF);
        cs.changeTime = SP.getInt(ColorConst.CHANGE_TIME, ColorConst.CHANGE_TIME_DEF);
        return cs;
    }

    public void OtherSettingSet(String name, boolean arg) {
        edit.putBoolean(name, arg);
        edit.apply();
    }

    public OtherSetting getOtherSetting() {
        OtherSetting otherSetting = new OtherSetting();
        otherSetting.shakeOn = SP.getBoolean(OtherSettingConst.SHAKE_ON, OtherSettingConst.SHAKE_ON_DEF);
        otherSetting.backgroundPlay = SP.getBoolean(OtherSettingConst.BACKGROUND_PLAY, OtherSettingConst.BACKGROUND_PLAY_DEF);
        otherSetting.keepOn = SP.getBoolean(OtherSettingConst.KEEP_ON, OtherSettingConst.KEEP_ON_DEF);
        otherSetting.isLooping = SP.getBoolean(OtherSettingConst.LOOPING, OtherSettingConst.LOOPING_DEF);

        return otherSetting;
    }
}
