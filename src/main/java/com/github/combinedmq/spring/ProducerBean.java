package com.github.combinedmq.spring;

import com.github.combinedmq.activemq.ActiveMqConfiguration;
import com.github.combinedmq.activemq.ActiveMqMessage;
import com.github.combinedmq.activemq.ActiveMqQueue;
import com.github.combinedmq.configuration.Configuration;
import com.github.combinedmq.kafka.KafkaConfiguration;
import com.github.combinedmq.kafka.KafkaMessage;
import com.github.combinedmq.kafka.KafkaQueue;
import com.github.combinedmq.message.Message;
import com.github.combinedmq.message.Queue;
import com.github.combinedmq.rabbitmq.RabbitMqConfiguration;
import com.github.combinedmq.rabbitmq.RabbitMqMessage;
import com.github.combinedmq.rabbitmq.RabbitMqQueue;
import com.github.combinedmq.spring.proxy.JavassistProxyFactory;
import com.github.combinedmq.spring.proxy.ProxyFactory;
import com.github.combinedmq.spring.support.MessageWrapper;
import com.github.combinedmq.spring.support.ProducerHolder;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ClassUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author xiaoyu
 */
public class ProducerBean implements FactoryBean, InitializingBean {
    private static final Serializer SERIALIZER = new JSONSerializer();
    private ProxyFactory proxyFactory;
    private Configuration configuration;
    private QueueBean queueRef;
    private Long delayMillis;
    private transient volatile Object ref;

    public ProducerBean() {
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public QueueBean getQueueRef() {
        return queueRef;
    }

    public void setQueueRef(QueueBean queueRef) {
        this.queueRef = queueRef;
    }

    public void setDelayMillis(Long delayMillis) {
        this.delayMillis = delayMillis;
    }

    public Long getDelayMillis() {
        return delayMillis;
    }

    @Override
    public Object getObject() throws Exception {
        return get();
    }

    public synchronized Object get() {
        if (ref == null) {
            ref = proxyFactory.getProxy(queueRef.getInterface(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if ("toString".equals(method.getName())) {
                        return (getClass().isInterface() ? "interface " : (getClass().isPrimitive() ? "" : "class "))
                                + getClass().getName();
                    }
                    if (ClassUtils.hasMethod(queueRef.getInterface(), method.getName(), method.getParameterTypes())) {
                        MessageWrapper wrapper = new MessageWrapper();
                        wrapper.setMethodName(method.getName());
                        wrapper.setInterfaceName(queueRef.getInterface().getName());
                        wrapper.setParamsList(Arrays.asList(args));
                        Queue queue = null;
                        Message message = null;
                        if (configuration instanceof RabbitMqConfiguration) {
                            queue = new RabbitMqQueue(queueRef.getName());
                            message = new RabbitMqMessage(SERIALIZER.serialize(wrapper), delayMillis);
                        } else if (configuration instanceof ActiveMqConfiguration) {
                            queue = new ActiveMqQueue(queueRef.getName(), queueRef.getQueueType());
                            message = new ActiveMqMessage(SERIALIZER.serialize(wrapper), delayMillis);
                        } else if (configuration instanceof KafkaConfiguration) {
                            queue = new KafkaQueue(queueRef.getName(), queueRef.getQueueType());
                            message = new KafkaMessage(SERIALIZER.serialize(wrapper));
                        }
                        ProducerHolder.get(configuration).send(queue, message);
                    }
                    return null;
                }
            });
        }
        return ref;
    }

    @Override
    public Class<?> getObjectType() {
        if (queueRef == null) {
            return null;
        }
        return queueRef.getInterface();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        proxyFactory = new JavassistProxyFactory();
    }

}
