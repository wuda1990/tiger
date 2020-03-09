package com.quantumn.tiger;

import com.quantumn.tiger.service.DistributedLocker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * @author: huajun.wu
 * @create: 2019-12-19
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class RedissonLockTest {
    @Autowired
    DistributedLocker distributedLocker;
    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void testRedisson() throws BrokenBarrierException, InterruptedException {
        String key = "redisson_key";
        CyclicBarrier cyclicBarrier = new CyclicBarrier(101);
        for (int i = 0; i < 100; i++) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.err.println("=============线程开启============" + Thread.currentThread().getName());

                         distributedLocker.lock(key,10L); //直接加锁，获取不到锁则一直等待获取锁
                         Thread.sleep(100); //获得锁之后可以进行相应的处理
                         System.err.println("======获得锁后进行相应的操作======"+Thread.
                         currentThread().getName());
                         distributedLocker.unlock(key); //解锁
                         System.err.println("============================="+
                         Thread.currentThread().getName());

//                        boolean isGetLock = distributedLocker.tryLock(key, TimeUnit.SECONDS, 5L, 10L); // 尝试获取锁，等待5秒，自己获得锁后一直不解锁则10秒后自动解锁
//                        if (isGetLock) {
//                            System.out.println("线程:" + Thread.currentThread().getName() + ",获取到了锁");
//                            Thread.sleep(100); // 获得锁之后可以进行相应的处理
//                            System.err.println("======获得锁后进行相应的操作======" + Thread.currentThread().getName());
//                            //distributedLocker.unlock(key);
//                            System.err.println("=============================" + Thread.currentThread().getName());
//                        }
                        cyclicBarrier.await();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            },"myThread-"+i);
            t.start();
        }
        cyclicBarrier.await();
        System.out.println("all done");
    }

    @Test
    public void testSimpleCmd() {
    }
}
