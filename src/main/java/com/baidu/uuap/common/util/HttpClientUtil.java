package com.baidu.uuap.common.util;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * Created by chenshouqin on 2017/12/11
 */
public class HttpClientUtil {

    private RequestConfig requestConfig;
    private PoolingHttpClientConnectionManager connectionManager;
    private JsonMapper jsonMapper = JsonMapper.nonDefaultMapper();

    public HttpClientUtil() throws NoSuchAlgorithmException {
        this(3000, 3000, 3000, 100, 20);
    }

    public HttpClientUtil(int conReqTimeoutMs, int conTimeoutMs, int soTimeoutMs, int conPoolMaxSize, int maxPerRoute)
            throws NoSuchAlgorithmException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        requestConfig = RequestConfig.custom()
                .setConnectTimeout(conTimeoutMs)
                .setSocketTimeout(soTimeoutMs)
                .setConnectionRequestTimeout(conReqTimeoutMs)
                .build();

        LayeredConnectionSocketFactory sslSocketFactory =
                new SSLConnectionSocketFactory(SSLContext.getDefault());
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslSocketFactory)
                .register("http", new PlainConnectionSocketFactory())
                .build();
        connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        connectionManager.setMaxTotal(conPoolMaxSize);
        connectionManager.setDefaultMaxPerRoute(maxPerRoute);
    }

    private static HttpClientUtil INSTANCE;

    public static HttpClientUtil defaultInstance() {
        if (null != INSTANCE) {
            return INSTANCE;
        } else {
            synchronized (HttpClientUtil.class) {
                if (null != INSTANCE) {
                    return INSTANCE;
                }
                try {
                    INSTANCE = new HttpClientUtil();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                return INSTANCE;
            }
        }
    }

    /**
     * 从连接池中获取连接
     *
     * @return
     */
    private CloseableHttpClient getHttpClient() {
        CloseableHttpClient httpClient = HttpClients
                .custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
        return httpClient;
    }

    private String httpRequest(HttpUriRequest httpRequest, String charset) throws Exception {
        CloseableHttpResponse response = null;
        String responseContent = null;
        try {
            CloseableHttpClient httpClient = getHttpClient();
            response = httpClient.execute(httpRequest);
            int responseCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String resp = null;
            if (null != entity) {
                resp = EntityUtils.toString(entity, charset);
            }
            if (200 == responseCode) {
                responseContent = resp;
            } else {
                throw new HttpResponseException(responseCode, resp);
            }
        } finally {
            if (null != response) {
                response.close();
            }
        }
        return responseContent;
    }


    /**
     * postJson数据
     *
     * @param url
     * @param jsonMap
     * @return
     * @throws Exception
     */
    public String postJson(String url, Map<String, Object> jsonMap) throws Exception {
        HttpPost post = new HttpPost(url);
        StringEntity entity = new StringEntity(jsonMapper.toJson(jsonMap), Charset.forName("UTF-8"));
        entity.setContentType("application/json");
        post.setEntity(entity);
        return httpRequest(post, "UTF-8");
    }

    /**
     * 发送get请求
     *
     * @param url
     * @param paramsMap
     * @return
     * @throws Exception
     */
    public String doGet(String url, Map<String, String> paramsMap) throws Exception {
        String paramsStr = null;
        if (null != paramsMap && paramsMap.size() > 0) {
            List<NameValuePair> params = Lists.newArrayList();
            for (String key : paramsMap.keySet()) {
                params.add(new BasicNameValuePair(key,
                        Optional.fromNullable(paramsMap.get(key)).or(StringUtils.EMPTY)));
            }
            paramsStr = EntityUtils.toString(new UrlEncodedFormEntity(params, Consts.UTF_8));
        }

        if (!Strings.isNullOrEmpty(paramsStr)) {
            url = new StringBuilder(url).append("?").append(paramsStr).toString();
        }
        HttpGet get = new HttpGet(url);
        return httpRequest(get, "UTF-8");
    }


    /**
     * 发送http post请求
     *
     * @param url
     * @param paramsMap
     * @return
     * @throws Exception
     */
    public String doPost(String url, Map<String, String> paramsMap) throws Exception {
        UrlEncodedFormEntity entity = null;
        if (null != paramsMap && paramsMap.size() > 0) {
            List<NameValuePair> params = Lists.newArrayList();
            for (String key : paramsMap.keySet()) {
                params.add(new BasicNameValuePair(key,
                        Optional.fromNullable(paramsMap.get(key)).or(StringUtils.EMPTY)));
            }
            entity = new UrlEncodedFormEntity(params, Consts.UTF_8);
        }

        HttpPost post = new HttpPost(url);
        if (null != entity) {
            post.setEntity(entity);
        }
        return httpRequest(post, "UTF-8");
    }


}
