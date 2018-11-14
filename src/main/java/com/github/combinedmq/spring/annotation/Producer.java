package com.github.combinedmq.spring.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a field as a message sender that must be an interface annotated by {@link Queue}.
 *
 * @author xiaoyu
 * @see Queue
 * @see ConsumerListener
 * @since 1.0.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Producer {
    long delayMillis() default 0;
}
