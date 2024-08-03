package com.log_monitoring.dto;

import com.log_monitoring.model.elasticsearch.LogData;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.elasticsearch.core.SearchHits;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class LogDataSearchResponse {

    private Long totalResultCount;
    private List<LogDataDto> result;

    @Builder
    public LogDataSearchResponse(Long resultCount, List<LogDataDto> result) {
        this.totalResultCount = resultCount;
        this.result = result;
    }

    public static LogDataSearchResponse fromSearchHits(SearchHits<LogData> searchHits) {
        return LogDataSearchResponse.builder()
                .resultCount(searchHits.getTotalHits())
                .result(searchHits.getSearchHits().stream()
                                .map(hit -> LogDataDto.fromEntity(hit.getContent()))
                                .toList())
                .build();
    }

}
