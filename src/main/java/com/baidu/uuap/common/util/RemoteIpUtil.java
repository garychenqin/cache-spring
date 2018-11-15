package com.baidu.uuap.common.util;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by chenshouqin on 2016/12/27.
 */
public class RemoteIpUtil {

    private static Logger logger = LoggerFactory.getLogger(RemoteIpUtil.class);

    private static final Pattern IP_PATTERN = Pattern.compile(
            "((25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))");

    // 通过代理获取真实IP的header, 必须保证顺序
    private static List<String> remoteIpHeaderFlags = Lists.newArrayList(
            "Clientip", "x-forwarded-for", "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP");

    private static String LOCAL_HOST_NAME = null;

    /**
     * get Real RemoteIp
     *
     * @param request
     * @return
     */
    public static String getRemoteIpByRequest(HttpServletRequest request) {
        for (String remoteIpHeaderFlag : remoteIpHeaderFlags) {
            String requestIp = request.getHeader(remoteIpHeaderFlag);
            if (!Strings.isNullOrEmpty(requestIp) && IP_PATTERN.matcher(requestIp).matches()) {
                return requestIp;
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * 获取本地的HostName
     *
     * @return
     */
    public static String getLocalHostname() {
        if (null != LOCAL_HOST_NAME) {
            return LOCAL_HOST_NAME;
        } else {
            synchronized (RemoteIpUtil.class) {
                if (null != LOCAL_HOST_NAME) {
                    return LOCAL_HOST_NAME;
                }
                try {
                    LOCAL_HOST_NAME = InetAddress.getLocalHost().getHostName();
                } catch (UnknownHostException e) {
                    logger.error("get local machine hostname failed.", e);
                }
            }
        }
        return LOCAL_HOST_NAME;
    }
}
