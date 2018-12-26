package org.mule.connector.justride.internal;

import static org.mule.runtime.http.api.HttpConstants.Method.GET;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.http.api.HttpConstants;
import org.mule.runtime.http.api.HttpService;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpClientConfiguration;
import org.mule.runtime.http.api.client.proxy.ProxyConfig;
import org.mule.runtime.http.api.domain.entity.HttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

/**
 * This class represents an extension connection just as example (there is no real connection with anything here c:).
 */
public final class JustrideConnection {

  private final String accessToken;
  private final String serverUrl;
  
  private HttpClient httpClient;
  private final JustrideRequestBuilderFactory requestBuilderFactory;
  public Auth auth;

  public JustrideConnection(String authToken, String apiUrl, HttpService httpService) {
    this.accessToken = authToken;
    this.serverUrl = apiUrl;
    
    initHttpClient(httpService);
    auth = new Auth(this);
    requestBuilderFactory = new JustrideRequestBuilderFactory(httpClient, authToken);
  }

  public String getAccessToken() {
    return accessToken;
  }
  
  public String getServerUrl() {
	return serverUrl;
  }

  public void invalidate() {
    // do something to invalidate this connection!
  }
  
  public void disconnect() {
      httpClient.stop();
  }
  
  public static <T> void ifPresent(T value, Consumer<T> consumer) {
      if (value != null) {
          consumer.accept(value);
      }
  }
  
  public CompletableFuture<HttpResponse> sendAsyncRequest(String uri, MultiMap<String, String> parameterMap) {
      return sendAsyncRequest(GET, uri, parameterMap, null);
  }
  
  public CompletableFuture<HttpResponse> sendAsyncRequest(HttpConstants.Method method, String uri, MultiMap<String, String> parameterMap, HttpEntity httpEntity) {
      parameterMap.put("token", getAccessToken());

      HttpRequestBuilder builder = HttpRequest.builder();

      if (httpEntity != null) {
          builder.entity(httpEntity);
      }

      return httpClient.sendAsync(builder
              .method(method)
              .uri(uri)
              .queryParams(parameterMap)
              .build(), 5000, true, null);
  }

  public CompletableFuture<HttpResponse> getWebSocketURI() {
      MultiMap<String, String> parameterMap = new MultiMap<>();
      parameterMap.put("token", getAccessToken());

      return httpClient.sendAsync(HttpRequest.builder()
              .method(GET)
              .uri(getServerUrl())
              .queryParams(parameterMap)
              .build(), 5000, true, null);
  }
  
  private void initHttpClient(HttpService httpService) {
      HttpClientConfiguration.Builder builder = new HttpClientConfiguration.Builder();
      
      builder.setName("Justride");
      httpClient = httpService.getClientFactory().create(builder.build());
      httpClient.start();
  }
}
