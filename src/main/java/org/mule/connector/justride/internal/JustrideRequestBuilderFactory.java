package org.mule.connector.justride.internal;

import static java.lang.String.valueOf;
import static org.mule.runtime.http.api.HttpConstants.Method.GET;

import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.util.concurrent.CompletableFuture;

public class JustrideRequestBuilderFactory {

    private static final String API_URI = "https://uat.justride.com/api/";

    private HttpClient httpClient;
    private String token;
    private int responseTimeout;

    public JustrideRequestBuilderFactory(HttpClient httpClient, String token) {
        this(httpClient, token, 5000);
    }

    public JustrideRequestBuilderFactory(HttpClient httpClient, String token, int responseTimeout) {
        this.httpClient = httpClient;
        this.token = token;
        this.responseTimeout = responseTimeout;
    }

    public JustrideRequestBuilder newRequest(String justrideMethod) {
        return new JustrideRequestBuilder(justrideMethod);
    }

    public class JustrideRequestBuilder {
        private String uri;
        private MultiMap<String, String> params;

        JustrideRequestBuilder(String justrideMethod) {
            this.params = new MultiMap<>();
            this.params.put("token", token);
            this.uri = API_URI + justrideMethod;
        }

        public JustrideRequestBuilder withOptionalParam(String name, Object value) {
            if (value != null) {
                this.params.put(name, valueOf(value));
            }
            return this;
        }

        public CompletableFuture<HttpResponse> sendAsyncRequest() {
            return httpClient.sendAsync(HttpRequest.builder()
                    .method(GET)
                    .uri(uri)
                    .queryParams(params)
                    .build(), responseTimeout, true, null);
        }

    }
}
