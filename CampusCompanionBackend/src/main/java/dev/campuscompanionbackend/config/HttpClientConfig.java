package dev.campuscompanionbackend.config;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfig {

    @Autowired
    private ZhipuConfig zhipuConfig;

    @Bean(destroyMethod = "close")
    public CloseableHttpClient closeableHttpClient() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(200);
        cm.setDefaultMaxPerRoute(50);

        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(zhipuConfig.getTimeout())
            .setSocketTimeout(zhipuConfig.getTimeout())
            .setConnectionRequestTimeout(zhipuConfig.getTimeout())
                .build();

        return HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(requestConfig)
                .evictIdleConnections(30_000, java.util.concurrent.TimeUnit.MILLISECONDS)
                .build();
    }
}
