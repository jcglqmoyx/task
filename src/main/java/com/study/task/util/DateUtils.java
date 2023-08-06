package com.study.task.util;

import java.util.Calendar;

public class DateUtils {
    public static String getNowDate() {
        Calendar calendar = java.util.Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DATE);
        return String.format("%d-%02d-%02d", year, month, day);
    }
}