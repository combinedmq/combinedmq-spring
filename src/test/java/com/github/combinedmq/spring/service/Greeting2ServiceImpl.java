package com.github.combinedmq.spring.service;

import com.github.combinedmq.spring.annotation.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author xiaoyu
 */
@Slf4j
@Consumer
@Service("greeting2Service")
public class Greeting2ServiceImpl implements Greeting2Service {
    @Override
    public void sayHi(String name) {
        log.info("接收到消息：{}", name);
    }
}
