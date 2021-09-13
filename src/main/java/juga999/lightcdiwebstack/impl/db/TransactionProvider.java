package juga999.lightcdiwebstack.impl.db;

import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import juga999.lightcdiwebstack.impl.metrics.prometheus.Metrics;
import juga999.lightcdiwebstack.meta.db.DataSource;
import juga999.lightcdiwebstack.meta.db.TransactionRequired;
import org.jdbi.v3.core.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TransactionRequired
@Interceptor
public class TransactionProvider {

    private static final Logger logger = LoggerFactory.getLogger(TransactionProvider.class);

    @Inject
    protected DataSource dataSource;

    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception {
        final Object[] result = new Object[1];

        if (dataSource.isInTransaction()) {
            result[0] = context.proceed();
        } else {
            String method = context.getMethod().getName();
            try (Handle ignored = dataSource.beginTransaction()) {
                Metrics.incInProgressTransactions(method);
                try {
                    logger.trace("Transaction start - " + method);
                    result[0] = context.proceed();
                    dataSource.commitTransaction();
                    Metrics.decInProgressTransactions(method);
                } catch (Exception e) {
                    logger.info("Transaction rollback - " + method);
                    dataSource.rollbackTransaction();
                    Metrics.decInProgressTransactions(method);
                    throw e;
                }
            } finally {
                logger.trace("Transaction end - " + method);
            }
        }

        return result[0];
    }
}
