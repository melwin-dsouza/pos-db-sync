package com.posdb.sync.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TextUtil {

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
