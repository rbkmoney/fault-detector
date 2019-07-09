package com.rbkmoney.faultdetector.repository;

import com.google.common.collect.Lists;
import com.rbkmoney.faultdetector.data.ServiceAggregates;
import com.rbkmoney.faultdetector.repository.setter.AggregatesBatchPreparedStatementSetter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AggregatesRepository implements CrudRepository<ServiceAggregates> {

    private final JdbcTemplate jdbcTemplate;

    @Value("${clickhouse.db.writeStatistic}")
    private boolean writeStatistic;

    private static final String SCHEME_NAME = "fault_detector";

    private static final String TABLE_NAME = "aggregates";

    private static final String INSERT = "INSERT INTO fault_detector.aggregates " +
            "(timestamp, serviceId, failureRate, operationsCount, successOperationsCount, " +
            "errorOperationsCount, overtimeOperationsCount)" +
            " VALUES (?, ?, ?, ?, ?, ?, ?)";

    @Override
    public void insert(ServiceAggregates preAggregates) {
        if (writeStatistic && preAggregates != null) {
            Map<String, Object> parameters = generateParams(preAggregates);
            SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getDataSource())
                    .withSchemaName(SCHEME_NAME)
                    .withTableName(TABLE_NAME);
            simpleJdbcInsert.setColumnNames(Lists.newArrayList(parameters.keySet()));
            simpleJdbcInsert.execute(parameters);
        }
    }

    @Override
    public void insertBatch(List<ServiceAggregates> aggregatesList) {
        if (writeStatistic && aggregatesList != null && !aggregatesList.isEmpty()) {
            jdbcTemplate.batchUpdate(INSERT, new AggregatesBatchPreparedStatementSetter(aggregatesList));
        }
    }

    public static Map<String, Object> generateParams(ServiceAggregates value) {
        Map<String, Object> parameters = new HashMap<>();

        parameters.put("timestamp", new Date(value.getAggregateTime()));
        parameters.put("serviceId", value.getServiceId());
        parameters.put("failureRate", value.getFailureRate());
        parameters.put("operationsCount", value.getOperationsCount());
        parameters.put("successOperationsCount", value.getSuccessOperationsCount());
        parameters.put("errorOperationsCount", value.getErrorOperationsCount());
        parameters.put("overtimeOperationsCount", value.getOvertimeOperationsCount());

        return parameters;
    }

}
