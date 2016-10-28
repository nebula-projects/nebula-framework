/*
 * Copyright 2002-2015 the original author or authors.
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
 */

package org.nebula.framework.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.nebula.framework.utils.RequestMapper;

import java.util.List;
import java.util.Map;

import static org.nebula.framework.utils.JsonUtils.convertValue;
import static org.nebula.framework.utils.JsonUtils.toJson;
import static org.nebula.framework.utils.JsonUtils.toObject;

public class NebulaRestClient implements NebulaClient {

  private final static Log logger = LogFactory.getLog(NebulaRestClient.class);

  private final static int DEFAULT_MAX_TOTAL_CONNECTIONS = 200;
  private final static int DEFAULT_CONNECTION_TIMEOUT_SECS = 30;
  private final static int DEFAULT_SOCKET_TIMEOUT_SECS = 30;

  private RequestConfig requestConfig;

  private RequestMapper requestMapper;
  private HttpClient client;
  private HttpHost target;
  private String accessId;
  private String secretKey;

  public NebulaRestClient(String accessId, String secretKey, String hostname,
                          int port) {
    this(accessId, secretKey, hostname, port, "");

  }

  public NebulaRestClient(String accessId, String secretKey, String hostname,
                          int port, String contextPath) {
    this(accessId, secretKey, hostname, port, contextPath, DEFAULT_CONNECTION_TIMEOUT_SECS,
         DEFAULT_SOCKET_TIMEOUT_SECS, DEFAULT_MAX_TOTAL_CONNECTIONS);

  }

  public NebulaRestClient(String accessId, String secretKey, String hostname,
                          int port, String contextPath, int connectionTimeoutInSecs,
                          int socketTimeoutInSecs, int maxTotalConnections) {
    this.accessId = accessId;
    this.secretKey = secretKey;
    this.target = new HttpHost(hostname, port);

    requestConfig = RequestConfig.custom()
        .setConnectionRequestTimeout(connectionTimeoutInSecs * 1000)
        .setConnectTimeout(connectionTimeoutInSecs * 1000)
        .setSocketTimeout(socketTimeoutInSecs * 1000).build();

    requestMapper = new RequestMapper(contextPath);

    PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    cm.setMaxTotal(maxTotalConnections);
    cm.setDefaultMaxPerRoute(maxTotalConnections);
    client = HttpClientBuilder.create().setConnectionManager(cm).build();

  }

  private static String buildQueryString(Object object) throws Exception {

    Map<String, Object> map = convertValue(object, Map.class);

    URIBuilder builder = new URIBuilder();
    for (String key : map.keySet()) {
      Object value = map.get(key);
      if (value == null) {
        continue;
      } else if (value instanceof List) {
        List v = (List) value;
        for (int i = 0; i < v.size(); i++) {
          builder.addParameter(key + "[" + i + "]", v.get(i)
              .toString());
        }
      } else {
        builder.addParameter(key, map.get(key).toString());
      }
    }

    return builder.build().toString();
  }

  private HttpClientContext createPreemptiveBasicAuthentication(String accessId,
                                                                String password) {
    CredentialsProvider credsProvider = new BasicCredentialsProvider();
    credsProvider.setCredentials(AuthScope.ANY,
                                 new UsernamePasswordCredentials(accessId, password));

    AuthCache authCache = new BasicAuthCache();
    authCache.put(target, new BasicScheme());

    // Add AuthCache to the execution context
    HttpClientContext context = HttpClientContext.create();
    context.setCredentialsProvider(credsProvider);
    context.setAuthCache(authCache);

    return context;
  }

  public <T> T post(Request request) throws Exception {

    request.setAccessId(accessId);

    Class requestClass = request.getClass();

    String uri = requestMapper.getUri(requestClass);
    Class responseClass = requestMapper.getResponse(requestClass);

    HttpPost httpPost = new HttpPost(uri);
    httpPost.setHeader(Request.AUTHORIZATION_HEADER, request.toSignedSignature(secretKey));

    httpPost.setConfig(requestConfig);

    String requestBodyJson = toJson(request);

    StringEntity s = new StringEntity(requestBodyJson);
    s.setContentEncoding("UTF-8");
    s.setContentType("application/json");
    httpPost.setEntity(s);

    logger.debug("post request" + requestClass.getSimpleName() + ", to " + uri + " with : "
                 + requestBodyJson);

    return execute(httpPost, responseClass);
  }

  public <T> T get(Request request) throws Exception {

    request.setAccessId(accessId);

    Class requestClass = request.getClass();

    String uri = requestMapper.getUri(requestClass);
    Class responseClass = requestMapper.getResponse(requestClass);

    String uriWithQueryString = uri + buildQueryString(request);
    HttpGet httpGet = new HttpGet(uriWithQueryString);

    httpGet.setConfig(requestConfig);

    httpGet.setHeader(Request.AUTHORIZATION_HEADER, request.toSignedSignature(secretKey));
    httpGet.addHeader("Content-Type", "application/json");

    logger.debug(
        "get request: " + requestClass.getSimpleName() + ", queryString: " + uriWithQueryString);
    return execute(httpGet, responseClass);

  }

  public <T> T delete(Request request) throws Exception {

    request.setAccessId(accessId);

    Class requestClass = request.getClass();

    String uri = requestMapper.getUri(requestClass);
    Class responseClass = requestMapper.getResponse(requestClass);

    String uriWithDeleteString = uri + buildQueryString(request);

    HttpDelete httpDelete = new HttpDelete(uriWithDeleteString);
    httpDelete.setConfig(requestConfig);

    httpDelete.setHeader(Request.AUTHORIZATION_HEADER, request.toSignedSignature(secretKey));
    httpDelete.addHeader("Content-Type", "application/json");

    return execute(httpDelete, responseClass);
  }

  public String getUser() {
    return accessId;
  }

  private <T> T execute(HttpRequestBase httpRequest, Class responseClass)
      throws Exception {

    try {
      HttpResponse
          res =
          client.execute(target, httpRequest,
                         createPreemptiveBasicAuthentication(accessId, secretKey));

      HttpEntity entity = res.getEntity();

      String content = EntityUtils.toString(entity);

      if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

        logger.debug("content=" + content);

        T t = (T) toObject(content, responseClass);

        return t;
      } else if (res.getStatusLine().getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
        logger.error("Illegal argument:" + content);
        throw new IllegalArgumentException(content);
      } else {
        logger.error("Error:" + content);

        // TODO: more specific info
        throw new Exception("error");
      }
    } catch (Exception e) {
      logger.error("Failed to execute http request.", e);
      throw e;
    } finally {
      httpRequest.releaseConnection();
    }
  }

  public RequestMapper getRequestMapper() {
    return requestMapper;
  }

  public void setRequestMapper(RequestMapper requestMapper) {
    this.requestMapper = requestMapper;
  }

}
