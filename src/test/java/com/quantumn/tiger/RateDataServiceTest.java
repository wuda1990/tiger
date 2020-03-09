package com.quantumn.tiger;

import com.quantumn.tiger.service.RateDataService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class RateDataServiceTest {

    @Autowired
    RateDataService rateDataService;

    private static final String idTxn = "10000029888770";

    @Test
    public void testLock() {
        ExecutorService pool = Executors.newFixedThreadPool(5);
        //latch for the two threads running at the same timeS
        CountDownLatch countDownLatch = new CountDownLatch(1);
        //latch for the main thread to wait for another threads
        CountDownLatch waitingLatch = new CountDownLatch(2);
        for (int i = 0; i < 2; i++) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        countDownLatch.await();
                        rateDataService.getRateData(idTxn);
                        waitingLatch.countDown();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        countDownLatch.countDown();
        log.info("countDown...");
        try {
            waitingLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("main waked up");
        rateDataService.printLock(idTxn);

    }


}
