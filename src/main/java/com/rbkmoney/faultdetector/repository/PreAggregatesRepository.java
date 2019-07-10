package com.rbkmoney.faultdetector.repository;

import com.google.common.collect.Lists;
import com.rbkmoney.faultdetector.data.PreAggregates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreAggregatesRepository implements CrudRepository<PreAggregates> {

    private final JdbcTemplate jdbcTemplate;

    @Value("${clickhouse.db.writeStatistic}")
    private boolean writeStatistic;

    private static final String SCHEME_NAME = "fault_detector";

    private static final String TABLE_NAME = "pre_aggregates";

    @Override
    public void insert(PreAggregates preAggregates) {
        if (writeStatistic && preAggregates != null) {
            Map<String, Object> parameters = generateParamsByFraudModel(preAggregates);
            SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getDataSource())
                    .withSchemaName(SCHEME_NAME)
                    .withTableName(TABLE_NAME);
            simpleJdbcInsert.setColumnNames(Lists.newArrayList(parameters.keySet()));
            simpleJdbcInsert.execute(parameters);
        }
    }

    public static Map<String, Object> generateParamsByFraudModel(PreAggregates value) {
        Map<String, Object> parameters = new HashMap<>();

        parameters.put("timestamp", new Date(value.getAggregationTime()));
        parameters.put("serviceId", value.getServiceId());
        parameters.put("operationsCount", value.getOperationsCount());
        parameters.put("runningOperationsCount", value.getRunningOperationsCount());
        parameters.put("overtimeOperationsCount", value.getOvertimeOperationsCount());
        parameters.put("errorOperationsCount", value.getErrorOperationsCount());
        parameters.put("successOperationsCount", value.getSuccessOperationsCount());
        parameters.put("operationTimeLimit", value.getOperationTimeLimit());
        parameters.put("slidingWindow", value.getSlidingWindow());
        parameters.put("preAggregationSize", value.getPreAggregationSize());

        return parameters;
    }

}
