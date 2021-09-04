package com.swust.bigdata.item.utils;

import org.apache.commons.lang3.time.FastDateFormat;
import org.junit.jupiter.api.Test;

import java.text.ParseException;

/**
 * 格式化日期
 */
public class DateFormatUtils {
    public static String getDateFormatUtils(String date) throws ParseException {
        ;
        String pattern = "YYYY-MM-dd-HH";
        FastDateFormat fdf = FastDateFormat.getInstance(pattern);
        String format = fdf.format(Long.valueOf(date));
        return format;
    }

    @Test
    public void Test() throws ParseException {
        String date = "1606829727740";
        getDateFormatUtils(date);
    }

}
