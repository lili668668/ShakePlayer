package com.ballfish.ShakePlayer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MusicChooseActivity extends AppCompatActivity {

    private Context context;
    private SharedPreferences SP;
    private SPManager SPM;
    private MagicFileChooser MFC;

    private MediaPlayer tryPlay;

    private File direct;
    private SimpleAdapter adapter;
    private List<HashMap<String,String>> musicItem;

    // 錄音器
    private MediaRecorder MR;
    private File RF;

    private Music music;

    private String tileString;

    private int highlightPos = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(com.ballfish.ShakePlayer.R.layout.activity_music_choose);

        context = this;
        SP = getSharedPreferences(SPManager.PreferencesConst.NAME, Context.MODE_PRIVATE);
        SPM = new SPManager(SP);
        direct = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Const.DIRECT_NAME);
        if (!direct.exists()) {
            direct.mkdir();
        }

        music = SPM.getMusic();

        prepareTitleString(music.name);
        prepareTmpString(music.name);

        // listview清單
        final ListView listview = (ListView) findViewById(com.ballfish.ShakePlayer.R.id.music_listview);

        // file choose檔案選擇
        Button file_choose = (Button) findViewById(com.ballfish.ShakePlayer.R.id.music_file_choose);

        // music_record錄音
        Button music_record = (Button) findViewById(com.ballfish.ShakePlayer.R.id.music_record);

        // 確認
        Button pos_bt = (Button) findViewById(com.ballfish.ShakePlayer.R.id.music_pos);

        // listview清單
        musicItem = prepareMusicItems();
        adapter = new SimpleAdapter(context, musicItem, com.ballfish.ShakePlayer.R.layout.music_item,
                new String[]{Const.MUSIC_ITEM_NAME, Const.MUSIC_ITEM_BELONG, Const.MUSIC_ITEM_RESOURCE, Const.MUSIC_ITEM_IS_RECORD},
                new int[]{com.ballfish.ShakePlayer.R.id.music_item_name, com.ballfish.ShakePlayer.R.id.music_item_belong, com.ballfish.ShakePlayer.R.id.music_item_resource, com.ballfish.ShakePlayer.R.id.music_item_is_record})
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                if (highlightPos == position) {
                    v.setBackgroundColor(getResources().getColor(com.ballfish.ShakePlayer.R.color.colorItemBackground));
                } else {
                    v.setBackgroundColor(getResources().getColor(com.ballfish.ShakePlayer.R.color.white));
                }
                return v;
            }
        };
        listview.setAdapter(adapter);

        // 音源選單監聽
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> tmp = (HashMap) parent.getItemAtPosition(position);
                music.mode = Integer.parseInt(tmp.get(Const.MUSIC_ITEM_BELONG));
                music.name = tmp.get(Const.MUSIC_ITEM_NAME);
                switch (music.mode) {
                    case SPManager.MusicConst.MODE_RESOURCE:
                        music.resource = Integer.parseInt(tmp.get(Const.MUSIC_ITEM_RESOURCE));
                        tryPlay(music.resource);
                        break;
                    case SPManager.MusicConst.MODE_FILE_PATH:
                        music.filePath = tmp.get(Const.MUSIC_ITEM_RESOURCE);
                        tryPlay(music.filePath);
                }
                prepareTmpString(music.name);
                highlightPos = position;
                adapter.notifyDataSetChanged();
            }
        });

        // 音源選單編輯監聽
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> tmp = (HashMap) parent.getItemAtPosition(position);
                int belong = Integer.parseInt(tmp.get(Const.MUSIC_ITEM_BELONG));
                final String name = tmp.get(Const.MUSIC_ITEM_NAME);
                final String resource = tmp.get(Const.MUSIC_ITEM_RESOURCE);
                boolean isRecord = Boolean.parseBoolean(tmp.get(Const.MUSIC_ITEM_IS_RECORD));

                if (belong == SPManager.MusicConst.MODE_FILE_PATH && isRecord) {
                    AlertDialog.Builder CRUD_builder = new AlertDialog.Builder(context);

                    LinearLayout CRUD_ly = new LinearLayout(context);
                    CRUD_ly.setOrientation(LinearLayout.VERTICAL);

                    Button delete_bt = new Button(context);
                    delete_bt.setText(com.ballfish.ShakePlayer.R.string.delete_file_bt);
                    delete_bt.setBackgroundColor(Color.TRANSPARENT);
                    CRUD_ly.addView(delete_bt, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

                    Button rename_bt = new Button(context);
                    rename_bt.setText(com.ballfish.ShakePlayer.R.string.rename_bt);
                    rename_bt.setBackgroundColor(Color.TRANSPARENT);
                    CRUD_ly.addView(rename_bt, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

                    CRUD_builder.setView(CRUD_ly);
                    final AlertDialog CRUD_dialog = CRUD_builder.create();

                    delete_bt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            File f = new File(resource);
                            if (f.exists()) {
                                f.delete();
                                if (name.equals(tileString)) {
                                    reset();
                                } else if (name.equals(music.name)) {
                                    music = SPM.getMusic();
                                    prepareTmpString(music.name);
                                }

                                highlightPos = -1;

                                musicItem.clear();
                                musicItem.addAll(prepareMusicItems());
                                adapter.notifyDataSetChanged();
                            }
                            CRUD_dialog.dismiss();
                        }
                    });

                    rename_bt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder input_builder = new AlertDialog.Builder(context);
                            input_builder.setMessage(com.ballfish.ShakePlayer.R.string.recorder_alert_messages);
                            final EditText et = new EditText(context);
                            input_builder.setView(et);

                            input_builder.setNeutralButton(com.ballfish.ShakePlayer.R.string.pos_bt, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String filename = "" + et.getText();
                                    filename = filename.replaceAll("\\.", "");
                                    if (!filename.equals("")) {
                                        File f = new File(resource);
                                        File fR = new File(direct, filename + ".amr");

                                        if (fR.exists()) {
                                            int cnt = 1;
                                            while (fR.exists()) {
                                                fR = new File(direct, filename + cnt + ".amr");
                                                cnt++;
                                            }
                                        }

                                        if (f.exists()) {
                                            if (name.equals(music.name)) {
                                                music.name = fR.getName();
                                                music.filePath = fR.getAbsolutePath();
                                                prepareTmpString(music.name);
                                            }
                                            if (name.equals(tileString)) {
                                                music.name = fR.getName();
                                                music.filePath = fR.getAbsolutePath();
                                                prepareTitleString(music.name);
                                                SPM.MusicSetApply(music);
                                            }
                                            f.renameTo(fR);

                                            highlightPos = -1;

                                            musicItem.clear();
                                            musicItem.addAll(prepareMusicItems());
                                            adapter.notifyDataSetChanged();
                                        } else {
                                            Toast.makeText(context, com.ballfish.ShakePlayer.R.string.file_unexist, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    dialog.dismiss();
                                }
                            });

                            input_builder.setPositiveButton(com.ballfish.ShakePlayer.R.string.nag_bt, null);

                            input_builder.create().show();

                            CRUD_dialog.dismiss();
                        }
                    });

                    CRUD_dialog.show();
                } else if (belong == SPManager.MusicConst.MODE_FILE_PATH && !isRecord) {
                    AlertDialog.Builder CRUD_builder = new AlertDialog.Builder(context);

                    LinearLayout CRUD_ly = new LinearLayout(context);
                    CRUD_ly.setOrientation(LinearLayout.VERTICAL);

                    Button delete_bt = new Button(context);
                    delete_bt.setText(com.ballfish.ShakePlayer.R.string.delete_from_list_bt);
                    delete_bt.setBackgroundColor(Color.TRANSPARENT);
                    CRUD_ly.addView(delete_bt, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

                    CRUD_builder.setView(CRUD_ly);
                    final AlertDialog CRUD_dialog = CRUD_builder.create();

                    delete_bt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String[] tmparr = new String[music.oftenFiles.size()];
                            music.oftenFiles.toArray(tmparr);
                            music.oftenFiles.remove(resource);
                            if (name.equals(tileString)) {
                                reset();
                            } else if (name.equals(music.name)) {
                                music = SPM.getMusic();
                                prepareTmpString(music.name);
                            }

                            highlightPos = -1;

                            musicItem.clear();
                            musicItem.addAll(prepareMusicItems());
                            adapter.notifyDataSetChanged();
                            CRUD_dialog.dismiss();
                        }
                    });

                    CRUD_dialog.show();
                }
                return true;
            }
        });

        // 無格線
        listview.setDivider(null);

        // file choose檔案選擇
        file_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
                MFC = new MagicFileChooser(MusicChooseActivity.this);
                if (!MFC.showFileChooser("audio/*", getString(com.ballfish.ShakePlayer.R.string.choose_title), false, true)) {
                    Toast.makeText(context, com.ballfish.ShakePlayer.R.string.file_fail, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // music_record錄音
        music_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();

                AlertDialog.Builder recorder_builder = new AlertDialog.Builder(context);
                recorder_builder.setMessage(com.ballfish.ShakePlayer.R.string.recorder_alert_messages);
                final EditText et = new EditText(context);
                recorder_builder.setView(et);
                recorder_builder.setNeutralButton(com.ballfish.ShakePlayer.R.string.recorder_pos_bt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String filename = "" + et.getText();
                        filename = filename.replaceAll("\\.", "");
                        if (filename.equals("")) {
                            filename = "temp";
                        }

                        try {
                            MR = new MediaRecorder();
                            MR.setAudioSource(MediaRecorder.AudioSource.MIC);
                            MR.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                            MR.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                            RF = new File(direct, filename + ".amr");
                            if (RF.exists()) {
                                int cnt = 1;
                                while (RF.exists()) {
                                    RF = new File(direct, filename + cnt + ".amr");
                                    cnt++;
                                }
                            } else {
                                RF.createNewFile();
                            }
                            MR.setOutputFile(RF.getAbsolutePath());
                            MR.prepare();
                            MR.start();
                            AlertDialog.Builder stop_builder = new AlertDialog.Builder(context);
                            LinearLayout stop_ly = new LinearLayout(context);
                            stop_ly.setOrientation(LinearLayout.VERTICAL);
                            Button stop_bt = new Button(context);
                            stop_bt.setText(com.ballfish.ShakePlayer.R.string.stop_bt);
                            stop_bt.setTextSize(25);
                            stop_bt.setTextColor(Color.RED);
                            stop_bt.setBackgroundColor(Color.TRANSPARENT);
                            stop_ly.addView(stop_bt, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                            stop_builder.setView(stop_ly);
                            final AlertDialog stop_dialog = stop_builder.create();
                            stop_dialog.show();
                            stop_bt.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    MR.stop();
                                    MR.release();
                                    MR = null;

                                    music.mode = SPManager.MusicConst.MODE_FILE_PATH;
                                    music.filePath = RF.getAbsolutePath();
                                    music.name = RF.getName();
                                    highlightPos = -1;

                                    prepareTmpString(music.name);
                                    tryPlay(music.filePath);

                                    musicItem.clear();
                                    musicItem.addAll(prepareMusicItems());
                                    adapter.notifyDataSetChanged();

                                    stop_dialog.dismiss();
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                });
                recorder_builder.create().show();
            }
        });

        // 確認
        pos_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();

                if (SPM.MusicSet(music)) {
                    Intent intent = new Intent();
                    intent.setClass(context, MainActivity.class);
                    setResult(Const.MUSIC_CHOOSE_INTENT_CODE, intent);
                    finish();
                }
            }
        });
    }

    private void prepareTitleString(String str) {
        if (str.equals(SPManager.MusicConst.NAME_DEF)) {
            setTitle(getResources().getString
                    (com.ballfish.ShakePlayer.R.string.music_alert_title)
                    + getResources().getStringArray(com.ballfish.ShakePlayer.R.array.music_basic_items)[0]);
        } else {
            setTitle(getResources().getString(com.ballfish.ShakePlayer.R.string.music_alert_title) + str);
        }

        this.tileString = str;
    }

    private void prepareTmpString(String str) {
        TextView tmp = (TextView) findViewById(com.ballfish.ShakePlayer.R.id.music_choose_name);
        if (str.equals(SPManager.MusicConst.NAME_DEF)) {
            tmp.setText(getResources().getStringArray(com.ballfish.ShakePlayer.R.array.music_basic_items)[0]);
        } else {
            tmp.setText(str);
        }
    }

    private List<HashMap<String, String>> prepareMusicItems() {
        // 音源選單
        List<HashMap<String, String>> musicItem = new ArrayList<>();
        // 基本
        String[] names = getResources().getStringArray(com.ballfish.ShakePlayer.R.array.music_basic_items);
        for (int cnt = 0;cnt < names.length;cnt++) {
            HashMap<String, String> tmp = new HashMap<>();
            tmp.put(Const.MUSIC_ITEM_NAME, names[cnt]);
            tmp.put(Const.MUSIC_ITEM_BELONG, SPManager.MusicConst.MODE_RESOURCE + "");
            tmp.put(Const.MUSIC_ITEM_RESOURCE, "" + Const.MUSIC_ITEMS[cnt]);
            tmp.put(Const.MUSIC_ITEM_IS_RECORD, "false");
            musicItem.add(tmp);
        }
        // 錄音
        names = direct.list();
        for (int cnt = 0;cnt < names.length;cnt++) {
            HashMap<String, String> tmp = new HashMap<>();
            tmp.put(Const.MUSIC_ITEM_NAME, names[cnt]);
            tmp.put(Const.MUSIC_ITEM_BELONG, SPManager.MusicConst.MODE_FILE_PATH + "");
            tmp.put(Const.MUSIC_ITEM_RESOURCE, direct.getAbsolutePath() + "/" + names[cnt]);
            tmp.put(Const.MUSIC_ITEM_IS_RECORD, "true");
            musicItem.add(tmp);
        }

        // 常用檔案
        Set<String> tmpset = new HashSet<>();
        Iterator<String> SPit = music.oftenFiles.iterator();
        while (SPit.hasNext()) {
            String tmpstr = SPit.next();
            File tmpf = new File(tmpstr);
            if (tmpf.exists()) {
                HashMap<String, String> tmp = new HashMap<>();
                String[] tmparr = tmpstr.split("/");
                tmp.put(Const.MUSIC_ITEM_NAME, tmparr[tmparr.length-1]);
                tmp.put(Const.MUSIC_ITEM_BELONG, SPManager.MusicConst.MODE_FILE_PATH + "");
                tmp.put(Const.MUSIC_ITEM_RESOURCE, tmpstr);
                tmp.put(Const.MUSIC_ITEM_IS_RECORD, "false");
                musicItem.add(tmp);
                tmpset.add(tmpstr);
            }
        }
        music.oftenFiles = tmpset;
        SPM.MusicOftenFileSet(music.oftenFiles);

        return musicItem;
    }

    private void reset() {
        music.mode = SPManager.MusicConst.MODE_DEF;
        music.resource = SPManager.MusicConst.RESOURCE_DEF;
        music.name = SPManager.MusicConst.NAME_DEF;
        prepareTitleString(music.name);
        prepareTmpString(music.name);

        SPM.MusicSetApply(music);
    }

    // 試播
    private void tryPlay(int resource) {
        stop();
        tryPlay = MediaPlayer.create(context, resource);
        tryPlay.start();
    }

    // 試播
    private void tryPlay(String path) {
        stop();
        File f = new File(path);
        Uri uri = Uri.fromFile(f);
        tryPlay = MediaPlayer.create(context, uri);
        tryPlay.start();
    }

    // 停止
    private void stop() {
        if (tryPlay != null) {
            if (tryPlay.isPlaying()) {
                tryPlay.stop();
                tryPlay.release();
                tryPlay = null;
            }
        }
    }

    @Override
    protected void onStop() {
        stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        stop();
        super.onDestroy();
    }

    // 檔案選擇
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (MFC.onActivityResult(requestCode, resultCode, data)) {
            File[] files = MFC.getChosenFiles();
            if (files.length == 1) {
                File f = files[0];
                if (f != null) {
                    String path = f.getAbsolutePath();
                    String fileName = f.getName();
                    if (fileName.indexOf(".mp3") >= 0
                            || fileName.indexOf(".ogg") >= 0
                            || fileName.indexOf(".wav") >= 0
                            || fileName.indexOf(".3gp") >= 0
                            || fileName.indexOf(".amr") >= 0
                            || fileName.indexOf(".m4a") >= 0) {

                        music.oftenFiles.add(path);
                        musicItem.clear();
                        musicItem.addAll(prepareMusicItems());
                        music.mode = SPManager.MusicConst.MODE_FILE_PATH;
                        music.filePath = path;
                        music.name = fileName;
                        prepareTmpString(music.name);
                        tryPlay(music.filePath);

                        highlightPos = -1;
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(context, com.ballfish.ShakePlayer.R.string.file_nonsupport, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, com.ballfish.ShakePlayer.R.string.file_fail, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, com.ballfish.ShakePlayer.R.string.file_zero, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, com.ballfish.ShakePlayer.R.string.file_zero, Toast.LENGTH_SHORT).show();
        }
    }
}
