// src/main/java/com/example/util/DateUtil.java
package jbook.jshop.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    // 원하는 포맷: "yyyyMMdd hh24:mi" (Java에서는 "yyyyMMdd HH:mm" 사용)
    private static final String FORMAT = "yyyyMMdd HH:mm";

    public static String format(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat(FORMAT).format(date);
    }
}
