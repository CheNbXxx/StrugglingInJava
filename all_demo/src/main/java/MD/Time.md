### 表示时间的类
1. Date   
  每个初学者必会的一个类,但在格式和时区等方面存在很多限制.
2. LocalDate     
  当前的时间,精确到天
3. LocalDateTime
  当前时间,精确到毫秒级
  
  
 ### 计算时间差
 - LocalDateTime.until(LocalDateTime)  计算调用时间到参数时间的时间差，例如`l1.until(l2,ChronoUnit.SECONDS);`就是是l1到l2的分钟差值 如果l1在l2之前则为负数      
 - Duration.between     