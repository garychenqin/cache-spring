package com.baidu.uuap.common.util;

import com.baidu.driver4j.bdrp.client.BdrpClient;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Set;

/**
 * redis分布式唯一锁工具类
 * Created by chenshouqin on 2018/3/23
 */
public class DistributedLockUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistributedLockUtil.class);
    private static Joiner joiner = Joiner.on(",").skipNulls();
    private static final String LUA_SCRIPT = "local key = KEYS[1]\nlocal expire_time = ARGV[1]\n"
            + "local operate_result = redis.call(\"SETNX\", key, 1)\nif operate_result == 1 then\n\t"
            + "redis.call(\"EXPIRE\", key, expire_time)\nend\nreturn operate_result";

    /**
     * 通过redis获取分布式锁
     *
     * @param bdrpClient
     * @param distributedLockKey          分布式锁的Key
     * @param distributedKeyExpireSeconds 分布式锁的key过期时间
     * @param competeHostNames            需要竞争锁的机器
     * @return
     */
    public static boolean getDistributedLock(BdrpClient bdrpClient, String distributedLockKey,
                                             int distributedKeyExpireSeconds, Set<String> competeHostNames) {
        Assert.notNull(bdrpClient, "bdrpClient cannot null");
        Assert.notNull(distributedLockKey, "distributedLockKey cannot null");

        String className = Thread.currentThread().getStackTrace()[2].getClassName();
        String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();

        if (null != competeHostNames && competeHostNames.size() > 0) {
            String localHostName = RemoteIpUtil.getLocalHostname();
            if (!competeHostNames.contains(localHostName)) {
                LOGGER.info("getDistributedLock failed, because localHostName: {} "
                                + "not in competeHostNames: [{}], method execute ignore, call Method: {}.{}",
                        new Object[]{localHostName, joiner.join(competeHostNames), className, methodName});
                return false;
            }
        }

        try {
            Long result = (Long) bdrpClient.eval(LUA_SCRIPT,
                    Collections.singletonList(distributedLockKey),
                    Collections.singletonList(String.valueOf(distributedKeyExpireSeconds)));

            if (result < 1) {
                LOGGER.info("failed get distributedLock, hostName: {}, distributedLockKey: {}, call Method: {}.{}",
                        new Object[]{RemoteIpUtil.getLocalHostname(), distributedLockKey, className, methodName});
                return false;
            } else {
                LOGGER.info("success get distributedLock, hostName: {}, distributedLockKey: {}, call Method: {}.{}",
                        new Object[]{RemoteIpUtil.getLocalHostname(), distributedLockKey, className, methodName});
                return true;
            }
        } catch (Exception e) {
            LOGGER.error(String.format("get distributedLock occur exception, "
                            + "method execute ignore, distributedLockKey: %s, call Method: %s.%s",
                    distributedLockKey, className, methodName), e);
            return false;
        }
    }
}
