package com.self.cloudserver.utils;


import com.self.cloudserver.constants.CommonConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 时间工具类
 */
public class DateTimeUtils {

    private static final Logger logger = LoggerFactory.getLogger(DateTimeUtils.class);

    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    public static final String YYYY_MM_DD = "yyyy-MM-dd";

    public static final DateFormat dateFormat = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS);

    public static final DateFormat shortDateFormat = new SimpleDateFormat(YYYY_MM_DD);

    private static final Object lockObj = new Object();

    private static Map<String, ThreadLocal<SimpleDateFormat>> sdfMap = new HashMap<>();

    public static SimpleDateFormat getSdf(final String pattern){
        ThreadLocal<SimpleDateFormat> threadLocal = sdfMap.get(pattern);

        if(threadLocal == null){
            synchronized (lockObj){
                threadLocal = sdfMap.get(pattern);
                if(threadLocal == null){
                    threadLocal = ThreadLocal.withInitial(() -> new SimpleDateFormat(pattern));
                    sdfMap.put(pattern, threadLocal);
                }
            }
        }

        return threadLocal.get();
    }

    public static String dateToString(Date date){
        return getSdf(YYYY_MM_DD_HH_MM_SS).format(date);
    }

    public static String shortDateToString(Date date){
        return getSdf(YYYY_MM_DD).format(date);
    }

    public static Date stringToDate(String dateStr){
        try {
            if(!dateStr.contains(CommonConstants.STR_COLON)){
                dateStr += " 00:00:00";
            }
            return getSdf(YYYY_MM_DD_HH_MM_SS).parse(dateStr);
        } catch (ParseException e) {
            logger.error("时间转换异常：", e);
            return null;
        }
    }

    public static Date stringToShortDate(String dateStr){
        try {
            return getSdf(YYYY_MM_DD).parse(dateStr);
        } catch (ParseException e) {
            logger.error("日期转换异常：", e);
            return null;
        }
    }

    /**
     * 时间转日期
     * @param date 时间
     * @return 日期
     */
    public static Date dateTimeConvertDate(Date date){
        if(date == null){
            return null;
        }

        return stringToShortDate(shortDateToString(date));
    }

    public static Long diffDays(Date startDate, Date endDate){
        //只保留日期
        startDate = dateTimeConvertDate(startDate);
        endDate = dateTimeConvertDate(endDate);

        if(startDate == null || endDate == null){
            return null;
        }

        if(startDate.after(endDate)){
            return 0L;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        long startTimeInMillis = calendar.getTimeInMillis();
        calendar.setTime(endDate);
        long endTimeInMillis = calendar.getTimeInMillis();
        long diffDays = (endTimeInMillis - startTimeInMillis) / (1000 * 3600 * 24);
        return Long.parseLong(String.valueOf(diffDays));
    }

    /**
     * 时间偏移天数
     * @param date 时间
     * @param num 偏移天数
     * @return 偏移时间
     */
    public static Date offsetDay(Date date, Integer num){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, num);

        return calendar.getTime();
    }

    /**
     * 时间数字补0占位
     * @param num 时间数字
     * @return 时间字符
     */
    public static String timeNumFillZero(int num){
        if(num < 0 || num > 9){
            return "" + num;
        }else{
            return "0" + num;
        }
    }

}
