package com.posdb.sync.utils;

import lombok.experimental.UtilityClass;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@UtilityClass
public class TextUtil {

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }


    public static Date parseDate(String dateStr) throws ParseException {
        if (isEmpty(dateStr)) {
            throw new ParseException("Date string cannot be empty", 0);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.parse(dateStr);
    }

}
