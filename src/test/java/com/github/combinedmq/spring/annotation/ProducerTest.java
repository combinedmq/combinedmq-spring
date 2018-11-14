package com.github.combinedmq.spring.annotation;

import com.github.combinedmq.spring.service.Greeting2Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author xiaoyu
 */
@Slf4j
@Component
public class ProducerTest {
    @Producer(delayMillis = 5000)
    private Greeting2Service greeting2Service;

    public static void main(String[] args) throws Exception {
        final ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext("annotationProducer.xml");
        final Greeting2Service greetingService = ac.getBean(Greeting2Service.class);
        log.info("启动成功");
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        int count = 1;
        final CountDownLatch countDownLatch = new CountDownLatch(count);
        long l = System.currentTimeMillis();
        final AtomicInteger n = new AtomicInteger(1);
        for (int i = 0; i < count; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    long ss = System.currentTimeMillis();
                    greetingService.sayHi("张三");
                    System.out.println(n.getAndIncrement() + "，耗时：" + (System.currentTimeMillis() - ss));
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        System.out.println("时间 " + (System.currentTimeMillis() - l));
    }
}
