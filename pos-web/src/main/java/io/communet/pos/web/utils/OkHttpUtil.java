package io.communet.pos.web.utils;

import com.squareup.okhttp.*;
import io.communet.pos.web.exception.ServiceException;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>function:
 * <p>User: leejohn
 * <p>Date: 16/5/10
 * <p>Version: 1.0
 */
public class OkHttpUtil {
    private static Logger logger = LoggerFactory.getLogger(OkHttpUtil.class);
    private static final OkHttpClient mOkHttpClient = new OkHttpClient();
    private static final OkHttpClient notRedirectClient = new OkHttpClient();//不允许重定向客户端
    private static Integer RETRY_TIMES = 32;

    static {
        mOkHttpClient.setConnectTimeout(2, TimeUnit.SECONDS);

        notRedirectClient.setConnectTimeout(60, TimeUnit.SECONDS);
        notRedirectClient.setFollowRedirects(false);//不允许重定向
//        InetSocketAddress socketAddress = new InetSocketAddress("202.143.162.38",8080);
//        mOkHttpClient.setProxy(new Proxy(Proxy.Type.HTTP, socketAddress));
    }

    /**
     * 该不会开启异步线程。
     *
     * @param request
     * @return
     * @throws IOException
     */
    public static Response execute(Request request) throws IOException {
        return mOkHttpClient.newCall(request).execute();
    }

    /**
     * 开启异步线程访问网络
     *
     * @param request
     * @param responseCallback
     */
    public static void enqueue(Request request, Callback responseCallback) {
        mOkHttpClient.newCall(request).enqueue(responseCallback);
    }

    /**
     * 开启异步线程访问网络, 且不在意返回结果（实现空callback）
     *
     * @param request
     */
    public static void enqueue(Request request) {
        mOkHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(Response arg0) throws IOException {

            }

            @Override
            public void onFailure(Request arg0, IOException arg1) {

            }
        });
    }

    public static String getStringFromServer(Request request) throws IOException {
        logger.info("request's url : " + request.urlString());
        Response response = execute(request);
        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            throw new IOException("Unexpected code " + response);
        }
    }

    public static Response getFromServer(Request request) throws IOException {
        logger.info("request's url : " + request.urlString());
        return execute(request);
    }

    public static String getStringFromServer(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        return getStringFromServer(request);
    }

    public static String postFormServer(String url, Map<String, String> params) throws IOException {
//        logger.info("post request url : "+url);
        FormEncodingBuilder paramsBuilder = new FormEncodingBuilder();
        if (params != null && params.size() > 0)
            for (Map.Entry<String, String> entry : params.entrySet()) {
                paramsBuilder.add(entry.getKey(), entry.getValue());
            }
        RequestBody body = paramsBuilder.build();

        Request request = new Request.Builder().url(url).post(body).build();

        return OkHttpUtil.getStringFromServer(request);
    }

    /**
     * 不允许重定向,多次访问
     */
    public static String getStringNotRedirects(String url, Integer retryTimes) throws IOException {
        if (retryTimes == null) {
            retryTimes = 0;
        }
        retryTimes++;
        Request request = new Request.Builder().url(url).build();
        logger.info("NotRedirects request's url : " + request.urlString());
        Response response = notRedirectClient.newCall(request).execute();
        if (response.isSuccessful()) {
            return response.body().string();
        } else if (response.code() == 302) {
            logger.error("response code : 302 ");
            if (retryTimes >= RETRY_TIMES) {
                logger.error("重定向过多");
                throw new ServiceException("g7.OkHttpUtil.getStringNotRedirects.retryTimes");
            }
            return getStringNotRedirects(url, retryTimes);
        } else {
            logger.error("Unexpected code " + response);
            throw new IOException("Unexpected code " + response);
        }
    }

    /**
     * 不允许重定向,一次访问不成功抛异常
     */
    public static String getStringNotRedirects(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        logger.info("NotRedirects request's url : " + request.urlString());
        Response response = notRedirectClient.newCall(request).execute();
        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            logger.error("Unexpected code " + response);
            throw new IOException("Unexpected code " + response);
        }
    }

    private static final String CHARSET_NAME = "UTF-8";

    /**
     * 这里使用了HttpClinet的API。只是为了方便
     *
     * @param params
     * @return
     */
    public static String formatParams(List<BasicNameValuePair> params) {
        return URLEncodedUtils.format(params, CHARSET_NAME);
    }

    /**
     * 为HttpGet 的 url 方便的添加多个name value 参数。
     *
     * @param url
     * @param params
     * @return
     */
    public static String attachHttpGetParams(String url, List<BasicNameValuePair> params) {
        return url + "?" + formatParams(params);
    }

    /**
     * 为HttpGet 的 url 方便的添加1个name value 参数。
     *
     * @param url
     * @param name
     * @param value
     * @return
     */
    public static String attachHttpGetParam(String url, String name, String value) {
        return url + "?" + name + "=" + value;
    }

    public static String upload(File file, String uploadUrl) throws IOException {
        RequestBody fileBody = RequestBody.create(MediaType.parse("text/xml "), file);
        RequestBody requestBody = new MultipartBuilder().type(MultipartBuilder.FORM)
                .addFormDataPart("file", file.getName(), fileBody)
                .build();
        Request request = new Request.Builder()
                .url(uploadUrl)
                .post(requestBody)
                .build();
        Response response;
        response = mOkHttpClient.newCall(request).execute();
        return response.body().string();
    }

    public static void setSocketProxy(String host, int port) throws IOException {
//        InetAddress inetAddress = InetAddress.getByName("192.168.1.101");
//        InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, 8082);
        //setProxy(new Proxy(Proxy.Type.SOCKS, inetSocketAddress));
  //      mOkHttpClient.setProxy(new Proxy(Proxy.Type.SOCKS,inetAddress));

    }

}
