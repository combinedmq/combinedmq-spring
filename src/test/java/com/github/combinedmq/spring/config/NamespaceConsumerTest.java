package com.github.combinedmq.spring.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Scanner;

/**
 * @author xiaoyu
 */
@Slf4j
public class NamespaceConsumerTest {
    public static void main(String[] args) throws Exception {
        new ClassPathXmlApplicationContext("applicationContextConsumer.xml");
        log.info("启动成功");
        new Scanner(System.in).nextLine();
    }
}
