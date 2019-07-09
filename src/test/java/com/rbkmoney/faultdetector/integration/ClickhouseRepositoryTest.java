package com.rbkmoney.faultdetector.integration;

import com.rbkmoney.faultdetector.data.ServiceAggregates;
import com.rbkmoney.faultdetector.repository.CrudRepository;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;

public class ClickhouseRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private CrudRepository<ServiceAggregates> aggregatesRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static final String GET_COUNT_OF_AGGREGATES = "SELECT count() as cnt from fault_detector.aggregates";

    @Test
    public void insert() throws SQLException {
        aggregatesRepository.insert(getTestServiceAggregates());
        Integer count = jdbcTemplate.queryForObject(GET_COUNT_OF_AGGREGATES,
                (resultSet, i) -> resultSet.getInt("cnt"));
        Assert.assertEquals(1, count.intValue());

    }

    private ServiceAggregates getTestServiceAggregates() {
        ServiceAggregates serviceAggregates = new ServiceAggregates();
        serviceAggregates.setAggregateTime(System.currentTimeMillis());
        serviceAggregates.setServiceId("TestService");
        serviceAggregates.setOperationsCount(10);
        serviceAggregates.setSuccessOperationsCount(9);
        serviceAggregates.setErrorOperationsCount(1);
        serviceAggregates.setOvertimeOperationsCount(0);
        serviceAggregates.setFailureRate(0.1);
        return serviceAggregates;
    }

}