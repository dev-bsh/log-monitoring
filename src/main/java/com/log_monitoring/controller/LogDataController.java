package com.log_monitoring.controller;

import com.log_monitoring.dto.LogDataAggSearchRequest;
import com.log_monitoring.dto.LogDataAggSearchResponse;
import com.log_monitoring.dto.LogDataSearchRequest;
import com.log_monitoring.dto.LogDataSearchResponse;
import com.log_monitoring.service.LogDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/log-data")
@RequiredArgsConstructor
public class LogDataController {

    private final LogDataService logDataService;

    // raw data 검색 (시간, 토픽, 조건)
    @PostMapping
    public ResponseEntity<LogDataSearchResponse> searchLogData(@RequestBody LogDataSearchRequest request) {
        return ResponseEntity.ok(logDataService.findAllByCondition(request));
    }

    // 통게 데이터 검색 (시간, 토픽, 조건)
    @PostMapping("/aggregation")
    public ResponseEntity<LogDataAggSearchResponse> searchLogDataAggregation(@RequestBody LogDataAggSearchRequest request) {
        return ResponseEntity.ok(logDataService.findAllAggByCondition(request));
    }

    // redis에 저장된 실시간 통계 전체 조회
    @GetMapping("/realtime/{topicName}")
    public void getAllRealTimeLogDataAggregations(@PathVariable String topicName) {

    }

}
