package com.github.combinedmq.spring.support;

import com.github.combinedmq.activemq.ActiveMqConfiguration;
import com.github.combinedmq.activemq.ActiveMqConnectionFactory;
import com.github.combinedmq.activemq.ActiveMqProducer;
import com.github.combinedmq.configuration.Configuration;
import com.github.combinedmq.connection.ConnectionFactory;
import com.github.combinedmq.kafka.KafkaConfiguration;
import com.github.combinedmq.kafka.KafkaProducer;
import com.github.combinedmq.producer.Producer;
import com.github.combinedmq.rabbitmq.RabbitMqConfiguration;
import com.github.combinedmq.rabbitmq.RabbitMqConnectionFactory;
import com.github.combinedmq.rabbitmq.RabbitMqProducer;

/**
 * @author xiaoyu
 */
public class ProducerHolder {
    private static volatile Producer producer;

    public static Producer get(Configuration configuration) {
        synchronized (ProducerHolder.class) {
            if (producer == null) {
                ConnectionFactory connectionFactory;
                if (configuration instanceof RabbitMqConfiguration) {
                    connectionFactory = new RabbitMqConnectionFactory(configuration);
                    producer = new RabbitMqProducer(connectionFactory);
                } else if (configuration instanceof ActiveMqConfiguration) {
                    connectionFactory = new ActiveMqConnectionFactory(configuration);
                    producer = new ActiveMqProducer(connectionFactory);
                } else if (configuration instanceof KafkaConfiguration) {
                    producer = new KafkaProducer(configuration);
                }
            }
        }
        return producer;
    }
}
