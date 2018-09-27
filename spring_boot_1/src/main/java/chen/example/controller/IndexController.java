package chen.example.controller;

import chen.example.annotation.MyVerify;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenbxxx
 * @email ai654778@vip.qq.com
 * @date 2018/9/11
 */
@RestController
@RequestMapping("index")
@Slf4j
public class IndexController {

    @GetMapping("test")
    @MyVerify
    public Map<Integer,Object> test(@RequestParam("param") List<String> param1){
        log.info(param1.toString());
        Map<Integer, Object> returnValue =new HashMap<>(1);
        returnValue.put(1,"success");
        log.info("Get into test");
        return returnValue;
    }
}
