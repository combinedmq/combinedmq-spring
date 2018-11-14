package com.github.combinedmq.spring.support;

import java.util.List;

/**
 * 消息封装类
 *
 * @author xiaoyu
 * @see com.github.combinedmq.message.Message
 * @since 1.0.0
 */
public class MessageWrapper {
    private String methodName;
    private String interfaceName;
    private List<Object> paramsList;

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public List<Object> getParamsList() {
        return paramsList;
    }

    public void setParamsList(List<Object> paramsList) {
        this.paramsList = paramsList;
    }
}
