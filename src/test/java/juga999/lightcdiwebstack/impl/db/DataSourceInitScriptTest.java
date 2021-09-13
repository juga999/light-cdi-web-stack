package juga999.lightcdiwebstack.impl.db;

import com.google.common.collect.Lists;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

public class DataSourceInitScriptTest extends DataSourceTest {

    private static final int EXPECTED_DB_VERSION = 1;

    @Rule
    public final TestRule chain = getRuleChain();

    public DataSourceInitScriptTest() {
        super(Lists.newArrayList());
    }

    @Test
    public void testGetDbVersion() {
        int dbVersion = dataSource.getDbVersion();
        Assertions.assertThat(dbVersion).isEqualTo(EXPECTED_DB_VERSION);
    }

    @Test
    public void testGetDbVersionBis() {
        int dbVersion = dataSource.getDbVersion();
        Assertions.assertThat(dbVersion).isEqualTo(EXPECTED_DB_VERSION);
    }
}
