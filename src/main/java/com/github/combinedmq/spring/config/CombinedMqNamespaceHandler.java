package com.github.combinedmq.spring.config;

import com.github.combinedmq.spring.ConsumerBean;
import com.github.combinedmq.spring.ProducerBean;
import com.github.combinedmq.spring.QueueBean;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author xiaoyu
 */
public class CombinedMqNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("queue", new CombinedMqBeanDefinitionParser(QueueBean.class));
        registerBeanDefinitionParser("consumer", new CombinedMqBeanDefinitionParser(ConsumerBean.class));
        registerBeanDefinitionParser("producer", new CombinedMqBeanDefinitionParser(ProducerBean.class));
    }
}
