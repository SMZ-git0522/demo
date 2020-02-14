package com.example.demo;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
/**
 * @Author: wangzhenze
 * @Description:
 * @Date: Created in 2020/2/14 11:12 上午
 */
public class YouDaoUtil {
    private   static  final   String    appKey ="4b6783e341b3a478"; //xxxx应用 ID
    public YouDaoUtil() {

    }
    public   String  main(String query,String form,String to){
        // 返回结果
        String  result="";
        String salt = String.valueOf(System.currentTimeMillis());
        String sign = md5(appKey + query + salt+ "rskIOnfN7WL0vRZlTaQeA8IUJq9uoqa3");//xxxx授权密钥
        Map params = new HashMap();
        params.put("q", query);  //要翻译的文本
        params.put("from", form); // 源语言
        params.put("to", to); //目标语言
        params.put("sign", sign);
        params.put("salt", salt);
        params.put("appKey", appKey);
        try {
            result=requestForHttp("http://openapi.youdao.com/api", params);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String requestForHttp(String url,Map requestParams) throws Exception{
        String result = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        /**HttpPost*/
        HttpPost httpPost = new HttpPost(url);
        List params = new ArrayList();
        Iterator it = requestParams.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry en = (Map.Entry) it.next();
            String key = (String) en.getKey();
            String value = (String) en.getValue();
            if (value != null) {
                params.add(new BasicNameValuePair(key, value));
            }
        }
        httpPost.setEntity(new UrlEncodedFormEntity(params,"UTF-8"));
        /**HttpResponse*/
        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        try{
            HttpEntity httpEntity = httpResponse.getEntity();
            result = EntityUtils.toString(httpEntity, "utf-8");
            EntityUtils.consume(httpEntity);
        }finally{
            try{
                if(httpResponse!=null){
                    httpResponse.close();
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 生成32位MD5摘要
     * @param string
     * @return
     */
    public static String md5(String string) {
        if(string == null){
            return null;
        }
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F'};

        byte[] btInput = new byte[0];
        try {
            btInput = string.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        /** 获得MD5摘要算法的 MessageDigest 对象 */
        MessageDigest mdInst = null;
        try {
            mdInst = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        /** 使用指定的字节更新摘要 */
        mdInst.update(btInput);
        /** 获得密文 */
        byte[] md = mdInst.digest();
        /** 把密文转换成十六进制的字符串形式 */
        int j = md.length;
        char str[] = new char[j * 2];
        int k = 0;
        for (byte byte0 : md) {
            str[k++] = hexDigits[byte0 >>> 4 & 0xf];
            str[k++] = hexDigits[byte0 & 0xf];
        }
        return new String(str);
    }

    /**
     * 根据api地址和参数生成请求URL
     * @param url
     * @param params
     * @return
     */
    public static String getUrlWithQueryString(String url, Map params) {
        if (params == null) {
            return url;
        }

        StringBuilder builder = new StringBuilder(url);
        if (url.contains("?")) {
            builder.append("&");
        } else {
            builder.append("?");
        }

        int i = 0;
        for (Object key : params.keySet()) {
            String value = (String) params.get(key);
            if (value == null) { // 过滤空的key
                continue;
            }

            if (i != 0) {
                builder.append('&');
            }

            builder.append(key);
            builder.append('=');
            builder.append(encode(value));

            i++;
        }

        return builder.toString();
    }
    /**
     * 进行URL编码
     * @param input
     * @return
     */
    public static String encode(String input) {
        if (input == null) {
            return "";
        }
        try {
            return URLEncoder.encode(input, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return input;
    }

}