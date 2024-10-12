package com.mypan.entity.constants;

public class Constants {
    public static final Integer length_5=5;
    public static final Integer length_15=15;
    public static final Integer length_10=10;
    public static final Integer length_30=30;
    public static final Integer length_20=20;
    public static final Integer length_50=50;
    public static final Integer length_150=150;
    public static final String check_code_key="check_code_key";
    public static final String check_code_key_email="check_code_key_email";
    public static final Integer zero=0;
    public static final String zero_str="0";
    public static final String redis_key_sys_setting="mypan:syssetting:";
    public static final Long MB=1024*1024L;
    public static final Integer redis_key_expires_one_min=60;
    public static final String redis_key_user_space_use="mypan:user:spaceuse:";
    public static final Long redis_key_expires_day=redis_key_expires_one_min*60*24L;
    public static final Long redis_key_expires_5min=redis_key_expires_one_min*5L;
    public static final Long redis_key_expires_one_hour=redis_key_expires_one_min*60L;
    public static final String redis_key_user_file_temp_size="mypan:user:file:temp:";
    public static final String redis_key_download="mypan:user:download:";

    public static final String session_key="session_key";
    public static final String session_share_key="session_share_key_";

    public static final String file_folder_file="/file/";
    public static final String file_folder_avatar_name="avatar/";
    public static final String file_folder_temp="/temp/";
    public static final String avatar_suffix=".jpg";
    public static final String default_avatar="default_avatar.jpg";
    public static final String view_obj_result_key="result";
    public static final String ts_name="index.ts";
    public static final String m3u8_name="index.m3u8";
    public static final String image_png_suffix=".png";
}
