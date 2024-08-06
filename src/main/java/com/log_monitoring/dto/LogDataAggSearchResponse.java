package com.log_monitoring.dto;


import co.elastic.clients.elasticsearch._types.aggregations.DateHistogramBucket;
import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class LogDataAggSearchResponse {

    private String topicName;
    private List<AggResult> result;

    @Builder
    public LogDataAggSearchResponse(String topicName, List<AggResult> result) {
        this.topicName = topicName;
        this.result = result;
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AggResult {
        private String settingName;
        private List<Data> data;

        @Getter @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class Data {
            private Long timestamp;
            private Long count;

            public static Data fromBucket(DateHistogramBucket bucket) {
                return Data.builder()
                        .timestamp(bucket.key())
                        .count(bucket.docCount())
                        .build();
            }
        }
    }

}
