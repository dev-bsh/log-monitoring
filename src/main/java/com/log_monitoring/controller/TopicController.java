package com.log_monitoring.controller;

import com.log_monitoring.dto.TopicDto;
import com.log_monitoring.dto.TopicSaveRequest;
import com.log_monitoring.dto.TopicUpdateRequest;
import com.log_monitoring.service.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Tag(name = "Topic", description = "토픽 관련 컨트롤러")
@Slf4j
@RestController
@RequestMapping("/api/topic")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @Operation(summary = "토픽 생성")
    @PostMapping("")
    public ResponseEntity<Long> createTopic(@RequestBody TopicSaveRequest topicSaveRequest) throws ExecutionException, InterruptedException {
        log.info("Create topic {}", topicSaveRequest.toString());
        return ResponseEntity.ok(topicService.createTopic(topicSaveRequest));
    }

    @Operation(summary = "토픽 전체 조회")
    @GetMapping("/list")
    public ResponseEntity<List<TopicDto>> getAllTopics() {
        return ResponseEntity.ok(topicService.findAllTopics());
    }

    @Operation(summary = "토픽 제거")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTopic(@PathVariable Long id) throws ExecutionException, InterruptedException {
        topicService.deleteTopic(id);
        return ResponseEntity.ok("Kafka topic, consumer, DB 메타데이터 제거 성공: " + id);
    }

    @Operation(summary = "토픽 수정")
    @PutMapping("")
    public ResponseEntity<TopicDto> updateTopic(@RequestBody TopicUpdateRequest topicUpdateRequest) {
        return ResponseEntity.ok(topicService.updateTopic(topicUpdateRequest));
    }

}
