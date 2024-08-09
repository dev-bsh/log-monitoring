package com.log_monitoring.service;

import com.log_monitoring.config.DynamicKafkaListenerManager;
import com.log_monitoring.dto.TopicSaveRequest;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class KafkaService {

    private final AdminClient adminClient;
    private final DynamicKafkaListenerManager listenerManager;

    public void createTopic(TopicSaveRequest request) throws ExecutionException, InterruptedException {
        NewTopic newTopic = new NewTopic(request.getTopicName(), request.getPartitions(), (short) request.getReplicationFactor());
        adminClient.createTopics(Collections.singleton(newTopic)).all().get();
        listenerManager.addListener(request.getTopicName());
    }

    public void deleteTopic(String topicName) throws ExecutionException, InterruptedException {
        listenerManager.removeListener(topicName);
        adminClient.deleteTopics(Collections.singleton(topicName)).all().get();
    }
}
