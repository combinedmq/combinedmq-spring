package com.github.combinedmq.spring;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * @author xiaoyu
 */
public class JSONSerializer extends Serializer {
    @Override
    public byte[] serialize(Object obj) {
        return JSON.toJSONBytes(obj, SerializerFeature.WriteMapNullValue);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> objClass) {
        return JSON.parseObject(bytes, objClass);
    }
}
