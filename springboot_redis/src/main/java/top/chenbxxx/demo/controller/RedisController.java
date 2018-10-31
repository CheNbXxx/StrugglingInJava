package top.chenbxxx.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.chenbxxx.demo.entity.Product;
import top.chenbxxx.demo.service.ProductService;

import javax.annotation.Resource;

/**
 * function:
 *
 * @author CheNbXxx
 * @email chenbxxx@gmail.con
 * @date 2018/10/31 9:13
 */
@RestController
@RequestMapping("redis")
@Slf4j
public class RedisController {

    @Resource
    private ProductService productService;

    @RequestMapping("test")
    private Product redisTest(){
        Long start = System.currentTimeMillis();
        Product byId = productService.getById(1);
        Long end = System.currentTimeMillis();
        log.info("*************** 耗时:"+(end - start));
        return byId;
    }
}
