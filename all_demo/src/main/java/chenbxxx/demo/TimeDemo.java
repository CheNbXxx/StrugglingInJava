package chenbxxx.demo;

import java.time.*;
import java.util.Date;

/**
 * @author chen
 * @email ai654778@vip.qq.com
 * @date 7/19/18
 */
public class TimeDemo {
    public static void main(String[] args) {
        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDateTime localDateTime1 = LocalDateTime.now().minusMinutes(10);
        Duration duration = Duration.between(localDateTime,localDateTime1);
        System.out.println(duration.toMinutes());
        new TimeDemo().showAllClassAboutTime();
    }

    public Date LocalDateTime2Date(LocalDateTime localDateTime){
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public void showAllClassAboutTime(){
        System.out.println(LocalDateTime.now());
        System.out.println(LocalDate.now());
        System.out.println(new Date());
        System.out.println(ZonedDateTime.now());
    }

}
