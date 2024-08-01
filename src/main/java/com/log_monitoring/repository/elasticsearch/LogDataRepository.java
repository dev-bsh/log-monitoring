package com.log_monitoring.repository.elasticsearch;

import com.log_monitoring.model.elasticsearch.LogData;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogDataRepository extends ElasticsearchRepository<LogData, String>, LogDataRepositoryExtension{
}
