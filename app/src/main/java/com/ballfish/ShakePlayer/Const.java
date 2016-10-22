package com.ballfish.ShakePlayer;

/**
 * Created by 慈吟 on 2015/11/26.
 */
public interface Const {

    // Intent Code
    int MUSIC_CHOOSE_INTENT_CODE = 1;
    int SCREEN_CHOOSE_INTENT_CODE = 2;

    // 音檔
    int[] MUSIC_ITEMS = {
		com.ballfish.ShakePlayer.R.raw.bit808,
		com.ballfish.ShakePlayer.R.raw.fema,
		com.ballfish.ShakePlayer.R.raw.hatz,
		com.ballfish.ShakePlayer.R.raw.horn,
		com.ballfish.ShakePlayer.R.raw.kick,
		com.ballfish.ShakePlayer.R.raw.maevox,
		com.ballfish.ShakePlayer.R.raw.monstervox,
		com.ballfish.ShakePlayer.R.raw.pitchvox,
		com.ballfish.ShakePlayer.R.raw.snarecap
	};

    // 清單相關
    String MUSIC_ITEM_NAME = "name";
    String MUSIC_ITEM_BELONG = "belong";
    String MUSIC_ITEM_RESOURCE = "resource";
    String MUSIC_ITEM_IS_RECORD = "isRecord";

    String MASCOT_ITEM_DRAWABLE = "drawable";
    String MASCOT_ITEM_NAME = "mascotName";

    String COLORS_ITEM_ICON = "icon";
    String COLORS_ITEM_PRE = "pre";

    // 吉祥物動畫
    int MASCOT_NONE = com.ballfish.ShakePlayer.R.drawable.none;

    String[] COLORS_ICONS = {
        "Color Group 1",
        "Color Group 2",
        "Color Group 3",
        "Color Group 4",
        "Color Group 5",
        "Color Group 6",
        "Color Group 7",
        "Color Group 8"
    };

    int[] COLOR_GROUPS = {
        com.ballfish.ShakePlayer.R.array.color1,
        com.ballfish.ShakePlayer.R.array.color2,
        com.ballfish.ShakePlayer.R.array.color3,
        com.ballfish.ShakePlayer.R.array.color4,
        com.ballfish.ShakePlayer.R.array.color5,
        com.ballfish.ShakePlayer.R.array.color6,
        com.ballfish.ShakePlayer.R.array.color7,
        com.ballfish.ShakePlayer.R.array.color8
    };

    // 資料夾名稱
    String DIRECT_NAME = "/ShakeAndCheer/";
}
