package com.github.combinedmq.spring.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author xiaoyu
 */
@Slf4j
@Service("greetingService")
public class GreetingServiceImpl implements GreetingService {
    @Override
    public void sayHi(String name) {
        log.info("接收到消息：{}", name);
    }
}
