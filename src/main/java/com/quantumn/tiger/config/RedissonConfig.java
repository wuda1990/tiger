package com.quantumn.tiger.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author: huajun.wu
 * @create: 2019-12-19
 **/
@Slf4j
//@Configuration
public class RedissonConfig {
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private String port;

    @Bean
    public RedissonClient redissonClient() {
        RedissonClient redissonClient;
        System.out.println("test.master");
        System.out.println("test.master.push");

        Config config = new Config();
        System.out.println("test!!!");
        String url = "redis://" + host + ":" + port;
        log.info("url:{}",url);
//        config.useSingleServer().setAddress(url);
        config.useMasterSlaveServers().setMasterAddress(url);
        try {
            log.info(config.toJSON().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            redissonClient = Redisson.create(config);
            return redissonClient;
        } catch (Exception e) {
            log.error("RedissonClient init redis url:[{}], Exception:", url, e);
            return null;
        }
    }

}
