package com.moonike.project.tookit;

import jakarta.servlet.http.HttpServletRequest;

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

    /**
     * 获取用户真实IP
     * @param request 请求
     * @return 用户真实IP地址
     */
    public static String getActualIP(HttpServletRequest request) {
        return request.getRemoteAddr();
//        String ip = null;
//        // 按优先级检查代理标头
//        String[] headers = {"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP"};
//        for (String header : headers) {
//            ip = request.getHeader(header);
//            if (isValidIp(ip)) {
//                break;
//            }
//        }
//
//        // 处理 X-Forwarded-For 的多级代理情况（取第一个有效 IP）
//        if (!isValidIp(ip) && ip != null && ip.contains(",")) {
//            ip = Arrays.stream(ip.split(","))
//                    .map(String::trim)
//                    .filter(LinkUtil::isValidIp)
//                    .findFirst()
//                    .orElse(null);
//        }
//
//        // 兜底方案
//        return isValidIp(ip) ? ip : request.getRemoteAddr();
    }

    /**
     * 判断是否是有效IP
     * @param ip
     * @return
     */
    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty()
                && !"unknown".equalsIgnoreCase(ip)
                && !ip.startsWith("127.0.0.1")
                && !ip.startsWith("0:0:0:0:0:0:0:1");
    }

    /**
     * 获取用户访问操作系统
     * @param request 请求
     * @return 访问操作系统
     */
    public static String getOs(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent.toLowerCase().contains("windows")) {
            return "Windows";
        } else if (userAgent.toLowerCase().contains("mac")) {
            return "Mac OS";
        } else if (userAgent.toLowerCase().contains("linux")) {
            return "Linux";
        } else if (userAgent.toLowerCase().contains("android")) {
            return "Android";
        } else if (userAgent.toLowerCase().contains("iphone") || userAgent.toLowerCase().contains("ipad")) {
            return "iOS";
        } else {
            return "Unknown";
        }
    }


}
