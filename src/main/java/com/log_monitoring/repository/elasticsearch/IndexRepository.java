package com.log_monitoring.repository.elasticsearch;

public interface IndexRepository {
    void createIndex(String topicName);
    void deleteIndex(String topicName);
}
