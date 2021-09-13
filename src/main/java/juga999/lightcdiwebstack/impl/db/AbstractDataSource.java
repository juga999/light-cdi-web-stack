package juga999.lightcdiwebstack.impl.db;

import juga999.lightcdiwebstack.meta.db.DataSource;
import org.h2.tools.RunScript;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.gson2.Gson2Plugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractDataSource implements DataSource {

    private static final Logger logger = LoggerFactory.getLogger(AbstractDataSource.class);

    private static final ThreadLocal<Handle> threadTransactionHandle = new ThreadLocal<>();

    private static final Pattern scriptDirPattern = Pattern.compile("v(\\d+)");

    protected abstract String getJdbcUrl();

    protected abstract Connection getConnection() throws SQLException;

    protected abstract Jdbi getJdbi();

    public int getDbVersion() {
        int version = 0;
        try {
            try (Connection connection = getConnection()) {
                try (Statement statement = connection.createStatement()) {
                    ResultSet resultSet = statement.executeQuery("SELECT version FROM db_version");
                    resultSet.next();
                    version = resultSet.getInt("version");
                }
            }
        } catch (SQLException e) {
            logger.info("No database found");
        }

        return version;
    }

    protected void applyMigrationScripts(String dir) {
        int dbVersion = getDbVersion();
        logger.info("Database version: " + dbVersion);

        URL sqlScriptsDirUrl = getClass().getClassLoader().getResource(dir);
        File sqlScriptsDir = new File(Objects.requireNonNull(sqlScriptsDirUrl).getFile());
        Arrays.stream(Objects.requireNonNull(sqlScriptsDir.listFiles()))
                .filter(File::isDirectory)
                .sorted()
                .forEach(subDir -> applyMigrationScripts(dbVersion, subDir));
    }

    protected void applyMigrationScripts(int dbVersion, File dir) {
        Matcher matcher = scriptDirPattern.matcher(dir.getName());
        if (matcher.find()) {
            int version = Integer.parseInt(matcher.group(1));
            if (version > dbVersion) {
                Arrays.stream(Objects.requireNonNull(dir.listFiles()))
                        .filter(File::isFile)
                        .sorted()
                        .forEach(this::executeScript);
                setDbVersion(version);
            }
        }
    }

    protected Jdbi initJdbi(Jdbi jdbi) {
        return jdbi
                .installPlugin(new SqlObjectPlugin())
                .installPlugin(new Gson2Plugin());
    }

    @Override
    public <R> R withHandle(Function<Handle, R> fct) {
        Handle handle = threadTransactionHandle.get();
        if (handle != null) {
            return fct.apply(handle);
        } else {
            return getJdbi().withHandle(fct::apply);
        }
    }

    @Override
    public void useHandle(Consumer<Handle> fct) {
        Handle handle = threadTransactionHandle.get();
        if (handle != null) {
            fct.accept(handle);
        } else {
            getJdbi().useHandle(fct::accept);
        }
    }

    @Override
    public boolean isInTransaction() {
        return threadTransactionHandle.get() != null;
    }

    @Override
    public Handle beginTransaction() {
        Handle handle = getJdbi().open().begin();
        threadTransactionHandle.set(handle);
        return handle;
    }

    @Override
    public void commitTransaction() {
        threadTransactionHandle.get().commit();
        threadTransactionHandle.set(null);
    }

    @Override
    public void rollbackTransaction() {
        threadTransactionHandle.get().rollback();
        threadTransactionHandle.set(null);
    }

    private void setDbVersion(int version) {
        try {
            try (Connection connection = getConnection()) {
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate("UPDATE db_version SET version = " + version);
                    connection.commit();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void executeScript(File scriptFile) {
        logger.info("Executing " + scriptFile.getAbsolutePath());
        try {
            RunScript.execute(getJdbcUrl(), "", "", scriptFile.getAbsolutePath(), StandardCharsets.UTF_8, false);
        } catch (SQLException e) {
            logger.error("Error in script " + scriptFile.getName(), e);
            throw new RuntimeException(e);
        }
    }
}
