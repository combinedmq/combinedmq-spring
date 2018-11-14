package com.github.combinedmq.spring.annotation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Scanner;

/**
 * @author xiaoyu
 */
@Slf4j
@EnableCombinedMq("com.github")
@Component
public class ConsumerTest {
    public static void main(String[] args) throws Exception {
        new ClassPathXmlApplicationContext("annotationConsumer.xml");
        log.info("启动成功");
        new Scanner(System.in).nextLine();
    }
}
