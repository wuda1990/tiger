package com.quantumn.tiger.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RateDataService {

    @Autowired
    LockService lockService;

    private static final String key_prefix = "lock_";

    public void getRateData(String idTxn) {
        String requestId = "";
        try {
            requestId = UUID.randomUUID().toString();
            log.info("uuid:"+requestId);
            if (lockService.tryLock(key_prefix + idTxn, 2, TimeUnit.HOURS, requestId)) {
                    log.info("handlering txn:{}", idTxn);
                    Thread.sleep(3000);
            } else {
                log.info("lock has been applied");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                lockService.releaseLock(key_prefix + idTxn, requestId);
            } catch (Exception e) {
                log.error("release error!",e);
            }
        }
    }

    public void printLock(String key) {
        lockService.printLock(key_prefix+key);
    }

}
