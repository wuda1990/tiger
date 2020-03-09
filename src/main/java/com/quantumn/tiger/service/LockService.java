package com.quantumn.tiger.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scripting.ScriptSource;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁
 * 通过redis的setnx命令实现，多个进程都去尝试获得锁，只有一个最后能拿到锁。
 */
@Service
@Slf4j
public class LockService {

    @Autowired
    @Qualifier("redisTemplate")
    RedisTemplate redisTemplate;

    /**
     * 尝试去获取锁资源
     * @param key
     * @param timeout
     * @param unit
     * @return
     */
    public boolean tryLock(String key, long timeout, TimeUnit unit,String requestId) {
        BoundValueOperations operations = redisTemplate.boundValueOps(key);
       return operations.setIfAbsent(requestId, timeout, unit);
    }

    /**
     * 释放锁资源
     * @param key
     */
    public boolean releaseLock(String key,String requestId) {
        log.info("Lock:"+ redisTemplate.boundValueOps(key).get());
        ScriptSource scriptSource = new ResourceScriptSource(new ClassPathResource("redis_unlock.lua"));
        DefaultRedisScript defaultRedisScript = new DefaultRedisScript();
        defaultRedisScript.setScriptSource(scriptSource);
        defaultRedisScript.setResultType(Long.class);
        List<String> keyList = new ArrayList<>();
        keyList.add(key);
        List<String> valueList = new ArrayList<>();
        valueList.add(requestId);
        //这里key,requestId用的默认的key和value序列化的方式存在redis里的
//        Long result = (Long) redisTemplate.execute(defaultRedisScript, keyList, requestId);
        Long result = (Long) redisTemplate.execute(defaultRedisScript, keyList, valueList.toArray());


        //stringRedisTemplate的写法，直接获得string的字节
//        boolean result = (boolean) redisTemplate.execute(new RedisCallback() {
//            @Override
//            public Object doInRedis(RedisConnection connection) throws DataAccessException {
//                try {
//                    //Here the return type must be Long, or redis can't return ,app will time out
//                    Long result = connection.eval(scriptSource.getScriptAsString().getBytes(),
//                            ReturnType.INTEGER, 1, key.getBytes(), requestId.getBytes());
//                    return result  == 1;
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                return false;
//            }
//        });
        log.info("Lock:"+ redisTemplate.boundValueOps(key).get()+",result:"+result);
        return result==1;
    }

    public void printLock(String key) {
        log.info("Lock:"+ redisTemplate.boundValueOps(key).get());
    }
}
