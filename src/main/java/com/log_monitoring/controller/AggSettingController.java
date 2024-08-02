package com.log_monitoring.controller;

import com.log_monitoring.dto.AggSettingResponse;
import com.log_monitoring.dto.AggSettingSaveRequest;
import com.log_monitoring.service.AggSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/aggregation")
@RequiredArgsConstructor
public class AggSettingController {

    private final AggSettingService aggSettingService;

    @PostMapping
    public ResponseEntity<Long> saveAggregationSetting(@RequestBody AggSettingSaveRequest request) {
        return ResponseEntity.ok(aggSettingService.save(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAggregationSetting(@PathVariable long id) {
        aggSettingService.delete(id);
        return ResponseEntity.ok("aggregation delete complete id =" + id);
    }

    @GetMapping("/list")
    public ResponseEntity<List<AggSettingResponse>> getAllAggregationSettings() {
        return ResponseEntity.ok(aggSettingService.findAll());
    }
}
