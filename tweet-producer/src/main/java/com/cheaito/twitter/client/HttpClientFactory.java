package com.cheaito.twitter.client;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.enterprise.inject.Produces;

public class HttpClientFactory {
    @Produces
    public CloseableHttpClient httpClientInstance() {
        return HttpClients.createDefault();
    }
}
