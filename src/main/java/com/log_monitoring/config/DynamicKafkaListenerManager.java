package com.log_monitoring.config;

import com.log_monitoring.service.MessageProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DynamicKafkaListenerManager  {

    private final MessageProcessingService messageProcessingService;
    private final ConsumerFactory<String, String> consumerFactory;
    private final Map<String, KafkaMessageListenerContainer<String, String>> listenerContainers = new HashMap<>();

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    public void addListener(String topic) {
        String id = getListenerId(topic);
        ContainerProperties containerProps = new ContainerProperties(topic);
        containerProps.setGroupId(groupId);
        containerProps.setMessageListener((MessageListener<String, String>) messageProcessingService::processMessage);

        KafkaMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(consumerFactory, containerProps);
        container.setBeanName(id);
        container.start();

        listenerContainers.put(id, container);
    }

    public void removeListener(String topic) {
        String id = getListenerId(topic);
        KafkaMessageListenerContainer<String, String> container = listenerContainers.remove(id);
        if (container != null) {
            container.stop();
        }
    }

    public String getListenerId(String topic) {
        return "listener_" + topic;
    }

}
