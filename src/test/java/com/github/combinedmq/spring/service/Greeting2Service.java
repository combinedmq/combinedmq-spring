package com.github.combinedmq.spring.service;

import com.github.combinedmq.spring.annotation.Queue;

/**
 * @author xiaoyu
 */
@Queue(name = "x.y.z")
public interface Greeting2Service {
    void sayHi(String name);
}
