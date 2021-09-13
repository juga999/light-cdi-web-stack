package juga999.lightcdiwebstack.impl.db.h2;

import jakarta.annotation.PostConstruct;
import juga999.lightcdiwebstack.impl.db.AbstractDataSource;
import org.h2.jdbcx.JdbcConnectionPool;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class H2InMemoryDataSource extends AbstractDataSource {

    private static final Logger logger = LoggerFactory.getLogger(H2InMemoryDataSource.class);

    private String jdbcUrl;

    private JdbcConnectionPool cp;

    private Jdbi jdbi;

    @PostConstruct
    void init() {
        jdbcUrl = "jdbc:h2:mem:testdb;MODE=PostgreSQL";

        cp = JdbcConnectionPool.create(jdbcUrl, "", "");

        applyMigrationScripts("db");
        applyMigrationScripts("db_test");

        jdbi = Jdbi.create(cp);

        initJdbi(jdbi);
    }

    public void dropAllObjects() {
        try {
            try (Connection connection = getConnection()) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("DROP ALL OBJECTS");
                    logger.info("Dropped all objects");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
