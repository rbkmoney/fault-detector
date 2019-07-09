package com.rbkmoney.faultdetector.repository;

import com.google.common.collect.Lists;
import com.rbkmoney.faultdetector.data.PreAggregates;
import com.rbkmoney.faultdetector.repository.setter.PreAggregatesBatchPreparedStatementSetter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreAggregatesRepository implements CrudRepository<PreAggregates> {

    private final JdbcTemplate jdbcTemplate;

    @Value("${clickhouse.db.writeStatistic}")
    private boolean writeStatistic;

    private static final String INSERT = "INSERT INTO fault_detector.pre_aggregates " +
            "(timestamp, serviceId, operationsCount, runningOperationsCount, overtimeOperationsCount, " +
            "errorOperationsCount, successOperationsCount, operationTimeLimit, slidingWindow, preAggregationSize)" +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    @Override
    public void insert(PreAggregates preAggregates) {
        if (writeStatistic && preAggregates != null) {
            Map<String, Object> parameters = generateParamsByFraudModel(preAggregates);
            SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getDataSource())
                    .withSchemaName("fault_detector")
                    .withTableName("pre_aggregates");
            simpleJdbcInsert.setColumnNames(Lists.newArrayList(parameters.keySet()));
            simpleJdbcInsert.execute(parameters);
        }
    }

    @Override
    public void insertBatch(List<PreAggregates> preAggregatesList) {
        if (writeStatistic && preAggregatesList != null && !preAggregatesList.isEmpty()) {
            jdbcTemplate.batchUpdate(INSERT, new PreAggregatesBatchPreparedStatementSetter(preAggregatesList));
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
