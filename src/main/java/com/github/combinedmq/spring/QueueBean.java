package com.github.combinedmq.spring;

import com.github.combinedmq.message.QueueType;
import org.springframework.beans.factory.InitializingBean;

import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.Method;

/**
 * @author xiaoyu
 */
public class QueueBean implements InitializingBean {
    private String name;
    private String type;
    private String interfaceName;

    private Class<?> interfaceClass;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public Class<?> getInterface() {
        return interfaceClass;
    }

    public void setInterface(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        Method[] methods = interfaceClass.getMethods();
        for (Method method : methods) {
            Class<?> returnType = method.getReturnType();
            if (returnType != void.class) {
                throw new WrongMethodTypeException("The queue interface allows only void type.");
            }
        }
    }

    public QueueType getQueueType() {
        QueueType[] values = QueueType.values();
        for (QueueType value : values) {
            if (value.toString().equalsIgnoreCase(type)) {
                return value;
            }
        }
        return null;
    }
}
