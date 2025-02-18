package com.moonike.project.tookit;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.concurrent.ThreadLocalRandom;

public class LinkUtil {

    public static final long ONE_WEEK_MILLIS = 7 * 24 * 60 * 60 * 1000L;
    public static final long ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L;

    /**
     * 获取短链接缓存过期时间戳
     *
     * @param date 短链接失效日期(为空表示短链接永久有效)
     * @return 短链接缓存过期时间戳
     */
    public static long getShortLinkCacheTime(LocalDateTime date) {
        // 当前时间
        long currentTimeMillis = System.currentTimeMillis();

        // 如果 date 为 null，则默认设置为一周后的时间戳，并随机一个 0 ~ 24 小时的时间
        if (date == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 7);
            return calendar.getTimeInMillis() + ThreadLocalRandom.current().nextLong(0, ONE_DAY_MILLIS);
        }

        // 将date转换为毫秒
        long dateMillis = date.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();

        // 检查 date 是否超过一周
        if (dateMillis - currentTimeMillis > ONE_WEEK_MILLIS) {
            // 如果超过一周，那么将有效时间设置在一周后，并随机一个 0 ~ 24 小时的时间
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 7);
            return calendar.getTimeInMillis() + ThreadLocalRandom.current().nextLong(0, ONE_DAY_MILLIS);
        } else {
            // 如果没有超过一周，则设置为 date 的时间戳
            return dateMillis;
        }
    }
}
