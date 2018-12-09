package chenbxxx.util;


import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/8/9
 * <p>
 * java.time包内，有几个比较重要的组件，
 * Instant（时间戳）
 * LocalDate（日期）
 * LocalDate（时间）
 * LocalDateTime（日期时间）；
 * ZonedDateTime（带有区域信息的日期时间，比如中国默认使用的是东八区）
 * Period（如两个日期之间相差的年、月、天数）
 * Druation（两个日期时间之间间隔的秒和纳秒）
 * ChronoUnit（计算各种单位的时间）
 */

public class TimeUtil {

    /**
     * Date -> LocalDateTime
     *
     * @param date
     * @return
     */
    public static LocalDateTime date2LocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * LocalDateTime -> Date
     *
     * @param localDateTime
     * @return
     */
    public static Date dateLocalTime2Date(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * date -> localDate
     *
     * @param date date对象
     * @return
     */
    public static LocalDate date2LocalDate(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toLocalDate();
    }


    /**
     * 计算给定时间到当前时间的年份数
     *
     * @param date
     * @return
     */
    public static Long dateToNowYearNum(Date date) {
        if (null == date) {
            return 0L;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dateTime = date2LocalDateTime(date);

        return Math.abs(now.until(dateTime, ChronoUnit.YEARS));
    }

    /**
     * 计算给定时间到当前时间的月份数
     *
     * @param date
     * @return
     */
    public static Long dateToNowMonthNum(Date date) {
        if (null == date) {
            return 0L;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dateTime = date2LocalDateTime(date);

        return Math.abs(now.until(dateTime, ChronoUnit.MONTHS));
    }

    /**
     * 计算给定时间到当前时间的天数
     *
     * @param date
     * @return
     */
    public static Long dateToNowDayNum(Date date) {
        if (null == date) {
            return 0L;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dateTime = date2LocalDateTime(date);

        return Math.abs(now.until(dateTime, ChronoUnit.DAYS));
    }

    /**
     * 计算给定时间到给定的月份
     *
     * @param start
     * @param end
     * @return
     */
    public static Long dateToDateMonthNum(Date start, Date end) {
        if (null == start || null == end) {
            return 0L;
        }
        LocalDateTime startTime = date2LocalDateTime(start);
        LocalDateTime endTime = date2LocalDateTime(end);

        return Math.abs(startTime.until(endTime, ChronoUnit.MONTHS));
    }


    public static void main(String[] args) {
        System.out.println(1 << 30);
            }
}
