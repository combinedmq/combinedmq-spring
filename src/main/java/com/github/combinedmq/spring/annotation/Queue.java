package com.github.combinedmq.spring.annotation;

import com.github.combinedmq.message.QueueType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Only allow for interface types, where an interface as a queue ,and should have different queue names.<p>
 * All methods of this queue interface cannot have a return value,it can only be void type.
 *
 * @author xiaoyu
 * @see Producer
 * @see Consumer
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Queue {
    /**
     * Using {@link Producer} annotations, the interface to call the queue annotation sends a message to the queue specifying the same name.
     *
     * @return the name of the queue
     */
    String name();

    /**
     * @return the type of the queue
     */
    QueueType type() default QueueType.POINT_TO_POINT;
}
