package com.tagmarshal.golf.constants;

import android.os.Environment;

public class LogFileConstants {
    public final static String fixes_filePath = Environment.getExternalStorageDirectory() + "/fixes_info.txt";
    public final static String raw_fixes_filePath = Environment.getExternalStorageDirectory() + "/raw_fixes.txt";
    public final static String fix_sent = "fix-sent";
    public final static String fix_failed = "fix-failed";
    public final static String fix_inaccurate = "fix-inaccurate";
    public final static String fix_disallowed = "fix-disallowed";
    public final static String fix_duplicate = "fix-duplicate";
    public final static String fixes_sent  = "fixes-sent";
    public final static String fixes_failed  = "fixes-failed";
    public final static String round_info  = "round-info";

    public static String map_downloaded = "map-downloaded";
    public static String map_failed = "map-failed";

    public static String token_updated = "token-updated";
    public static String course_changed = "course-changed";
    public static String config_updated= "config-updated";
}
