package com.log_monitoring.repository.elasticsearch;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class IndexRepositoryImpl implements IndexRepository {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ObjectMapper objectMapper;

    @Value("${app.index-config-file}")
    private String indexConfigFile;

    @Override
    public void createIndex(String topicName) {
        try {
            IndexCoordinates index = getIndexCoordinates(topicName);
            ClassPathResource resource = new ClassPathResource(indexConfigFile);
            Map<String, Object> file = objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
            Map<String, Object> settings = (Map<String, Object>) file.get("settings");
            Map<String, Object> mappings = (Map<String, Object>) file.get("mappings");
            if (!elasticsearchOperations.indexOps(index).exists()) {
                elasticsearchOperations.indexOps(index).create(settings, Document.from(mappings));
                log.info("Index created: {}", index.getIndexName());
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public void deleteIndex(String topicName) {
        IndexCoordinates index = getIndexCoordinates(topicName);
        elasticsearchOperations.indexOps(index).delete();
        log.info("Index deleted: {}", index.getIndexName());
    }

    private IndexCoordinates getIndexCoordinates(String topicName) {
        String indexName = "index_" + topicName;
        return IndexCoordinates.of(indexName.toLowerCase());
    }
}
