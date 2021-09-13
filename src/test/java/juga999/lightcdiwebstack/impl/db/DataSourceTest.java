package juga999.lightcdiwebstack.impl.db;

import com.google.common.collect.Lists;
import jakarta.inject.Inject;
import juga999.lightcdiwebstack.AppConfig;
import juga999.lightcdiwebstack.impl.db.h2.H2InMemoryDataSource;
import juga999.lightcdiwebstack.impl.metrics.prometheus.Metrics;
import juga999.lightcdiwebstack.meta.db.DataSource;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.junit4.WeldInitiator;
import org.junit.rules.ExternalResource;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.util.List;

public class DataSourceTest {

    private class DatabaseResource extends ExternalResource {
        protected void after() {
            ((H2InMemoryDataSource)dataSource).dropAllObjects();
        }
    }

    protected TestRule getRuleChain() {
        List<Class<?>> injectionList = Lists.newArrayList(
                H2InMemoryDataSource.class,
                TransactionProvider.class,
                AppConfig.class,
                Metrics.class);

        Weld weld = WeldInitiator.createWeld()
                .addBeanClasses(getInjections())
                .addBeanClasses(injectionList.toArray(new Class<?>[injectionList.size()]))
                .enableInterceptors(TransactionProvider.class);
        return RuleChain
                .outerRule(WeldInitiator.from(weld).inject(this).build())
                .around(new DatabaseResource());
    }

    private final Class<?>[] injections;

    @Inject
    protected DataSource dataSource;

    public DataSourceTest(List<Class<?>> otherInjections) {
        List<Class<?>> injectionList = Lists.newArrayList(
                AppConfig.class,
                Metrics.class);

        injectionList.addAll(otherInjections);

        injections = injectionList.toArray(new Class<?>[injectionList.size()]);
    }

    protected Class<?>[] getInjections() {
        return injections;
    }

}
