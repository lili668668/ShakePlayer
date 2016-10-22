package com.ballfish.ShakePlayer;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ScreenChooseActivity extends AppCompatActivity {

    private Context context;
    private SharedPreferences SP;
    private SPManager SPM;
    private MagicFileChooser MFC;

    private Screen screen;
    private ColorSet colorSet;

    private SimpleAdapter mascot_adapter;

    private AlertDialog colors_dialog;

    private int highlightPos = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(com.ballfish.ShakePlayer.R.layout.activity_screen_choose);

        context = this;

        SP = getSharedPreferences(SPManager.PreferencesConst.NAME, Context.MODE_PRIVATE);
        SPM = new SPManager(SP);

        screen = SPM.getScreen();
        colorSet = SPM.getColoSet();

        AlertDialog.Builder colors_builder = new AlertDialog.Builder(context);

        View colors_layout = getLayoutInflater().inflate(com.ballfish.ShakePlayer.R.layout.colors_choose_dialog, (ViewGroup) findViewById(com.ballfish.ShakePlayer.R.id.colors_dialog));

        colors_dialog = colors_builder
                .setView(colors_layout)
                .setNegativeButton(com.ballfish.ShakePlayer.R.string.choose_one_color, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        changeDialog(1);
                    }
                })
                .create();

        ListView colors_listview = (ListView) colors_layout.findViewById(com.ballfish.ShakePlayer.R.id.colors_listview);
        final List<HashMap<String, Object>> colorsItem = prepareColors();
        final SimpleAdapter color_adapter = new SimpleAdapter(context, colorsItem, com.ballfish.ShakePlayer.R.layout.colors_item,
                new String[]{Const.COLORS_ITEM_ICON},
                new int[]{com.ballfish.ShakePlayer.R.id.colors_icon});
        colors_listview.setAdapter(color_adapter);

        colors_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                colorSet.mode = SPManager.ColorConst.MODE_GROUP;
                colorSet.group = Const.COLOR_GROUPS[position];
                colorSet.groupPos = position;
                prepareButtonColor();
                colors_dialog.dismiss();
            }
        });

        // button 選擇底色
        ImageButton color_bt = (ImageButton) findViewById(com.ballfish.ShakePlayer.R.id.color_button);

        // switch 要不要圖片
        Switch mascot_none_switch = (Switch) findViewById(com.ballfish.ShakePlayer.R.id.mascot_none_switch);

        // listview清單
        final ListView listview = (ListView) findViewById(com.ballfish.ShakePlayer.R.id.mascot_listview);

        // disable list不能使用的薄膜
        final ImageView disable = (ImageView) findViewById(com.ballfish.ShakePlayer.R.id.disable);

        // file choose檔案選擇
        Button file_choose = (Button) findViewById(com.ballfish.ShakePlayer.R.id.screen_file_choose);

        // 確認
        Button pos_bt = (Button) findViewById(com.ballfish.ShakePlayer.R.id.screen_pos);

        prepareTitleString(screen.name);
        prepareTmpString(screen.name);
        if (screen.mode == SPManager.ScreenConst.MODE_NONE) {
            listview.setEnabled(false);
            disable.setVisibility(View.VISIBLE);
        }

        // button 選擇底色
        prepareButtonColor();
        color_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (colorSet.mode == SPManager.ColorConst.MODE_ONE) {
                    changeDialog(1);
                } else {
                    changeDialog(2);
                }
            }
        });

        // switch 要不要圖片
        mascot_none_switch.setChecked(screen.mode != SPManager.ScreenConst.MODE_NONE);

        mascot_none_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    SPM.ScreenBeforeSet(screen);

                    screen.mode = SPManager.ScreenConst.MODE_NONE;
                    screen.name = getString(com.ballfish.ShakePlayer.R.string.none);
                    prepareTmpString(screen.name);

                    listview.setEnabled(false);
                    disable.setVisibility(View.VISIBLE);
                } else {
                    screen = SPM.getScreenBefore();

                    prepareTmpString(screen.name);

                    listview.setEnabled(true);
                    disable.setVisibility(View.GONE);
                }
            }
        });

        // file choose檔案選擇
        file_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MFC = new MagicFileChooser(ScreenChooseActivity.this);
                if (!MFC.showFileChooser("image/*")) {
                    Toast.makeText(context, com.ballfish.ShakePlayer.R.string.file_fail, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 確認
        pos_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SPM.ScreenSet(screen) && SPM.ColorSetSet(colorSet)) {
                    Intent intent = new Intent();
                    intent.setClass(context, MainActivity.class);
                    setResult(Const.SCREEN_CHOOSE_INTENT_CODE, intent);
                    finish();
                }
            }
        });


    }

    private List<HashMap<String, Object>> prepareColors() {
        List<HashMap<String, Object>> colorsItem = new ArrayList<>();

        // resource
        int size = Const.COLOR_GROUPS.length;
        for (int cnt = 0;cnt < size;cnt++) {
            HashMap<String, Object> tmp = new HashMap<>();
            tmp.put(Const.COLORS_ITEM_ICON, Const.COLORS_ICONS[cnt]);
            colorsItem.add(tmp);
        }

        return colorsItem;
    }

    private void prepareTitleString(String str) {
        if (str.equals(SPManager.ScreenConst.NAME_DEF)) {
            setTitle(getResources().getString
                    (com.ballfish.ShakePlayer.R.string.screen_alert_title)
                    + getResources().getStringArray(com.ballfish.ShakePlayer.R.array.mascot_names)[0]);
        } else {
            setTitle(getResources().getString(com.ballfish.ShakePlayer.R.string.screen_alert_title) + str);
        }
    }

    private void prepareTmpString(String str) {
        TextView tmp = (TextView) findViewById(com.ballfish.ShakePlayer.R.id.tmp_text);
        if (str.equals(SPManager.ScreenConst.NAME_DEF)) {
            tmp.setText(getResources().getStringArray(com.ballfish.ShakePlayer.R.array.mascot_names)[0]);
        } else {
            tmp.setText(str);
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void prepareButtonColor() {
        ImageButton color_button = (ImageButton) findViewById(com.ballfish.ShakePlayer.R.id.color_button);
        switch (colorSet.mode) {
            case SPManager.ColorConst.MODE_ONE:
                color_button.setBackground(null);
                color_button.setImageResource(com.ballfish.ShakePlayer.R.drawable.color_button);
                color_button.setColorFilter(colorSet.one);
                break;
            case SPManager.ColorConst.MODE_GROUP:
                color_button.setBackground(null);
                color_button.setImageResource(com.ballfish.ShakePlayer.R.drawable.color_button);
                color_button.setColorFilter(Color.BLACK);
        }
    }

    private void changeDialog(int n) {
        switch (n) {
            case 1:
                ColorPickerDialogBuilder
                        .with(context)
                        .initialColor(colorSet.one)
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorSelectedListener(new OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int selectedColor) {

                            }
                        })
                        .setPositiveButton(getString(com.ballfish.ShakePlayer.R.string.pos_bt), new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                colorSet.mode = SPManager.ColorConst.MODE_ONE;
                                colorSet.one = selectedColor;
                                prepareButtonColor();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(getString(com.ballfish.ShakePlayer.R.string.choose_color_group), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                changeDialog(2);
                            }
                        })
                .build().show();
                break;
            case 2:
                final ProgressDialog progressDialog = new ProgressDialog(context);
                progressDialog.setCancelable(false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setTitle(com.ballfish.ShakePlayer.R.string.loading);
                progressDialog.show();
                new CountDownTimer(3000, 1000) {
                    int count = 0;
                    @Override
                    public void onTick(long millisUntilFinished) {
                        count++;
                        if (count == 2) {
                            colors_dialog.show();
                        }
                    }

                    // 過場
                    @Override
                    public void onFinish() {
                        progressDialog.dismiss();
                    }
                }.start();
                break;
        }
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
                    if (fileName.indexOf(".jpg") >= 0
                            || fileName.indexOf(".JPG") >= 0
                            || fileName.indexOf(".png") >= 0
                            || fileName.indexOf(".PNG") >= 0
                            || fileName.indexOf(".gif") >= 0
                            || fileName.indexOf(".jpeg") >= 0
                            || fileName.indexOf(".bmp") >= 0
                            || fileName.indexOf(".mp3") >= 0
                            || fileName.indexOf(".webp") >= 0) {
                        screen.mode = SPManager.ScreenConst.MODE_FILE_PATH;
                        screen.filePath = path;
                        screen.name = fileName;

                        highlightPos = -1;

                        mascot_adapter.notifyDataSetChanged();
                        prepareTmpString(screen.name);
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
