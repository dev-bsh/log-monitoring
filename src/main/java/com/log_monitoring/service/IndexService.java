package com.log_monitoring.service;

import com.log_monitoring.repository.elasticsearch.IndexRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IndexService {

    private final IndexRepository indexRepository;

    public void createIndex(String topicName) {
        indexRepository.createIndex(topicName);
    }

    public void deleteIndex(String topicName) {
        indexRepository.deleteIndex(topicName);
    }
}
