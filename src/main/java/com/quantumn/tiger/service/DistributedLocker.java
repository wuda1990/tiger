package com.quantumn.tiger.service;

import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;

/**
 * @author: huajun.wu
 * @create: 2019-12-19
 **/
public interface DistributedLocker {
    RLock lock(String lockKey);

    RLock lock(String lockKey, long timeout);

    RLock lock(String lockKey, TimeUnit unit, long timeout);

    boolean tryLock(String lockKey, TimeUnit unit, long waitTime, long leaseTime);

    void unlock(String lockKey);

    void unlock(RLock lock);

}
