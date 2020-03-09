package com.quantumn.tiger;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author: huajun.wu
 * @create: 2019-12-05
 **/
public class DateTools {

    public static long getNowTimeStampSeconds() {
        return LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8));
    }

    public static long getNowTimeStampMilliSeconds() {
        return LocalDateTime.now().toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
    }
}
