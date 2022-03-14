package com.niucube.compui.beauty;

import com.sensetime.stmobile.STEffectBeautyType;

public class Constants {



    public final static String GROUP_2D = "2c120dd0c67711ea83df02d98a1a02bd";
    public final static String GROUP_3D = "24b19c20c67e11ea83df02d98a1a02bd";
    public final static String GROUP_HAND = "42d080b0c67711ea83df02d98a1a02bd";
    public final static String GROUP_BG = "8df81880c68811ea83df02d98a1a02bd";

    public final static String GROUP_LIP = "e3b3c4d0c68911ea83df02d98a1a02bd";
    public final static String GROUP_EYEBALL = "40c75600c58111ea83df02d98a1a02bd";
    public final static String GROUP_BLUSH = "ff781360c68911ea83df02d98a1a02bd";
    public final static String GROUP_BROW = "19dd2010c68a11ea83df02d98a1a02bd";
    public final static String GROUP_HIGHLIGHT = "2ce3e220c68a11ea83df02d98a1a02bd";
    public final static String GROUP_EYE = "44c61480c68a11ea83df02d98a1a02bd";
    public final static String GROUP_EYELINER = "5e1bfb20c68a11ea83df02d98a1a02bd";
    public final static String GROUP_EYELASH = "70eabd40c68a11ea83df02d98a1a02bd";

    public static final String BASE_BEAUTY = "baseBeauty";
    public static final String PROFESSIONAL_BEAUTY = "professionalBeauty";
    public static final String MICRO_BEAUTY = "microBeauty";
    public static final String ADJUST_BEAUTY = "adjustBeauty";

    public static final String MAKEUP_BLUSH = "makeup_blush";
    public static final String MAKEUP_BROW = "makeup_brow";
    public static final String MAKEUP_EYE = "makeup_eye";
    public static final String MAKEUP_LIP = "makeup_lip";
    public static final String MAKEUP_HIGHLIGHT = "makeup_highlight";
    public static final String MAKEUP_EYELINER = "makeup_eyeliner";
    public static final String MAKEUP_EYELASH = "makeup_eyelash";
    public static final String MAKEUP_EYEBALL = "makeup_eyeball";

    public static final String FILTER_PORTRAIT = "filter_portrait";
    public static final String FILTER_SCENERY = "filter_scenery";
    public static final String FILTER_STILL_LIFE = "filter_still_life";
    public static final String FILTER_FOOD = "filter_food";

    public static final String NEW_ENGINE = "newEngine";
    public static final String STICKER_NEW_ENGINE = "sticker_new_engine";

    public static final String ORIGINAL = "original";


    public static final int[] BEAUTY_TYPES = {
            STEffectBeautyType.EFFECT_BEAUTY_BASE_REDDEN,
            STEffectBeautyType.EFFECT_BEAUTY_BASE_FACE_SMOOTH,
            STEffectBeautyType.EFFECT_BEAUTY_BASE_WHITTEN,
            STEffectBeautyType.EFFECT_BEAUTY_RESHAPE_ENLARGE_EYE,
            STEffectBeautyType.EFFECT_BEAUTY_RESHAPE_SHRINK_FACE,
            STEffectBeautyType.EFFECT_BEAUTY_RESHAPE_SHRINK_JAW,
            STEffectBeautyType.EFFECT_BEAUTY_TONE_CONTRAST,
            STEffectBeautyType.EFFECT_BEAUTY_TONE_SATURATION,
            STEffectBeautyType.EFFECT_BEAUTY_RESHAPE_NARROW_FACE,
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_NARROW_NOSE,
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_NOSE_LENGTH,
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_CHIN_LENGTH,
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_MOUTH_SIZE,
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_PHILTRUM_LENGTH,
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_HAIRLINE_HEIGHT,
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_THIN_FACE,
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_EYE_DISTANCE,
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_EYE_ANGLE,
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_OPEN_CANTHUS,
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_PROFILE_RHINOPLASTY,
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_BRIGHT_EYE,
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_REMOVE_DARK_CIRCLES,
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_REMOVE_NASOLABIAL_FOLDS,
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_WHITE_TEETH,
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_APPLE_MUSLE,
            STEffectBeautyType.EFFECT_BEAUTY_RESHAPE_ROUND_EYE,
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_SHRINK_CHEEKBONE,
            STEffectBeautyType.EFFECT_BEAUTY_TONE_SHARPEN
    };

    public static final int ST_BEAUTIFY_REDDEN_STRENGTH = 0;
    public static final int ST_BEAUTIFY_SMOOTH_STRENGTH = 1;
    public static final int ST_BEAUTIFY_WHITEN_STRENGTH = 2;
    public static final int ST_BEAUTIFY_ENLARGE_EYE_RATIO = 3;
    public static final int ST_BEAUTIFY_SHRINK_FACE_RATIO = 4;
    public static final int ST_BEAUTIFY_SHRINK_JAW_RATIO = 5;
    public static final int ST_BEAUTIFY_CONSTRACT_STRENGTH = 6;
    public static final int ST_BEAUTIFY_SATURATION_STRENGTH = 7;
    public static final int ST_BEAUTIFY_NARROW_FACE_STRENGTH = 8;
    public static final int ST_BEAUTIFY_3D_NARROW_NOSE_RATIO = 9;
    public static final int ST_BEAUTIFY_3D_NOSE_LENGTH_RATIO = 10;
    public static final int ST_BEAUTIFY_3D_CHIN_LENGTH_RATIO = 11;
    public static final int ST_BEAUTIFY_3D_MOUTH_SIZE_RATIO = 12;
    public static final int ST_BEAUTIFY_3D_PHILTRUM_LENGTH_RATIO = 13;
    public static final int ST_BEAUTIFY_3D_HAIRLINE_HEIGHT_RATIO = 14;
    public static final int ST_BEAUTIFY_3D_THIN_FACE_SHAPE_RATIO = 15;
    public final static int ST_BEAUTIFY_3D_EYE_DISTANCE_RATIO = 16;
    public final static int ST_BEAUTIFY_3D_EYE_ANGLE_RATIO = 17;
    public final static int ST_BEAUTIFY_3D_OPEN_CANTHUS_RATIO = 18;
    public final static int ST_BEAUTIFY_3D_PROFILE_RHINOPLASTY_RATIO = 19;
    public final static int ST_BEAUTIFY_3D_BRIGHT_EYE_RATIO = 20;
    public final static int ST_BEAUTIFY_3D_REMOVE_DARK_CIRCLES_RATIO = 21;
    public final static int ST_BEAUTIFY_3D_REMOVE_NASOLABIAL_FOLDS_RATIO = 22;
    public final static int ST_BEAUTIFY_3D_WHITE_TEETH_RATIO = 23;
    public final static int ST_BEAUTIFY_3D_APPLE_MUSLE_RATIO = 24;
    public final static int ST_BEAUTIFY_ROUND_EYE_RATIO = 25;
    public final static int ST_BEAUTIFY_SHRINK_CHEEKBONE_RATIO = 26;
    public final static int ST_BEAUTIFY_SHARPEN_STRENGTH = 27;

    public static final int ST_MAKEUP_LIP = STEffectBeautyType.EFFECT_BEAUTY_MAKEUP_LIP;
    public static final int ST_MAKEUP_HIGHLIGHT = STEffectBeautyType.EFFECT_BEAUTY_MAKEUP_NOSE;
    public static final int ST_MAKEUP_BLUSH = STEffectBeautyType.EFFECT_BEAUTY_MAKEUP_CHEEK;
    public static final int ST_MAKEUP_BROW = STEffectBeautyType.EFFECT_BEAUTY_MAKEUP_EYE_BROW;
    public static final int ST_MAKEUP_EYE = STEffectBeautyType.EFFECT_BEAUTY_MAKEUP_EYE_SHADOW;
    public static final int ST_MAKEUP_EYELINER = STEffectBeautyType.EFFECT_BEAUTY_MAKEUP_EYE_LINE;
    public static final int ST_MAKEUP_EYELASH = STEffectBeautyType.EFFECT_BEAUTY_MAKEUP_EYE_LASH;
    public static final int ST_MAKEUP_EYEBALL = STEffectBeautyType.EFFECT_BEAUTY_MAKEUP_EYE_BALL;

    public static final int MAKEUP_TYPE_COUNT = 9;
}
