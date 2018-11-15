package com.github.combinedmq.spring;

import com.github.combinedmq.activemq.ActiveMqConfiguration;
import com.github.combinedmq.activemq.ActiveMqQueue;
import com.github.combinedmq.configuration.Configuration;
import com.github.combinedmq.consumer.Consumer;
import com.github.combinedmq.exception.MqException;
import com.github.combinedmq.kafka.KafkaConfiguration;
import com.github.combinedmq.kafka.KafkaQueue;
import com.github.combinedmq.message.Message;
import com.github.combinedmq.message.MessageListener;
import com.github.combinedmq.message.Queue;
import com.github.combinedmq.rabbitmq.RabbitMqConfiguration;
import com.github.combinedmq.rabbitmq.RabbitMqQueue;
import com.github.combinedmq.spring.support.ConsumerHolder;
import com.github.combinedmq.spring.support.MessageWrapper;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.ResourceLoader;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xiaoyu
 */
public class ConsumerBean implements InitializingBean, DisposableBean, ResourceLoaderAware, ApplicationListener<ContextRefreshedEvent> {
    private static final Serializer SERIALIZER = new JSONSerializer();
    private Configuration configuration;
    private QueueBean queueRef;
    private Object implementRef;
    private ResourceLoader resourceLoader;

    public QueueBean getQueueRef() {
        return queueRef;
    }

    public void setQueueRef(QueueBean queueRef) {
        this.queueRef = queueRef;
    }

    public Object getImplementRef() {
        return implementRef;
    }

    public void setImplementRef(Object implementRef) {
        this.implementRef = implementRef;
    }


    public void setBeanName(String s) {

    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public void destroy() throws Exception {

    }


    @Override
    public void afterPropertiesSet() throws Exception {
        Queue queue = null;
        if (configuration instanceof RabbitMqConfiguration) {
            queue = new RabbitMqQueue(queueRef.getName());
        } else if (configuration instanceof ActiveMqConfiguration) {
            queue = new ActiveMqQueue(queueRef.getName(), queueRef.getQueueType());
        } else if (configuration instanceof KafkaConfiguration) {
            queue = new KafkaQueue(queueRef.getName(), queueRef.getQueueType());
        }
        Consumer consumer = ConsumerHolder.get(configuration);
        bindListener(consumer, queue, queueRef.getInterface());
    }


    private void bindListener(Consumer consumer, Queue queue, final Class<?> interfaceClass) {
        consumer.bindMessageListener(queue, new MessageListener() {
            @Override
            public void onMessage(Message message) {
                MessageWrapper wrapper = SERIALIZER.deserialize(message.getBytes(), MessageWrapper.class);
                String interfaceName = wrapper.getInterfaceName();
                if (!interfaceClass.getName().equals(interfaceName)) {
                    throw new ClassCastException(interfaceName);
                }
                List<Object> paramsList = wrapper.getParamsList();
                List<Class<?>> classes = new ArrayList<>();
                for (Object o : paramsList) {
                    classes.add(o.getClass());
                }
                try {
                    Method method = interfaceClass.getMethod(wrapper.getMethodName(), classes.toArray(new Class<?>[0]));
                    method.invoke(implementRef, paramsList.toArray());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            ConsumerHolder.listen();
        } catch (MqException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
