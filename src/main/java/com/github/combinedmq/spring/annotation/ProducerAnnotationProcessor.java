package com.github.combinedmq.spring.annotation;

import com.github.combinedmq.spring.ProducerBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ProducerAnnotationProcessor
 *
 * @author xiaoyu
 * @see Producer
 * @since 1.0.0
 */
public class ProducerAnnotationProcessor extends InstantiationAwareBeanPostProcessorAdapter
        implements MergedBeanDefinitionPostProcessor, PriorityOrdered, BeanFactoryAware {
    private final Log logger = LogFactory.getLog(getClass());

    private final ConcurrentMap<String, InjectionMetadata> injectionMetadataCache =
            new ConcurrentHashMap<String, InjectionMetadata>(256);
    private BeanDefinitionRegistry registry;

    @Override
    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
        if (beanType != null) {
            InjectionMetadata metadata = findProducerMetadata(beanName, beanType, null);
            metadata.checkConfigMembers(beanDefinition);
        }
    }

    @Override
    public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {
        InjectionMetadata metadata = findProducerMetadata(beanName, bean.getClass(), pvs);
        try {
            metadata.inject(bean, beanName, pvs);
        } catch (BeanCreationException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new BeanCreationException(beanName, "Injection of @Producer dependencies failed", ex);
        }
        return pvs;
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    private InjectionMetadata findProducerMetadata(String beanName, Class<?> clazz, PropertyValues pvs) {
        // Fall back to class name as cache key, for backwards compatibility with custom callers.
        String cacheKey = (StringUtils.hasLength(beanName) ? beanName : clazz.getName());
        // Quick check on the concurrent map first, with minimal locking.
        InjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);
        if (InjectionMetadata.needsRefresh(metadata, clazz)) {
            synchronized (this.injectionMetadataCache) {
                metadata = this.injectionMetadataCache.get(cacheKey);
                if (InjectionMetadata.needsRefresh(metadata, clazz)) {
                    if (metadata != null) {
                        metadata.clear(pvs);
                    }
                    try {
                        metadata = buildProducerMetadata(clazz);
                        this.injectionMetadataCache.put(cacheKey, metadata);
                    } catch (NoClassDefFoundError err) {
                        throw new IllegalStateException("Failed to introspect bean class [" + clazz.getName() +
                                "] for producer metadata: could not find class that it depends on", err);
                    }
                }
            }
        }
        return metadata;
    }

    private InjectionMetadata buildProducerMetadata(final Class<?> beanClass) {
        final LinkedList<InjectionMetadata.InjectedElement> elements = new LinkedList<InjectionMetadata.InjectedElement>();
        ReflectionUtils.doWithLocalFields(beanClass, new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                Producer producer = AnnotationUtils.getAnnotation(field, Producer.class);
                if (producer != null) {
                    Queue queue = AnnotationUtils.getAnnotation(field.getType(), Queue.class);
                    if (queue == null) {
                        throw new IllegalStateException("The class marked by the @Producer must have queue annotations");
                    }
                    if (Modifier.isStatic(field.getModifiers())) {
                        logger.warn("@Producer annotation is not supported on static fields: " + field);
                        return;
                    }
                    elements.add(new ProducerFieldElement(field));
                }
            }
        });
        ReflectionUtils.doWithLocalMethods(beanClass, new ReflectionUtils.MethodCallback() {
            @Override
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {

            }
        });
        return new InjectionMetadata(beanClass, elements);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (!(beanFactory instanceof BeanDefinitionRegistry)) {
            throw new IllegalArgumentException(
                    "ProducerAnnotationProcessor requires a BeanDefinitionRegistry: " + beanFactory);
        }
        this.registry = (BeanDefinitionRegistry) beanFactory;
    }

    private class ProducerFieldElement extends InjectionMetadata.InjectedElement {
        private Field field;
        private Class<?> queueInterfaceClass;
        private String producerBeanId;
        private long delayMillis;

        protected ProducerFieldElement(Field field) {
            super(field, null);
            this.field = field;
            queueInterfaceClass = field.getType();
            delayMillis = AnnotationUtils.getAnnotation(field, Producer.class).delayMillis();
            producerBeanId = queueInterfaceClass + "$producer";
        }

        @Override
        protected void inject(Object target, String requestingBeanName, PropertyValues pvs) throws Throwable {
            ReflectionUtils.makeAccessible(field);
            if (!registry.containsBeanDefinition(producerBeanId)) {
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ProducerBean.class);
                builder.addPropertyReference("queueRef", queueInterfaceClass.getName());
                if (delayMillis > 0) {
                    builder.addPropertyValue("delayMillis", delayMillis);
                }
                registry.registerBeanDefinition(producerBeanId, builder.getBeanDefinition());
            }
            field.set(target, ((BeanFactory) registry).getBean(producerBeanId));
        }
    }

}
