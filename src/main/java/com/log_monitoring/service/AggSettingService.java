package com.log_monitoring.service;

import com.log_monitoring.dto.AggSettingResponse;
import com.log_monitoring.dto.AggSettingSaveRequest;
import com.log_monitoring.model.AggSetting;
import com.log_monitoring.repository.AggSettingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AggSettingService {

    private final AggSettingRepository aggSettingRepository;

    @Transactional
    public Long save(AggSettingSaveRequest request) {
        AggSetting aggSetting = request.toEntity();
        return aggSettingRepository.save(aggSetting).getId();
    }

    @Transactional
    public void delete(long id) {
        AggSetting aggSetting = aggSettingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AggregationSetting id : " + id + " not found"));
        aggSettingRepository.delete(aggSetting);
    }

    public List<AggSettingResponse> findAll() {
        return aggSettingRepository.findAll().stream()
                .map(AggSettingResponse::fromEntity)
                .toList();
    }
}
