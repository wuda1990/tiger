package com.quantumn.tiger;

import com.quantumn.tiger.service.SemaphoreService;
import com.quantumn.tiger.service.SemaphoreServiceLua;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.*;

/**
 * @author: huajun.wu
 * @create: 2019-12-06
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class SemaphoreServiceTest {
    @Autowired
    RedisTemplate redisTemplate;

    @Test
    public void test() throws InterruptedException {
        SemaphoreService semaphore = new SemaphoreService("testSemaphore2", 3, redisTemplate, 1000);
        ExecutorService threadPool = Executors.newCachedThreadPool();
        CyclicBarrier barrier = new CyclicBarrier(6);
        CountDownLatch startLatch = new CountDownLatch(1);
        for (int i = 0; i < 5; i++) {
            Task task = new Task(semaphore, startLatch, barrier);
            threadPool.submit(task);
        }
//        Start all threads
        startLatch.countDown();
        try {
            barrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
        log.info("all done");

    }

    static class Task implements Runnable {
        SemaphoreService semaphore;
        CountDownLatch startLatch;
        CyclicBarrier barrier;

        public Task(SemaphoreService semaphore, CountDownLatch startLatch, CyclicBarrier barrier) {
            this.semaphore = semaphore;
            this.startLatch = startLatch;
            this.barrier = barrier;
        }

        @Override
        public void run() {
            String uuid = null;
            try {
                startLatch.await();
                log.info("start acquire...");
                uuid = semaphore.acquireSemaphore();
                if (uuid != null) {
                    log.info("acquire success! uuid={}", uuid);
                } else {
                    log.info("acquire fail!");
                }
//                do some work
                Thread.sleep(1000);
                if (uuid != null)
                    semaphore.releaseSempahore(uuid);
            } catch (Exception e) {
                log.error("acquire exception", e);
            }finally {
                //await the barrier
                try {
                    barrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    @Test
    public void testSub() {
//        String s = "      ";
//        s.substring(0, 6);
        log.info("result:{}","20200".equals(null));
        Assert.assertNotEquals(true,"20200".equals(null));
    }

}
