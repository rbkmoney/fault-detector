package com.rbkmoney.faultdetector.integration;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.ClickHouseContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import ru.yandex.clickhouse.ClickHouseDataSource;
import ru.yandex.clickhouse.settings.ClickHouseProperties;

import java.sql.Connection;
import java.sql.SQLException;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration(initializers = AbstractIntegrationTest.Initializer.class)
public abstract class AbstractIntegrationTest {

    public static final String KAFKA_DOCKER_VERSION = "5.0.1";

    @ClassRule
    public static KafkaContainer kafka = new KafkaContainer(KAFKA_DOCKER_VERSION).withEmbeddedZookeeper();

    @ClassRule
    public static ClickHouseContainer clickHouseContainer = new ClickHouseContainer();

    @Before
    public void setUp() throws Exception {
        Connection connection = getSystemConn();
        String sql = IOUtils.toString(ClassLoader.getSystemClassLoader()
                .getResourceAsStream("sql/db_init.sql"), "UTF8");
        String[] split = sql.split(";");
        for (String exec : split) {
            connection.createStatement().execute(exec);
        }
        connection.close();
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues
                    .of("kafka.bootstrap.servers=" + kafka.getBootstrapServers(),
                        "kafka.ssl.enable=false",
                        "clickhouse.db.url=" + clickHouseContainer.getJdbcUrl(),
                        "clickhouse.db.user=" + clickHouseContainer.getUsername(),
                        "clickhouse.db.password=" + clickHouseContainer.getPassword(),
                        "clickhouse.db.writeStatistic=true")
                    .applyTo(configurableApplicationContext.getEnvironment());
        }

    }

    public Connection getSystemConn() throws SQLException {
        ClickHouseProperties properties = new ClickHouseProperties();
        ClickHouseDataSource dataSource = new ClickHouseDataSource(clickHouseContainer.getJdbcUrl(), properties);
        return dataSource.getConnection();
    }

}
