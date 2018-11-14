package com.github.combinedmq.spring.annotation;

import com.github.combinedmq.spring.QueueBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;

/**
 * @author xiaoyu
 */
public class CombinedMqScanRegistrar implements ImportBeanDefinitionRegistrar {
    private static final String CONSUMER_BEAN = ConsumerAnnotationProcessor.class.getName();
    private static final String PRODUCER_BEAN = ProducerAnnotationProcessor.class.getName();

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Set<String> packagesToScan = getPackagesToScan(importingClassMetadata);
        for (String s : packagesToScan) {
            registerQueueBeanDefinitions(registry, s);
        }
        registerConsumerListenerProcessor(registry);
        registerProducerProcessor(registry);
    }

    protected void registerQueueBeanDefinitions(BeanDefinitionRegistry registry, String basePackage) {
        CombinedMqClassPathBeanDefinitionScanner scanner = new CombinedMqClassPathBeanDefinitionScanner(registry);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Queue.class));
        scanner.setBeanNameGenerator(new DefaultBeanNameGenerator());
        Set<BeanDefinitionHolder> beanDefinitionHolders = scanner.doScan(basePackage);
        if (!CollectionUtils.isEmpty(beanDefinitionHolders)) {
            for (BeanDefinitionHolder beanDefinitionHolder : beanDefinitionHolders) {
                registry.removeBeanDefinition(beanDefinitionHolder.getBeanName());
                String beanClassName = beanDefinitionHolder.getBeanDefinition().getBeanClassName();
                System.out.println(beanDefinitionHolder.getBeanName());
                Class<?> interfaceClass = null;
                try {
                    interfaceClass = ClassUtils.forName(beanClassName, this.getClass().getClassLoader());
                    if (!Modifier.isInterface(interfaceClass.getModifiers())) {
                        throw new IllegalStateException("Annotation @Queue must be used in interface type");
                    }
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(e);
                }
                Queue queue = AnnotationUtils.findAnnotation(interfaceClass, Queue.class);
                BeanDefinitionBuilder builder = rootBeanDefinition(QueueBean.class);
                builder.addPropertyValue("name", queue.name())
                        .addPropertyValue("type", queue.type().toString())
                        .addPropertyValue("interface", interfaceClass);
                BeanDefinitionHolder holder = new BeanDefinitionHolder(builder.getBeanDefinition(), beanClassName);
                BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
            }
        }
    }

    protected void registerConsumerListenerProcessor(BeanDefinitionRegistry registry) {
        if (registry.containsBeanDefinition(CONSUMER_BEAN)) {
            return;
        }
        BeanDefinitionBuilder builder = rootBeanDefinition(ConsumerAnnotationProcessor.class);
        builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        BeanDefinitionHolder holder = new BeanDefinitionHolder(builder.getBeanDefinition(), CONSUMER_BEAN);
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
    }

    protected void registerProducerProcessor(BeanDefinitionRegistry registry) {
        if (registry.containsBeanDefinition(PRODUCER_BEAN)) {
            return;
        }
        BeanDefinitionBuilder builder = rootBeanDefinition(ProducerAnnotationProcessor.class);
        builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        BeanDefinitionHolder holder = new BeanDefinitionHolder(builder.getBeanDefinition(), PRODUCER_BEAN);
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
    }

    private Set<String> getPackagesToScan(AnnotationMetadata metadata) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(
                metadata.getAnnotationAttributes(EnableCombinedMq.class.getName()));
        String[] basePackages = attributes.getStringArray("basePackages");
        Class<?>[] basePackageClasses = attributes.getClassArray("basePackageClasses");
        String[] value = attributes.getStringArray("value");
        // Appends value array attributes
        Set<String> packagesToScan = new LinkedHashSet<String>(Arrays.asList(value));
        packagesToScan.addAll(Arrays.asList(basePackages));
        for (Class<?> basePackageClass : basePackageClasses) {
            packagesToScan.add(ClassUtils.getPackageName(basePackageClass));
        }
        if (packagesToScan.isEmpty()) {
            return Collections.singleton(ClassUtils.getPackageName(metadata.getClassName()));
        }
        return packagesToScan;
    }
}
