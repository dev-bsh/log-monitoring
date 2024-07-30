package com.log_monitoring.controller;

import com.log_monitoring.dto.TopicDto;
import com.log_monitoring.dto.TopicSaveRequest;
import com.log_monitoring.dto.TopicUpdateRequest;
import com.log_monitoring.service.TopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping("/api/topic")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @PostMapping("")
    public ResponseEntity<Long> createTopic(@RequestBody TopicSaveRequest topicSaveRequest) throws ExecutionException, InterruptedException {
        log.info("Create topic {}", topicSaveRequest.toString());
        return ResponseEntity.ok(topicService.createTopic(topicSaveRequest));
    }

    @GetMapping("/list")
    public ResponseEntity<List<TopicDto>> getAllTopics() {
        return ResponseEntity.ok(topicService.findAllTopics());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTopic(@PathVariable Long id) throws ExecutionException, InterruptedException {
        topicService.deleteTopic(id);
        return ResponseEntity.ok("Kafka topic, consumer, DB 메타데이터 제거 성공: " + id);
    }

    @PutMapping("")
    public ResponseEntity<TopicDto> updateTopic(@RequestBody TopicUpdateRequest topicUpdateRequest) {
        return ResponseEntity.ok(topicService.updateTopic(topicUpdateRequest));
    }

}
