package com.quantumn.tiger.service;

import com.quantumn.tiger.DateTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author: huajun.wu
 * @create: 2019-12-06
 **/
@Slf4j
public class SemaphoreService {
    protected String semaphoreName;
    //semaphore limit
    protected int limit;
    protected long timeout;
    RedisTemplate redisTemplate;

    public SemaphoreService(String semaphoreName, int limit, RedisTemplate redisTemplate, long timeout) {
        this.semaphoreName = semaphoreName;
        this.limit = limit;
        this.redisTemplate = redisTemplate;
        this.timeout = timeout;
    }

    public SemaphoreService(String semaphoreName, int limit, RedisTemplate redisTemplate) {
        //default timeout is 1000 ms
        this(semaphoreName, limit, redisTemplate, 1000);
    }

    /**
     * acquire the semaphore using transaction
     * the current timestamp must be got in the transaction
     * to decide whether the watch condition is broken, the size of result is also need to be considered
     * @return
     */
    public String acquireSemaphore() {
        String uuid = UUID.randomUUID().toString();
        List<Object> result = (List<Object>) redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                List result = null;
                do {
                    operations.watch(semaphoreName);
                    operations.multi();
                    BoundZSetOperations zSet = operations.boundZSetOps(semaphoreName);
                    long now = DateTools.getNowTimeStampMilliSeconds();
                    log.info("uuid:{},timestamp:{}",uuid,now);
                    long stTime = now - timeout;
                    zSet.removeRangeByScore(0, stTime);
                    //add the semaphore uuid to sorted set
                    zSet.add(uuid, now);
                    zSet.rangeWithScores(0, -1);
                    zSet.rank(uuid);//rank when zset changes by other thread
                    result = operations.exec();
                } while (result == null || result.size()==0);
                return result;
            }
        });

        if (CollectionUtils.isEmpty(result) || StringUtils.isEmpty(result.get(2))) {
            return null;
        }
        log.info("remove {} semaphores",(Long)result.get(0));
        Set ansSet = (Set) result.get(2);
        for (Object ans : ansSet) {
            DefaultTypedTuple tuple = (DefaultTypedTuple) ans;
            log.info("value={},sorce={}",tuple.getValue(),tuple.getScore().longValue());
        }
        log.info("rank:{}",(Long)result.get(3));
        if ((Long)result.get(3) >= limit) {
            redisTemplate.opsForZSet().remove(semaphoreName, uuid);
            return null;
        }
        return uuid;
    }

    public void releaseSempahore(String uuid) {
        log.info("release uuid:{}",uuid);
        redisTemplate.opsForZSet().remove(semaphoreName, uuid);
    }

}
