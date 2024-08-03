package com.log_monitoring.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class LogDataSearchKeywordRequest {

    private Long from;
    private Long to;
    private String topicName;
    private String fieldName;
    private String keyword;

    @Builder
    public LogDataSearchKeywordRequest(Long from, Long to, String topicName, String fieldName, String keyword) {
        this.from = from;
        this.to = to;
        this.topicName = topicName;
        this.fieldName = fieldName;
        this.keyword = keyword;
    }
}
