package com.log_monitoring.controller;

import com.log_monitoring.dto.LogDataSearchKeywordRequest;
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

    @PostMapping
    public ResponseEntity<LogDataSearchResponse> searchLogData(@RequestBody LogDataSearchRequest request) {
        return ResponseEntity.ok(logDataService.findAllBetweenTime(request));
    }

    @PostMapping("/keyword")
    public ResponseEntity<LogDataSearchResponse> searchLogDataByKeyword(@RequestBody LogDataSearchKeywordRequest request) {
        return ResponseEntity.ok(logDataService.findAllByKeywordBetweenTime(request));
    }

    @PostMapping("/aggregation")
    public void searchLogDataAggregation() {

    }

    // redis에 저장된 실시간 통계 전체 조회
    @GetMapping("/realtime/list")
    public void getAllRealTimeLogDataAggregations() {

    }

    // redis에 저장된 실시간 통계 최근 1분치 조회
    @GetMapping("/realtime")
    public void getRealTimeLogDataAggregationData() {

    }

}
