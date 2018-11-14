package com.github.combinedmq.spring.service;

import lombok.extern.slf4j.Slf4j;

/**
 * @author xiaoyu
 */
@Slf4j
public class GreetingServiceImpl implements GreetingService {
    @Override
    public void sayHi(String name) {
        log.info("接收到消息：{}", name);
    }
}
