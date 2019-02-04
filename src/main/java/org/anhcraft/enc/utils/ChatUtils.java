package org.anhcraft.enc.utils;

public class ChatUtils {
    public static final char SECTION_SIGN = '\u00A7';

    public static String reverseColorCode(String str){
        return str.replace(SECTION_SIGN, '&');
    }
}
