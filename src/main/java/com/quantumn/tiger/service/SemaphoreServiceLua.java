package com.quantumn.tiger.service;

import com.quantumn.tiger.DateTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.ScriptSource;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.Collections;
import java.util.UUID;

/**
 * @author: huajun.wu
 * @create: 2019-12-12
 **/
@Slf4j
public class SemaphoreServiceLua extends SemaphoreService {
    private DefaultRedisScript defaultRedisScript;
    public SemaphoreServiceLua(String semaphoreName, int limit, RedisTemplate redisTemplate, long timeout) {
        super(semaphoreName, limit, redisTemplate, timeout);
        ScriptSource scriptSource = new ResourceScriptSource(new ClassPathResource("acquire_semaphore.lua"));
        defaultRedisScript = new DefaultRedisScript();
        defaultRedisScript.setScriptSource(scriptSource);
        defaultRedisScript.setResultType(Long.class);
    }

    public SemaphoreServiceLua(String semaphoreName, int limit, RedisTemplate redisTemplate) {
        this(semaphoreName, limit, redisTemplate,1000);
    }

    public String acquireSemaphore() {
        String uuid = UUID.randomUUID().toString();
        synchronized (this) { //add this synchronized block to make client acquire the semaphore in order, or else some client firstly get timestamp, but redis server execute its lua script not firstly,
            //it will led to more than the limited clients get sempahore
            Long rank = (Long) redisTemplate.execute(defaultRedisScript, Collections.singletonList(semaphoreName), uuid,
                    timeout, limit,DateTools.getNowTimeStampMilliSeconds());
            if (rank == -1) {
                return null;
            }
            return uuid;
        }
    }
}
