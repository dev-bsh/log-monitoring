package com.log_monitoring.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class LogDataSearchRequest {

    private Long from;
    private Long to;
    private String topicName;
    private List<ConditionDto> condition;
    private Integer pageSize;
    private Integer pageNo;

    public LogDataSearchRequest(Long from, Long to, String topicName, List<ConditionDto> condition, Integer pageSize, Integer pageNo) {
        this.from = from;
        this.to = to;
        this.topicName = topicName;
        this.condition = condition;
        this.pageSize = pageSize;
        this.pageNo = pageNo;
    }
}
