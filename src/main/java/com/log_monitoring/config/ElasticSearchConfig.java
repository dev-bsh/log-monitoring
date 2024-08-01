package com.log_monitoring.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchClients;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.log_monitoring.repository.elasticsearch")
public class ElasticSearchConfig {

    @Value("${spring.elasticsearch.uris}")
    private String elasticUri;

    @Bean
    public RestClient restClient() {
        return RestClient.builder(HttpHost.create(elasticUri)).build();
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(RestClient restClient) {
        return ElasticsearchClients.createImperative(restClient);
    }

    @Bean(name = { "elasticsearchOperations", "elasticsearchTemplate" })
    public ElasticsearchOperations elasticsearchOperations(ElasticsearchClient elasticsearchClient) {
        return new ElasticsearchTemplate(elasticsearchClient);
    }

}
