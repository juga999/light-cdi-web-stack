package juga999.lightcdiwebstack.impl.db.h2;

import com.google.common.collect.Lists;
import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import juga999.lightcdiwebstack.AppConfig;
import org.h2.jdbcx.JdbcConnectionPool;
import org.jdbi.v3.core.Jdbi;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class H2PersistentDataSource extends H2DataSource {

    private String jdbcUrl;

    private JdbcConnectionPool cp;

    private Jdbi jdbi;

    @Inject
    private AppConfig appConfig;

    class ActiveConnectionsCollector extends Collector {
        @Override
        public List<MetricFamilySamples> collect() {
            List<MetricFamilySamples> mfs = Lists.newArrayList();
            int activeConnections = cp.getActiveConnections();
            mfs.add(new GaugeMetricFamily("sql_connections_active", "help", activeConnections));
            return mfs;
        }
    }

    @PostConstruct
    void init() {
        Path dataBaseDir = appConfig.getDataBaseDir();
        String h2dbUrl = dataBaseDir.resolve("h2db").toAbsolutePath().toString();
        jdbcUrl = "jdbc:h2:" + h2dbUrl + ";MODE=PostgreSQL";

        cp = JdbcConnectionPool.create(jdbcUrl, "", "");

        applyMigrationScripts("db");

        jdbi = Jdbi.create(cp);

        initJdbi(jdbi);

        new ActiveConnectionsCollector().register();
    }

    @Override
    protected String getJdbcUrl() {
        return jdbcUrl;
    }

    @Override
    protected Connection getConnection() throws SQLException {
        return cp.getConnection();
    }

    @Override
    protected Jdbi getJdbi() {
        return jdbi;
    }

}
