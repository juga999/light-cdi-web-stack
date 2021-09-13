package juga999.lightcdiwebstack.impl.db;

import com.google.common.collect.Lists;
import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import juga999.lightcdiwebstack.AppConfig;
import org.h2.jdbcx.JdbcConnectionPool;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.h2.H2DatabasePlugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@ApplicationScoped
public class PooledDataSource extends AbstractDataSource {

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
        jdbcUrl = appConfig.getJdbcUrl();

        if (jdbcUrl.startsWith("jdbc:h2")) {
            cp = JdbcConnectionPool.create(jdbcUrl, "", "");
            jdbi = Jdbi.create(cp);
            initJdbi(jdbi).installPlugin(new H2DatabasePlugin());
        } else {
            throw new RuntimeException("Unsupported database connection string: " + jdbcUrl);
        }

        applyMigrationScripts("db");

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
