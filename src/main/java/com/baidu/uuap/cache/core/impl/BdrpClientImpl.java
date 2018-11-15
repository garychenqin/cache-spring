package com.baidu.uuap.cache.core.impl;

import com.baidu.driver4j.bdrp.client.BdrpClient;
import com.baidu.uuap.cache.core.interfaces.ICacheClient;
import com.baidu.uuap.common.util.SerializeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * Bdrp Redis实现类
 * Created by chenshouqin on 2017/9/29.
 */
public class BdrpClientImpl implements ICacheClient<BdrpClient> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private BdrpClient bdrpClient;

    @Override
    public void put(String key, Object value) {
        Assert.notNull(key, "cacheKey cannot empty");
        try {
            if (null != value) {
                bdrpClient.set(SerializeUtil.serialize(key), SerializeUtil.serialize(value));
            }
        } catch (Exception e) {
            logger.error(String.format("put cache to redis failed, key: %s, valueType: %s",
                    key, value.getClass().getName()), e);
        }
    }

    @Override
    public void put(String key, Object value, int expireSeconds) {
        Assert.notNull(key, "cacheKey cannot empty");
        try {
            if (expireSeconds > 0 && null != value) {
                bdrpClient.setex(SerializeUtil.serialize(key), expireSeconds, SerializeUtil.serialize(value));
            } else {
                this.put(key, value);
            }
        } catch (Exception e) {
            logger.error(String.format("put cache to redis failed, key: %s, valueType: %s, expireSeconds: %s",
                    key, value.getClass().getName(), expireSeconds), e);
        }
    }

    @Override
    public void remove(String key) {
        Assert.notNull(key, "cacheKey cannot empty");
        try {
            bdrpClient.del(SerializeUtil.serialize(key));
        } catch (Exception e) {
            logger.error("remove cache from redis failed, key: {}", key, e);
        }
    }

    @Override
    public Object get(String key) {
        Assert.notNull(key, "cacheKey cannot empty");
        try {
            byte[] dataAsBytes = bdrpClient.get(SerializeUtil.serialize(key));
            if (null == dataAsBytes) {
                return null;
            } else {
                return SerializeUtil.deserialize(dataAsBytes);
            }
        } catch (Exception e) {
            logger.error("get cache from redis failed, key: {}", key, e);
            return null;
        }
    }

    @Override
    public void setCacheClient(BdrpClient bdrpClient) {
        this.bdrpClient = bdrpClient;
    }
}
