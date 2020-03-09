package com.quantumn.tiger.service;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author: huajun.wu
 * @create: 2019-12-19
 **/
@Slf4j
//@Service
public class RedissonDistributedLocker implements DistributedLocker {
    @Autowired
    private RedissonClient redissonClient;
    @Override
    public RLock lock(String lockKey) {
        RLock rLock = redissonClient.getLock(lockKey);
        rLock.lock();
        return rLock;
    }

    @Override
    public RLock lock(String lockKey, long timeout) {
        RLock rLock = redissonClient.getLock(lockKey);
        rLock.lock(timeout,TimeUnit.SECONDS);
        return rLock;
    }

    @Override
    public RLock lock(String lockKey, TimeUnit unit, long timeout) {
        RLock rLock = redissonClient.getLock(lockKey);
        rLock.lock(timeout,unit);
        return rLock;
    }

    @Override
    public boolean tryLock(String lockKey, TimeUnit unit, long waitTime, long leaseTime) {
    RLock rLock = redissonClient.getLock(lockKey);
        try {
        return rLock.tryLock(waitTime, leaseTime, unit);
    } catch (InterruptedException e) {
        log.error("try lock {} error!",lockKey,e);
    }
        return false;
}

    @Override
    public void unlock(String lockKey) {
        redissonClient.getLock(lockKey).unlock();
    }

    @Override
    public void unlock(RLock lock) {
        lock.unlock();
    }
}
