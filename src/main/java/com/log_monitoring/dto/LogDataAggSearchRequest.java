package com.log_monitoring.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class LogDataAggSearchRequest {

    private Long from;
    private Long to;
    private String topicName;
    private List<SearchSetting> searchSettings;

    @Builder
    public LogDataAggSearchRequest(Long from, Long to, String topicName, List<SearchSetting> searchSettings) {
        this.from = from;
        this.to = to;
        this.topicName = topicName;
        this.searchSettings = searchSettings;
    }

    @Getter @Setter
    @NoArgsConstructor
    public static class SearchSetting {
        private String settingName;
        private List<ConditionDto> conditionList;

        @Builder
        public SearchSetting(String settingName, List<ConditionDto> conditionList) {
            this.settingName = settingName;
            this.conditionList = conditionList;
        }
    }
}

