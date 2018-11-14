package com.github.combinedmq.spring;

/**
 * @author xiaoyu
 * @see JSONSerializer
 * @see JavaSerializer
 */
public abstract class Serializer {
    /**
     * serialize
     *
     * @param obj
     * @return
     */
    public abstract byte[] serialize(Object obj);

    /**
     * deserialize
     *
     * @param bytes
     * @param objClass
     * @param <T>
     * @return
     */
    public abstract <T> T deserialize(byte[] bytes, Class<T> objClass);
}

