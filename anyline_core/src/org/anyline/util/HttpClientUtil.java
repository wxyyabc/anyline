/* 
 * Copyright 2006-2015 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *          AnyLine以及一切衍生库 不得用于任何与网游相关的系统
 */
package org.anyline.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
/**
 * 基于hpptclient4.x
 * 第一个参数用来保持session连接 
 * null或default:整个应用使用同一个session 
 * createClient(name):为不同域名创建各自的session
 *HttpClientUtil.post(null, "http://www.anyline.org", "UTF-8", "name", "zhang", "age", "20");
 *HttpClientUtil.post(HttpClientUtil.defaultClient(), "http://www.anyline.org", "UTF-8", "name", "zhang", "age", "20");
 *HttpClientUtil.post(HttpClientUtil.createClient("deep"), "http://www.anyline.org", "UTF-8", "name", "zhang", "age", "20");
 *HttpClientUtil.post(null, "http://www.anyline.org", "UTF-8", "name", "zhang", "age", "20");
 *
 */
public class HttpClientUtil {
	private static Logger log = Logger.getLogger(HttpClientUtil.class);
	private static Map<String, CloseableHttpClient> clients = new HashMap<String,CloseableHttpClient>();
    private static PoolingHttpClientConnectionManager connMgr;  
    private static RequestConfig requestConfig;  
    private static final int MAX_TIMEOUT = 7200; 
    
    static {  
        // 设置连接池  
        connMgr = new PoolingHttpClientConnectionManager();  
        // 设置连接池大小  
        connMgr.setMaxTotal(100);  
        connMgr.setDefaultMaxPerRoute(connMgr.getMaxTotal());  
  
        RequestConfig.Builder configBuilder = RequestConfig.custom();  
        // 设置连接超时  
        configBuilder.setConnectTimeout(MAX_TIMEOUT);  
        // 设置读取超时  
        configBuilder.setSocketTimeout(MAX_TIMEOUT);  
        // 设置从连接池获取连接实例的超时  
        configBuilder.setConnectionRequestTimeout(MAX_TIMEOUT);  
        // 在提交请求之前 测试连接是否可用  
        configBuilder.setStaleConnectionCheckEnabled(true);  
        requestConfig = configBuilder.build();  
    } 
	public static Source post(CloseableHttpClient client, String url, String encode, String... params) {
		return post(client, null, url, encode, params);
	}
	public static Source post(CloseableHttpClient client, String url, String encode, HttpEntity... entitys) {
		return post(client, null, url, encode, entitys);
	}

	public static Source post(CloseableHttpClient client, Map<String, String> headers, String url, String encode, String... params) {
		Map<String, String> map = paramToMap(params);
		return post(client, headers, url, encode, map);
	}
	public static Source post(CloseableHttpClient client, Map<String, String> headers, String url, String encode, HttpEntity ... entitys) {
		List<HttpEntity> list = new ArrayList<HttpEntity>();
		if(null != entitys){
			for(HttpEntity entity:entitys){
				list.add(entity);
			}
		}
		return post(client, headers, url, encode, list);
	}

	public static Source post(CloseableHttpClient client, String url, String encode, Map<String, String> params) {
		return post(client, null, url, encode, params);
	}
	public static Source post(CloseableHttpClient client, Map<String, String> headers, String url, String encode, Map<String, String> params) {
		List<HttpEntity> entitys = new ArrayList<HttpEntity>();
		if(null != params && !params.isEmpty()){
			List<NameValuePair> pairs = packNameValuePair(params);
			try {
				HttpEntity entity = new UrlEncodedFormEntity(pairs);
				entitys.add(entity);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		return post(client, headers, url, encode, entitys);
	}


	public static Source post(CloseableHttpClient client, Map<String, String> headers, String url, String encode,  List<HttpEntity> entitys) {
		if(null == client){
			if(url.contains("https://")){
				client = defaultSSLClient();
			}else{
				client =  defaultClient();
			}
		}
		Source result = new Source();
		HttpPost method = new HttpPost(url);
		if(null != entitys){
			for(HttpEntity entity:entitys){
				method.setEntity(entity);
			}
		}
		setHeader(method, headers);
		result = exe(client, method, encode);
		return result;
	}


	public static Source put(CloseableHttpClient client, String url, String encode, String... params) {
		return put(client, null, url, encode, params);
	}
	public static Source put(CloseableHttpClient client, String url, String encode, HttpEntity... entitys) {
		return put(client, null, url, encode, entitys);
	}

	public static Source put(CloseableHttpClient client, Map<String, String> headers, String url, String encode, String... params) {
		Map<String, String> map = paramToMap(params);
		return put(client, headers, url, encode, map);
	}
	public static Source put(CloseableHttpClient client, Map<String, String> headers, String url, String encode, HttpEntity ... entitys) {
		List<HttpEntity> list = new ArrayList<HttpEntity>();
		if(null != entitys){
			for(HttpEntity entity:entitys){
				list.add(entity);
			}
		}
		return put(client, headers, url, encode, list);
	}

	public static Source put(CloseableHttpClient client, String url, String encode, Map<String, String> params) {
		return put(client, null, url, encode, params);
	}
	public static Source put(CloseableHttpClient client, Map<String, String> headers, String url, String encode, Map<String, String> params) {
		List<HttpEntity> entitys = new ArrayList<HttpEntity>();
		if(null != params && !params.isEmpty()){
			List<NameValuePair> pairs = packNameValuePair(params);
			try {
				HttpEntity entity = new UrlEncodedFormEntity(pairs);
				entitys.add(entity);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		return put(client, headers, url, encode, entitys);
	}


	public static Source put(CloseableHttpClient client, Map<String, String> headers, String url, String encode,  List<HttpEntity> entitys) {
		if(null == client){
			if(url.contains("https://")){
				client = defaultSSLClient();
			}else{
				client =  defaultClient();
			}
		}
		Source result = new Source();
		HttpPut method = new HttpPut(url);
		if(null != entitys){
			for(HttpEntity entity:entitys){
				method.setEntity(entity);
			}
		}
		setHeader(method, headers);

		setHeader(method, headers);
		result = exe(client, method, encode);
		return result;
	}
	public static Source get(CloseableHttpClient client, String url, String encode, String... params) {
		return get(client, null, url, encode, params);
	}

	public static Source get(CloseableHttpClient client, Map<String, String> headers, String url, String encode, String... params) {
		Map<String, String> map = paramToMap(params);
		return get(client, headers, url, encode, map);
	}

	public static Source get(CloseableHttpClient client, String url, String encode, Map<String, String> params) {
		return get(client, null, url, encode, params);
	}

	public static Source get(CloseableHttpClient client, Map<String, String> headers, String url, String encode, Map<String, String> params) {
		List<NameValuePair> pairs = packNameValuePair(params);
		return get(client, headers, url, encode, pairs);
	}

	public static Source get(CloseableHttpClient client, String url, String encode, List<NameValuePair> pairs) {
		return get(client, null, url, encode, pairs);
	}

	public static Source get(CloseableHttpClient client, Map<String, String> headers, String url, String encode, List<NameValuePair> pairs) {
		if(null == client){
			if(url.contains("https://")){
				client = defaultSSLClient();
			}else{
				client =  defaultClient();
			}
		}
		Source result = new Source();
		if (null != pairs) {
			String params = "";
			for (NameValuePair pair : pairs) {
				String kv = pair.getName() + "=" + pair.getValue();
				if ("".equals(params)) {
					params += kv;
				} else {
					params += "&" + kv;
				}
			}
			if (url.contains("?")) {
				url += "&" + params;
			} else {
				url += "?" + params;
			}
		}
		HttpGet method = new HttpGet(url);
		setHeader(method, headers);
		result = exe(client, method, encode);
		return result;
	}

	public static Source delete(CloseableHttpClient client, String url, String encode, String... params) {
		return delete(client, null, url, encode, params);
	}

	public static Source delete(CloseableHttpClient client, Map<String, String> headers, String url, String encode, String... params) {
		Map<String, String> map = paramToMap(params);
		return delete(client, headers, url, encode, map);
	}

	public static Source delete(CloseableHttpClient client, String url, String encode, Map<String, String> params) {
		return delete(client, null, url, encode, params);
	}

	public static Source delete(CloseableHttpClient client, Map<String, String> headers, String url, String encode, Map<String, String> params) {
		List<NameValuePair> pairs = packNameValuePair(params);
		return delete(client, headers, url, encode, pairs);
	}

	public static Source delete(CloseableHttpClient client, String url, String encode, List<NameValuePair> pairs) {
		return delete(client, null, url, encode, pairs);
	}

	public static Source delete(CloseableHttpClient client, Map<String, String> headers, String url, String encode, List<NameValuePair> pairs) {
		if(null == client){
			if(url.contains("https://")){
				client = defaultSSLClient();
			}else{
				client =  defaultClient();
			}
		}
		Source result = new Source();
		if (null != pairs) {
			String params = "";
			for (NameValuePair pair : pairs) {
				String kv = pair.getName() + "=" + pair.getValue();
				if ("".equals(params)) {
					params += kv;
				} else {
					params += "&" + kv;
				}
			}
			if (url.contains("?")) {
				url += "&" + params;
			} else {
				url += "?" + params;
			}
		}
		HttpDelete method = new HttpDelete(url);
		setHeader(method, headers);
		result = exe(client, method, encode);
		return result;
	}

	private static Source exe(CloseableHttpClient client, HttpRequestBase method, String encode){
		CloseableHttpResponse response = null;
		Source result = null;
		try {
			response = client.execute(method);
			result = getResult(result,response, encode);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				response.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 设置header
	 * 
	 * @param method
	 * @param headers
	 */
	private static void setHeader(HttpRequestBase method,
			Map<String, String> headers) {
		if (null != headers) {
			Iterator<String> keys = headers.keySet().iterator();
			while (keys.hasNext()) {
				String key = keys.next();
				String value = headers.get(key);
				method.setHeader(key, value);
			}
		}
	}

	public static Source getResult(Source src, CloseableHttpResponse response,
			String encode) {
		if (null == src) {
			src = new Source();
		}
		try {
			Map<String, String> headers = new HashMap<String, String>();
			Header[] all = response.getAllHeaders();
			for (Header header : all) {
				String key = header.getName();
				String value = header.getValue();
				headers.put(key, value);
				if ("Set-Cookie".equalsIgnoreCase(key)) {
					HttpCookie c = new HttpCookie(value);
					src.setCookie(c);
				}
			}
			src.setHeaders(headers);

			HttpEntity entity = response.getEntity();

			if (null != entity) {
				String text = EntityUtils.toString(entity, encode);
				src.setText(text);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return src;
	}
	public static List<NameValuePair> packNameValuePair(Map<String,String> params){
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		if (null != params) {
			Iterator<String> keys = params.keySet().iterator();
			while (keys.hasNext()) {
				String key = keys.next();
				String value = params.get(key);
				pairs.add(new BasicNameValuePair(key, value));
			}
		}
		return pairs;
	}
	private static Map<String, String> paramToMap(String... params) {
		Map<String, String> result = new HashMap<String, String>();
		if (null != params) {
			int size = params.length;
			for (int i = 0; i < size - 1; i += 2) {
				String key = params[i];
				String value = params[i + 1];
				if (null == value) {
					value = "";
				}
				result.put(key.toString(), value);
			}
		}
		return result;
	}

	public static CloseableHttpClient defaultClient(){
		return createClient("default");
	}
	public static CloseableHttpClient createClient(String key){
		CloseableHttpClient client = clients.get(key);
		if(null == client){
			client = HttpClients.createDefault();
			clients.put(key, client);
			if(ConfigTable.isDebug()){
				log.warn("[创建Http Client][KEY:"+key+"]");
			}
		}else{
			if(ConfigTable.isDebug()){
				log.warn("[Http Client缓存][KEY:"+key+"]");
			}
		}
		return client;
	}
	
	public static CloseableHttpClient defaultSSLClient(){
		return ceateSSLClient("default");
	}
	
	public static CloseableHttpClient ceateSSLClient(String key){
		key = "SSL:"+key;
		CloseableHttpClient client = clients.get(key);
		if(null == client){
			client = HttpClients.custom().setSSLSocketFactory(createSSLConnSocketFactory()).setConnectionManager(connMgr).setDefaultRequestConfig(requestConfig).build();
			clients.put(key, client);
			if(ConfigTable.isDebug()){
				log.warn("[创建Https Client][KEY:"+key+"]");
			}
		}else{
			if(ConfigTable.isDebug()){
				log.warn("[Https Client缓存][KEY:"+key+"]");
			}
		}
		 return client;
	}
    private static SSLConnectionSocketFactory createSSLConnSocketFactory() {  
        SSLConnectionSocketFactory sslsf = null;  
        try {  
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {  
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {  
                    return true;  
                }  
            }).build();  
            sslsf = new SSLConnectionSocketFactory(sslContext, new X509HostnameVerifier() {  
  
                @Override  
                public boolean verify(String arg0, SSLSession arg1) {  
                    return true;  
                }  
  
                @Override  
                public void verify(String host, SSLSocket ssl) throws IOException {  
                }  
  
                @Override  
                public void verify(String host, X509Certificate cert) throws SSLException {  
                }  
  
                @Override  
                public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {  
                }  
            });  
        } catch (GeneralSecurityException e) {  
            e.printStackTrace();  
        }  
        return sslsf;  
    }  
}