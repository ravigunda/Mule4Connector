package org.mule.connector.justride.internal;

import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.util.concurrent.CompletableFuture;

public class Auth {
    private JustrideConnection justrideConnection;

    public Auth(JustrideConnection justrideConnection) {
        this.justrideConnection = justrideConnection;
    }

    public CompletableFuture<HttpResponse> test(String apiUrlTest) {
        MultiMap<String, String> parameterMap = new MultiMap<>();
        return justrideConnection.sendAsyncRequest(apiUrlTest, parameterMap);
    }
}
