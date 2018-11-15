package com.github.combinedmq.spring;

import com.github.combinedmq.activemq.ActiveMqConfigurationFactory;
import com.github.combinedmq.configuration.Configuration;
import com.github.combinedmq.kafka.KafkaConfigurationFactory;
import com.github.combinedmq.rabbitmq.RabbitMqConfigurationFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author xiaoyu
 */
public class ConfigBean implements FactoryBean {
    public static final String CONFIG_BEAN_NAME = "combinedMqConfiguration";
    private Configuration configuration;

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public Object getObject() throws Exception {
        if (configuration == null) {
            configuration = checkConfig();
        }
        return configuration;
    }

    @Override
    public Class<?> getObjectType() {
        if (configuration == null) {
            return null;
        }
        return configuration.getClass();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    private Configuration checkConfig() {
        Configuration activemqConfig = new ActiveMqConfigurationFactory().getConfiguration();
        Configuration rabbitmqConfig = new RabbitMqConfigurationFactory().getConfiguration();
        Configuration kafkaConfig = new KafkaConfigurationFactory().getConfiguration();
        if (activemqConfig == null && rabbitmqConfig == null && kafkaConfig == null) {
            throw new IllegalStateException("CombinedMq配置不存在");
        }
        if ((rabbitmqConfig != null && activemqConfig != null)
                || (rabbitmqConfig != null && kafkaConfig != null)
                || (activemqConfig != null && kafkaConfig != null)) {
            throw new IllegalStateException("配置重复，rabbitmq、activemq、kafka只能存在一种");
        }
        if (rabbitmqConfig != null) {
            return rabbitmqConfig;
        } else if (activemqConfig != null) {
            return activemqConfig;
        } else if (kafkaConfig != null) {
            return kafkaConfig;
        }
        throw new NullPointerException();
    }
}