package com.self.cloudserver.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class CronUtils {

    private static final SimpleDateFormat cronFormat = DateTimeUtils.getSdf("ss mm HH dd MM ? yyyy");

    public static String formatDateByPattern(Date date){
        String formatTimeStr = null;
        if (Objects.nonNull(date)) {
            formatTimeStr = cronFormat.format(date);
        }
        return formatTimeStr;
    }

    public static String getCron(Date date) {
        return formatDateByPattern(date);
    }

}
