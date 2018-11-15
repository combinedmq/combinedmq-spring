package com.github.combinedmq.spring.support;

import com.github.combinedmq.activemq.ActiveMqConfiguration;
import com.github.combinedmq.activemq.ActiveMqConnectionFactory;
import com.github.combinedmq.activemq.ActiveMqConsumer;
import com.github.combinedmq.configuration.Configuration;
import com.github.combinedmq.connection.ConnectionFactory;
import com.github.combinedmq.consumer.Consumer;
import com.github.combinedmq.exception.MqException;
import com.github.combinedmq.kafka.KafkaConfiguration;
import com.github.combinedmq.kafka.KafkaConsumer;
import com.github.combinedmq.rabbitmq.RabbitMqConfiguration;
import com.github.combinedmq.rabbitmq.RabbitMqConnectionFactory;
import com.github.combinedmq.rabbitmq.RabbitMqConsumer;

/**
 * @author xiaoyu
 */
public class ConsumerHolder {
    private static volatile Consumer consumer;

    public static Consumer get(Configuration configuration) {
        synchronized (ConsumerHolder.class) {
            if (consumer == null) {
                ConnectionFactory connectionFactory;
                if (configuration instanceof RabbitMqConfiguration) {
                    connectionFactory = new RabbitMqConnectionFactory(configuration, false);
                    consumer = new RabbitMqConsumer(connectionFactory);
                } else if (configuration instanceof ActiveMqConfiguration) {
                    connectionFactory = new ActiveMqConnectionFactory(configuration, false);
                    consumer = new ActiveMqConsumer(connectionFactory);
                } else if (configuration instanceof KafkaConfiguration) {
                    consumer = new KafkaConsumer(configuration);
                }
            }
        }
        return consumer;
    }

    public static void listen() throws MqException {
        if (consumer != null) {
            consumer.listen();
        }
    }
}
