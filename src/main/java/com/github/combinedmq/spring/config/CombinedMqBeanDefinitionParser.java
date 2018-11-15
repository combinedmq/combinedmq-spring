package com.github.combinedmq.spring.config;

import com.github.combinedmq.spring.ConfigBean;
import com.github.combinedmq.spring.ConsumerBean;
import com.github.combinedmq.spring.ProducerBean;
import com.github.combinedmq.spring.QueueBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * @author xiaoyu
 */
public class CombinedMqBeanDefinitionParser implements BeanDefinitionParser {
    private final Class<?> beanClass;


    public CombinedMqBeanDefinitionParser(Class<?> beanClass) {
        this.beanClass = beanClass;

    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClass(beanClass);
        beanDefinition.setLazyInit(false);

        String id = element.getAttribute("id");
        if (QueueBean.class == beanClass) {
            if (StringUtils.isEmpty(id)) {
                throw new IllegalStateException("The queue id be defined");
            }
            String interfaceName = element.getAttribute("interface");
            try {
                Class<?> interfaceClass = ClassUtils.forName(interfaceName, getClass().getClassLoader());
                beanDefinition.getPropertyValues().add("interface", interfaceClass);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
            String name = element.getAttribute("name");
            if (StringUtils.isEmpty(name)) {
                name = interfaceName;
            }
            beanDefinition.getPropertyValues().add("name", name);
            String type = element.getAttribute("type");
            beanDefinition.getPropertyValues().add("type", type);

            if (!parserContext.getRegistry().containsBeanDefinition(ConfigBean.CONFIG_BEAN_NAME)) {
                RootBeanDefinition configBeanDefinition = new RootBeanDefinition();
                configBeanDefinition.setBeanClass(ConfigBean.class);
                configBeanDefinition.setLazyInit(false);
                parserContext.getRegistry().registerBeanDefinition(ConfigBean.CONFIG_BEAN_NAME, configBeanDefinition);
            }
        } else if (ConsumerBean.class == beanClass) {
            String queueRef = element.getAttribute("queue-ref");
            String implementRef = element.getAttribute("implement-ref");
            beanDefinition.getPropertyValues().add("queueRef", new RuntimeBeanReference(queueRef));
            beanDefinition.getPropertyValues().add("implementRef", new RuntimeBeanReference(implementRef));
            beanDefinition.getPropertyValues().add("configuration", new RuntimeBeanReference(ConfigBean.CONFIG_BEAN_NAME));
            if (StringUtils.isEmpty(id)) {
                id = "consumerListener";
                int counter = 2;
                while (parserContext.getRegistry().containsBeanDefinition(id)) {
                    id = "consumerListener" + (counter++);
                }
            }
        } else if (ProducerBean.class == beanClass) {
            if (StringUtils.isEmpty(id)) {
                throw new IllegalStateException("The producer id be defined");
            }
            String queueRef = element.getAttribute("queue-ref");
            String delayMillis = element.getAttribute("delay-millis");
            beanDefinition.getPropertyValues().add("queueRef", new RuntimeBeanReference(queueRef));
            beanDefinition.getPropertyValues().add("configuration", new RuntimeBeanReference(ConfigBean.CONFIG_BEAN_NAME));
            if (!StringUtils.isEmpty(delayMillis)) {
                beanDefinition.getPropertyValues().add("delayMillis", Long.parseLong(delayMillis));
            }
        }

        parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);
        return beanDefinition;
    }
}
